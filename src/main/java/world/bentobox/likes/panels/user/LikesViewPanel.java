//
// Created by BONNe
// Copyright - 2019
//


package world.bentobox.likes.panels.user;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.likes.LikesAddon;
import world.bentobox.likes.database.objects.LikesObject;
import world.bentobox.likes.panels.GuiUtils;
import world.bentobox.likes.utils.Constants;


/**
 * This gui shows all information about Likes Object.
 */
public class LikesViewPanel
{
	/**
	 * This is internal constructor. It is used internally in current class to avoid
	 * creating objects everywhere.
	 * @param addon Likes object.
	 * @param user User who opens Panel.
	 * @param world World where gui is opened
	 * @param permissionPrefix Permission Prefix
	 * @param likesObject LikeObject that will be viewed.
	 */
	private LikesViewPanel(LikesAddon addon, User user, World world, String permissionPrefix, LikesObject likesObject)
	{
		this.addon = addon;
		this.user = user;
		this.world = world;

		this.permissionPrefix = permissionPrefix;

		this.likesObject = likesObject;

		this.likedByUsers = this.likesObject.getLikedBy().stream().
			map(uuid -> this.addon.getPlayers().getName(uuid)).
			sorted(String::compareToIgnoreCase).
			collect(Collectors.toList());

		this.dislikedByUsers = this.likesObject.getDislikedBy().stream().
			map(uuid -> this.addon.getPlayers().getName(uuid)).
			sorted(String::compareToIgnoreCase).
			collect(Collectors.toList());

		this.likeRank = this.addon.getManager().getSortedLikes(world).entryIndex(likesObject) + 1;
		this.dislikeRank = this.addon.getManager().getSortedDislikes(world).entryIndex(likesObject) + 1;
		this.overallRank = this.addon.getManager().getSortedRank(world).entryIndex(likesObject) + 1;
	}


	/**
	 * This method is used to open UserPanel outside this class. It will be much easier
	 * to open panel with single method call then initializing new object.
	 * @param addon Likes Addon object
	 * @param user User who opens panel
	 * @param world World where gui is opened
	 * @param permissionPrefix Permission Prefix
	 * @param likesObject LikeObject that will be viewed.
	 */
	public static void openPanel(LikesAddon addon, User user, World world, String permissionPrefix, LikesObject likesObject)
	{
		new LikesViewPanel(addon, user, world, permissionPrefix, likesObject).build();
	}


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


	private void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().
			name(this.user.getTranslation(Constants.TITLE + "view")).
			user(this.user);

		GuiUtils.fillBorder(panelBuilder, 6, Material.MAGENTA_STAINED_GLASS_PANE);

		panelBuilder.item(10, this.createButton(Button.LIKE));
		panelBuilder.item(11, this.createButton(Button.LIKE_RANK));

		panelBuilder.item(15, this.createButton(Button.OVERALL));
		panelBuilder.item(16, this.createButton(Button.OVERALL_RANK));

		this.populateLikers(panelBuilder);

		panelBuilder.item(28, this.createButton(Button.DISLIKE));
		panelBuilder.item(29, this.createButton(Button.DISLIKE_RANK));

		this.populateDislikers(panelBuilder);

