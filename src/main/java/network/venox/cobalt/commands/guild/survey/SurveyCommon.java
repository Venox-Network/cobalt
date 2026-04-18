package network.venox.cobalt.commands.guild.survey;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import dev.freya02.jda.emojis.unicode.Emojis;

import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler;
import io.github.freya022.botcommands.api.components.Buttons;
import io.github.freya022.botcommands.api.components.SelectMenus;
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener;
import io.github.freya022.botcommands.api.components.builder.select.ephemeral.EphemeralEntitySelectBuilder;
import io.github.freya022.botcommands.api.components.event.ButtonEvent;
import io.github.freya022.botcommands.api.core.annotations.Handler;
import io.github.freya022.botcommands.api.modals.ModalEvent;
import io.github.freya022.botcommands.api.modals.Modals;
import io.github.freya022.botcommands.api.modals.annotations.ModalData;
import io.github.freya022.botcommands.api.modals.annotations.ModalHandler;
import io.github.freya022.botcommands.api.modals.annotations.ModalInput;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.modals.Modal;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import network.venox.cobalt.CoEmoji;
import network.venox.cobalt.CoUtility;
import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.Survey;
import network.venox.cobalt.mongo.SurveyResponse;

import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.javautilities.StringUtility;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.emoji.LazyEmoji;
import xyz.srnyx.lazylibrary.utility.LazyUtilities;

import xyz.srnyx.magicmongo.MagicCollection;
import xyz.srnyx.magicmongo.builders.UpdateBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@Handler
public class SurveyCommon {
    @NotNull public static final String AC_ID = "SurveyCommon.ac.id";
    @NotNull private static final String BUTTON_RESPOND = "SurveyCommon.button.respond";
    @NotNull private static final String MODAL_QUESTION = "SurveyCommon.modal.question";
    @NotNull private static final String FIELD_QUESTION_NAME = "SurveyCommon.modal.question.name";
    @NotNull private static final String FIELD_QUESTION_DESCRIPTION = "SurveyCommon.modal.question.description";
    @NotNull private static final String FIELD_QUESTION_PLACEHOLDER = "SurveyCommon.modal.question.placeholder";
    @NotNull public static final String MODAL_RESPOND = "SurveyRespond.modal.respond";

    @NotNull public static final Permission REQUIRED_PERMISSION = Permission.MANAGE_SERVER;

    @NotNull private final MongoProvider mongo;
    @NotNull private final Buttons buttons;
    @NotNull private final SelectMenus menus;
    @NotNull private final Modals modals;

    public SurveyCommon(@NotNull MongoProvider mongo, @NotNull Buttons buttons, @NotNull SelectMenus menus, @NotNull Modals modals) {
        this.mongo = mongo;
        this.buttons = buttons;
        this.menus = menus;
        this.modals = modals;
    }

    @AutocompleteHandler(AC_ID) @NotNull
    public List<Command.Choice> acId(@NotNull CommandAutoCompleteInteractionEvent event) {
        final Member member = event.getMember();
        if (member == null || !member.hasPermission(REQUIRED_PERMISSION)) return List.of();
        return mongo.database.getMagicCollection(Survey.class)
                .find(Filters.eq(Survey.PROP_GUILD, member.getGuild().getIdLong()))
                .map(Survey::toCommandChoice)
                .into(new ArrayList<>());
    }

