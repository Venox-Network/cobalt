package network.venox.cobalt.data.objects;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

import network.venox.cobalt.data.CoObject;
import network.venox.cobalt.utility.CoMapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;


public record CoWarning(int id, long user, @NotNull String reason, long moderator) implements CoObject {
    @Override @NotNull
    public Map<String, Object> toMap() {
        final Map<String, Object> map = new HashMap<>();
        map.put("user", user);
        map.put("reason", reason);
        map.put("moderator", moderator);
        return map;
    }

    @Nullable
    public User getUser(@NotNull JDA jda) {
        return CoMapper.handleException(() -> jda.retrieveUserById(user).complete());
    }

    @Nullable
    public User getModerator(@NotNull JDA jda) {
        return CoMapper.handleException(() -> jda.retrieveUserById(moderator).complete());
    }
}
