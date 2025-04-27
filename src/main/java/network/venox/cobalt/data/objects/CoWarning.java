package network.venox.cobalt.data.objects;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import xyz.srnyx.javautilities.MiscUtility;

import java.util.Map;
import java.util.Optional;


public final class CoWarning extends CoObject {
    @NotNull private final JDA jda;

    public final int id;
    public final long user;
    @NotNull public final String reason;
    public final long moderator;

    public CoWarning(@NotNull JDA jda, int id, long user, @NotNull String reason, long moderator) {
        this.jda = jda;
        this.id = id;
        this.user = user;
        this.reason = reason;
        this.moderator = moderator;
    }

    @Override @NotNull @Contract(" -> new")
    public Map<String, Object> toMap() {
        return Map.of(
                "user", user,
                "reason", reason,
                "moderator", moderator);
    }

    @Override
    public boolean isNull() {
        return getUser().isEmpty() || getModerator().isEmpty();
    }

    @NotNull
    public Optional<CacheRestAction<User>> getUser() {
        return MiscUtility.handleException(() -> jda.retrieveUserById(user));
    }

    @NotNull
    public Optional<CacheRestAction<User>> getModerator() {
        return MiscUtility.handleException(() -> jda.retrieveUserById(moderator));
    }
}