    @NotNull
    public MessageCreateData getBuilder(@NotNull Survey survey) {
        final MessageCreateBuilder builder = new MessageCreateBuilder().useComponentsV2();

        // Container
        builder.addComponents(survey.toContainer());

        // Get question menu options
        final List<SelectOption> questionOptions = new ArrayList<>();
        for (final Survey.Question question : survey.questions) questionOptions.add(question.toSelectOption());
        if (survey.questions.size() < Survey.MAX_QUESTIONS) questionOptions.add(SelectOption.of("Add question", "add").withEmoji(CoEmoji.PLUS));

        // Add question menu
        builder.addComponents(ActionRow.of(menus.stringSelectMenu().ephemeral()
                .bindTo(menu -> {
                    final String selected = menu.getSelectedOptions().getFirst().getValue();
                    final Survey.Question existing = !selected.equals("add") ? CoUtility.toObjectId(selected)
                                                                               .map(survey::question)
                                                                               .orElse(null) : null;
                    final boolean editing = existing != null;
                    menu.replyModal(modals.create(editing ? "Edit question" : "Add question")
                            .bindTo(MODAL_QUESTION, survey.id.toHexString(), editing ? existing.id.toHexString() : null)
                            .addComponents(
                                    Label.of("Name", editing ? "Unset this field and Submit to remove question (NOT RECOMMENDED, undefined behavior!)" : null, TextInput.create(FIELD_QUESTION_NAME, TextInputStyle.SHORT)
                                            .setRequired(false)
                                            .setMaxLength(Label.LABEL_MAX_LENGTH)
                                            .setValue(editing ? existing.name : null)
                                            .build()),
                                    Label.of("Description", TextInput.create(FIELD_QUESTION_DESCRIPTION, TextInputStyle.PARAGRAPH)
                                            .setRequired(false)
                                            .setMaxLength(Label.DESCRIPTION_MAX_LENGTH)
                                            .setValue(editing ? existing.description : null)
                                            .build()),
                                    Label.of("Placeholder", TextInput.create(FIELD_QUESTION_PLACEHOLDER, TextInputStyle.SHORT)
                                            .setRequired(false)
                                            .setMaxLength(TextInput.MAX_PLACEHOLDER_LENGTH)
                                            .setValue(editing ? existing.placeholder : null).build())).build()).queue();
                })
                .addOptions(questionOptions)
                .setMaxValues(1)
                .setPlaceholder("Edit and add questions")
                .build()));

        final Bson filter = Filters.eq("_id", survey.id);

        // Add panel channel menu
        final EphemeralEntitySelectBuilder panelChannelMenu = menus.entitySelectMenu(EntitySelectMenu.SelectTarget.CHANNEL).ephemeral()
                .bindTo(menu -> {
                    // Delete old panel (if exist)
                    deletePanel(menu, survey);

                    // Set new panel
                    survey.panel = new Survey.Panel(menu.getMentions().getChannels().getFirst().getIdLong());
                    mongo.database.getMagicCollection(Survey.class).upsertOne(
                            filter,
                            Updates.set(Survey.PROP_PANEL, survey.panel));

                    // Send panel
                    final RestAction<Message> panelMessage = sendPanel(menu, survey);
                    if (panelMessage == null) return;

                    // Edit
                    panelMessage.flatMap(_ -> menu.editMessage(MessageEditData.fromCreateData(getBuilder(survey))).useComponentsV2()).queue();
                });
        if (survey.panel != null) panelChannelMenu.setDefaultValues(EntitySelectMenu.DefaultValue.channel(survey.panel.channel));
        builder.addComponents(ActionRow.of(panelChannelMenu
                .setChannelTypes(ChannelType.TEXT, ChannelType.NEWS)
                .setPlaceholder("Send panel in a channel")
                .build()));

        // Add notification channel menu
        final EphemeralEntitySelectBuilder notificationChannelMenu = menus.entitySelectMenu(EntitySelectMenu.SelectTarget.CHANNEL).ephemeral()
                .bindTo(menu -> {
                    survey.notificationChannel = menu.getMentions().getChannels().getFirst().getIdLong();
                    mongo.database.getMagicCollection(Survey.class).upsertOne(
                            filter,
                            Updates.set(Survey.PROP_NOTIFICATION_CHANNEL, survey.notificationChannel));
                    menu.editMessage(MessageEditData.fromCreateData(getBuilder(survey))).useComponentsV2().queue();
                });
        if (survey.notificationChannel != null) notificationChannelMenu.setDefaultValues(EntitySelectMenu.DefaultValue.channel(survey.notificationChannel));
        builder.addComponents(ActionRow.of(notificationChannelMenu
                .setChannelTypes(ChannelType.TEXT, ChannelType.NEWS)
                .setPlaceholder("Set notification channel")
                .build()));

        final List<Button> actionRow = new ArrayList<>();

        // Open/Close button
        actionRow.add(buttons.of(
                        survey.open ? ButtonStyle.DANGER : ButtonStyle.SUCCESS,
                        survey.open ? "Close" : "Open",
                        survey.open ? LazyEmoji.LOCK_CLEAR_DARK : LazyEmoji.UNLOCK_CLEAR_DARK).ephemeral()
                .bindTo(toggle -> {
                    // Check if there's any questions
                    if (survey.questions.isEmpty()) {
                        toggle.reply(LazyEmoji.NO + " You can't open a survey that doesn't have any questions!").setEphemeral(true).queue();
                        return;
                    }

                    // Toggle status
                    survey.open = !survey.open;
                    mongo.database.getMagicCollection(Survey.class).upsertOne(
                            filter,
                            Updates.set(Survey.PROP_OPEN, survey.open));
                    toggle.editMessage(MessageEditData.fromCreateData(getBuilder(survey))).useComponentsV2().queue();
                })
                .build());

        // Re-send panel button
        if (survey.panel != null) actionRow.add(buttons.secondary("Re-send panel", LazyEmoji.CHAT_CLEAR).ephemeral()
                .bindTo(reSendPanel -> {
                    // Delete old panel (if exist)
                    deletePanel(reSendPanel, survey);

                    // Send panel
                    final RestAction<Message> panelMessage = sendPanel(reSendPanel, survey);
                    if (panelMessage == null) return;

                    panelMessage.flatMap(message -> reSendPanel.reply(LazyEmoji.YES + " Panel re-sent")
                            .setComponents(ActionRow.of(Button.link(message.getJumpUrl(), "Go to panel")))
                            .setEphemeral(true)).queue();
                })
                .build());

        // Unset notification channel button
        if (survey.notificationChannel != null) actionRow.add(buttons.danger("Unset notification channel", LazyEmoji.TRASH_CLEAR_DARK).ephemeral()
                .bindTo(button -> {
                    survey.notificationChannel = null;
                    mongo.database.getMagicCollection(Survey.class).upsertOne(
                            filter,
                            Updates.unset(Survey.PROP_NOTIFICATION_CHANNEL));
                    button.editMessage(MessageEditData.fromCreateData(getBuilder(survey))).useComponentsV2().queue();
                })
                .build());

        builder.addComponents(ActionRow.of(actionRow));
        return builder.build();
    }