		// At the end we just call build method that creates and opens panel.
		panelBuilder.build();
	}


	/**
	 * This method creates PanelItem button based on given button type.
	 * @param button Button that must be created.
	 * @return PanelItem object that represents given button.
	 */
	private PanelItem createButton(Button button)
	{
		ItemStack icon;
		String name;
		List<String> description;
		PanelItem.ClickHandler clickHandler;

		switch (button)
		{
			case LIKE:
			{
				icon = new ItemStack(Material.GOLD_INGOT);
				name = this.user.getTranslation(Constants.BUTTON + "like");

				description = new ArrayList<>(2);
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "like"));
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value",
					"[value]",
					this.likesObject.getLikes() + ""));

				clickHandler = null;

				break;
			}
			case LIKE_RANK:
			{
				icon = new ItemStack(Material.GOLD_BLOCK);
				name = this.user.getTranslation(Constants.BUTTON + "like-rank");

				description = new ArrayList<>(2);
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "like-rank"));
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value",
					"[value]",
					this.likeRank + ""));

				clickHandler = null;

				break;
			}
			case DISLIKE:
			{
				icon = new ItemStack(Material.IRON_INGOT);
				name = this.user.getTranslation(Constants.BUTTON + "dislike");

				description = new ArrayList<>(2);
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "dislike"));
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value",
					"[value]",
					this.likesObject.getDislikes() + ""));

				clickHandler = null;

				break;
			}
			case DISLIKE_RANK:
			{
				icon = new ItemStack(Material.IRON_BLOCK);
				name = this.user.getTranslation(Constants.BUTTON + "dislike-rank");

				description = new ArrayList<>(2);
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "dislike-rank"));
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value",
					"[value]",
					this.dislikeRank + ""));

				clickHandler = null;

				break;
			}
			case OVERALL:
			{
				icon = new ItemStack(Material.DIAMOND);
				name = this.user.getTranslation(Constants.BUTTON + "overall");

				description = new ArrayList<>(2);
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "overall"));
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value",
					"[value]",
					this.likesObject.getRank() + ""));

				clickHandler = null;

				break;
			}
			case OVERALL_RANK:
			{
				icon = new ItemStack(Material.DIAMOND_BLOCK);
				name = this.user.getTranslation(Constants.BUTTON + "overall-rank");

				description = new ArrayList<>(2);
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "overall-rank"));
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value",
					"[value]",
					this.overallRank + ""));

				clickHandler = null;

				break;
			}
			case NEXT_LIKE:
			{
				icon = new ItemStack(Material.OAK_SIGN);
				name = this.user.getTranslation(Constants.BUTTON + "next");
				description = new ArrayList<>(1);
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "like-next"));

				clickHandler = (panel, user, clickType, slot) -> {
					this.likeOffset++;
					this.build();
					return true;
				};

				break;
			}
			case PREVIOUS_LIKE:
			{
				icon = new ItemStack(Material.OAK_SIGN);
				name = this.user.getTranslation(Constants.BUTTON + "previous");
				description = new ArrayList<>(1);
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "like-previous"));

				clickHandler = (panel, user, clickType, slot) -> {
					this.likeOffset--;
					this.build();
					return true;
				};

				break;
			}
			case NEXT_DISLIKE:
			{
				icon = new ItemStack(Material.OAK_SIGN);
				name = this.user.getTranslation(Constants.BUTTON + "next");
				description = new ArrayList<>(1);
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "dislike-next"));

				clickHandler = (panel, user, clickType, slot) -> {
					this.dislikeOffset++;
					this.build();
					return true;
				};

				break;
			}
			case PREVIOUS_DISLIKE:
			{
				icon = new ItemStack(Material.OAK_SIGN);
				name = this.user.getTranslation(Constants.BUTTON + "previous");
				description = new ArrayList<>(1);
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "dislike-previous"));

				clickHandler = (panel, user, clickType, slot) -> {
					this.dislikeOffset--;
					this.build();
					return true;
				};

				break;
			}
			default:
				return null;
		}

		return new PanelItemBuilder().
			icon(icon).
			name(name).
			description(GuiUtils.stringSplit(description, 999)).
			glow(false).
			clickHandler(clickHandler).
			build();
	}


	/**
	 * This method populates all likers into given panel builder.
	 * @param panelBuilder PanelBuilder object.
	 */
	private void populateLikers(PanelBuilder panelBuilder)
	{
		if (this.likeOffset > 0)
		{
			panelBuilder.item(18, this.createButton(Button.PREVIOUS_LIKE));
		}

		if ((this.likeOffset + 1) * 7 < this.likesObject.getLikes())
		{
			panelBuilder.item(18, this.createButton(Button.NEXT_LIKE));
		}

		final int startIndex = this.likeOffset * 7;

		for (int index = 0; index < 7 && startIndex + index < this.likesObject.getLikes(); index++)
		{
			String userName = this.likedByUsers.get(startIndex + index);

			panelBuilder.item(19 + index, new PanelItemBuilder().
				icon(userName).
				glow(false).
				build());
		}
	}


	/**
	 * This method populates all dislikers into given panel builder.
	 * @param panelBuilder PanelBuilder object.
	 */
	private void populateDislikers(PanelBuilder panelBuilder)
	{
		if (this.dislikeOffset > 0)
		{
			panelBuilder.item(28, this.createButton(Button.PREVIOUS_DISLIKE));
		}

		if ((this.dislikeOffset + 1) * 7 < this.likesObject.getDislikes())
		{
			panelBuilder.item(29, this.createButton(Button.NEXT_DISLIKE));
		}

		final int startIndex = this.dislikeOffset * 7;

		for (int index = 0; index < 7 && startIndex + index < this.likesObject.getDislikes(); index++)
		{
			String userName = this.dislikedByUsers.get(startIndex + index);

			panelBuilder.item(19 + index, new PanelItemBuilder().
				icon(userName).
				glow(false).
				build());
		}
	}


// ---------------------------------------------------------------------
// Section: Enums
// ---------------------------------------------------------------------


	/**
	 * This enum holds all action buttons that can be added in current gui.
	 */
	private enum Button
	{
		LIKE,
		LIKE_RANK,
		DISLIKE,
		DISLIKE_RANK,
		OVERALL,
		OVERALL_RANK,

		NEXT_LIKE,
		PREVIOUS_LIKE,

		NEXT_DISLIKE,
		PREVIOUS_DISLIKE
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

	/**
	 * This variable holds likes object that is view by user in current gui.
	 */
	private final LikesObject likesObject;

	/**
	 * This variable stores index of current liker.
	 */
	private int likeOffset;

	/**
	 * This variable stores index of current disliker.
	 */
	private int dislikeOffset;

	/**
	 * This list contains player names that liked current island.
	 */
	private final List<String> likedByUsers;

	/**
	 * This list contains player names that disliked current island.
	 */
	private final List<String> dislikedByUsers;

	/**
	 * This variable holds island rank by likes.
	 */
	private final long likeRank;

	/**
	 * This variable holds island rank by dislikes.
	 */
	private final long dislikeRank;

	/**
	 * This variable holds island rank by rank.
	 */
	private final long overallRank;
}
