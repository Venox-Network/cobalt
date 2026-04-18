package network.venox.cobalt.mongo;

import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.List;
import java.util.Optional;


public class SurveyResponse {
    @NotNull public static final String PROP_SURVEY = "survey";
    @NotNull public static final String PROP_USER = "user";
    @NotNull public static final String PROP_CREATED = "created";
    @NotNull public static final String PROP_EDITED = "edited";
    @NotNull public static final String PROP_ANSWERS = "answers";

    @BsonId public ObjectId id;
    @BsonProperty(PROP_SURVEY) public ObjectId survey;
    @BsonProperty(PROP_USER) public long user;
    @BsonProperty(PROP_CREATED) public Date created;
    @BsonProperty(PROP_EDITED) @Nullable public Date edited;
    @BsonProperty(PROP_ANSWERS) public List<Answer> answers;

    public SurveyResponse() {}

    public SurveyResponse(@NotNull ObjectId survey, long user, @NotNull Date created, @Nullable Date edited, @NotNull List<Answer> answers) {
        this.survey = survey;
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
         * {@link Survey.Question#id Question ID}
         */
        @BsonId public ObjectId id;
        @BsonProperty(PROP_QUESTION_NAME) public String questionName;
        @BsonProperty(PROP_ANSWER) public String answer;

        public Answer() {}

        public Answer(@NotNull Survey.Question question, @NotNull String answer) {
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
