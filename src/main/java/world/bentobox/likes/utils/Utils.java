//
// Created by BONNe
// Copyright - 2019
//


package world.bentobox.likes.utils;


import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.World;
import org.bukkit.permissions.PermissionAttachmentInfo;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.user.User;


public class Utils
{
	/**
	 * This method gets string value of given permission prefix. If user does not have
	 * given permission or it have all (*), then return default value.
	 * @param user User who's permission should be checked.
	 * @param permissionPrefix Prefix that need to be found.
	 * @param defaultValue Default value that will be returned if permission not found.
	 * @return String value that follows permissionPrefix.
	 */
	public static String getPermissionValue(User user, String permissionPrefix, String defaultValue)
	{
		if (user != null && user.isPlayer())
		{
			if (permissionPrefix.endsWith("."))
			{
				permissionPrefix = permissionPrefix.substring(0, permissionPrefix.length() - 1);
			}

			String permPrefix = permissionPrefix + ".";

			List<String> permissions = user.getEffectivePermissions().stream().
				map(PermissionAttachmentInfo::getPermission).
				filter(permission -> permission.startsWith(permPrefix)).
				collect(Collectors.toList());

			for (String permission : permissions)
			{
				if (permission.contains(permPrefix + "*"))
				{
					// * means all. So continue to search more specific.
					continue;
				}

				String[] parts = permission.split(permPrefix);

				if (parts.length > 1)
				{
					return parts[1];
				}
			}
		}

		return defaultValue;
	}


	/**
	 * This method transforms given World into GameMode name. If world is not a GameMode
	 * world then it returns null.
	 * @param world World which gameMode name must be found out.
	 * @return GameMode name or null.
	 */
	public static String getGameMode(World world)
	{
		return BentoBox.getInstance().getIWM().getAddon(world).
			map(gameModeAddon -> gameModeAddon.getDescription().getName()).
			orElse(null);
	}


	/**
	 * This method transforms given GameMode into name.
	 * @param gameModeAddon GameMode which name must be returned.
	 * @return GameMode name.
	 */
	public static String getGameMode(GameModeAddon gameModeAddon)
	{
		return gameModeAddon.getDescription().getName();
	}


	/**
	 * This method allows to get next value from array list after given value.
	 * @param values Array that should be searched for given value.
	 * @param currentValue Value which next element should be found.
	 * @param <T> Instance of given object.
	 * @return Next value after currentValue in values array.
	 */
	public static <T> T getNextValue(T[] values, T currentValue)
	{
		for (int i = 0; i < values.length; i++)
		{
			if (values[i].equals(currentValue))
			{
				if (i + 1 == values.length)
				{
					return values[0];
				}
				else
				{
					return values[i + 1];
				}
			}
		}

		return currentValue;
	}


	/**
	 * This method allows to get previous value from array list after given value.
	 * @param values Array that should be searched for given value.
	 * @param currentValue Value which previous element should be found.
	 * @param <T> Instance of given object.
	 * @return Previous value before currentValue in values array.
	 */
	public static <T> T getPreviousValue(T[] values, T currentValue)
	{
		for (int i = 0; i < values.length; i++)
		{
			if (values[i].equals(currentValue))
			{
				if (i > 0)
				{
					return values[i - 1];
				}
				else
				{
					return values[values.length - 1];
				}
			}
		}

		return currentValue;
	}
}
