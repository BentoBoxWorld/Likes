//
// Created by BONNe
// Copyright - 2019
//


package world.bentobox.likes.panels.admin;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.likes.LikesAddon;
import world.bentobox.likes.config.Settings;
import world.bentobox.likes.panels.GuiUtils;
import world.bentobox.likes.panels.util.SelectBlocksGUI;
import world.bentobox.likes.utils.Constants;


/**
 * This class allows to edit all addon settings via GUI.
 */
public class EditSettingsPanel
{
	/**
	 * This is internal constructor. It is used internally in current class to avoid
	 * creating objects everywhere.
	 * @param addon Likes object.
	 * @param user User who opens Panel.
	 * @param world World where gui is opened
	 * @param permissionPrefix Permission Prefix
	 */
	private EditSettingsPanel(LikesAddon addon, User user, World world, String permissionPrefix)
	{
		this.addon = addon;
		this.user = user;
		this.world = world;

		this.permissionPrefix = permissionPrefix;

		this.settings = addon.getSettings();
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
		new EditSettingsPanel(addon, user, world, permissionPrefix).build();
	}


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


	private void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().
			user(this.user).
			name(this.user.getTranslation(Constants.TITLE + "settings"));

		GuiUtils.fillBorder(panelBuilder);

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

		// Open admin Panel
		panelBuilder.item(44, this.createButton(Button.RETURN));

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
		List<String> description = new ArrayList<>();
		boolean glow;
		PanelItem.ClickHandler clickHandler;

