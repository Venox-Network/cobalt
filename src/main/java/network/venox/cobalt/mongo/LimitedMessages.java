package network.venox.cobalt.mongo;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import io.github.freya022.botcommands.api.core.BContext;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.requests.CompletedRestAction;
import network.venox.cobalt.MongoProvider;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.lazylibrary.emoji.LazyEmoji;
import xyz.srnyx.lazylibrary.utility.LazyUtilities;

import java.util.*;
import java.util.stream.Collectors;


public class LimitedMessages {
    @NotNull public static final String PROP_GUILD = "guild";
    @NotNull public static final String PROP_LIMIT = "limit";
    @NotNull public static final String PROP_ROLE = "role";
    @NotNull public static final String PROP_USERS = "users";

    @BsonId public long channel;
    @BsonProperty(PROP_GUILD) public long guild;
    @BsonProperty(PROP_LIMIT) public int limit;
    @BsonProperty(PROP_ROLE) @Nullable public Long role;
    @BsonProperty(PROP_USERS) @NotNull public Map<String, Integer> users = new HashMap<>();

    @BService
    public static class Manager {
        @NotNull private final BContext context;
        @NotNull private final MongoProvider mongo;

        public Manager(@NotNull BContext context, @NotNull MongoProvider mongo) {
            this.context = context;
            this.mongo = mongo;
        }

        @NotNull
        public Optional<Guild> guild(@NotNull LimitedMessages limitedMessages) {
            return Optional.ofNullable(context.getJda().getGuildById(limitedMessages.guild));
        }

        @NotNull
        public Optional<GuildMessageChannel> channel(@NotNull LimitedMessages limitedMessages) {
            return guild(limitedMessages).map(guild -> guild.getChannelById(GuildMessageChannel.class, limitedMessages.channel));
        }

        @Nullable
        public RestAction<Role> role(@NotNull LimitedMessages limitedMessages) {
            final Guild guild = guild(limitedMessages).orElse(null);
            if (guild == null) return null;

            // Return existing role
            if (limitedMessages.role != null) {
                final Role role = guild.getRoleById(limitedMessages.role);
                if (role != null) return new CompletedRestAction<>(guild.getJDA(), role);
            }

            // Create role
            return channel(limitedMessages)
                    .map(channel -> guild.createRole()
                            .setName("#" + channel.getName())
                            .setMentionable(false)
                            .setHoisted(false)
                            .setPermissions(Permission.EMPTY_PERMISSIONS)
                            .onSuccess(roleJda -> {
                                // Update in Mongo
                                limitedMessages.role = roleJda.getIdLong();
                                mongo.database.getMagicCollection(LimitedMessages.class).updateOne(
                                        Filters.eq("_id", limitedMessages.channel),
                                        Updates.set(LimitedMessages.PROP_ROLE, limitedMessages.role));

                                // Add channel permission override
                                channel.getPermissionContainer().upsertPermissionOverride(roleJda).setDenied(Permission.MESSAGE_SEND).queue();
                            }))
                    .orElse(null);
        }

        @NotNull
        public Optional<Map<Member, Integer>> users(@NotNull LimitedMessages limitedMessages) {
            return guild(limitedMessages).map(jdaGuild -> limitedMessages.users.entrySet().stream()
                    .map(entry -> {
                        final Member member = jdaGuild.retrieveMemberById(entry.getKey()).complete();
                        return member == null ? null : Map.entry(member, entry.getValue());
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }

        public void processMessage(@NotNull LimitedMessages limitedMessages, @NotNull Message message) {
            final Member author = message.getMember();
            if (author == null) return;

            // Update user count
            final String id = author.getId();
            final int newCount = limitedMessages.users.getOrDefault(id, 0) + 1;
            limitedMessages.users.put(id, newCount);

            // Check if user has reached limit
            if (newCount == limitedMessages.limit && checkUser(limitedMessages, author)) {
                author.getUser().openPrivateChannel()
                        .flatMap(privateChannel -> privateChannel.sendMessage(LazyEmoji.WARNING + " You have reached the message limit of `" + limitedMessages.limit + "` in <#" + limitedMessages.channel + ">!"))
                        .queue(null, LazyUtilities.IGNORE_CANNOT_SEND_TO_USER);
            }
        }

        public boolean checkUser(@NotNull LimitedMessages limitedMessages, @NotNull Member member) {
            final Guild guild = guild(limitedMessages).orElse(null);
            if (guild == null) return false;
            final RestAction<Role> roleAction = role(limitedMessages);
            if (roleAction == null) return false;
            final List<Role> roles = member.getRoles();
            // Need to compare with Role inside queue because roleAction can create role (limitedMessages.role would be stale)

            // Remove role
            if (limitedMessages.users.getOrDefault(member.getId(), 0) < limitedMessages.limit) {
                roleAction.queue(roleJda -> {
                    if (roles.contains(roleJda)) guild.removeRoleFromMember(member, roleJda).queue();
                });
                return false;
            }

            // Add role
            roleAction.queue(roleJda -> {
                if (!roles.contains(roleJda)) guild.addRoleToMember(member, roleJda).queue();
            });
            return true;
        }

        public void checkAllUsers(@NotNull LimitedMessages limitedMessages) {
            users(limitedMessages).ifPresent(members -> members.keySet().forEach(member -> checkUser(limitedMessages, member)));
        }
    }
}
