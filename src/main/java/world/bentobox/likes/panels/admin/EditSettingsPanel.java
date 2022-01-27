///
// Created by BONNe
// Copyright - 2021
///


package world.bentobox.likes.panels.admin;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.likes.LikesAddon;
import world.bentobox.likes.config.Settings;
import world.bentobox.likes.panels.CommonPanel;
import world.bentobox.likes.panels.ConversationUtils;
import world.bentobox.likes.panels.util.SingleBlockSelector;
import world.bentobox.likes.utils.Constants;


/**
 * This class allows to edit all addon settings via GUI.
 */
public class EditSettingsPanel extends CommonPanel
{
    /**
     * This is internal constructor. It is used internally in current class to avoid creating objects everywhere.
     *
     * @param addon Likes object.
     * @param user User who opens Panel.
     * @param world World where gui is opened
     * @param permissionPrefix Permission Prefix
     */
    private EditSettingsPanel(LikesAddon addon, User user, World world, String permissionPrefix)
    {
        super(addon, user, world, permissionPrefix);
        this.settings = this.addon.getSettings();
    }


    /**
     * This is internal constructor. It is used internally in current class to avoid creating objects everywhere.
     *
     * @param parent Parent Panel
     */
    private EditSettingsPanel(CommonPanel parent)
    {
        super(parent);
        this.settings = this.addon.getSettings();
    }


    @Override
    public void build()
    {
        PanelBuilder panelBuilder = new PanelBuilder().
            user(this.user).
            name(this.user.getTranslation(Constants.TITLES + "settings"));

        PanelUtils.fillBorder(panelBuilder);

        // Like cost
        panelBuilder.item(10, this.createButton(Button.LIKE_COST));
        panelBuilder.item(19, this.createButton(Button.LIKE_REMOVE_COST));

        // Dislike cost
        panelBuilder.item(11, this.createButton(Button.DISLIKE_COST));
        panelBuilder.item(20, this.createButton(Button.DISLIKE_REMOVE_COST));

        // Default icon
        panelBuilder.item(22, this.createButton(Button.DEFAULT_ICON));

        panelBuilder.item(15, this.createButton(Button.INFORM_PLAYERS));
        panelBuilder.item(24, this.createButton(Button.LOG_HISTORY));

        // Reset likes on restart
        panelBuilder.item(25, this.createButton(Button.RESET_LIKES));

        // Add Return Button
        panelBuilder.item(44, this.returnButton);

        panelBuilder.build();
    }


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
        boolean glow;

        final String reference = Constants.BUTTONS + button.name().toLowerCase();
        String name = this.user.getTranslation(reference + ".name");

        List<String> description = new ArrayList<>(4);
        description.add(this.user.getTranslationOrNothing(reference + ".description"));

