package network.venox.cobalt.data.objects;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;

import network.venox.cobalt.data.CoObject;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;


public record CoWarning(@NotNull JDA jda, int id, long user, @NotNull String reason, long moderator) implements CoObject {
    @Override @NotNull @Contract(" -> new")
    public Map<String, Object> toMap() {
        return Map.of(
                "user", user,
                "reason", reason,
                "moderator", moderator);
    }

    @NotNull
    public CacheRestAction<User> getUser() {
        return jda.retrieveUserById(user);
    }

    @NotNull
    public CacheRestAction<User> getModerator() {
        return jda.retrieveUserById(moderator);
    }
}
