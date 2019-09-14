//
// Created by BONNe
// Copyright - 2019
//


package world.bentobox.likes.panels.admin;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.likes.LikesAddon;
import world.bentobox.likes.panels.GuiUtils;
import world.bentobox.likes.utils.Constants;


/**
 * This class allows to manage all admin operations from GUI.
 */
public class AdminPanel
{
	/**
	 * This is internal constructor. It is used internally in current class to avoid
	 * creating objects everywhere.
	 * @param addon Likes object.
	 * @param user User who opens Panel.
	 * @param world World where gui is opened
	 * @param permissionPrefix Permission Prefix
	 */
	private AdminPanel(LikesAddon addon, User user, World world, String permissionPrefix)
	{
		this.addon = addon;
		this.user = user;
		this.world = world;

		this.permissionPrefix = permissionPrefix;
	}


	/**
	 * This method is used to open UserPanel outside this class. It will be much easier
	 * to open panel with single method call then initializing new object.
	 * @param addon Likes Addon object
	 * @param user User who opens panel
	 * @param world World where gui is opened
	 * @param permissionPrefix Permission Prefix
	 */
	public static void openPanel(LikesAddon addon, User user, World world, String permissionPrefix)
	{
		new AdminPanel(addon, user, world, permissionPrefix).build();
	}


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


	private void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().
			user(this.user).
			name(this.user.getTranslation(Constants.TITLE + "admin"));

		GuiUtils.fillBorder(panelBuilder);

		panelBuilder.item(10, this.createButton(Button.ADD_REMOVE_LIKE));
		panelBuilder.item(19, this.createButton(Button.VIEW_LIKES));

		// Add All Player Data removal.
		panelBuilder.item(28, this.createButton(Button.WIPE_DATA));

		// Edit Addon Settings
		panelBuilder.item(16, this.createButton(Button.EDIT_SETTINGS));

		panelBuilder.build();
	}


	/**
	 * This method creates given button as PanelItem
	 * @param button Button that must be created.
	 * @return PanelItem of given button.
	 */
	private PanelItem createButton(Button button)
	{
		ItemStack icon;
		String name;
		String description;
		boolean glow;
		PanelItem.ClickHandler clickHandler;

		switch (button)
		{
			case ADD_REMOVE_LIKE:
			{
				name = this.user.getTranslation(Constants.BUTTON + "manage-likes");
				description = this.user.getTranslation(Constants.DESCRIPTION + "manage-likes");
				icon = new ItemStack(Material.WRITABLE_BOOK);
				clickHandler = (panel, user, clickType, slot) -> {
					// TODO: Need an Island select panel where selected island opens a gui with users
					// that added likes and dislikes.
					// This GUI need an ability to remove users who like or dislike only.
					return true;
				};
				glow = false;

				break;
			}
			case VIEW_LIKES:
			{
				name = this.user.getTranslation(Constants.BUTTON + "view-likes");
				description = this.user.getTranslation(Constants.DESCRIPTION + "view-likes");
				icon = new ItemStack(Material.WRITTEN_BOOK);
				clickHandler = (panel, user, clickType, slot) -> {
					// TODO: Need an Island select panel where selected island opens a gui where
					// Admin can add like or dislike without being on island.
					return true;
				};
				glow = false;

				break;
			}
			case WIPE_DATA:
			{
				name = this.user.getTranslation(Constants.BUTTON + "wipe-data");
				description = this.user.getTranslation(Constants.DESCRIPTION + "wipe-data");
				icon = new ItemStack(Material.TNT);
				clickHandler = (panel, user, clickType, slot) -> {
					this.addon.getManager().wipeData(this.world);
					return true;
				};
				glow = false;

				break;
			}
			case EDIT_SETTINGS:
			{
				name = this.user.getTranslation(Constants.BUTTON + "edit-settings");
				description = this.user.getTranslation(Constants.DESCRIPTION + "edit-settings");
				icon = new ItemStack(Material.CRAFTING_TABLE);
				clickHandler = (panel, user, clickType, slot) -> {
					EditSettingsPanel.openPanel(this.addon, this.user, this.world, this.permissionPrefix);
					return true;
				};
				glow = false;

				break;
			}
			default:
				return null;
		}

		return new PanelItemBuilder().
			icon(icon).
			name(name).
			description(GuiUtils.stringSplit(description, 999)).
			glow(glow).
			clickHandler(clickHandler).
			build();
	}


// ---------------------------------------------------------------------
// Section: Enums
// ---------------------------------------------------------------------


	/**
	 * This enum contains all buttons for current GUI
	 */
	private enum Button
	{
		VIEW_LIKES,
		WIPE_DATA,
		EDIT_SETTINGS,
		ADD_REMOVE_LIKE
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * This variable allows to access addon object.
	 */
	private final LikesAddon addon;

	/**
	 * This variable holds user who opens panel. Without it panel cannot be opened.
	 */
	private final User user;

	/**
	 * This variable holds a world to which gui referee.
	 */
	private final World world;

	/**
	 * Permission prefix
	 */
	private final String permissionPrefix;
}
