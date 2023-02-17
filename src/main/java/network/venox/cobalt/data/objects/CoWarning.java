package network.venox.cobalt.data.objects;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

import network.venox.cobalt.data.CoObject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;


public record CoWarning(@NotNull JDA jda, int id, long user, @NotNull String reason, long moderator) implements CoObject {
    @Nullable
    public User getUser() {
        return jda.retrieveUserById(user).complete();
    }

    @Nullable
    public User getModerator() {
        return jda.retrieveUserById(moderator).complete();
    }

    @Override @NotNull
    public Map<String, Object> toMap() {
        final Map<String, Object> map = new HashMap<>();
        map.put("user", user);
        map.put("reason", reason);
        map.put("moderator", moderator);
        return map;
    }
}
