//
// Created by BONNe
// Copyright - 2019
//


package world.bentobox.likes.panels;


import org.bukkit.Material;
import org.bukkit.World;
import org.eclipse.jdt.annotation.NonNull;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.likes.LikesAddon;
import world.bentobox.likes.config.Settings;
import world.bentobox.likes.utils.Constants;


/**
 * This abstract panel allows to use panel hierarchy much easier.
 */
public abstract class CommonPanel
{
	/**
	 * Default constructor without parent.
	 * @param addon Likes Addon instance
	 * @param user User who opens Panel
	 * @param world World where GUI is opened.
	 * @param permissionPrefix GameMode main prefix.
	 */
	protected CommonPanel(LikesAddon addon, User user, World world, String permissionPrefix)
	{
		this(addon, user, world, permissionPrefix, null);
	}


	/**
	 * Default constructor from single parent.
	 * @param parent Parent Panel.
	 */
	protected CommonPanel(@NonNull CommonPanel parent)
	{
		this(parent.addon, parent.user, parent.world, parent.permissionPrefix, parent);
	}


	/**
	 * Default constructor without parent.
	 * @param addon Likes Addon instance
	 * @param user User who opens Panel
	 * @param world World where GUI is opened.
	 * @param permissionPrefix GameMode main prefix.
	 * @param parent Parent Panel
	 */
	private CommonPanel(LikesAddon addon, User user, World world, String permissionPrefix, CommonPanel parent)
	{
		this.addon = addon;
		this.user = user;
		this.world = world;
		this.permissionPrefix = permissionPrefix;
		this.parent = parent;

		this.returnButton = new PanelItemBuilder().
			name(this.user.getTranslation(Constants.BUTTON + "return")).
			icon(Material.OAK_DOOR).
			clickHandler((panel, user1, clickType, i) -> {

				if (this.parent == null)
				{
					this.user.closeInventory();
					return true;
				}

				this.parent.build();
				return true;
			}).build();
	}


// ---------------------------------------------------------------------
// Section: Abstract methods
// ---------------------------------------------------------------------


	/**
	 * Method that must be added in all panels.
	 */
	public abstract void build();


// ---------------------------------------------------------------------
// Section: Common methods
// ---------------------------------------------------------------------


	/**
	 * This method returns operational mode for addon.
	 * @return Which LikeMode is currently active.
	 */
	protected final Settings.LikeMode getMode()
	{
		return this.addon.getSettings().getMode();
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * This variable stores parent gui.
	 */
	protected final CommonPanel parent;

	/**
	 * Variable stores Challenges addon.
	 */
	protected final LikesAddon addon;

	/**
	 * Variable stores world in which panel is referred to.
	 */
	protected final World world;

	/**
	 * Variable stores user who created this panel.
	 */
	protected final User user;

	/**
	 * Variable stores permission prefix of command from which panel was called.
	 */
	protected final String permissionPrefix;

	/**
	 * Variable stores return button to avoid creating it in every panel.
	 */
	protected final PanelItem returnButton;
}
