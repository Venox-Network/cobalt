package network.venox.cobalt.mongo;

import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.internal.utils.Helpers;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.javautilities.StringUtility;

import xyz.srnyx.lazylibrary.emoji.LazyEmoji;

import java.awt.*;
import java.util.*;
import java.util.List;


public class Survey {
    public static final int MAX_QUESTIONS = 5;

    @NotNull public static final String PROP_GUILD = "guild";
    @NotNull public static final String PROP_NAME = "name";
    @NotNull public static final String PROP_CREATED = "created";
    @NotNull public static final String PROP_CREATOR = "creator";
    @NotNull public static final String PROP_OPEN = "open";
    @NotNull public static final String PROP_QUESTIONS = "questions";
    @NotNull public static final String PROP_PANEL = "panel";
    @NotNull public static final String PROP_NOTIFICATION_CHANNEL = "notification_channel";
    @NotNull public static final String PROP_RESPONSES = "responses";

    @BsonId public ObjectId id;
    @BsonProperty(PROP_GUILD) public long guild;
    @BsonProperty(PROP_NAME) public String name;
    @BsonProperty(PROP_CREATED) public Date created;
    @BsonProperty(PROP_CREATOR) public long creator;
    @BsonProperty(PROP_OPEN) public boolean open;
    @BsonProperty(PROP_QUESTIONS) public List<Question> questions;
    @BsonProperty(PROP_RESPONSES) public List<Response> responses;
    @BsonProperty(PROP_PANEL) @Nullable public Panel panel;
    @BsonProperty(PROP_NOTIFICATION_CHANNEL) @Nullable public Long notificationChannel;

    public Survey() {}

    public Survey(long guild, long creator, @NotNull String name) {
        this.guild = guild;
        this.name = name;
        this.created = new Date();
        this.creator = creator;
        this.questions = new ArrayList<>();
        this.responses = new ArrayList<>();
    }

    @NotNull
    public Color color() {
        return new Color(id.hashCode());
    }

    @Nullable
    public Question question(@NotNull ObjectId id) {
        for (final Question question : questions) if (question.id.equals(id)) return question;
        return null;
    }

    @NotNull
    public Optional<Response> response(long user) {
        for (final Response response : responses) if (response.user == user) return Optional.of(response);
        return Optional.empty();
    }

    @NotNull
    public Container toContainer() {
        final StringBuilder builder = new StringBuilder();
        builder.append("# ").append(name).append("\n");
        builder.append("**ID:** `").append(id.toHexString()).append("`\n");
        builder.append("**Created:** <t:").append(created.getTime() / 1000).append(":F>\n");
        builder.append("**Creator:** <@").append(creator).append(">\n");
        if (panel != null) {
            builder.append("**Panel:** ");
            if (panel.message != null) {
                builder.append(Helpers.format(Message.JUMP_URL, guild, panel.channel, panel.message));
            } else {
                builder.append("<#").append(panel.channel).append(">");
            }
            builder.append("\n");
        }
        if (notificationChannel != null) builder.append("**Notification channel:** <#").append(notificationChannel).append(">\n");
        builder.append("**Status:** ").append(open ? "Open" : "Closed").append("\n");
        builder.append("## Questions\n");
        for (final Question question : questions) builder.append(question.toDisplayString()).append("\n");
        return Container.of(TextDisplay.of(builder.toString())).withAccentColor(color());
    }

    @NotNull
    public Command.Choice toCommandChoice() {
        return new Command.Choice(name, id.toHexString());
    }

    public static class Question {
        @NotNull public static final String PROP_NAME = "name";
        @NotNull public static final String PROP_DESCRIPTION = "description";
        @NotNull public static final String PROP_PLACEHOLDER = "placeholder";

        @BsonId public ObjectId id;
        @BsonProperty(PROP_NAME) public String name;
        @BsonProperty(PROP_DESCRIPTION) @Nullable public String description;
        @BsonProperty(PROP_PLACEHOLDER) @Nullable public String placeholder;

