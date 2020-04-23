//
// Created by BONNe
// Copyright - 2019
//


package world.bentobox.likes.placeholders;



import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.likes.LikesAddon;
import world.bentobox.likes.config.Settings;
import world.bentobox.likes.database.objects.LikesObject;
import world.bentobox.likes.utils.collections.IndexedTreeSet;


/**
 * This enum holds all possible placeholder values for Likes Addon.
 */
public enum LikesAddonPlaceholderType
{
	/**
	 * This placeholder returns likes that is set for given island.
	 */
	ISLAND_LIKES("island_likes_count", (addon, gameModeAddon, island) -> island != null ?
		String.valueOf(LikesAddonPlaceholderType.getObject(addon, gameModeAddon, island).getLikes()) : ""),

	/**
	 * This placeholder returns island rank placement by their like count.
	 */
	ISLAND_LIKES_PLACE("island_likes_place", (addon, gameModeAddon, island) -> island != null ?
		String.valueOf(LikesAddonPlaceholderType.getSortedLikes(addon, gameModeAddon).
			entryIndex(LikesAddonPlaceholderType.getObject(addon, gameModeAddon, island))) : ""),

	/**
	 * This placeholder returns dislikes that is set for given island.
	 */
	ISLAND_DISLIKES("island_dislikes_count", (addon, gameModeAddon, island) -> island != null ?
		String.valueOf(LikesAddonPlaceholderType.getObject(addon, gameModeAddon, island).getDislikes()) : ""),

	/**
	 * This placeholder returns island rank placement by their dislike count.
	 */
	ISLAND_DISLIKES_PLACE("island_dislikes_place", (addon, gameModeAddon, island) -> island != null ?
		String.valueOf(LikesAddonPlaceholderType.getSortedDislikes(addon, gameModeAddon).
			entryIndex(LikesAddonPlaceholderType.getObject(addon, gameModeAddon, island))) : ""),

	/**
	 * This placeholder returns rank that is set for given island.
	 */
	ISLAND_RANK("island_rank_count", (addon, gameModeAddon, island) -> island != null ?
		String.valueOf(LikesAddonPlaceholderType.getObject(addon, gameModeAddon, island).getRank()) : ""),

	/**
	 * This placeholder returns island rank placement by their rank count.
	 */
	ISLAND_RANK_PLACE("island_rank_place", (addon, gameModeAddon, island) -> island != null ?
		String.valueOf(LikesAddonPlaceholderType.getSortedRank(addon, gameModeAddon).
			entryIndex(LikesAddonPlaceholderType.getObject(addon, gameModeAddon, island))) : ""),

	/**
	 * This placeholder returns likes that is set for given island.
	 */
	ISLAND_STARS("island_stars_value", (addon, gameModeAddon, island) -> island != null ?
		String.valueOf(LikesAddonPlaceholderType.getObject(addon, gameModeAddon, island).getStarsValue()) : ""),

	/**
	 * This placeholder returns island rank placement by their like count.
	 */
	ISLAND_STARS_PLACE("island_stars_place", (addon, gameModeAddon, island) -> island != null ?
		String.valueOf(LikesAddonPlaceholderType.getSortedStars(addon, gameModeAddon).
			entryIndex(LikesAddonPlaceholderType.getObject(addon, gameModeAddon, island))) : ""),


// ---------------------------------------------------------------------
// Section: Top By Likes
// ---------------------------------------------------------------------

