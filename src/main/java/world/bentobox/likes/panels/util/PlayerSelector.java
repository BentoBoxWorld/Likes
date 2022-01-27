package world.bentobox.likes.panels.util;


import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.likes.utils.Constants;


/**
 * This class creates a GUI that allows to select single user from input list.
 */
public class PlayerSelector extends PagedSelector<User>
{
	/**
	 * Instantiates a new Player selector.
	 *
	 * @param user the user
	 * @param inputList the input list
	 * @param excludedUsers the excluded users
	 * @param consumer the consumer
	 */
	private PlayerSelector(User user, List<User> inputList, Set<User> excludedUsers, BiConsumer<Boolean, User> consumer)
	{
		super(user);
		this.consumer = consumer;

		this.activeFilter = Filter.ONLINE_PLAYERS;

		this.elements = inputList.stream().
			filter(player -> excludedUsers.isEmpty() || !excludedUsers.contains(player)).
			collect(Collectors.toList());

		// Init without filters applied.
		this.updateFilters();
	}


	/**
	 * Open given gui that allow to choose a user.
	 *
	 * @param user the user
	 * @param userList the user list
	 * @param excludedList the excluded list
	 * @param consumer the consumer
	 */
	public static void open(User user, List<User> userList, List<User> excludedList, BiConsumer<Boolean, User> consumer)
	{
		new PlayerSelector(user, userList, new HashSet<>(excludedList), consumer).build();
	}


	/**
	 * Open given gui without excluding any player from it.
	 *
	 * @param user the user
	 * @param userList the user list
	 * @param consumer the consumer
	 */
	public static void open(User user, List<User> userList, BiConsumer<Boolean, User> consumer)
	{
		new PlayerSelector(user, userList, Collections.emptySet(), consumer).build();
	}


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


	/**
	 * This method builds all necessary elements in GUI panel.
	 */
	@Override
	protected void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user);
		panelBuilder.name(this.user.getTranslation(Constants.TITLES + "select-player"));

		PanelUtils.fillBorder(panelBuilder, Material.BLUE_STAINED_GLASS_PANE);

		this.populateElements(panelBuilder, this.filterElements);

		panelBuilder.item(3, this.createButton());

		// Add 2 filter buttons
		panelBuilder.item(5, this.createFilterButton(Filter.ONLINE_PLAYERS));
		panelBuilder.item(6, this.createFilterButton(Filter.ALL_PLAYERS));

		panelBuilder.build();
	}


	/**
	 * This method is called when filter value is updated.
	 */
	@Override
	protected void updateFilters()
	{
		if (this.searchString == null || this.searchString.isBlank())
		{
			this.filterElements = this.elements;
		}
		else
		{
			this.filterElements = this.elements.stream().
				filter(element -> {
					// If element name is set and name contains search field, then do not filter out.
					return element.getName().toLowerCase().contains(this.searchString.toLowerCase());
				}).
				distinct().
				collect(Collectors.toList());
		}

		if (this.activeFilter == Filter.ONLINE_PLAYERS)
		{
			// Remove non-online players
			this.filterElements = this.filterElements.stream().
				filter(User::isOnline).
				collect(Collectors.toList());
		}

		// Sort by name.
		this.filterElements.sort(Comparator.comparing(User::getName));
	}


	/**
	 * This method creates PanelItem button of requested type.
	 * @return new PanelItem with requested functionality.
	 */
	private PanelItem createButton()
	{
		final String reference = Constants.BUTTONS + "cancel.";

		final String name = this.user.getTranslation(reference + "name");
		final List<String> description = new ArrayList<>(3);
		description.add(this.user.getTranslation(reference + "description"));

		ItemStack icon = new ItemStack(Material.IRON_DOOR);
		PanelItem.ClickHandler clickHandler = (panel, user1, clickType, slot) ->
		{
			this.consumer.accept(false, null);
			return true;
		};

		description.add("");
		description.add(this.user.getTranslation(Constants.TIPS + "click-to-cancel"));

		return new PanelItemBuilder().
			icon(icon).
			name(name).
			description(description).
			clickHandler(clickHandler).
			build();
	}


	/**
	 * This method creates panel item for given button type.
	 *
	 * @param button Button type.
	 * @return Clickable PanelItem button.
	 */
	private PanelItem createFilterButton(Filter button)
	{
		String name = this.user.getTranslation(Constants.BUTTONS + button.name().toLowerCase() + ".name");
		List<String> description = new ArrayList<>();
		description.add(this.user.getTranslationOrNothing(
			Constants.BUTTONS + button.name().toLowerCase() + ".description"));

		if (this.activeFilter != button)
		{
			description.add("");
			description.add(this.user.getTranslation(Constants.TIPS + "click-to-view"));
		}

		PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) ->
		{
			this.activeFilter = button;
			this.updateFilters();
			this.build();
			return true;
		};

		Material material = switch (button) {
			case ONLINE_PLAYERS -> Material.FILLED_MAP;
			case ALL_PLAYERS -> Material.CHEST;
		};

		return new PanelItemBuilder().
			name(name).
			description(description).
			icon(material).
			clickHandler(clickHandler).
			glow(this.activeFilter == button).
			build();
	}


	/**
	 * This method creates button for given element.
	 * @param element element which button must be created.
	 * @return new Button for element.
	 */
	@Override
	protected PanelItem createElementButton(User element)
	{
		List<String> description = new ArrayList<>(2);
		description.add("");
		description.add(this.user.getTranslationOrNothing(Constants.TIPS + "click-to-select"));

		return new PanelItemBuilder().
			icon(user.getName()).
			description(description).
			name(this.user.getTranslation(Constants.BUTTONS + "user.name", Constants.PARAMETER_NAME, user.getName())).
			clickHandler((panel, user1, clickType, slot) ->
			{
				this.consumer.accept(true, user);
				return true;
			}).
			build();
	}


// ---------------------------------------------------------------------
// Section: Enums
// ---------------------------------------------------------------------


	/**
	 * This enum holds all possible filters in current GUI.
	 */
	private enum Filter
	{
		/**
		 * Shows online users.
		 */
		ONLINE_PLAYERS,
		/**
		 * Shows all users.
		 */
		ALL_PLAYERS
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * List with elements that will be displayed in current GUI.
	 */
	private final List<User> elements;

	/**
	 * This variable stores consumer.
	 */
	private final BiConsumer<Boolean, User> consumer;

	/**
	 * Stores filtered items.
	 */
	private List<User> filterElements;

	/**
	 * Variable stores active filter.
	 */
	private Filter activeFilter;
}