		switch (button)
		{
			case LIKE_COST:
			{
				name = this.user.getTranslation(Constants.BUTTON + "like-cost");
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "like-cost"));
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value",
					"[value]", String.valueOf(this.settings.getLikeAddCost())));
				icon = new ItemStack(Material.GOLD_INGOT);
				clickHandler = (panel, user, clickType, slot) -> {
					this.getNumberInput(number -> this.settings.setLikeAddCost(number.doubleValue()),
						this.user.getTranslation(Constants.QUESTIONS + "like-cost"));
					return true;
				};
				glow = false;

				break;
			}
			case LIKE_REMOVE_COST:
			{
				name = this.user.getTranslation(Constants.BUTTON + "like-remove-cost");
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "like-remove-cost"));
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value",
					"[value]", String.valueOf(this.settings.getLikeRemoveCost())));;
				icon = new ItemStack(Material.GOLD_NUGGET);
				clickHandler = (panel, user, clickType, slot) -> {
					this.getNumberInput(number -> this.settings.setLikeRemoveCost(number.doubleValue()),
						this.user.getTranslation(Constants.QUESTIONS + "like-remove-cost"));
					return true;
				};
				glow = false;

				break;
			}
			case DISLIKE_COST:
			{
				name = this.user.getTranslation(Constants.BUTTON + "dislike-cost");
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "dislike-cost"));
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value",
					"[value]", String.valueOf(this.settings.getDislikeAddCost())));;
				icon = new ItemStack(Material.IRON_INGOT);
				clickHandler = (panel, user, clickType, slot) -> {
					this.getNumberInput(number -> this.settings.setDislikeAddCost(number.doubleValue()),
						this.user.getTranslation(Constants.QUESTIONS + "dislike-cost"));
					return true;
				};
				glow = false;

				break;
			}
			case DISLIKE_REMOVE_COST:
			{
				name = this.user.getTranslation(Constants.BUTTON + "dislike-remove-cost");
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "dislike-remove-cost"));
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "current-value",
					"[value]", String.valueOf(this.settings.getDislikeRemoveCost())));;
				icon = new ItemStack(Material.IRON_NUGGET);
				clickHandler = (panel, user, clickType, slot) -> {
					this.getNumberInput(number -> this.settings.setDislikeRemoveCost(number.doubleValue()),
						this.user.getTranslation(Constants.QUESTIONS + "dislike-remove-cost"));
					return true;
				};
				glow = false;

				break;
			}
			case DEFAULT_ICON:
			{
				name = this.user.getTranslation(Constants.BUTTON + "default-icon");
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "default-icon"));
				icon = new ItemStack(this.settings.getDefaultIcon());
				clickHandler = (panel, user, clickType, slot) -> {
					SelectBlocksGUI.open(user, (value, materials) -> {
						if (value)
						{
							this.settings.setDefaultIcon(materials.iterator().next());
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
				name = this.user.getTranslation(Constants.BUTTON + "inform-players");
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "inform-players"));
				icon = new ItemStack(Material.JUKEBOX);
				clickHandler = (panel, user, clickType, slot) -> {
					this.settings.setInformPlayers(!this.settings.isInformPlayers());
					this.build();
					return true;
				};
				glow = this.settings.isInformPlayers();

				break;
			}
			case LOG_HISTORY:
			{
				name = this.user.getTranslation(Constants.BUTTON + "log-history");
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "log-history"));
				icon = new ItemStack(Material.WRITABLE_BOOK);
				clickHandler = (panel, user, clickType, slot) -> {
					this.settings.setLogHistory(!this.settings.isLogHistory());
					this.build();
					return true;
				};
				glow = this.settings.isLogHistory();

				break;
			}
			case RESET_LIKES:
			{
				name = this.user.getTranslation(Constants.BUTTON + "reset-likes");
				description.add(this.user.getTranslation(Constants.DESCRIPTION + "reset-likes"));
				icon = new ItemStack(Material.LAVA_BUCKET);
				clickHandler = (panel, user, clickType, slot) -> {
					this.settings.setResetLikes(!this.settings.isResetLikes());
					this.build();
					return true;
				};
				glow = this.settings.isResetLikes();

				break;
			}
			case RETURN:
			{
				name = this.user.getTranslation(Constants.BUTTON + "return");
				icon = new ItemStack(Material.OAK_DOOR);
				clickHandler = (panel, user, clickType, slot) -> {
					AdminPanel.openPanel(this.addon, this.user, this.world, this.permissionPrefix);
					return true;
				};
				glow = false;

				break;
			}
			default:
				// this should never happen.
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
// Section: Conversation API implementation
// ---------------------------------------------------------------------


	/**
	 * This method will close opened gui and writes inputText in chat. After players answers on
	 * inputText in chat, message will trigger consumer and gui will reopen.
	 * @param consumer Consumer that accepts player output text.
	 * @param question Message that will be displayed in chat when player triggers conversion.
	 */
	private void getNumberInput(Consumer<Number> consumer, @NonNull String question)
	{
		final User user = this.user;

		Conversation conversation =
			new ConversationFactory(BentoBox.getInstance()).withFirstPrompt(
				new NumericPrompt()
				{
					/**
					 * Override this method to perform some action
					 * with the user's integer response.
					 *
					 * @param context Context information about the
					 * conversation.
					 * @param input The user's response as a {@link
					 * Number}.
					 * @return The next {@link Prompt} in the prompt
					 * graph.
					 */
					@Override
					protected Prompt acceptValidatedInput(ConversationContext context, Number input)
					{
						// Add answer to consumer.
						consumer.accept(input);
						// Reopen GUI
						EditSettingsPanel.this.build();
						// End conversation
						return Prompt.END_OF_CONVERSATION;
					}


					/**
					 * Override this method to do further validation on the numeric player
					 * input after the input has been determined to actually be a number.
					 *
					 * @param context Context information about the conversation.
					 * @param input The number the player provided.
					 * @return The validity of the player's input.
					 */
					protected boolean isNumberValid(ConversationContext context, Number input)
					{
						return input.doubleValue() >= 0 && input.doubleValue() <= Double.MAX_VALUE;
					}


					/**
					 * Optionally override this method to display an additional message if the
					 * user enters an invalid number.
					 *
					 * @param context Context information about the conversation.
					 * @param invalidInput The invalid input provided by the user.
					 * @return A message explaining how to correct the input.
					 */
					@Override
					protected String getInputNotNumericText(ConversationContext context, String invalidInput)
					{
						return EditSettingsPanel.this.user.getTranslation(Constants.ERRORS + "numeric-only", "[value]", invalidInput);
					}


					/**
					 * Optionally override this method to display an additional message if the
					 * user enters an invalid numeric input.
					 *
					 * @param context Context information about the conversation.
					 * @param invalidInput The invalid input provided by the user.
					 * @return A message explaining how to correct the input.
					 */
					@Override
					protected String getFailedValidationText(ConversationContext context, Number invalidInput)
					{
						return EditSettingsPanel.this.user.getTranslation(Constants.ERRORS + "not-valid-value",
							"[value]", invalidInput.toString(),
							"[min]", Double.toString(0),
							"[max]", Double.toString(Double.MAX_VALUE));
					}


					/**
					 * @see Prompt#getPromptText(ConversationContext)
					 */
					@Override
					public String getPromptText(ConversationContext conversationContext)
					{
						// Close input GUI.
						user.closeInventory();

						// There are no editable message. Just return question.
						return question;
					}
				}).
				withLocalEcho(false).
				withPrefix(context -> EditSettingsPanel.this.user.getTranslation(Constants.QUESTIONS + "prefix")).
				buildConversation(user.getPlayer());

		conversation.begin();
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
		DISLIKE_COST,
		DISLIKE_REMOVE_COST,
		DEFAULT_ICON,
		INFORM_PLAYERS,
		LOG_HISTORY,
		RESET_LIKES,
		RETURN,
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * This variable allows to access addon object.
	 */
	private final LikesAddon addon;

	/**
	 * This variable stores settings for current addon.
	 */
	private final Settings settings;

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
