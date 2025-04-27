package network.venox.cobalt.mongo;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;

import network.venox.cobalt.Cobalt;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.javautilities.StringUtility;


public class Question {
    @BsonId public ObjectId id;
    @BsonProperty("question_id") public int questionId;
    public String question;
    @BsonProperty("user") public long userId;
    public int used;

    public Question() {}

    public Question(int questionId, @NotNull String question, long userId, int used) {
        this.questionId = questionId;
        this.question = question;
        this.userId = userId;
        this.used = used;
    }

    @NotNull
    public CacheRestAction<User> getUser(@NotNull JDA jda) {
        return jda.retrieveUserById(userId);
    }

    @NotNull
    public String getFormatted() {
        return "**" + id + " (<@" + userId + ">, " + used + "):** " + question;
    }

    @NotNull
    public String getCleaned(@NotNull Cobalt cobalt) {
        return clean(cobalt, question);
    }

    public void send(int count, @NotNull StandardGuildMessageChannel channel, @Nullable Role role) {
        // Get roleString
        String roleString = "";
        if (role != null) roleString = role.getAsMention();

        // Send message
        channel.sendMessage("**QOTW #" + count + ":** " + question + " " + roleString + "\n*You can also answer in your server's general chat by prefixing " + channel.getAsMention() + " to your message*")
                .flatMap(message -> message.createThreadChannel(StringUtility.shorten(count + ": " + question, 100))
                        .setAutoArchiveDuration(ThreadChannel.AutoArchiveDuration.TIME_24_HOURS))
                .queue();
    }

    @NotNull
    public static String clean(@NotNull Cobalt cobalt, @NotNull String question) {
        String clean = question.toLowerCase().trim().replace("[\\p{P} ]", "");
        for (final String word : cobalt.config.qotwSimilarityIgnored) clean = clean.replace(word, "");
        return clean;
    }
}