    public void deletePanel(@NotNull GenericInteractionCreateEvent event, @NotNull Survey survey) {
        if (survey.panel == null) return;
        final Guild guild = event.getGuild();
        if (guild == null) return;
        final StandardGuildMessageChannel oldChannel = guild.getChannelById(StandardGuildMessageChannel.class, survey.panel.channel);
        if (oldChannel != null && survey.panel.message != null) oldChannel.deleteMessageById(survey.panel.message).queue(null, LazyUtilities.IGNORE_UNKNOWN_MESSAGE);
    }

    @Nullable
    public RestAction<Message> sendPanel(@NotNull IReplyCallback event, @NotNull Survey survey) {
        if (survey.panel == null) {
            event.replyEmbeds(LazyEmbed.unexpectedError("survey.panel is null").build()).setEphemeral(true).queue();
            return null;
        }

        // Get guild
        final Guild guild = event.getGuild();
        if (guild == null) {
            event.replyEmbeds(LazyEmbed.unexpectedError("Guild is null").build()).setEphemeral(true).queue();
            return null;
        }

        // Get channel
        final StandardGuildMessageChannel channel = guild.getChannelById(StandardGuildMessageChannel.class, survey.panel.channel);
        if (channel == null) {
            event.reply(LazyEmoji.NO + " Panel channel not found!").setEphemeral(true).queue();
            return null;
        }

        // Send panel
        return channel.sendMessageComponents(Container.of(
                                TextDisplay.of("## New Survey Available!\nClick the button below to respond to **" + survey.name + "**"),
                                Separator.createInvisible(Separator.Spacing.SMALL),
                                ActionRow.of(buttons.primary("Respond", Emojis.MEMO).persistent().bindTo(BUTTON_RESPOND).build()))
                        .withAccentColor(survey.color()))
                .useComponentsV2()
                .onSuccess(message -> {
                    // Update panel message in database
                    survey.panel.message = message.getIdLong();
                    mongo.database.getMagicCollection(Survey.class).upsertOne(
                            Filters.eq("_id", survey.id),
                            Updates.set(Survey.PROP_PANEL, survey.panel));
                });
    }