        switch (button)
        {
            case STARS_COST:
            case LIKE_COST:
            {
                description.add(this.user.getTranslation(reference + ".cost",
                    Constants.PARAMETER_NUMBER, String.valueOf(this.settings.getLikeAddCost())));
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

                icon = new ItemStack(Material.GOLD_INGOT);

                clickHandler = (panel, user, clickType, slot) ->
                {
                    Consumer<Number> numberConsumer = number ->
                    {
                        if (number != null)
                        {
                            this.addon.getSettings().setLikeAddCost(number.doubleValue());
                            this.addon.saveSettings();
                        }

                        // reopen panel
                        this.build();
                    };

                    ConversationUtils.createNumericInput(numberConsumer,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
                        0,
                        Double.MAX_VALUE);

                    return true;
                };
                glow = false;
                break;
            }
            case STARS_REMOVE_COST:
            case LIKE_REMOVE_COST:
            {
                description.add(this.user.getTranslation(reference + ".cost",
                    Constants.PARAMETER_NUMBER, String.valueOf(this.settings.getLikeRemoveCost())));
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

                icon = new ItemStack(Material.GOLD_NUGGET);

                clickHandler = (panel, user, clickType, slot) ->
                {
                    Consumer<Number> numberConsumer = number ->
                    {
                        if (number != null)
                        {
                            this.addon.getSettings().setLikeRemoveCost(number.doubleValue());
                            this.addon.saveSettings();
                        }

                        // reopen panel
                        this.build();
                    };

                    ConversationUtils.createNumericInput(numberConsumer,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
                        0,
                        Double.MAX_VALUE);

                    return true;
                };
                glow = false;
                break;
            }
            case DISLIKE_COST:
            {
                description.add(this.user.getTranslation(reference + ".cost",
                    Constants.PARAMETER_NUMBER, String.valueOf(this.settings.getDislikeAddCost())));
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

                icon = new ItemStack(Material.IRON_INGOT);

                clickHandler = (panel, user, clickType, slot) ->
                {
                    Consumer<Number> numberConsumer = number ->
                    {
                        if (number != null)
                        {
                            this.addon.getSettings().setDislikeAddCost(number.doubleValue());
                            this.addon.saveSettings();
                        }

                        // reopen panel
                        this.build();
                    };

                    ConversationUtils.createNumericInput(numberConsumer,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
                        0,
                        Double.MAX_VALUE);

                    return true;
                };
                glow = false;
                break;
            }
            case DISLIKE_REMOVE_COST:
            {
                description.add(this.user.getTranslation(reference + ".cost",
                    Constants.PARAMETER_NUMBER, String.valueOf(this.settings.getDislikeRemoveCost())));
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

                icon = new ItemStack(Material.IRON_NUGGET);

                clickHandler = (panel, user, clickType, slot) ->
                {
                    Consumer<Number> numberConsumer = number ->
                    {
                        if (number != null)
                        {
                            this.addon.getSettings().setDislikeRemoveCost(number.doubleValue());
                            this.addon.saveSettings();
                        }

                        // reopen panel
                        this.build();
                    };

                    ConversationUtils.createNumericInput(numberConsumer,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
                        0,
                        Double.MAX_VALUE);

                    return true;
                };
                glow = false;
                break;
            }
            case DEFAULT_ICON:
            {
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

                icon = new ItemStack(this.settings.getDefaultIcon());
                clickHandler = (panel, user, clickType, slot) ->
                {
                    SingleBlockSelector.open(this.user,
                        SingleBlockSelector.Mode.ANY,
                        (status, block) -> {
                            if (status)
                            {
                                this.settings.setDefaultIcon(block);
                                this.addon.saveSettings();
                            }

                            this.build();
                        });

                    return true;
                };
                glow = false;

                break;
            }
            case INFORM_PLAYERS:
            {
                glow = this.settings.isInformPlayers();

                if (glow)
                {
                    description.add(this.user.getTranslation(reference + ".enabled"));
                    description.add("");
                    description.add(this.user.getTranslation(Constants.TIPS + "click-to-disable"));
                }
                else
                {
                    description.add(this.user.getTranslation(reference + ".disabled"));
                    description.add("");
                    description.add(this.user.getTranslation(Constants.TIPS + "click-to-enable"));
                }

                icon = new ItemStack(Material.JUKEBOX);
                clickHandler = (panel, user, clickType, slot) ->
                {
                    this.settings.setInformPlayers(!this.settings.isInformPlayers());
                    this.addon.saveSettings();
                    panel.getInventory().setItem(slot, this.createButton(button).getItem());
                    return true;
                };

                break;
            }
            case LOG_HISTORY:
            {
                glow = this.settings.isLogHistory();

                if (glow)
                {
                    description.add(this.user.getTranslation(reference + ".enabled"));
                    description.add("");
                    description.add(this.user.getTranslation(Constants.TIPS + "click-to-disable"));
                }
                else
                {
                    description.add(this.user.getTranslation(reference + ".disabled"));
                    description.add("");
                    description.add(this.user.getTranslation(Constants.TIPS + "click-to-enable"));
                }

                icon = new ItemStack(Material.WRITABLE_BOOK);
                clickHandler = (panel, user, clickType, slot) ->
                {
                    this.settings.setLogHistory(!this.settings.isLogHistory());
                    this.addon.saveSettings();
                    panel.getInventory().setItem(slot, this.createButton(button).getItem());
                    return true;
                };

                break;
            }
            case RESET_LIKES:
            {
                glow = this.settings.isResetLikes();

                if (glow)
                {
                    description.add(this.user.getTranslation(reference + ".enabled"));
                    description.add("");
                    description.add(this.user.getTranslation(Constants.TIPS + "click-to-disable"));
                }
                else
                {
                    description.add(this.user.getTranslation(reference + ".disabled"));
                    description.add("");
                    description.add(this.user.getTranslation(Constants.TIPS + "click-to-enable"));
                }

                icon = new ItemStack(Material.LAVA_BUCKET);
                clickHandler = (panel, user, clickType, slot) ->
                {
                    this.settings.setResetLikes(!this.settings.isResetLikes());
                    this.addon.saveSettings();
                    panel.getInventory().setItem(slot, this.createButton(button).getItem());
                    return true;
                };

                break;
            }
            default:
                // this should never happen.
                return PanelItem.empty();
        }

        return new PanelItemBuilder().
            icon(icon).
            name(name).
            description(description).
            glow(glow).
            clickHandler(clickHandler).
            build();
    }


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


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
        new EditSettingsPanel(addon, user, world, permissionPrefix).build();
    }


    /**
     * This method is used to open UserPanel outside this class. It will be much easier to open panel with single method
     * call then initializing new object.
     *
     * @param parent Parent Panel
     */
    public static void openPanel(@NonNull CommonPanel parent)
    {
        new EditSettingsPanel(parent).build();
    }


// ---------------------------------------------------------------------
// Section: Enums
// ---------------------------------------------------------------------


    /**
     * This enum contains all buttons for current GUI
     */
    private enum Button
    {
        LIKE_COST,
        LIKE_REMOVE_COST,
        STARS_COST,
        STARS_REMOVE_COST,
        DISLIKE_COST,
        DISLIKE_REMOVE_COST,
        DEFAULT_ICON,
        INFORM_PLAYERS,
        LOG_HISTORY,
        RESET_LIKES,
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


    /**
     * This variable stores settings for current addon.
     */
    private final Settings settings;
}
