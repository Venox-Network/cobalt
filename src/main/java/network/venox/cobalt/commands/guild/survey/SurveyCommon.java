package network.venox.cobalt.commands.guild.survey;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import dev.freya02.jda.emojis.unicode.Emojis;

import io.github.freya022.botcommands.api.components.Buttons;
import io.github.freya022.botcommands.api.components.SelectMenus;
import io.github.freya022.botcommands.api.components.builder.select.ephemeral.EphemeralEntitySelectBuilder;
import io.github.freya022.botcommands.api.core.annotations.Handler;
import io.github.freya022.botcommands.api.modals.ModalEvent;
import io.github.freya022.botcommands.api.modals.Modals;
import io.github.freya022.botcommands.api.modals.annotations.ModalData;
import io.github.freya022.botcommands.api.modals.annotations.ModalHandler;
import io.github.freya022.botcommands.api.modals.annotations.ModalInput;

import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.Survey;

import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.LazyEmoji;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


@Handler
public class SurveyCommon {
    @NotNull private static final String MODAL_QUESTION = "SurveyCommon.modal.question";
    @NotNull private static final String FIELD_QUESTION_NAME = "SurveyCommon.modal.question.name";
    @NotNull private static final String FIELD_QUESTION_DESCRIPTION = "SurveyCommon.modal.question.description";
    @NotNull private static final String FIELD_QUESTION_PLACEHOLDER = "SurveyCommon.modal.question.placeholder";

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

    @NotNull
    public MessageCreateData getBuilder(@NotNull Survey survey) {
        final MessageCreateBuilder builder = new MessageCreateBuilder().useComponentsV2();

        // Container
        builder.addComponents(Container.of(TextDisplay.of(survey.string())));

        // Get question menu options
        final List<SelectOption> questionOptions = new ArrayList<>();
        for (final Survey.Question question : survey.questions) questionOptions.add(SelectOption.of(question.name, question.id.toHexString()));
        if (survey.questions.size() < Survey.MAX_QUESTIONS) questionOptions.add(SelectOption.of("Add question", "add").withEmoji(Emojis.HEAVY_PLUS_SIGN));

        // Add question menu
        builder.addComponents(ActionRow.of(menus.stringSelectMenu().ephemeral()
                .bindTo(menu -> {
                    final String selected = menu.getSelectedOptions().getFirst().getValue();
                    final Survey.Question existing = selected.equals("add") ? null : survey.question(new ObjectId(selected));
                    menu.replyModal(modals.create(existing != null ? "Edit question" : "Add question")
                            .bindTo(MODAL_QUESTION, survey.id.toHexString(), existing != null ? existing.id.toHexString() : null)
                            .addComponents(
                                    Label.of("Name", TextInput.create(FIELD_QUESTION_NAME, TextInputStyle.SHORT)
                                            .setRequired(true)
                                            .setMaxLength(Label.LABEL_MAX_LENGTH)
                                            .setValue(existing != null ? existing.name : null)
                                            .build()),
                                    Label.of("Description", TextInput.create(FIELD_QUESTION_DESCRIPTION, TextInputStyle.PARAGRAPH)
                                            .setRequired(false)
                                            .setMaxLength(Label.DESCRIPTION_MAX_LENGTH)
                                            .setValue(existing != null ? existing.description : null)
                                            .build()),
                                    Label.of("Placeholder", TextInput.create(FIELD_QUESTION_PLACEHOLDER, TextInputStyle.SHORT)
                                            .setRequired(false)
                                            .setMaxLength(TextInput.MAX_PLACEHOLDER_LENGTH)
                                            .setValue(existing != null ? existing.placeholder : null).build())).build()).queue();
                })
                .addOptions(questionOptions)
                .setMaxValues(1)
                .setPlaceholder("Edit and add questions")
                .build()));

        // Add notification channel menu
        final EphemeralEntitySelectBuilder notificationChannelMenu = menus.entitySelectMenu(EntitySelectMenu.SelectTarget.CHANNEL).ephemeral()
                .bindTo(menu -> {
                    survey.notificationChannel = menu.getMentions().getChannels().getFirst().getIdLong();
                    mongo.database.getMagicCollection(Survey.class).upsertOne(
                            Filters.eq("_id", survey.id),
                            Updates.set(Survey.PROP_NOTIFICATION_CHANNEL, survey.notificationChannel));
                    menu.editMessage(MessageEditData.fromCreateData(getBuilder(survey))).useComponentsV2().queue();
                });
        if (survey.notificationChannel != null) notificationChannelMenu.setDefaultValues(EntitySelectMenu.DefaultValue.channel(survey.notificationChannel));
        builder.addComponents(ActionRow.of(notificationChannelMenu
                .setPlaceholder("Set the notification channel")
                .build()));

        // Add buttons
        builder.addComponents(ActionRow.of(buttons.primary(survey.open ? "Close" : "Open", survey.open ? LazyEmoji.LOCK_CLEAR_DARK.emoji : LazyEmoji.UNLOCK_CLEAR_DARK.emoji).ephemeral()
                .bindTo(toggle -> {
                    survey.open = !survey.open;
                    mongo.database.getMagicCollection(Survey.class).upsertOne(
                            Filters.eq("_id", survey.id),
                            Updates.set("open", survey.open));
                    toggle.editMessage(MessageEditData.fromCreateData(getBuilder(survey))).useComponentsV2().queue();
                })
                .build()));

        return builder.build();
    }

    @ModalHandler(MODAL_QUESTION)
    public void handleQuestionModal(@NotNull ModalEvent event,
                                    @ModalInput(FIELD_QUESTION_NAME) @NotNull String name,
                                    @ModalInput(FIELD_QUESTION_DESCRIPTION) @Nullable String description,
                                    @ModalInput(FIELD_QUESTION_PLACEHOLDER) @Nullable String placeholder,
                                    @ModalData @NotNull String surveyId,
                                    @ModalData @Nullable String questionId) {
        final Bson filter = Filters.eq("_id", new ObjectId(surveyId));
        final Survey survey = mongo.database.getMagicCollection(Survey.class)
                .findOne(filter)
                .orElse(null);
        if (survey == null) {
            event.reply(LazyEmoji.NO + " Survey not found!").setEphemeral(true).queue();
            return;
        }

        if (questionId != null) {
            // Edit existing question
            final Survey.Question question = survey.question(new ObjectId(questionId));
            if (question == null) {
                event.reply(LazyEmoji.NO + " Question not found!").setEphemeral(true).queue();
                return;
            }
            question.name = name;
            question.description = description;
            question.placeholder = placeholder;
        } else {
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
}
