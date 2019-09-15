package world.bentobox.likes.panels.util;


import org.bukkit.Material;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.likes.panels.GuiUtils;
import world.bentobox.likes.utils.Constants;


/**
 * This class contains all necessary things that allows to select single user from inputList by removing excludedUsers.
 */
public class SelectUserGUI
{
	private SelectUserGUI(User user, List<User> inputList, Set<User> excludedUsers, Consumer<User> consumer)
	{
		this.consumer = consumer;
		this.user = user;

		this.elements = inputList.stream().
			filter(player -> excludedUsers.isEmpty() || !excludedUsers.contains(player)).
			collect(Collectors.toList());
	}


	public static void open(User user, List<User> userList, List<User> excludedList, Consumer<User> consumer)
	{
		new SelectUserGUI(user, userList, new HashSet<>(excludedList), consumer).build(0);
	}


	public static void open(User user, List<User> userList, Consumer<User> consumer)
	{
		new SelectUserGUI(user, userList, Collections.emptySet(), consumer).build(0);
	}


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


	/**
	 * This method builds all necessary elements in GUI panel.
	 */
	private void build(int pageIndex)
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user).
			name(this.user.getTranslation(Constants.TITLE + "select-player"));

		GuiUtils.fillBorder(panelBuilder, Material.BLUE_STAINED_GLASS_PANE);

		final int MAX_ELEMENTS = 21;
		final int correctPage;

		if (pageIndex < 0)
		{
			correctPage = this.elements.size() / MAX_ELEMENTS;
		}
		else if (pageIndex > (this.elements.size() / MAX_ELEMENTS))
		{
			correctPage = 0;
		}
		else
		{
			correctPage = pageIndex;
		}

		int userIndex = MAX_ELEMENTS * correctPage;

		// I want first row to be only for navigation and return button.
		int index = 10;

		while (userIndex < ((correctPage + 1) * MAX_ELEMENTS) &&
			userIndex < this.elements.size())
		{
			if (!panelBuilder.slotOccupied(index))
			{
				panelBuilder.item(index, this.createUserButton(this.elements.get(userIndex++)));
			}

			index++;
		}


		if (this.elements.size() > MAX_ELEMENTS)
		{
			// Navigation buttons if necessary

			panelBuilder.item(18,
				new PanelItemBuilder().
					icon(Material.OAK_SIGN).
					name(this.user.getTranslation(Constants.BUTTON + "previous")).
					clickHandler((panel, user1, clickType, slot) -> {
						this.build(correctPage - 1);
						return true;
					}).build());

			panelBuilder.item(26,
				new PanelItemBuilder().
					icon(Material.OAK_SIGN).
					name(this.user.getTranslation(Constants.BUTTON + "next")).
					clickHandler((panel, user1, clickType, slot) -> {
						this.build(correctPage + 1);
						return true;
					}).build());
		}

		panelBuilder.item(44,
			new PanelItemBuilder().
				icon(Material.OAK_DOOR).
				name(this.user.getTranslation(Constants.BUTTON + "return")).
				clickHandler( (panel, user1, clickType, slot) -> {
					this.consumer.accept(null);
					return true;
				}).build());

		panelBuilder.build();
	}


	/**
	 * This method creates PanelItem that represents given user.
	 * @param user Material which icon must be created.
	 * @return PanelItem that represents given user.
	 */
	private PanelItem createUserButton(User user)
	{
		return new PanelItemBuilder().
			name(user.getName()).
			icon(user.getName()).
			clickHandler((panel, user1, clickType, slot) -> {
				this.consumer.accept(user);
				return true;
			}).
			build();
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * List with elements that will be displayed in current GUI.
	 */
	private List<User> elements;

	/**
	 * This variable stores consumer.
	 */
	private Consumer<User> consumer;

	/**
	 * User who runs GUI.
	 */
	private User user;
}
