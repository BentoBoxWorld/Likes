package world.bentobox.likes.panels.admin;


import com.google.common.collect.ImmutableSet;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.likes.database.objects.LikesObject;
import world.bentobox.likes.panels.CommonPanel;
import world.bentobox.likes.panels.GuiUtils;
import world.bentobox.likes.panels.util.SelectBlocksGUI;
import world.bentobox.likes.utils.Constants;
import world.bentobox.likes.utils.Utils;


/**
 * This class contains all necessary things that allows to select single island from all existing
 * in given world.
 */
public class ListIslandsPanel extends CommonPanel
{
	/**
	 * Default constructor.
	 * @param parent Parent Panel
	 * @param type Action type
	 */
	private ListIslandsPanel(CommonPanel parent, Type type)
	{
		super(parent);

		this.iconPermission = this.permissionPrefix + "likes.icon";

		this.elements = new ArrayList<>(this.addon.getIslands().getIslands(this.world));
		this.type = type;
		this.pageIndex = 0;
	}


	/**
	 * Opens gui with simple method call.
	 * @param parent Parent Panel
	 * @param type Action type
	 */
	public static void open(CommonPanel parent, Type type)
	{
		new ListIslandsPanel(parent, type).build();
	}


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


	/**
	 * This method builds all necessary elements in GUI panel.
	 */
	@Override
	public void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().
			user(this.user).
			name(this.user.getTranslation(Constants.TITLE + "select-island"));

		GuiUtils.fillBorder(panelBuilder, Material.BLUE_STAINED_GLASS_PANE);

		final int MAX_ELEMENTS = 21;
		final int correctPage;

		if (this.pageIndex < 0)
		{
			correctPage = this.elements.size() / MAX_ELEMENTS;
		}
		else if (this.pageIndex > (this.elements.size() / MAX_ELEMENTS))
		{
			correctPage = 0;
		}
		else
		{
			correctPage = this.pageIndex;
		}

		int islandIndex = MAX_ELEMENTS * correctPage;

		// I want first row to be only for navigation and return button.
		int index = 10;

		while (islandIndex < ((correctPage + 1) * MAX_ELEMENTS) &&
			islandIndex < this.elements.size())
		{
			if (!panelBuilder.slotOccupied(index))
			{
				panelBuilder.item(index, this.createIslandButton(this.elements.get(islandIndex++)));
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
						this.pageIndex = correctPage - 1;
						this.build();
						return true;
					}).build());

			panelBuilder.item(26,
				new PanelItemBuilder().
					icon(Material.OAK_SIGN).
					name(this.user.getTranslation(Constants.BUTTON + "next")).
					clickHandler((panel, user1, clickType, slot) -> {
						this.pageIndex = correctPage + 1;
						this.build();
						return true;
					}).build());
		}

		panelBuilder.item(44, this.returnButton);

		panelBuilder.build();
	}


	/**
	 * This method creates PanelItem that represents given island.
	 * @param island island which icon must be created.
	 * @return PanelItem that represents given island.
	 */
	private PanelItem createIslandButton(Island island)
	{
		LikesObject likesObject = this.addon.getManager().getExistingIslandLikes(island.getUniqueId());

		List<String> description = new ArrayList<>();

		UUID ownerId = island.getOwner();
		String ownerName = this.addon.getPlayers().getName(ownerId);
		description.add(ownerName);

		if (likesObject != null)
		{
			description.add(this.user.getTranslation(Constants.DESCRIPTION + "values",
				"[likes]", "" + likesObject.getLikes(),
				"[dislikes]", "" + likesObject.getDislikes(),
				"[rank]", "" + likesObject.getRank()));
		}

		ImmutableSet<UUID> members = island.getMemberSet();

		if (members.size() > 1)
		{
			members.forEach(uuid -> {
				if (uuid != ownerId)
				{
					description.add(ChatColor.AQUA + this.addon.getPlayers().getName(uuid));
				}
			});
		}

		Material material = Material.matchMaterial(Utils.getPermissionValue(User.getInstance(ownerId),
			this.iconPermission,
			likesObject == null ? Material.PAPER.name() : Material.WRITTEN_BOOK.name()));

		if (material == null)
		{
			material = Material.PAPER;
		}

		String name = island.getName() != null ? island.getName() : ownerName;

		PanelItem.ClickHandler handler = (panel, user, clickType, slot) -> {

			switch (this.type)
			{
				case MANAGE:
					AdminViewPanel.openPanel(this, island);
					break;
				case ICON:
					SelectBlocksGUI.open(user, (hasSelected, materials) -> {
						if (hasSelected && materials.size() == 1)
						{
							user.addPerm(this.iconPermission + "." + materials.iterator().next().name());
						}

						this.build();
					});
					break;
			}

			return true;
		};


		if (material.equals(Material.PLAYER_HEAD))
		{
			return new PanelItemBuilder().
				name(this.user.getTranslation(Constants.BUTTON + "name", "[name]", name)).
				description(GuiUtils.stringSplit(description, 999)).
				icon(ownerName).
				clickHandler(handler).
				glow(false).
				build();
		}
		else
		{
			return new PanelItemBuilder().
				name(this.user.getTranslation(Constants.BUTTON + "name", "[name]", name)).
				description(GuiUtils.stringSplit(description, 999)).
				icon(material).
				clickHandler(handler).
				glow(false).
				build();
		}
	}


// ---------------------------------------------------------------------
// Section: Enums
// ---------------------------------------------------------------------


	/**
	 * Action type that should happen when PanelItem is clicked.
	 */
	enum Type
	{
		MANAGE,
		ICON
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * List with elements that will be displayed in current GUI.
	 */
	private final List<Island> elements;

	/**
	 * This variable stores gui type.
	 */
	private final Type type;

	/**
	 * Prefix for custom icon.
	 */
	private final String iconPermission;

	/**
	 * Index of current search page.
	 */
	private int pageIndex;
}