    public void respond(@NotNull IModalCallback event, @NotNull Survey survey) {
        // Get question components
        final List<ModalTopLevelComponent> components = new ArrayList<>();
        final Optional<SurveyResponse> existingResponse = mongo.database.getMagicCollection(SurveyResponse.class).findOne(Filters.and(
                Filters.eq(SurveyResponse.PROP_SURVEY, survey.id),
                Filters.eq(SurveyResponse.PROP_USER, event.getUser().getIdLong())));
        for (final Survey.Question question : survey.questions) components.add(question.toLabel(existingResponse
                .flatMap(response -> response.answer(question.id))
                .map(answer -> answer.answer)
                .orElse(null)));

        // Reply with response modal
        event.replyModal(modals.create(StringUtility.shorten(survey.name, Modal.MAX_TITLE_LENGTH))
                .bindTo(SurveyCommon.MODAL_RESPOND, survey.id.toHexString())
                .addComponents(components)
                .build()).queue();
    }

    @JDAButtonListener(BUTTON_RESPOND)
    public void buttonRespond(@NotNull ButtonEvent event) {
        // Get Guild
        final Guild guild = event.getGuild();
        if (guild == null) {
            event.replyEmbeds(LazyEmbed.unexpectedError("Guild is null").build()).setEphemeral(true).queue();
            return;
        }

        // Get Survey
        final Survey survey = mongo.database.getMagicCollection(Survey.class)
                .findOne(Filters.and(
                        Filters.eq(Survey.PROP_GUILD, event.getGuild().getIdLong()),
                        Filters.eq(Survey.PROP_PANEL + "." + Survey.Panel.PROP_MESSAGE, event.getMessageIdLong()),
                        Filters.eq(Survey.PROP_OPEN, true)))
                .orElse(null);
        if (survey == null) {
            event.reply(LazyEmoji.NO + " Survey not found or is no longer open!").setEphemeral(true).queue();
            return;
        }

        // Reply with modal
        respond(event, survey);
    }

    @ModalHandler(MODAL_QUESTION)
    public void modalQuestion(@NotNull ModalEvent event,
                              @ModalInput(FIELD_QUESTION_NAME) @Nullable String name,
                              @ModalInput(FIELD_QUESTION_DESCRIPTION) @Nullable String description,
                              @ModalInput(FIELD_QUESTION_PLACEHOLDER) @Nullable String placeholder,
                              @ModalData @NotNull String surveyId,
                              @ModalData @Nullable String questionId) {
        // Get Survey
        final Bson filter = Filters.eq("_id", new ObjectId(surveyId));
        final Survey survey = mongo.database.getMagicCollection(Survey.class)
                .findOne(filter)
                .orElse(null);
        if (survey == null) {
            event.reply(LazyEmoji.NO + " Survey not found!").setEphemeral(true).queue();
            return;
        }

        // Handle blanks
        final boolean nameBlank = StringUtility.isBlank(name);
        if (StringUtility.isBlank(description)) description = null;
        if (StringUtility.isBlank(placeholder)) placeholder = null;

        if (questionId != null) {
            // Get existing question
            final Survey.Question question = survey.question(new ObjectId(questionId));
            if (question == null) {
                event.reply(LazyEmoji.NO + " Question not found!").setEphemeral(true).queue();
                return;
            }

            if (nameBlank) {
                // Removing question
                survey.questions.remove(question);
            } else {
                // Editing question
                question.name = name;
                question.description = description;
                question.placeholder = placeholder;
            }
        } else {
            // Can't have a blank new question
            if (nameBlank) {
                event.reply(LazyEmoji.NO + " Question name cannot be blank!").setEphemeral(true).queue();
                return;
            }

            // Add new question
            if (survey.questions.size() >= Survey.MAX_QUESTIONS) {
                event.reply(LazyEmoji.NO + " Maximum number of questions reached!").setEphemeral(true).queue();
                return;
            }
            survey.questions.add(new Survey.Question(name, description, placeholder));
        }

        // Update in database
        mongo.database.getMagicCollection(Survey.class).upsertOne(
                filter,
                Updates.set(Survey.PROP_QUESTIONS, survey.questions));

        // Update message
        event.editMessage(MessageEditData.fromCreateData(getBuilder(survey))).useComponentsV2().queue();
    }

