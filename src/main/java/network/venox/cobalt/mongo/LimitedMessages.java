package network.venox.cobalt.mongo;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.RoleAction;
import net.dv8tion.jda.internal.requests.CompletedRestAction;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.LazyEmoji;

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
    @BsonProperty(PROP_ROLE) @Nullable private Long role;
    @BsonProperty(PROP_USERS) @NotNull public Map<String, Integer> users = new HashMap<>();

    @NotNull
    public Optional<Guild> guild(@NotNull JDA jda) {
        return Optional.ofNullable(jda.getGuildById(guild));
    }

    @NotNull
    public Optional<GuildMessageChannel> channel(@NotNull JDA jda) {
        return guild(jda).map(guild -> guild.getChannelById(GuildMessageChannel.class, channel));
    }

    @NotNull
    public Optional<RestAction<Role>> role(@NotNull JDA jda) {
        final Guild guild = guild(jda).orElse(null);
        if (guild == null) return Optional.empty();

        // Return existing role
        if (role != null) {
            final Role roleJda = guild.getRoleById(role);
            if (roleJda != null) return Optional.of(new CompletedRestAction<>(guild.getJDA(), roleJda));
        }

        // Create role
        final GuildMessageChannel channelJda = channel(jda).orElse(null);
        if (channelJda == null) return Optional.empty();
        final RoleAction action = guild.createRole()
                .setName("#" + channelJda.getName())
                .setMentionable(false)
                .setHoisted(false)
                .setPermissions(Permission.EMPTY_PERMISSIONS);
        return Optional.of(action.onSuccess(roleJda -> {
            role = roleJda.getIdLong();
            channelJda.getPermissionContainer().upsertPermissionOverride(roleJda).setDenied(Permission.MESSAGE_SEND).queue();
        }));
    }

    @NotNull
    public Optional<Map<Member, Integer>> users(@NotNull JDA jda) {
        return guild(jda).map(jdaGuild -> users.entrySet().stream()
                .map(entry -> {
                    final Member member = jdaGuild.retrieveMemberById(entry.getKey()).complete();
                    return member == null ? null : Map.entry(member, entry.getValue());
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    public void processMessage(@NotNull Message message) {
        final Member author = message.getMember();
        if (author == null) return;
        final String id = author.getId();
        final int count = users.getOrDefault(id, 0);

        // Check if user has reached limit
        if (checkUser(author) && count + 1 == limit) author.getUser().openPrivateChannel()
                .flatMap(privateChannel -> privateChannel.sendMessage(LazyEmoji.WARNING + " You have reached the message limit of `" + limit + "` in <#" + channel + ">!"))
                .queue(s -> {}, f -> {});

        // Update user count
        users.put(id, count + 1);
    }

    public boolean checkUser(@NotNull Member member) {
        final JDA jda = member.getJDA();
        final Guild guild = guild(jda).orElse(null);
        if (guild == null) return false;
        final List<Role> roles = member.getRoles();
        final RestAction<Role> roleAction = role(jda).orElse(null);

        // Remove role
        if (users.getOrDefault(member.getId(), 0) + 1 < limit) {
            if (roleAction != null) roleAction.queue(roleJda -> {
                if (roles.contains(roleJda)) guild.removeRoleFromMember(member, roleJda).queue();
            });
            return false;
        }

        // Add role
        if (roleAction != null) roleAction.queue(roleJda -> {
            if (!roles.contains(roleJda)) guild.addRoleToMember(member, roleJda).queue();
        });
        return true;
    }

    public void checkAllUsers(@NotNull JDA jda) {
        users(jda).ifPresent(members -> members.keySet().forEach(this::checkUser));
    }
}
