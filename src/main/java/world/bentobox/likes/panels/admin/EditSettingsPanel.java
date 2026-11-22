///
// Created by BONNe
// Copyright - 2021
///


package world.bentobox.likes.panels.admin;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.NonNull;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

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
        this.settings = Objects.requireNonNull(this.addon, "addon").getSettings();
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
        final String reference = Constants.BUTTONS + button.name().toLowerCase();

        return switch (button)
        {
            case STARS_COST, LIKE_COST -> this.createCostButton(reference,
                Material.GOLD_INGOT,
                this.settings::getLikeAddCost,
                value -> this.addon.getSettings().setLikeAddCost(value));
            case STARS_REMOVE_COST, LIKE_REMOVE_COST -> this.createCostButton(reference,
                Material.GOLD_NUGGET,
                this.settings::getLikeRemoveCost,
                value -> this.addon.getSettings().setLikeRemoveCost(value));
            case DISLIKE_COST -> this.createCostButton(reference,
                Material.IRON_INGOT,
                this.settings::getDislikeAddCost,
                value -> this.addon.getSettings().setDislikeAddCost(value));
            case DISLIKE_REMOVE_COST -> this.createCostButton(reference,
                Material.IRON_NUGGET,
                this.settings::getDislikeRemoveCost,
                value -> this.addon.getSettings().setDislikeRemoveCost(value));
            case DEFAULT_ICON -> this.createDefaultIconButton(reference);
            case INFORM_PLAYERS -> this.createToggleButton(button,
                Material.JUKEBOX,
                this.settings::isInformPlayers,
                this.settings::setInformPlayers);
            case LOG_HISTORY -> this.createToggleButton(button,
                Material.WRITABLE_BOOK,
                this.settings::isLogHistory,
                this.settings::setLogHistory);
            case RESET_LIKES -> this.createToggleButton(button,
                Material.LAVA_BUCKET,
                this.settings::isResetLikes,
                this.settings::setResetLikes);
            // this should never happen.
            default -> PanelItem.empty();
        };
    }


    /**
     * This method creates button that allows to change a cost value.
     *
     * @param reference Translation reference of the button.
     * @param material Icon of the button.
     * @param getter Getter of the current cost.
     * @param setter Setter of the new cost.
     * @return PanelItem object that represents cost button.
     */
    private PanelItem createCostButton(String reference,
        Material material,
        DoubleSupplier getter,
        DoubleConsumer setter)
    {
        List<String> description = new ArrayList<>(4);
        description.add(this.user.getTranslationOrNothing(reference + DESCRIPTION_KEY_SUFFIX));
        description.add(this.user.getTranslation(reference + COST_KEY_SUFFIX,
            Constants.PARAMETER_NUMBER, String.valueOf(getter.getAsDouble())));
        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + CLICK_TO_CHANGE_TIP));

        PanelItem.ClickHandler clickHandler = (panel, clicker, clickType, slot) ->
        {
            Consumer<Number> numberConsumer = number ->
            {
                if (number != null)
                {
                    setter.accept(number.doubleValue());
                    this.addon.saveSettings();
                }

                // reopen panel
                this.build();
            };

            ConversationUtils.createNumericInput(numberConsumer,
                this.user,
                this.user.getTranslation(Constants.CONVERSATIONS + INPUT_NUMBER_KEY),
                0,
                Double.MAX_VALUE);

            return true;
        };

        return new PanelItemBuilder().
            icon(new ItemStack(material)).
            name(this.user.getTranslation(reference + NAME_KEY_SUFFIX)).
            description(description).
            glow(false).
            clickHandler(clickHandler).
            build();
    }


    /**
     * This method creates button that allows to change the default icon.
     *
     * @param reference Translation reference of the button.
     * @return PanelItem object that represents default icon button.
     */
    private PanelItem createDefaultIconButton(String reference)
    {
        List<String> description = new ArrayList<>(4);
        description.add(this.user.getTranslationOrNothing(reference + DESCRIPTION_KEY_SUFFIX));
        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + CLICK_TO_CHANGE_TIP));

        PanelItem.ClickHandler clickHandler = (panel, clicker, clickType, slot) ->
        {
            SingleBlockSelector.open(this.user,
                SingleBlockSelector.Mode.ANY,
                (status, block) -> {
                    if (Boolean.TRUE.equals(status))
                    {
                        this.settings.setDefaultIcon(block);
                        this.addon.saveSettings();
                    }

                    this.build();
                });

            return true;
        };

        return new PanelItemBuilder().
            icon(new ItemStack(this.settings.getDefaultIcon())).
            name(this.user.getTranslation(reference + NAME_KEY_SUFFIX)).
            description(description).
            glow(false).
            clickHandler(clickHandler).
            build();
    }


    /**
     * This method creates button that allows to enable and disable a setting.
     *
     * @param button Button that must be created.
     * @param material Icon of the button.
     * @param getter Getter of the current value.
     * @param setter Setter of the new value.
     * @return PanelItem object that represents toggle button.
     */
    private PanelItem createToggleButton(Button button,
        Material material,
        BooleanSupplier getter,
        Consumer<Boolean> setter)
    {
        final String reference = Constants.BUTTONS + button.name().toLowerCase();
        final boolean enabled = getter.getAsBoolean();

        List<String> description = new ArrayList<>(4);
        description.add(this.user.getTranslationOrNothing(reference + DESCRIPTION_KEY_SUFFIX));
        description.add(this.user.getTranslation(reference + (enabled ? ENABLED_KEY_SUFFIX : DISABLED_KEY_SUFFIX)));
        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS +
            (enabled ? CLICK_TO_DISABLE_TIP : CLICK_TO_ENABLE_TIP)));

        PanelItem.ClickHandler clickHandler = (panel, clicker, clickType, slot) ->
        {
            setter.accept(!getter.getAsBoolean());
            this.addon.saveSettings();
            panel.getInventory().setItem(slot, this.createButton(button).getItem());
            return true;
        };

        return new PanelItemBuilder().
            icon(new ItemStack(material)).
            name(this.user.getTranslation(reference + NAME_KEY_SUFFIX)).
            description(description).
            glow(enabled).
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
// Section: Constants
// ---------------------------------------------------------------------

    /**
     * String constant for ".cost" button key suffix.
     */
    private static final String NAME_KEY_SUFFIX = ".name";

    private static final String DESCRIPTION_KEY_SUFFIX = ".description";

    private static final String COST_KEY_SUFFIX = ".cost";

    /**
     * String constant for "click-to-change" tip key.
     */
    private static final String CLICK_TO_CHANGE_TIP = "click-to-change";

    /**
     * String constant for "input-number" conversation key.
     */
    private static final String INPUT_NUMBER_KEY = "input-number";

    /**
     * String constant for ".enabled" button key suffix.
     */
    private static final String ENABLED_KEY_SUFFIX = ".enabled";

    /**
     * String constant for "click-to-disable" tip key.
     */
    private static final String CLICK_TO_DISABLE_TIP = "click-to-disable";

    /**
     * String constant for ".disabled" button key suffix.
     */
    private static final String DISABLED_KEY_SUFFIX = ".disabled";

    /**
     * String constant for "click-to-enable" tip key.
     */
    private static final String CLICK_TO_ENABLE_TIP = "click-to-enable";

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
