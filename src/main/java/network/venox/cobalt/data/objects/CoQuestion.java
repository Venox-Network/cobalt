package network.venox.cobalt.data.objects;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;

import network.venox.cobalt.data.CoObject;
import network.venox.cobalt.utility.CoUtilities;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;


public class CoQuestion implements CoObject {
    @NotNull private final JDA jda;

    public final int id;
    @NotNull public final String question;
    public final long user;
    public int used;

    public CoQuestion(@NotNull JDA jda, int id, @NotNull String question, long user, int used) {
        this.jda = jda;
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

    @NotNull
    public CacheRestAction<User> getUser() {
        return jda.retrieveUserById(user);
    }

    public void send(int count, @NotNull StandardGuildMessageChannel channel, @Nullable Role role) {
        // Get roleString
        String roleString = "";
        if (role != null) roleString = role.getAsMention();

        // Send message
        channel.sendMessage("**QOTD #" + count + ":** " + question + " " + roleString + "\n*You can also answer in your server's general chat by prefixing " + channel.getAsMention() + " to your message*")
                .flatMap(message -> message.createThreadChannel(CoUtilities.shorten(count + ": " + question, 100))
                        .setAutoArchiveDuration(ThreadChannel.AutoArchiveDuration.TIME_24_HOURS))
                .queue();
    }
}