    @ModalHandler(MODAL_RESPOND)
    public void modalRespond(@NotNull ModalEvent event,
                             @ModalData @NotNull String id) {
        // Get guild
        final Guild guild = event.getGuild();
        if (guild == null) {
            event.replyEmbeds(LazyEmbed.unexpectedError("Guild is null").build()).setEphemeral(true).queue();
            return;
        }

        // Get Survey
        final Survey survey = mongo.database.getMagicCollection(Survey.class)
                .findOne(Filters.and(
                        Filters.eq(Survey.PROP_GUILD, event.getGuild().getIdLong()),
                        Filters.eq("_id", new ObjectId(id)),
                        Filters.eq(Survey.PROP_OPEN, true)))
                .orElse(null);
        if (survey == null) {
            event.reply("Survey with ID `" + id + "` not found or is no longer open!").setEphemeral(true).queue();
            return;
        }

        final MagicCollection<SurveyResponse> responseCollection = mongo.database.getMagicCollection(SurveyResponse.class);

        // Determine if editing or adding
        final long userId = event.getUser().getIdLong();
        final Bson responseFilter = Filters.and(
                Filters.eq(SurveyResponse.PROP_SURVEY, survey.id),
                Filters.eq(SurveyResponse.PROP_USER, userId));
        final Optional<SurveyResponse> existing = responseCollection.findOne(responseFilter);
        final boolean editing = existing.isPresent();

        // Build Answers
        final List<SurveyResponse.Answer> answers = new ArrayList<>();
        for (final Survey.Question question : survey.questions) {
            final ModalMapping value = event.getValue(question.id.toHexString());
            if (value != null) answers.add(new SurveyResponse.Answer(question, value.getAsString()));
        }

        // Build update
        final UpdateBuilder update = new UpdateBuilder();
        final Date now = new Date();
        update.add(Updates.set(SurveyResponse.PROP_ANSWERS, answers));
        update.add(Updates.setOnInsert(SurveyResponse.PROP_CREATED, now));
        if (editing) update.add(Updates.set(SurveyResponse.PROP_EDITED, now));

        // Upsert
        final SurveyResponse response = responseCollection.findOneAndUpsert(
                responseFilter,
                update.build());

        // Reply
        event.reply(LazyEmoji.YES + " Your response for **" + survey.name + "** has been " + (editing ? "updated" : "recorded")).setEphemeral(true).queue();

        // Send notification
        if (survey.notificationChannel == null) return;
        final StandardGuildMessageChannel channel = guild.getChannelById(StandardGuildMessageChannel.class, survey.notificationChannel);
        if (channel != null) channel.sendMessageComponents(
                        TextDisplay.of("**" + (editing ? LazyEmoji.MAYBE + " Updated" : LazyEmoji.YES + " New") + "** response for **" + survey.name + "**"),
                        response.toContainer().withAccentColor(survey.color()))
                .useComponentsV2().queue();
    }
}
