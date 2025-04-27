package network.venox.cobalt.data.objects;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;

import network.venox.cobalt.Cobalt;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.javautilities.StringUtility;

import java.util.Map;


public class CoQuestion extends CoObject {
    @NotNull private final Cobalt cobalt;

    public final int id;
    @NotNull public final String question;
    public final long user;
    public int used;

    public CoQuestion(@NotNull Cobalt cobalt, int id, @NotNull String question, long user, int used) {
        this.cobalt = cobalt;
        this.id = id;
        this.question = question;
        this.user = user;
        this.used = used;
    }

    @Override @NotNull @Contract(" -> new")
    public Map<String, Object> toMap() {
        return Map.of(
                "question", question,
                "user", user,
                "used", used);
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @NotNull
    public CacheRestAction<User> getUser() {
        return cobalt.jda.retrieveUserById(user);
    }

    @NotNull
    public String getFormatted() {
        return "**" + id + " (<@" + user + ">, " + used + "):** " + question;
    }

    @NotNull
    public String getCleaned() {
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
