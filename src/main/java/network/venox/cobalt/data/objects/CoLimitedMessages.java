package network.venox.cobalt.data.objects;

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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.LazyEmoji;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


public class CoLimitedMessages extends CoObject {
    @Nullable
    public Guild getGuild() {
        return jda.getGuildById(guildId);
    }

    @Nullable
    public GuildMessageChannel getChannel() {
        final Guild guild = getGuild();
        if (guild == null) return null;
        return guild.getChannelById(GuildMessageChannel.class, channel);
    }

    @Nullable
    public RestAction<Role> getRole() {
        final Guild guild = getGuild();
        if (guild == null) return null;

        // Return existing role
        if (role != null) {
            final Role roleJda = guild.getRoleById(role);
            if (roleJda != null) return new CompletedRestAction<>(guild.getJDA(), roleJda);
        }

        // Create role
        final GuildMessageChannel channelJda = getChannel();
        if (channelJda == null) return null;
        final RoleAction action = guild.createRole()
                .setName("#" + channelJda.getName())
                .setMentionable(false)
                .setHoisted(false)
                .setPermissions(Permission.EMPTY_PERMISSIONS);
        return action.onSuccess(roleJda -> {
            role = roleJda.getIdLong();
            channelJda.getPermissionContainer().upsertPermissionOverride(roleJda).setDenied(Permission.MESSAGE_SEND).queue();
        });
    }

    @Nullable
    public Map<Member, Integer> getUsers() {
        final Guild guild = getGuild();
        if (guild == null) return null;
        return users.entrySet().stream()
                .map(entry -> {
                    final Member member = guild.getMemberById(entry.getKey());
                    if (member == null) return null;
                    return Map.entry(member, entry.getValue());
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public void processMessage(@NotNull Message message) {
        final Member author = message.getMember();
        if (author == null) return;
        final long id = author.getIdLong();
        final int count = users.getOrDefault(id, 0);

        // Check if user has reached limit
        if (checkUser(author) && count + 1 == limit) {
            author.getUser().openPrivateChannel()
                    .flatMap(privateChannel -> privateChannel.sendMessage(LazyEmoji.WARNING + " You have reached the message limit of `" + limit + "` in <#" + channel + ">!"))
                    .queue(s -> {}, f -> {});
        }

        // Update user count
        users.put(id, count + 1);
    }

    public boolean checkUser(@NotNull Member member) {
        final Guild guild = getGuild();
        if (guild == null) return false;
        final List<Role> roles = member.getRoles();
        final RestAction<Role> roleAction = getRole();

        // Remove role
        if (users.getOrDefault(member.getIdLong(), 0) + 1 < limit) {
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

    public void checkAllUsers() {
        final Map<Member, Integer> members = getUsers();
        if (members != null) members.keySet().forEach(this::checkUser);
    }
}