	/**
	 * This placeholder returns name of island owner which island is in top 1 place by like count.
	 */
	BY_LIKES_OWNER_NAME_1("top_likes_owner_name_1", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedLikes(addon, gameModeAddon).exact(0);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 1 place by like count.
	 */
	BY_LIKES_ISLAND_NAME_1("top_likes_island_name_1", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedLikes(addon, gameModeAddon).exact(0);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns count of likes for island that is in top 1 place by like count.
	 */
	BY_LIKES_COUNT_1("top_likes_count_1", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedLikes(addon, gameModeAddon).exact(0);
		return object != null ? String.valueOf(object.getLikes()) : "";
	}),

	/**
	 * This placeholder returns name of island owner which island is in top 2 place by like count.
	 */
	BY_LIKES_OWNER_NAME_2("top_likes_owner_name_2", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedLikes(addon, gameModeAddon).exact(1);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 2 place by like count.
	 */
	BY_LIKES_ISLAND_NAME_2("top_likes_island_name_2", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedLikes(addon, gameModeAddon).exact(1);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns count of likes for island that is in top 2 place by like count.
	 */
	BY_LIKES_COUNT_2("top_likes_count_2", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedLikes(addon, gameModeAddon).exact(1);
		return object != null ? String.valueOf(object.getLikes()) : "";
	}),

	/**
	 * This placeholder returns name of island owner which island is in top 3 place by like count.
	 */
	BY_LIKES_OWNER_NAME_3("top_likes_owner_name_3", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedLikes(addon, gameModeAddon).exact(2);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 3 place by like count.
	 */
	BY_LIKES_ISLAND_NAME_3("top_likes_island_name_3", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedLikes(addon, gameModeAddon).exact(2);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns count of likes for island that is in top 3 place by like count.
	 */
	BY_LIKES_COUNT_3("top_likes_count_3", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedLikes(addon, gameModeAddon).exact(2);
		return object != null ? String.valueOf(object.getLikes()) : "";
	}),

	/**
	 * This placeholder returns name of island owner which island is in top 4 place by like count.
	 */
	BY_LIKES_OWNER_NAME_4("top_likes_owner_name_4", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedLikes(addon, gameModeAddon).exact(3);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 4 place by like count.
	 */
	BY_LIKES_ISLAND_NAME_4("top_likes_island_name_4", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedLikes(addon, gameModeAddon).exact(3);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns count of likes for island that is in top 4 place by like count.
	 */
	BY_LIKES_COUNT_4("top_likes_count_4", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedLikes(addon, gameModeAddon).exact(3);
		return object != null ? String.valueOf(object.getLikes()) : "";
	}),

	/**
	 * This placeholder returns name of island owner which island is in top 5 place by like count.
	 */
	BY_LIKES_OWNER_NAME_5("top_likes_owner_name_5", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedLikes(addon, gameModeAddon).exact(4);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 5 place by like count.
	 */
	BY_LIKES_ISLAND_NAME_5("top_likes_island_name_5", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedLikes(addon, gameModeAddon).exact(4);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns count of likes for island that is in top 5 place by like count.
	 */
	BY_LIKES_COUNT_5("top_likes_count_5", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedLikes(addon, gameModeAddon).exact(4);
		return object != null ? String.valueOf(object.getLikes()) : "";
	}),

	/**
	 * This placeholder returns name of island owner which island is in top 6 place by like count.
	 */
	BY_LIKES_OWNER_NAME_6("top_likes_owner_name_6", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedLikes(addon, gameModeAddon).exact(5);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 6 place by like count.
	 */
	BY_LIKES_ISLAND_NAME_6("top_likes_island_name_6", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedLikes(addon, gameModeAddon).exact(5);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns count of likes for island that is in top 6 place by like count.
	 */
	BY_LIKES_COUNT_6("top_likes_count_6", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedLikes(addon, gameModeAddon).exact(5);
		return object != null ? String.valueOf(object.getLikes()) : "";
	}),

	/**
	 * This placeholder returns name of island owner which island is in top 7 place by like count.
	 */
	BY_LIKES_OWNER_NAME_7("top_likes_owner_name_7", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedLikes(addon, gameModeAddon).exact(6);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 7 place by like count.
	 */
	BY_LIKES_ISLAND_NAME_7("top_likes_island_name_7", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedLikes(addon, gameModeAddon).exact(6);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns count of likes for island that is in top 7 place by like count.
	 */
	BY_LIKES_COUNT_7("top_likes_count_7", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedLikes(addon, gameModeAddon).exact(6);
		return object != null ? String.valueOf(object.getLikes()) : "";
	}),

	/**
	 * This placeholder returns name of island owner which island is in top 8 place by like count.
	 */
	BY_LIKES_OWNER_NAME_8("top_likes_owner_name_8", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedLikes(addon, gameModeAddon).exact(7);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 8 place by like count.
	 */
	BY_LIKES_ISLAND_NAME_8("top_likes_island_name_8", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedLikes(addon, gameModeAddon).exact(7);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns count of likes for island that is in top 8 place by like count.
	 */
	BY_LIKES_COUNT_8("top_likes_count_8", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedLikes(addon, gameModeAddon).exact(7);
		return object != null ? String.valueOf(object.getLikes()) : "";
	}),

	/**
	 * This placeholder returns name of island owner which island is in top 9 place by like count.
	 */
	BY_LIKES_OWNER_NAME_9("top_likes_owner_name_9", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedLikes(addon, gameModeAddon).exact(8);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 9 place by like count.
	 */
	BY_LIKES_ISLAND_NAME_9("top_likes_island_name_9", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedLikes(addon, gameModeAddon).exact(8);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns count of likes for island that is in top 9 place by like count.
	 */
	BY_LIKES_COUNT_9("top_likes_count_9", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedLikes(addon, gameModeAddon).exact(8);
		return object != null ? String.valueOf(object.getLikes()) : "";
	}),

	/**
	 * This placeholder returns name of island owner which island is in top 10 place by like count.
	 */
	BY_LIKES_OWNER_NAME_10("top_likes_owner_name_10", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedLikes(addon, gameModeAddon).exact(9);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 10 by like count.
	 */
	BY_LIKES_ISLAND_NAME_10("top_likes_island_name_10", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedLikes(addon, gameModeAddon).exact(9);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns count of likes for island that is in top 10 place by like count.
	 */
	BY_LIKES_COUNT_10("top_likes_count_10", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedLikes(addon, gameModeAddon).exact(9);
		return object != null ? String.valueOf(object.getLikes()) : "";
	}),

// ---------------------------------------------------------------------
// Section: Top By Dislikes
// ---------------------------------------------------------------------


	/**
	 * This placeholder returns name of island owner which island is in top 1 place by dislike count.
	 */
	BY_DISLIKES_OWNER_NAME_1("top_dislikes_owner_name_1", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedDislikes(addon, gameModeAddon).exact(0);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 1 place by dislike count.
	 */
	BY_DISLIKES_ISLAND_NAME_1("top_dislikes_island_name_1", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedDislikes(addon, gameModeAddon).exact(0);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns count of dislikes for island that is in top 1 place by dislike count.
	 */
	BY_DISLIKES_COUNT_1("top_dislikes_count_1", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedDislikes(addon, gameModeAddon).exact(0);
		return object != null ? String.valueOf(object.getDislikes()) : "";
	}),

	/**
	 * This placeholder returns name of island owner which island is in top 2 place by dislike count.
	 */
	BY_DISLIKES_OWNER_NAME_2("top_dislikes_owner_name_2", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedDislikes(addon, gameModeAddon).exact(1);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 2 place by dislike count.
	 */
	BY_DISLIKES_ISLAND_NAME_2("top_dislikes_island_name_2", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedDislikes(addon, gameModeAddon).exact(1);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns count of dislikes for island that is in top 2 place by dislike count.
	 */
	BY_DISLIKES_COUNT_2("top_dislikes_count_2", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedDislikes(addon, gameModeAddon).exact(1);
		return object != null ? String.valueOf(object.getDislikes()) : "";
	}),

	/**
	 * This placeholder returns name of island owner which island is in top 3 place by dislike count.
	 */
	BY_DISLIKES_OWNER_NAME_3("top_dislikes_owner_name_3", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedDislikes(addon, gameModeAddon).exact(2);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 3 place by dislike count.
	 */
	BY_DISLIKES_ISLAND_NAME_3("top_dislikes_island_name_3", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedDislikes(addon, gameModeAddon).exact(2);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns count of dislikes for island that is in top 3 place by dislike count.
	 */
	BY_DISLIKES_COUNT_3("top_dislikes_count_3", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedDislikes(addon, gameModeAddon).exact(2);
		return object != null ? String.valueOf(object.getDislikes()) : "";
	}),

	/**
	 * This placeholder returns name of island owner which island is in top 4 place by dislike count.
	 */
	BY_DISLIKES_OWNER_NAME_4("top_dislikes_owner_name_4", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedDislikes(addon, gameModeAddon).exact(3);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 4 place by dislike count.
	 */
	BY_DISLIKES_ISLAND_NAME_4("top_dislikes_island_name_4", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedDislikes(addon, gameModeAddon).exact(3);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns count of dislikes for island that is in top 4 place by dislike count.
	 */
	BY_DISLIKES_COUNT_4("top_dislikes_count_4", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedDislikes(addon, gameModeAddon).exact(3);
		return object != null ? String.valueOf(object.getDislikes()) : "";
	}),

	/**
	 * This placeholder returns name of island owner which island is in top 5 place by dislike count.
	 */
	BY_DISLIKES_OWNER_NAME_5("top_dislikes_owner_name_5", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedDislikes(addon, gameModeAddon).exact(4);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 5 place by dislike count.
	 */
	BY_DISLIKES_ISLAND_NAME_5("top_dislikes_island_name_5", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedDislikes(addon, gameModeAddon).exact(4);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns count of dislikes for island that is in top 5 place by dislike count.
	 */
	BY_DISLIKES_COUNT_5("top_dislikes_count_5", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedDislikes(addon, gameModeAddon).exact(4);
		return object != null ? String.valueOf(object.getDislikes()) : "";
	}),

	/**
	 * This placeholder returns name of island owner which island is in top 6 place by dislike count.
	 */
	BY_DISLIKES_OWNER_NAME_6("top_dislikes_owner_name_6", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedDislikes(addon, gameModeAddon).exact(5);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 6 place by dislike count.
	 */
	BY_DISLIKES_ISLAND_NAME_6("top_dislikes_island_name_6", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedDislikes(addon, gameModeAddon).exact(5);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns count of dislikes for island that is in top 6 place by dislike count.
	 */
	BY_DISLIKES_COUNT_6("top_dislikes_count_6", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedDislikes(addon, gameModeAddon).exact(5);
		return object != null ? String.valueOf(object.getDislikes()) : "";
	}),

	/**
	 * This placeholder returns name of island owner which island is in top 7 place by dislike count.
	 */
	BY_DISLIKES_OWNER_NAME_7("top_dislikes_owner_name_7", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedDislikes(addon, gameModeAddon).exact(6);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 7 place by dislike count.
	 */
	BY_DISLIKES_ISLAND_NAME_7("top_dislikes_island_name_7", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedDislikes(addon, gameModeAddon).exact(6);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns count of dislikes for island that is in top 7 place by dislike count.
	 */
	BY_DISLIKES_COUNT_7("top_dislikes_count_7", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedDislikes(addon, gameModeAddon).exact(6);
		return object != null ? String.valueOf(object.getDislikes()) : "";
	}),

	/**
	 * This placeholder returns name of island owner which island is in top 8 place by dislike count.
	 */
	BY_DISLIKES_OWNER_NAME_8("top_dislikes_owner_name_8", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedDislikes(addon, gameModeAddon).exact(7);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 8 place by dislike count.
	 */
	BY_DISLIKES_ISLAND_NAME_8("top_dislikes_island_name_8", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedDislikes(addon, gameModeAddon).exact(7);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns count of dislikes for island that is in top 8 place by dislike count.
	 */
	BY_DISLIKES_COUNT_8("top_dislikes_count_8", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedDislikes(addon, gameModeAddon).exact(7);
		return object != null ? String.valueOf(object.getDislikes()) : "";
	}),

	/**
	 * This placeholder returns name of island owner which island is in top 9 place by dislike count.
	 */
	BY_DISLIKES_OWNER_NAME_9("top_dislikes_owner_name_9", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedDislikes(addon, gameModeAddon).exact(8);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 9 place by dislike count.
	 */
	BY_DISLIKES_ISLAND_NAME_9("top_dislikes_island_name_9", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedDislikes(addon, gameModeAddon).exact(8);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns count of dislikes for island that is in top 9 place by dislike count.
	 */
	BY_DISLIKES_COUNT_9("top_dislikes_count_9", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedDislikes(addon, gameModeAddon).exact(8);
		return object != null ? String.valueOf(object.getDislikes()) : "";
	}),

	/**
	 * This placeholder returns name of island owner which island is in top 10 place by dislike count.
	 */
	BY_DISLIKES_OWNER_NAME_10("top_dislikes_owner_name_10", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedDislikes(addon, gameModeAddon).exact(9);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 10 by dislike count.
	 */
	BY_DISLIKES_ISLAND_NAME_10("top_dislikes_island_name_10", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedDislikes(addon, gameModeAddon).exact(9);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns count of dislikes for island that is in top 10 place by dislike count.
	 */
	BY_DISLIKES_COUNT_10("top_dislikes_count_10", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedDislikes(addon, gameModeAddon).exact(9);
		return object != null ? String.valueOf(object.getDislikes()) : "";
	}),

// ---------------------------------------------------------------------
// Section: Top By Rank
// ---------------------------------------------------------------------


	/**
	 * This placeholder returns name of island owner which island is in top 1 place by rank.
	 */
	BY_RANK_OWNER_NAME_1("top_rank_owner_name_1", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedRank(addon, gameModeAddon).exact(0);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 1 place by rank.
	 */
	BY_RANK_ISLAND_NAME_1("top_rank_island_name_1", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedRank(addon, gameModeAddon).exact(0);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns rank number for island that is in top 1 place by rank.
	 */
	BY_RANK_COUNT_1("top_rank_count_1", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedRank(addon, gameModeAddon).exact(0);
		return object != null ? String.valueOf(object.getRank()) : "";
	}),

	/**
	 * This placeholder returns name of island owner which island is in top 2 place by rank.
	 */
	BY_RANK_OWNER_NAME_2("top_rank_owner_name_2", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedRank(addon, gameModeAddon).exact(1);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 2 place by rank.
	 */
	BY_RANK_ISLAND_NAME_2("top_rank_island_name_2", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedRank(addon, gameModeAddon).exact(1);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns rank number for island that is in top 2 place by rank.
	 */
	BY_RANK_COUNT_2("top_rank_count_2", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedRank(addon, gameModeAddon).exact(1);
		return object != null ? String.valueOf(object.getRank()) : "";
	}),

	/**
	 * This placeholder returns name of island owner which island is in top 3 place by rank.
	 */
	BY_RANK_OWNER_NAME_3("top_rank_owner_name_3", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedRank(addon, gameModeAddon).exact(2);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 3 place by rank.
	 */
	BY_RANK_ISLAND_NAME_3("top_rank_island_name_3", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedRank(addon, gameModeAddon).exact(2);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns rank number for island that is in top 3 place by rank.
	 */
	BY_RANK_COUNT_3("top_rank_count_3", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedRank(addon, gameModeAddon).exact(2);
		return object != null ? String.valueOf(object.getRank()) : "";
	}),

	/**
	 * This placeholder returns name of island owner which island is in top 4 place by rank.
	 */
	BY_RANK_OWNER_NAME_4("top_rank_owner_name_4", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedRank(addon, gameModeAddon).exact(3);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 4 place by rank.
	 */
	BY_RANK_ISLAND_NAME_4("top_rank_island_name_4", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedRank(addon, gameModeAddon).exact(3);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns rank number for island that is in top 4 place by rank.
	 */
	BY_RANK_COUNT_4("top_rank_count_4", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedRank(addon, gameModeAddon).exact(3);
		return object != null ? String.valueOf(object.getRank()) : "";
	}),

	/**
	 * This placeholder returns name of island owner which island is in top 5 place by rank.
	 */
	BY_RANK_OWNER_NAME_5("top_rank_owner_name_5", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedRank(addon, gameModeAddon).exact(4);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 5 place by rank.
	 */
	BY_RANK_ISLAND_NAME_5("top_rank_island_name_5", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedRank(addon, gameModeAddon).exact(4);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns rank number for island that is in top 5 place by rank.
	 */
	BY_RANK_COUNT_5("top_rank_count_5", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedRank(addon, gameModeAddon).exact(4);
		return object != null ? String.valueOf(object.getRank()) : "";
	}),

	/**
	 * This placeholder returns name of island owner which island is in top 6 place by rank.
	 */
	BY_RANK_OWNER_NAME_6("top_rank_owner_name_6", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedRank(addon, gameModeAddon).exact(5);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 6 place by rank.
	 */
	BY_RANK_ISLAND_NAME_6("top_rank_island_name_6", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedRank(addon, gameModeAddon).exact(5);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns rank number for island that is in top 6 place by rank.
	 */
	BY_RANK_COUNT_6("top_rank_count_6", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedRank(addon, gameModeAddon).exact(5);
		return object != null ? String.valueOf(object.getRank()) : "";
	}),

	/**
	 * This placeholder returns name of island owner which island is in top 7 place by rank.
	 */
	BY_RANK_OWNER_NAME_7("top_rank_owner_name_7", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedRank(addon, gameModeAddon).exact(6);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 7 place by rank.
	 */
	BY_RANK_ISLAND_NAME_7("top_rank_island_name_7", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedRank(addon, gameModeAddon).exact(6);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns rank number for island that is in top 7 place by rank.
	 */
	BY_RANK_COUNT_7("top_rank_count_7", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedRank(addon, gameModeAddon).exact(6);
		return object != null ? String.valueOf(object.getRank()) : "";
	}),

	/**
	 * This placeholder returns name of island owner which island is in top 8 place by rank.
	 */
	BY_RANK_OWNER_NAME_8("top_rank_owner_name_8", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedRank(addon, gameModeAddon).exact(7);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 8 place by rank.
	 */
	BY_RANK_ISLAND_NAME_8("top_rank_island_name_8", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedRank(addon, gameModeAddon).exact(7);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns rank number for island that is in top 8 place by rank.
	 */
	BY_RANK_COUNT_8("top_rank_count_8", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedRank(addon, gameModeAddon).exact(7);
		return object != null ? String.valueOf(object.getRank()) : "";
	}),

	/**
	 * This placeholder returns name of island owner which island is in top 9 place by rank.
	 */
	BY_RANK_OWNER_NAME_9("top_rank_owner_name_9", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedRank(addon, gameModeAddon).exact(8);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 9 place by rank.
	 */
	BY_RANK_ISLAND_NAME_9("top_rank_island_name_9", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedRank(addon, gameModeAddon).exact(8);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns rank number for island that is in top 9 place by rank.
	 */
	BY_RANK_COUNT_9("top_rank_count_9", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedRank(addon, gameModeAddon).exact(8);
		return object != null ? String.valueOf(object.getRank()) : "";
	}),

	/**
	 * This placeholder returns name of island owner which island is in top 10 place by rank.
	 */
	BY_RANK_OWNER_NAME_10("top_rank_owner_name_10", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedRank(addon, gameModeAddon).exact(9);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 10 by rank.
	 */
	BY_RANK_ISLAND_NAME_10("top_rank_island_name_10", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedRank(addon, gameModeAddon).exact(9);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns rank number for island that is in top 10 place by rank.
	 */
	BY_RANK_COUNT_10("top_rank_count_10", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedRank(addon, gameModeAddon).exact(9);
		return object != null ? String.valueOf(object.getRank()) : "";
	}),


// ---------------------------------------------------------------------
// Section: Top By Stars
// ---------------------------------------------------------------------


	/**
	 * This placeholder returns name of island owner which island is in top 1 place by average stars value.
	 */
	BY_STARS_OWNER_NAME_1("top_stars_owner_name_1", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedStars(addon, gameModeAddon).exact(0);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 1 place by average stars value.
	 */
	BY_STARS_ISLAND_NAME_1("top_stars_island_name_1", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedStars(addon, gameModeAddon).exact(0);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns count of likes for island that is in top 1 place by average stars value.
	 */
	BY_STARS_COUNT_1("top_stars_count_1", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedStars(addon, gameModeAddon).exact(0);
		return object != null ? String.valueOf(object.getStarsValue()) : "";
	}),

	/**
	 * This placeholder returns name of island owner which island is in top 2 place by average stars value.
	 */
	BY_STARS_OWNER_NAME_2("top_stars_owner_name_2", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedStars(addon, gameModeAddon).exact(1);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 2 place by average stars value.
	 */
	BY_STARS_ISLAND_NAME_2("top_stars_island_name_2", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedStars(addon, gameModeAddon).exact(1);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns count of likes for island that is in top 2 place by average stars value.
	 */
	BY_STARS_COUNT_2("top_stars_count_2", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedStars(addon, gameModeAddon).exact(1);
		return object != null ? String.valueOf(object.getStarsValue()) : "";
	}),

	/**
	 * This placeholder returns name of island owner which island is in top 3 place by average stars value.
	 */
	BY_STARS_OWNER_NAME_3("top_stars_owner_name_3", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedStars(addon, gameModeAddon).exact(2);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 3 place by average stars value.
	 */
	BY_STARS_ISLAND_NAME_3("top_stars_island_name_3", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedStars(addon, gameModeAddon).exact(2);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns count of likes for island that is in top 3 place by average stars value.
	 */
	BY_STARS_COUNT_3("top_stars_count_3", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedStars(addon, gameModeAddon).exact(2);
		return object != null ? String.valueOf(object.getStarsValue()) : "";
	}),

	/**
	 * This placeholder returns name of island owner which island is in top 4 place by average stars value.
	 */
	BY_STARS_OWNER_NAME_4("top_stars_owner_name_4", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedStars(addon, gameModeAddon).exact(3);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 4 place by average stars value.
	 */
	BY_STARS_ISLAND_NAME_4("top_stars_island_name_4", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedStars(addon, gameModeAddon).exact(3);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns count of likes for island that is in top 4 place by average stars value.
	 */
	BY_STARS_COUNT_4("top_stars_count_4", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedStars(addon, gameModeAddon).exact(3);
		return object != null ? String.valueOf(object.getStarsValue()) : "";
	}),

	/**
	 * This placeholder returns name of island owner which island is in top 5 place by average stars value.
	 */
	BY_STARS_OWNER_NAME_5("top_stars_owner_name_5", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedStars(addon, gameModeAddon).exact(4);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 5 place by average stars value.
	 */
	BY_STARS_ISLAND_NAME_5("top_stars_island_name_5", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedStars(addon, gameModeAddon).exact(4);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns count of likes for island that is in top 5 place by average stars value.
	 */
	BY_STARS_COUNT_5("top_stars_count_5", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedStars(addon, gameModeAddon).exact(4);
		return object != null ? String.valueOf(object.getStarsValue()) : "";
	}),

	/**
	 * This placeholder returns name of island owner which island is in top 6 place by average stars value.
	 */
	BY_STARS_OWNER_NAME_6("top_stars_owner_name_6", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedStars(addon, gameModeAddon).exact(5);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 6 place by average stars value.
	 */
	BY_STARS_ISLAND_NAME_6("top_stars_island_name_6", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedStars(addon, gameModeAddon).exact(5);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns count of likes for island that is in top 6 place by average stars value.
	 */
	BY_STARS_COUNT_6("top_stars_count_6", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedStars(addon, gameModeAddon).exact(5);
		return object != null ? String.valueOf(object.getStarsValue()) : "";
	}),

	/**
	 * This placeholder returns name of island owner which island is in top 7 place by average stars value.
	 */
	BY_STARS_OWNER_NAME_7("top_stars_owner_name_7", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedStars(addon, gameModeAddon).exact(6);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 7 place by average stars value.
	 */
	BY_STARS_ISLAND_NAME_7("top_stars_island_name_7", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedStars(addon, gameModeAddon).exact(6);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns count of likes for island that is in top 7 place by average stars value.
	 */
	BY_STARS_COUNT_7("top_stars_count_7", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedStars(addon, gameModeAddon).exact(6);
		return object != null ? String.valueOf(object.getStarsValue()) : "";
	}),

	/**
	 * This placeholder returns name of island owner which island is in top 8 place by average stars value.
	 */
	BY_STARS_OWNER_NAME_8("top_stars_owner_name_8", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedStars(addon, gameModeAddon).exact(7);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 8 place by average stars value.
	 */
	BY_STARS_ISLAND_NAME_8("top_stars_island_name_8", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedStars(addon, gameModeAddon).exact(7);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns count of likes for island that is in top 8 place by average stars value.
	 */
	BY_STARS_COUNT_8("top_stars_count_8", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedStars(addon, gameModeAddon).exact(7);
		return object != null ? String.valueOf(object.getStarsValue()) : "";
	}),

	/**
	 * This placeholder returns name of island owner which island is in top 9 place by average stars value.
	 */
	BY_STARS_OWNER_NAME_9("top_stars_owner_name_9", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedStars(addon, gameModeAddon).exact(8);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 9 place by average stars value.
	 */
	BY_STARS_ISLAND_NAME_9("top_stars_island_name_9", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedStars(addon, gameModeAddon).exact(8);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns count of likes for island that is in top 9 place by average stars value.
	 */
	BY_STARS_COUNT_9("top_stars_count_9", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedStars(addon, gameModeAddon).exact(8);
		return object != null ? String.valueOf(object.getStarsValue()) : "";
	}),

	/**
	 * This placeholder returns name of island owner which island is in top 10 place by average stars value.
	 */
	BY_STARS_OWNER_NAME_10("top_stars_owner_name_10", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedStars(addon, gameModeAddon).exact(9);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
	}),

	/**
	 * This placeholder returns name of island which is in top 10 by average stars value.
	 */
	BY_STARS_ISLAND_NAME_10("top_stars_island_name_10", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedStars(addon, gameModeAddon).exact(9);

		return object == null ? "" : addon.getIslands().getIslandById(object.getUniqueId()).
			map(value -> value.getName() == null ? addon.getPlayers().getName(value.getOwner()) : value.getName()).
			orElse("");
	}),

	/**
	 * This placeholder returns count of likes for island that is in top 10 place by average stars value.
	 */
	BY_STARS_COUNT_10("top_stars_count_10", (addon, gameModeAddon, island) -> {
		LikesObject object = LikesAddonPlaceholderType.getSortedStars(addon, gameModeAddon).exact(9);
		return object != null ? String.valueOf(object.getStarsValue()) : "";
	});


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


	/**
	 * Constructor that creates placeholders.
	 *
	 * @param placeholder PlaceHolder String.
	 * @param replacer Placeholder Replacer.
	 */
	LikesAddonPlaceholderType(String placeholder, LikesAddonPlaceholderReplacer replacer)
	{
		this.placeholder = "likes_addon_" + placeholder;
		this.replacer = replacer;
	}


	/**
	 * Method LikesAddonPlaceholderType#getPlaceholder returns the placeholder of this object.
	 *
	 * @return the placeholder (type String) of this object.
	 */
	public String getPlaceholder()
	{
		return placeholder;
	}


	/**
	 * Method LikesAddonPlaceholderType#getReplacer returns the replacer of this object.
	 *
	 * @return the replacer (type GameModePlaceholderReplacer) of this object.
	 */
	public LikesAddonPlaceholderReplacer getReplacer()
	{
		return replacer;
	}


// ---------------------------------------------------------------------
// Section: Static method
// ---------------------------------------------------------------------


	/**
	 * This method returns likes object that referees to given island object. If object does not exist,
	 * it creates new empty object.
	 * @param addon Likes Addon instance
	 * @param gameModeAddon GameMode addon instance
	 * @param island Island Object instance.
	 * @return Likes Object that represents given island.
	 */
	private static LikesObject getObject(LikesAddon addon, GameModeAddon gameModeAddon, Island island)
	{
		return addon.getManager().getIslandLikes(island.getUniqueId(), gameModeAddon);
	}


	/**
	 * This method returns IndexedTreeSet that contains ordered LikesObject by their like count.
	 * @param addon Likes Addon.
	 * @param gameModeAddon Target GameMode addon.
	 * @return IndexedTreeSet where objects are ordered by their like count.
	 */
	private static IndexedTreeSet<LikesObject> getSortedLikes(LikesAddon addon, GameModeAddon gameModeAddon)
	{
		return addon.getManager().getSortedLikes(gameModeAddon.getDescription().getName());
	}


	/**
	 * This method returns IndexedTreeSet that contains ordered LikesObject by their dislike count.
	 * @param addon Likes Addon.
	 * @param gameModeAddon Target GameMode addon.
	 * @return IndexedTreeSet where objects are ordered by their dislike count.
	 */
	private static IndexedTreeSet<LikesObject> getSortedDislikes(LikesAddon addon, GameModeAddon gameModeAddon)
	{
		return addon.getManager().getSortedDislikes(gameModeAddon.getDescription().getName());
	}


	/**
	 * This method returns IndexedTreeSet that contains ordered LikesObject by their rank number.
	 * @param addon Likes Addon.
	 * @param gameModeAddon Target GameMode addon.
	 * @return IndexedTreeSet where objects are ordered by their rank number.
	 */
	private static IndexedTreeSet<LikesObject> getSortedRank(LikesAddon addon, GameModeAddon gameModeAddon)
	{
		return addon.getManager().getSortedRank(gameModeAddon.getDescription().getName());
	}


	/**
	 * This method returns IndexedTreeSet that contains ordered LikesObject by their stars value.
	 * @param addon Likes Addon.
	 * @param gameModeAddon Target GameMode addon.
	 * @return IndexedTreeSet where objects are ordered by their stars value.
	 */
	private static IndexedTreeSet<LikesObject> getSortedStars(LikesAddon addon, GameModeAddon gameModeAddon)
	{
		return addon.getManager().getSortedStars(gameModeAddon.getDescription().getName());
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * This variable stores string text for placeholder.
	 */
	private String placeholder;

	/**
	 * This variable stores object that will replace placeholder with text.
	 */
	private LikesAddonPlaceholderReplacer replacer;
}
