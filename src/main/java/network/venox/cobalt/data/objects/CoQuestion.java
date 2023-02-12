package network.venox.cobalt.data.objects;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;

import network.venox.cobalt.data.CoObject;
import network.venox.cobalt.utility.CoMapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;


public class CoQuestion implements CoObject {
    public final int id;
    @NotNull public final String question;
    public final long user;
    public int used;

    public CoQuestion(int id, @NotNull String question, long user, int used) {
        this.id = id;
        this.question = question;
        this.user = user;
        this.used = used;
    }

    @NotNull
    @Override
    public Map<String, Object> toMap() {
        final Map<String, Object> map = new HashMap<>();
        map.put("question", question);
        map.put("user", user);
        map.put("used", used);
        return map;
    }

    @Nullable
    public User getUser(@NotNull JDA jda) {
        return CoMapper.handleException(() -> jda.retrieveUserById(user).complete());
    }

    public void send(int count, @NotNull TextChannel channel, @Nullable Role role) {
        // Get role
        String roleString = "";
        if (role != null) roleString = role.getAsMention();

        // Send message
        channel.sendMessage("**QOTD #" + count + ":** " + roleString + "\n" + question).complete()
                .createThreadChannel(count + ": " + question)
                .setAutoArchiveDuration(ThreadChannel.AutoArchiveDuration.TIME_24_HOURS).queue();
    }
}
