package network.venox.cobalt.mongo;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Survey {
    public static final int MAX_QUESTIONS = 5;

    @NotNull public static final String PROP_NAME = "name";
    @NotNull public static final String PROP_CREATED = "created";
    @NotNull public static final String PROP_OPEN = "open";
    @NotNull public static final String PROP_QUESTIONS = "questions";
    @NotNull public static final String PROP_NOTIFICATION_CHANNEL = "notification_channel";
    @NotNull public static final String PROP_RESPONSES = "responses";

    @BsonId public ObjectId id;
    @BsonProperty(PROP_NAME) public String name;
    @BsonProperty(PROP_CREATED) public Date created;
    @BsonProperty(PROP_OPEN) public boolean open;
    @BsonProperty(PROP_QUESTIONS) public List<Question> questions;
    @BsonProperty(PROP_NOTIFICATION_CHANNEL) @Nullable public Long notificationChannel;
    @BsonProperty(PROP_RESPONSES) public List<Response> responses;

    public Survey() {}

    public Survey(@NotNull String name) {
        this.name = name;
        this.created = new Date();
        this.questions = new ArrayList<>();
        this.responses = new ArrayList<>();
    }

    @Nullable
    public Question question(@NotNull ObjectId questionId) {
        for (final Question question : questions) if (question.id.equals(questionId)) return question;
        return null;
    }

    @NotNull
    public String string() {
        final StringBuilder builder = new StringBuilder();
        builder.append("# ").append(name).append("\n");
        builder.append("**ID:** `").append(id.toHexString()).append("`\n");
        builder.append("**Created:** <t:").append(created.getTime() / 1000).append(":F>\n");
        if (notificationChannel != null) builder.append("**Notification channel:** <#").append(notificationChannel).append(">\n");
        builder.append("**Status:** ").append(open ? "Open" : "Closed").append("\n");
        builder.append("## Questions\n");
        for (final Question question : questions) builder.append(question.string()).append("\n");
        return builder.toString();
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
        public String string() {
            final StringBuilder builder = new StringBuilder();
            builder.append("### ").append(name).append("\n");
            if (description != null) builder.append("**Description:** ").append(description).append("\n");
            if (placeholder != null) builder.append("**Placeholder:** ").append(placeholder).append("\n");
            return builder.toString();
        }
    }

    public static class Response {
        @NotNull public static final String PROP_USER = "user";
        @NotNull public static final String PROP_TIMESTAMP = "timestamp";
        @NotNull public static final String PROP_ANSWERS = "answers";

        @BsonProperty(PROP_USER) public long user;
        @BsonProperty(PROP_TIMESTAMP) public Date timestamp;
        @BsonProperty(PROP_ANSWERS) public List<Answer> answers;

        public Response() {}

        public Response(long user, @NotNull Date timestamp, @NotNull List<Answer> answers) {
            this.user = user;
            this.timestamp = timestamp;
            this.answers = answers;
        }

        public static class Answer {
            @NotNull public static final String PROP_QUESTION = "question";
            @NotNull public static final String PROP_RESPONSE = "response";

            @BsonProperty(PROP_QUESTION) public ObjectId question;
            @BsonProperty(PROP_RESPONSE) public String response;

            public Answer() {}

            public Answer(@NotNull ObjectId question, @NotNull String response) {
                this.question = question;
                this.response = response;
            }
        }
    }
}