        public Question() {}

        public Question(@NotNull String name, @Nullable String description, @Nullable String placeholder) {
            this.id = new ObjectId();
            this.name = name;
            this.description = description;
            this.placeholder = placeholder;
        }

        @NotNull
        public String toDisplayString() {
            final StringBuilder builder = new StringBuilder();
            builder.append("### ").append(name).append("\n");
            if (description != null) builder.append("**Description:** ").append(description).append("\n");
            if (placeholder != null) builder.append("**Placeholder:** ").append(placeholder).append("\n");
            return builder.substring(0, builder.length() - 1);
        }

        @NotNull
        public SelectOption toSelectOption() {
            return SelectOption.of(StringUtility.shorten(name, SelectOption.LABEL_MAX_LENGTH), id.toHexString())
                    .withDescription(StringUtility.shortenElseNull(description, SelectOption.DESCRIPTION_MAX_LENGTH))
                    .withEmoji(LazyEmoji.QUESTION_CLEAR);
        }

        @NotNull
        public Label toLabel(@Nullable String value) {
            return Label.of(name, TextInput.create(id.toHexString(), TextInputStyle.PARAGRAPH)
                            .setPlaceholder(StringUtility.shortenElseNull(placeholder, TextInput.MAX_PLACEHOLDER_LENGTH))
                            .setValue(value)
                            .build())
                    .withDescription(description);
        }
    }

    public static class Response {
        @NotNull public static final String PROP_USER = "user";
        @NotNull public static final String PROP_CREATED = "created";
        @NotNull public static final String PROP_EDITED = "edited";
        @NotNull public static final String PROP_ANSWERS = "answers";

        @BsonProperty(PROP_USER) public long user;
        @BsonProperty(PROP_CREATED) public Date created;
        @BsonProperty(PROP_EDITED) @Nullable public Date edited;
        @BsonProperty(PROP_ANSWERS) public List<Answer> answers;

        public Response() {}

        public Response(long user, @NotNull Date created, @Nullable Date edited, @NotNull List<Answer> answers) {
            this.user = user;
            this.created = created;
            this.edited = edited;
            this.answers = answers;
        }

        @NotNull
        public Optional<Answer> answer(@NotNull ObjectId questionId) {
            for (final Answer answer : answers) if (answer.id.equals(questionId)) return Optional.of(answer);
            return Optional.empty();
        }

        @NotNull
        public Container toContainer() {
            final StringBuilder builder = new StringBuilder();
            builder.append("## <@").append(user).append(">\n");
            builder.append("**Created:** <t:").append(created.getTime() / 1000).append(":F>\n");
            if (edited != null) builder.append("**Edited:** <t:").append(edited.getTime() / 1000).append(":F>\n");
            for (final Answer answer : answers) builder.append(answer.toDisplayString()).append("\n");
            return Container.of(TextDisplay.of(builder.substring(0, builder.length() - 1)));
        }

        public static class Answer {
            @NotNull public static final String PROP_QUESTION_NAME = "question_name";
            @NotNull public static final String PROP_ANSWER = "answer";

            /**
             * {@link Question#id Question ID}
             */
            @BsonId public ObjectId id;
            @BsonProperty(PROP_QUESTION_NAME) public String questionName;
            @BsonProperty(PROP_ANSWER) public String answer;

            public Answer() {}

            public Answer(@NotNull Question question, @NotNull String answer) {
                this.id = question.id;
                this.questionName = question.name;
                this.answer = answer;
            }

            @NotNull
            public String toDisplayString() {
                return "### " + questionName + "\n" + answer;
            }
        }
    }

    public static class Panel {
        @NotNull public static final String PROP_CHANNEL = "channel";
        @NotNull public static final String PROP_MESSAGE = "message";

        @BsonProperty(PROP_CHANNEL) public long channel;
        @BsonProperty(PROP_MESSAGE) @Nullable public Long message;

        public Panel() {}

        public Panel(long channel) {
            this.channel = channel;
        }
    }
}
