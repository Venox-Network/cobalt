package network.venox.cobalt.mongo;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import org.jetbrains.annotations.NotNull;


public class Warning {
    @BsonId public ObjectId id;
    @BsonProperty("warning_id") public long warningId;
    @BsonProperty("user") public long userId;
    public String reason;
    @BsonProperty("moderator") public long moderatorId;

    public Warning() {}

    public Warning(long warningId, long userId, String reason, long moderatorId) {
        this.warningId = warningId;
        this.userId = userId;
        this.reason = reason;
        this.moderatorId = moderatorId;
    }

    @NotNull
    public CacheRestAction<User> getUser(@NotNull JDA jda) {
        return jda.retrieveUserById(userId);
    }

    @NotNull
    public CacheRestAction<User> getModerator(@NotNull JDA jda) {
        return jda.retrieveUserById(moderatorId);
    }
}
