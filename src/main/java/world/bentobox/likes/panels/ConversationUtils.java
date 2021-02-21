//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.likes.panels;


import org.apache.commons.lang.ArrayUtils;
import org.bukkit.ChatColor;
import org.bukkit.conversations.*;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.likes.utils.Constants;


public class ConversationUtils
{
    // ---------------------------------------------------------------------
// Section: Conversation API implementation
// ---------------------------------------------------------------------


    /**
     * This method will close opened gui and writes question in chat. After players answers on question in chat, message
     * will trigger consumer and gui will reopen. Success and fail messages can be implemented like that, as user's chat
     * options are disabled while it is in conversation.
     *
     * @param consumer Consumer that accepts player output text.
     * @param question Message that will be displayed in chat when player triggers conversion.
     * @param successMessage Message that will be displayed on successful operation.
     * @param user User who is targeted with current confirmation.
     */
    public static void createConfirmation(Consumer<Boolean> consumer,
        User user,
        @NonNull String question,
        @Nullable String successMessage)
    {
        ValidatingPrompt confirmationPrompt = new ValidatingPrompt()
        {
            /**
             * Is input valid boolean.
             *
             * @param context the context
             * @param input the input
             * @return the boolean
             */
            @Override
            protected boolean isInputValid(@NonNull ConversationContext context, @NonNull String input)
            {
                // Get valid strings from translations
                String validEntry = user.getTranslation(Constants.CONVERSATIONS + "confirm-string") +
                    "," + user.getTranslation(Constants.CONVERSATIONS + "deny-string") +
                    "," + user.getTranslation(Constants.CONVERSATIONS + "exit-string") +
                    "," + user.getTranslation(Constants.CONVERSATIONS + "cancel-string");

                // Split and check if they exist in valid entries.
                String[] accepted = validEntry.toLowerCase().replaceAll("\\s", "").split(",");
                return ArrayUtils.contains(accepted, input.toLowerCase());
            }


            /**
             * Accept validated input prompt.
             *
             * @param context the context
             * @param input the input
             * @return the prompt
             */
            @Nullable
            protected Prompt acceptValidatedInput(@NonNull ConversationContext context, @NonNull String input)
            {
                String validEntry = user.getTranslation(Constants.CONVERSATIONS + "confirm-string").toLowerCase();

                if (ArrayUtils.contains(validEntry.replaceAll("\\s", "").split(","), input.toLowerCase()))
                {
                    // Add answer to consumer.
                    consumer.accept(true);
                    // Return message about success.
                    return ConversationUtils.endMessagePrompt(successMessage);
                }
                else
                {
                    // Add answer to consumer.
                    consumer.accept(false);

                    // Return message about failed operation.
                    return ConversationUtils.endMessagePrompt(
                        user.getTranslation(Constants.CONVERSATIONS + "cancelled"));
                }
            }


            /**
             * @see Prompt#getPromptText(ConversationContext)
             */
            @Override
            public @NonNull String getPromptText(@NonNull ConversationContext conversationContext)
            {
                // Close input GUI.
                user.closeInventory();
                // There are no editable message. Just return question.
                return question;
            }
        };

        new ConversationFactory(BentoBox.getInstance()).
            withPrefix(context -> user.getTranslation(Constants.CONVERSATIONS + "prefix")).
            withFirstPrompt(confirmationPrompt).
            withLocalEcho(false).
            withTimeout(90).
            buildConversation(user.getPlayer()).
            begin();
    }


    /**
     * This method will close opened gui and writes inputText in chat. After players answers on inputText in chat,
     * message will trigger consumer and gui will reopen.
     *
     * @param consumer Consumer that accepts player output text.
     * @param question Message that will be displayed in chat when player triggers conversion.
     */
    public static void createNumericInput(Consumer<Number> consumer,
        @NonNull User user,
        @NonNull String question,
        Number minValue,
        Number maxValue)
    {
        // Create NumericPromt instance that will validate and process input.
        NumericPrompt numberPrompt = new NumericPrompt()
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
                return input.doubleValue() >= minValue.doubleValue() &&
                    input.doubleValue() <= maxValue.doubleValue();
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
                return user.getTranslation(Constants.CONVERSATIONS + "numeric-only",
                    Constants.PARAMETER_VALUE, invalidInput);
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
                return user.getTranslation(Constants.CONVERSATIONS + "not-valid-value",
                    Constants.PARAMETER_VALUE, invalidInput.toString(),
                    Constants.PARAMETER_MIN, Double.toString(minValue.doubleValue()),
                    Constants.PARAMETER_MAX, Double.toString(maxValue.doubleValue()));
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
        };

