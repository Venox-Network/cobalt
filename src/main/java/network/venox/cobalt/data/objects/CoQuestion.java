package network.venox.cobalt.data.objects;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;

import network.venox.cobalt.data.CoObject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
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

    @Override @NotNull
    public Map<String, Object> toMap() {
        final Map<String, Object> map = new HashMap<>();
        map.put("question", question);
        map.put("user", user);
        map.put("used", used);
        return map;
    }

    @Nullable
    public User getUser() {
        return jda.retrieveUserById(user).complete();
    }

    public void send(int count, @NotNull StandardGuildMessageChannel channel, @Nullable Role role) {
        // Get role
        String roleString = "";
        if (role != null) roleString = role.getAsMention() + " ";

        // Send message
        channel.sendMessage(roleString + "**QOTD #" + count + ":** " + question + "\n*You can also answer in your server's general chat by prefixing " + channel.getAsMention() + " to your message*")
                .flatMap(message -> message.createThreadChannel(count + ": " + question)
                        .setAutoArchiveDuration(ThreadChannel.AutoArchiveDuration.TIME_24_HOURS))
                .queue();
    }
}
