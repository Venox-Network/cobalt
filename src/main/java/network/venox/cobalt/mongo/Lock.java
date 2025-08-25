package network.venox.cobalt.mongo;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.requests.RestAction;

import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.commands.guild.locking.LockingCommon;
import network.venox.cobalt.commands.guild.locking.LockingUnlock;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.utility.LazyUtilities;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;


public class Lock {
    @NotNull public static final String PROP_ALLOWED_ROLES = "allowed_roles";
    @NotNull public static final String PROP_PREVIOUS_PERMISSIONS = "previous_permissions";
    @NotNull public static final String PROP_STICKY_MESSAGE_CONTENT = "sticky_message_content";
    @NotNull public static final String PROP_STICKY_MESSAGE = "sticky_message";

    /**
     * [channel ID, future]
     */
    @NotNull public static final Map<Long, ScheduledFuture<?>> LOCK_FUTURES = new HashMap<>();

    @BsonId public long channel;
    @BsonProperty(PROP_ALLOWED_ROLES) public Set<Long> allowedRoles;
    /**
     * [role_id, [allow, deny]]
     */
    @BsonProperty(PROP_PREVIOUS_PERMISSIONS) public Map<String, PreviousPermissions> previousPermissions;
    @BsonProperty(PROP_STICKY_MESSAGE_CONTENT) @Nullable public String stickyMessageContent;
    @BsonProperty(PROP_STICKY_MESSAGE) @Nullable public Long stickyMessage;

    @NotNull
    public RestAction<Message> replaceStickyMessage(@NotNull MongoProvider mongo, @NotNull GuildMessageChannel textChannel) {
        deleteStickyMessage(textChannel).ifPresent(action -> action.queue(null, LazyUtilities.IGNORE_UNKNOWN_MESSAGE));
        return sendStickyMessage(mongo, textChannel);
    }

    @NotNull
    public RestAction<Message> sendStickyMessage(@NotNull MongoProvider mongo, @NotNull GuildMessageChannel textChannel) {
        return textChannel.sendMessage(LockingCommon.getMessage(allowedRoles, stickyMessageContent))
                .setAllowedMentions(Set.of())
                .onSuccess(msg -> {
                    stickyMessage = msg.getIdLong();
                    mongo.database.getMagicCollection(Lock.class).updateOne(Filters.eq("_id", channel), Updates.set(PROP_STICKY_MESSAGE, stickyMessage));
                });
    }

    /**
     * Does not unset {@code sticky_message} in database because it's assumed {@link #replaceStickyMessage(MongoProvider, GuildMessageChannel) the message is being replaced} or {@link LockingUnlock#unlock(GuildSlashEvent, TextChannel) the channel is being unlocked}
     */
    @NotNull
    public Optional<RestAction<?>> deleteStickyMessage(@NotNull GuildMessageChannel textChannel) {
        if (stickyMessage == null) return Optional.empty();
        final long oldStickyMessage = stickyMessage;
        stickyMessage = null;
        return Optional.of(textChannel.retrieveMessageById(oldStickyMessage).flatMap(Message::delete));
    }

    public static class PreviousPermissions {
        @NotNull private static final String ALLOWED = "allowed";
        @NotNull private static final String DENIED = "denied";

        @BsonProperty(ALLOWED) public Set<Permission> allowed;
        @BsonProperty(DENIED) public Set<Permission> denied;

        public PreviousPermissions() {}

        public PreviousPermissions(@NotNull PermissionOverride override) {
            allowed = filter(override.getAllowed());
            denied = filter(override.getDenied());
        }

        @NotNull
        private static Set<Permission> filter(@NotNull Set<Permission> permissions) {
            permissions.retainAll(LockingCommon.PERMISSIONS);
            return permissions;
        }
    }
}