        // Init conversation api.
        new ConversationFactory(BentoBox.getInstance()).
            withPrefix(context -> user.getTranslation(Constants.CONVERSATIONS + "prefix")).
            withFirstPrompt(numberPrompt).
            withLocalEcho(false).
            withTimeout(90).
            withEscapeSequence(user.getTranslation(Constants.CONVERSATIONS + "cancel-string")).
            // Use null value in consumer to detect if user has abandoned conversation.
                addConversationAbandonedListener(ConversationUtils.getAbandonListener(consumer, user)).
            buildConversation(user.getPlayer()).
            begin();
    }


    /**
     * This method will close opened gui and writes question in chat. After players answers on question in chat, message
     * will trigger consumer and gui will reopen. Be aware, consumer does not return (and validate) sanitized value,
     * while sanitization is done in failure for better informing. Proper implementation would be with adding new
     * consumer for failure message.
     *
     * @param consumer Consumer that accepts player output text.
     * @param question Message that will be displayed in chat when player triggers conversion.
     * @param user User who is targeted with current confirmation.
     */
    public static void createStringListInput(Consumer<List<String>> consumer,
        User user,
        @NonNull String question,
        @NonNull String successMessage)
    {
        final String SESSION_CONSTANT = Constants.CONVERSATIONS + user.getUniqueId();

        // Successful message about completing.
        MessagePrompt messagePrompt = new MessagePrompt()
        {
            @Override
            public @NonNull String getPromptText(@NonNull ConversationContext context)
            {
                List<String> description = (List<String>) context.getSessionData(SESSION_CONSTANT);

                if (description != null)
                {
                    consumer.accept(description);
                    return successMessage;
                }
                else
                {
                    return user.getTranslation(Constants.CONVERSATIONS + "cancelled");
                }
            }


            @Override
            protected @Nullable Prompt getNextPrompt(@NonNull ConversationContext context)
            {
                return Prompt.END_OF_CONVERSATION;
            }
        };

        // Text input message.
        StringPrompt stringPrompt = new StringPrompt()
        {
            @Override
            public @NonNull String getPromptText(@NonNull ConversationContext context)
            {
                user.closeInventory();

                if (context.getSessionData(SESSION_CONSTANT) != null)
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append(user.getTranslation(Constants.CONVERSATIONS + "new-description"));
                    sb.append(System.getProperty("line.separator"));

                    for (String line : ((List<String>) context.getSessionData(SESSION_CONSTANT)))
                    {
                        sb.append(line);
                        sb.append(System.getProperty("line.separator"));
                    }

                    return sb.toString();
                }

                return question;
            }


            @Override
            public @Nullable Prompt acceptInput(@NonNull ConversationContext context, @Nullable String input)
            {
                String[] exit = user.getTranslation(Constants.CONVERSATIONS + "exit-string").
                    toLowerCase().replaceAll("\\s", "").
                    split(",");

                if (ArrayUtils.contains(exit, input.toLowerCase()))
                {
                    return messagePrompt;
                }

                List<String> desc = new ArrayList<>();

                if (context.getSessionData(SESSION_CONSTANT) != null)
                {
                    desc = ((List<String>) context.getSessionData(SESSION_CONSTANT));
                }

                desc.add(ChatColor.translateAlternateColorCodes('&', input));
                context.setSessionData(SESSION_CONSTANT, desc);
                return this;
            }
        };

        new ConversationFactory(BentoBox.getInstance()).
            withPrefix(context -> user.getTranslation(Constants.CONVERSATIONS + "prefix")).
            withFirstPrompt(stringPrompt).
            withModality(true).
            withLocalEcho(false).
            withTimeout(90).
            withEscapeSequence(user.getTranslation(Constants.CONVERSATIONS + "cancel-string")).
            addConversationAbandonedListener(ConversationUtils.getAbandonListener(consumer, user)).
            buildConversation(user.getPlayer()).
            begin();
    }


    /**
     * This method will close opened gui and writes question in chat. After players answers on question in chat, message
     * will trigger consumer and gui will reopen.
     *
     * @param consumer Consumer that accepts player output text.
     * @param question Message that will be displayed in chat when player triggers conversion.
     * @param user User who is targeted with current confirmation.
     */
    public static void createStringInput(Consumer<String> consumer,
        User user,
        @NonNull String question,
        @Nullable String successMessage)
    {
        // Text input message.
        StringPrompt stringPrompt = new StringPrompt()
        {
            @Override
            public @NonNull String getPromptText(@NonNull ConversationContext context)
            {
                user.closeInventory();
                return question;
            }


            @Override
            public @NonNull Prompt acceptInput(@NonNull ConversationContext context, @Nullable String input)
            {
                consumer.accept(input);
                return ConversationUtils.endMessagePrompt(successMessage);
            }
        };

        new ConversationFactory(BentoBox.getInstance()).
            withPrefix(context -> user.getTranslation(Constants.CONVERSATIONS + "prefix")).
            withFirstPrompt(stringPrompt).
            // On cancel conversation will be closed.
                withLocalEcho(false).
            withTimeout(90).
            withEscapeSequence(user.getTranslation(Constants.CONVERSATIONS + "cancel-string")).
            // Use null value in consumer to detect if user has abandoned conversation.
                addConversationAbandonedListener(ConversationUtils.getAbandonListener(consumer, user)).
            buildConversation(user.getPlayer()).
            begin();
    }


    /**
     * This is just a simple end message prompt that displays requested message.
     *
     * @param message Message that will be displayed.
     * @return MessagePrompt that displays given message and exists from conversation.
     */
    private static MessagePrompt endMessagePrompt(@Nullable String message)
    {
        return new MessagePrompt()
        {
            @Override
            public @NonNull String getPromptText(@NonNull ConversationContext context)
            {
                return message == null ? "" : message;
            }


            @Override
            protected @Nullable Prompt getNextPrompt(@NonNull ConversationContext context)
            {
                return Prompt.END_OF_CONVERSATION;
            }
        };
    }


    /**
     * This method creates and returns abandon listener for every conversation.
     *
     * @param consumer Consumer which must return null value.
     * @param user User who was using conversation.
     * @return ConversationAbandonedListener instance.
     */
    private static ConversationAbandonedListener getAbandonListener(Consumer<?> consumer, User user)
    {
        return abandonedEvent ->
        {
            if (!abandonedEvent.gracefulExit())
            {
                consumer.accept(null);
                // send cancell message
                abandonedEvent.getContext().getForWhom().sendRawMessage(
                    user.getTranslation(Constants.CONVERSATIONS + "prefix") +
                        user.getTranslation(Constants.CONVERSATIONS + "cancelled"));
            }
        };
    }
}
