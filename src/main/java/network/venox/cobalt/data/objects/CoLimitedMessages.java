package network.venox.cobalt.data.objects;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import network.venox.cobalt.data.CoObject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


public class CoLimitedMessages implements CoObject {
    public final long channel;
    public int limit;
    @Nullable private Long role;
    @NotNull public Map<Long, Integer> users = new HashMap<>();

    public CoLimitedMessages(long channel, int limit, @Nullable Long role, @Nullable Map<Long, Integer> users) {
        this.channel = channel;
        this.limit = limit;
        this.role = role;
        if (users != null) this.users = users;
    }

    @Override @NotNull
    public Map<String, Object> toMap() {
        final Map<String, Object> map = new HashMap<>();
        map.put("limit", limit);
        map.put("role", role);
        if (!users.isEmpty()) map.put("users", users);
        return map;
    }

    @Nullable
    public GuildMessageChannel getChannel(@NotNull Guild guild) {
        return guild.getChannelById(GuildMessageChannel.class, channel);
    }

    @Nullable
    private Role getRole(@NotNull Guild guild) {
        // Return existing role
        if (role != null) {
            final Role roleJda = guild.getRoleById(role);
            if (roleJda != null) return roleJda;
        }

        // Create role
        final GuildMessageChannel channelJda = getChannel(guild);
        if (channelJda == null) return null;
        final Role roleJda = guild.createRole()
                .setName("#" + channelJda.getName())
                .setMentionable(false)
                .setHoisted(false)
                .setPermissions(Permission.EMPTY_PERMISSIONS)
                .complete();
        channelJda.getPermissionContainer().upsertPermissionOverride(roleJda).setDenied(Permission.MESSAGE_SEND).queue();
        role = roleJda.getIdLong();
        return roleJda;
    }

    @Nullable
    public Map<Member, Integer> getUsers(@NotNull Guild guild) {
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
                    .flatMap(privateChannel -> privateChannel.sendMessage("You have reached the message limit of `" + limit + "` in <#" + channel + ">!"))
                    .queue(s -> {}, f -> {});
        }

        // Update user count
        users.put(id, count + 1);
    }

    public boolean checkUser(@NotNull Member member) {
        final Guild guild = member.getGuild();
        final List<Role> roles = member.getRoles();
        final Role roleJda = getRole(guild);

        // Remove role
        if (users.getOrDefault(member.getIdLong(), 0) + 1 < limit) {
            if (roleJda != null && roles.contains(roleJda)) guild.removeRoleFromMember(member, roleJda).queue();
            return false;
        }

        // Add role
        if (roleJda != null && !roles.contains(roleJda)) guild.addRoleToMember(member, roleJda).queue();
        return true;
    }

    public void checkAllUsers(@NotNull Guild guild) {
        final Map<Member, Integer> members = getUsers(guild);
        if (members != null) members.keySet().forEach(this::checkUser);
    }
}
