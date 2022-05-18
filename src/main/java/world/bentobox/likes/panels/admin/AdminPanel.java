///
// Created by BONNe
// Copyright - 2021
///


package world.bentobox.likes.panels.admin;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.likes.LikesAddon;
import world.bentobox.likes.panels.CommonPanel;
import world.bentobox.likes.panels.ConversationUtils;
import world.bentobox.likes.utils.Constants;
import world.bentobox.likes.utils.Utils;


/**
 * This class allows to manage all admin operations from GUI.
 */
public class AdminPanel extends CommonPanel
{
    /**
     * This is internal constructor. It is used internally in current class to avoid creating objects everywhere.
     *
     * @param addon Likes object.
     * @param user User who opens Panel.
     * @param world World where gui is opened
     * @param permissionPrefix Permission Prefix
     */
    private AdminPanel(LikesAddon addon, User user, World world, String permissionPrefix)
    {
        super(addon, user, world, permissionPrefix);
    }


    @Override
    public void build()
    {
        PanelBuilder panelBuilder = new PanelBuilder().
            user(this.user).
            name(this.user.getTranslation(Constants.TITLES + "admin"));

        PanelUtils.fillBorder(panelBuilder);

        panelBuilder.item(10, this.createButton(Button.MANAGE_LIKES));
        panelBuilder.item(11, this.createButton(Button.LIKES_ICON));

        // Add All Player Data removal.
        panelBuilder.item(28, this.createButton(Button.WIPE_DATA));

        // Edit Addon Settings
        panelBuilder.item(16, this.createButton(Button.SETTINGS));

        // Add Return Button
        panelBuilder.item(44, this.returnButton);

        panelBuilder.build();
    }


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


    /**
     * This method creates given button as PanelItem
     *
     * @param button Button that must be created.
     * @return PanelItem of given button.
     */
    private PanelItem createButton(Button button)
    {
        ItemStack icon;
        PanelItem.ClickHandler clickHandler;

        final String reference = Constants.BUTTONS + button.name().toLowerCase();
        String name = this.user.getTranslation(reference + ".name");

        List<String> description = new ArrayList<>(3);
        description.add(this.user.getTranslation(reference + ".description"));
        description.add("");

        switch (button)
        {
            case MANAGE_LIKES:
            {
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-manage"));

                icon = new ItemStack(Material.WRITABLE_BOOK);
                clickHandler = (panel, user, clickType, slot) -> {
                    ListIslandsPanel.open(this, ListIslandsPanel.Type.MANAGE);
                    return true;
                };

                break;
            }
            case LIKES_ICON:
            {
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

                icon = new ItemStack(Material.ENCHANTING_TABLE);
                clickHandler = (panel, user, clickType, slot) -> {
                    ListIslandsPanel.open(this, ListIslandsPanel.Type.ICON);
                    return true;
                };

                break;
            }
            case WIPE_DATA:
            {
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-wipe"));

                icon = new ItemStack(Material.TNT);
                clickHandler = (panel, user, clickType, slot) ->
                {
                    // Create consumer that accepts value from conversation.
                    Consumer<Boolean> consumer = value ->
                    {
                        if (value)
                        {
                            this.addon.getAddonManager().wipeData(this.world);
                        }

                        this.build();
                    };

                    // Create conversation that gets user acceptance to delete island data.
                    ConversationUtils.createConfirmation(
                        consumer,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "confirm-island-data-deletion",
                            Constants.PARAMETER_GAMEMODE, Utils.getGameMode(this.world)),
                        this.user.getTranslation(Constants.CONVERSATIONS + "user-data-removed",
                            Constants.PARAMETER_GAMEMODE, Utils.getGameMode(this.world)));

                    return true;
                };

                break;
            }
            case SETTINGS:
            {
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-open"));

                icon = new ItemStack(Material.CRAFTING_TABLE);
                clickHandler = (panel, user, clickType, slot) ->
                {
                    EditSettingsPanel.openPanel(this);
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
            description(description).
            glow(false).
            clickHandler(clickHandler).
            build();
    }


    /**
     * This method is used to open UserPanel outside this class. It will be much easier to open panel with single method
     * call then initializing new object.
     *
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
// Section: Enums
// ---------------------------------------------------------------------


    /**
     * This enum contains all buttons for current GUI
     */
    private enum Button
    {
        LIKES_ICON,
        WIPE_DATA,
        SETTINGS,
        MANAGE_LIKES
    }
}
