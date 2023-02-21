package network.venox.cobalt.data;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;

import network.venox.cobalt.CoFile;
import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.objects.*;
import network.venox.cobalt.utility.CoMapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.NodeStyle;

import java.util.*;
import java.util.stream.Collectors;


public class CoGuild {
    @NotNull private final Cobalt cobalt;
    public final long guildId;
    @NotNull private final CoFile file;

    @Nullable public Long qotdChannel;
    @Nullable public Long qotdRole;
    @Nullable public Long welcomeChannel;
    public int maxNicknameLength;
    @Nullable public String moderatedNickname;
    @NotNull public final Set<String> nicknameBlacklist = new HashSet<>();
    @NotNull public final Set<Long> nicknameWhitelist = new HashSet<>();
    @NotNull public final Set<Long> bannedNicknameUsers = new HashSet<>();
    @NotNull public final Set<CoReactChannel> reactChannels = new HashSet<>();
    @NotNull public final Set<CoThreadChannel> threadChannels = new HashSet<>();
    @NotNull public final Set<CoStickyMessage> stickyMessages = new HashSet<>();
    @NotNull public final Set<CoSlowmode> slowmodes = new HashSet<>();
    @NotNull public final Set<CoLimitedMessages> limitedMessages = new HashSet<>();
    @NotNull public final Map<Long, Set<String>> statusRoles = new HashMap<>();

    public CoGuild(@NotNull Cobalt cobalt, long guildId) {
        this.cobalt = cobalt;
        this.guildId = guildId;
        this.file = new CoFile("data/guilds/" + guildId, NodeStyle.FLOW, false);

        // Load
        try {
            load();
        } catch (final SerializationException e) {
            e.printStackTrace();
        }
    }

    public CoGuild(@NotNull Cobalt cobalt, @NotNull Guild guild) {
        this(cobalt, guild.getIdLong());
    }

    public void load() throws SerializationException {
        final Guild guild = getGuild();
        if (guild == null) return;

        // qotdChannel
        final long qotdChannelId = file.yaml.node("qotd-channel").getLong();
        if (qotdChannelId != 0) this.qotdChannel = qotdChannelId;

        // qotdRole
        final long qotdRoleId = file.yaml.node("qotd-role").getLong();
        if (qotdRoleId != 0) this.qotdRole = qotdRoleId;

        // welcomeChannel
        final long welcomeChannelId = file.yaml.node("welcome-channel").getLong();
        if (welcomeChannelId != 0) this.welcomeChannel = welcomeChannelId;

        // maxNicknameLength
        this.maxNicknameLength = file.yaml.node("max-nickname-length").getInt(0);

        // moderatedNickname
        this.moderatedNickname = file.yaml.node("moderated-nickname").getString();

        // nicknameBlacklist
        for (final ConfigurationNode node : file.yaml.node("nickname-blacklist").childrenList()) {
            final String nickname = node.getString();
            if (nickname != null) nicknameBlacklist.add(nickname);
        }

        // nicknameWhitelist
        final List<Long> nicknameWhitelistList = file.yaml.node("nickname-whitelist").getList(Long.class);
        if (nicknameWhitelistList != null) nicknameWhitelist.addAll(nicknameWhitelistList);

        // bannedNicknameUsers
        final List<Long> bannedNicknameUsersList = file.yaml.node("banned-nickname-users").getList(Long.class);
        if (bannedNicknameUsersList != null) bannedNicknameUsers.addAll(bannedNicknameUsersList);

        // reactChannels
        for (final ConfigurationNode node : file.yaml.node("react-channels").childrenList()) {
            // Get emojis
            List<String> emojis = null;
            final ConfigurationNode emojisNode = node.node("emojis");
            if (!emojisNode.virtual()) {
                try {
                    emojis = emojisNode.getList(String.class);
                } catch (final SerializationException ignored) {
                    // Ignored
                }
            }
            // Add to list
            final CoReactChannel reactChannel = new CoReactChannel(node.node("channel").getLong(), emojis);
            if (reactChannel.getChannel(guild) != null) reactChannels.add(reactChannel);
        }

        // threadChannels
        for (final ConfigurationNode node : file.yaml.node("thread-channels").childrenList()) {
            final Long id = CoMapper.toLong(node.key());
            if (id == null) continue;
            final List<String> ignoredPhrasesList = node.node("ignored-phrases").getList(String.class);
            final List<Long> ignoredRolesList = node.node("ignored-roles").getList(Long.class);
            final CoThreadChannel threadChannel = new CoThreadChannel(id, node.node("name").getString(), node.node("count").getInt(), ignoredPhrasesList == null ? null : new HashSet<>(ignoredPhrasesList), ignoredRolesList == null ? null : new HashSet<>(ignoredRolesList));
            if (threadChannel.getChannel(guild) != null) threadChannels.add(threadChannel);
        }

        // stickyMessages
        for (final ConfigurationNode node : file.yaml.node("sticky-messages").childrenMap().values()) {
            final Long id = CoMapper.toLong(node.key());
            if (id == null) continue;
            final ConfigurationNode messageNode = node.node("message");
            final CoStickyMessage stickyMessage = new CoStickyMessage(cobalt, id,
                    new CoMessage(messageNode.node("content").getString(), messageNode.node("embeds").childrenMap().values().stream()
                            .map(CoEmbed::new)
                            .toList()),
                    node.node("current").getLong());
            if (stickyMessage.getChannel(guild) != null) stickyMessages.add(stickyMessage);
        }

        // slowmodes
        for (final ConfigurationNode node : file.yaml.node("slowmodes").childrenMap().values()) {
            final Long id = CoMapper.toLong(node.key());
            if (id == null) continue;
            final CoSlowmode slowmode = new CoSlowmode(id, node.node("minimum").getInt(), node.node("maximum").getInt());
            if (slowmode.getChannel(guild) != null) slowmodes.add(slowmode);
        }

        // limitedMessages
        for (final ConfigurationNode node : file.yaml.node("limited-messages").childrenMap().values()) {
            final Long id = CoMapper.toLong(node.key());
            if (id == null) continue;
            Long role = node.node("role").getLong();
            if (role == 0) role = null;
            final Map<Long, Integer> users = node.node("users").childrenMap().entrySet().stream()
                    .map(entry -> {
                        final Long userId = CoMapper.toLong(entry.getKey());
                        if (userId == null) return null;
                        return Map.entry(userId, entry.getValue().getInt(0));
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            final CoLimitedMessages limitedMessage = new CoLimitedMessages(id, node.node("limit").getInt(), role, users);
            if (limitedMessage.getChannel(guild) != null) limitedMessages.add(limitedMessage);
        }

        // statusRoles
        for (final ConfigurationNode node : file.yaml.node("status-roles").childrenMap().values()) {
            final Long id = CoMapper.toLong(node.key());
            final List<String> statusesList = node.getList(String.class);
            if (id != null && statusesList != null) statusRoles.put(id, new HashSet<>(statusesList));
        }
    }

    public void save() throws SerializationException {
        final Guild guild = getGuild();
        if (guild == null) return;

        // qotdChannel
        file.yaml.node("qotd-channel").set(getQotdChannel() == null ? null : qotdChannel);

        // qotdRole
        file.yaml.node("qotd-role").set(getQotdRole() == null ? null : qotdRole);

        // welcomeChannel
        file.yaml.node("welcome-channel").set(getWelcomeChannel() == null ? null : welcomeChannel);

        // maxNicknameLength
        file.yaml.node("max-nickname-length").set(maxNicknameLength == 0 ? null : maxNicknameLength);

        // moderatedNickname
        file.yaml.node("moderated-nickname").set(moderatedNickname);

        // nicknameBlacklist
        final ConfigurationNode nicknameBlacklistNode = file.yaml.node("nickname-blacklist");
        nicknameBlacklistNode.set(null);
        for (final String nickname : nicknameBlacklist) nicknameBlacklistNode.appendListNode().set(nickname);

        // nicknameWhitelist
        final ConfigurationNode nicknameWhitelistNode = file.yaml.node("nickname-whitelist");
        nicknameWhitelistNode.set(null);
        for (final Long role : nicknameWhitelist) if (guild.getRoleById(role) != null) nicknameWhitelistNode.appendListNode().set(role);

        // bannedNicknameUsers
        final ConfigurationNode bannedNicknameUsersNode = file.yaml.node("banned-nickname-users");
        bannedNicknameUsersNode.set(null);
        for (final Long user : bannedNicknameUsers) if (guild.getMemberById(user) != null) bannedNicknameUsersNode.appendListNode().set(user);

        // reactChannels
        final ConfigurationNode reactChannelsNode = file.yaml.node("react-channels");
        reactChannelsNode.set(null);
        for (final CoReactChannel reactChannel : reactChannels) if (reactChannel.getChannel(guild) != null) reactChannelsNode.appendListNode().set(reactChannel.toMap());

        // threadChannels
        final ConfigurationNode threadChannelsNode = file.yaml.node("thread-channels");
        threadChannelsNode.set(null);
        for (final CoThreadChannel threadChannel : threadChannels) if (threadChannel.getChannel(guild) != null) threadChannelsNode.node(threadChannel.channel).set(threadChannel.toMap());

        // stickyMessages
        final ConfigurationNode stickyMessagesNode = file.yaml.node("sticky-messages");
        stickyMessagesNode.set(null);
        for (final CoStickyMessage stickyMessage : stickyMessages) if (stickyMessage.getChannel(guild) != null) stickyMessagesNode.node(stickyMessage.channel).set(stickyMessage.toMap());

        // slowmodes
        final ConfigurationNode slowmodesNode = file.yaml.node("slowmodes");
        slowmodesNode.set(null);
        for (final CoSlowmode slowmode : slowmodes) if (slowmode.getChannel(guild) != null) slowmodesNode.node(slowmode.channel).set(slowmode.toMap());

        // limitedMessages
        final ConfigurationNode limitedMessagesNode = file.yaml.node("limited-messages");
        limitedMessagesNode.set(null);
        for (final CoLimitedMessages limitedMessage : limitedMessages) if (limitedMessage.getChannel(guild) != null) limitedMessagesNode.node(limitedMessage.channel).set(limitedMessage.toMap());

        // statusRoles
        final ConfigurationNode statusRolesNode = file.yaml.node("status-roles");
        statusRolesNode.set(null);
        for (final Map.Entry<Long, Set<String>> entry : statusRoles.entrySet()) {
            final Long id = entry.getKey();
            final Set<String> statuses = entry.getValue();
            if (guild.getRoleById(id) != null && statuses != null) statusRolesNode.node(id).set(statuses);
        }

        // SAVE FILE
        file.save();
    }

    @Nullable
    public Guild getGuild() {
        return cobalt.jda.getGuildById(guildId);
    }

    @Nullable
    public StandardGuildMessageChannel getQotdChannel() {
        if (qotdChannel == null) return null;
        final Guild guild = getGuild();
        if (guild == null) return null;
        return guild.getChannelById(StandardGuildMessageChannel.class, qotdChannel);
    }

    @Nullable
    public Role getQotdRole() {
        if (qotdRole == null) return null;
        final Guild guild = getGuild();
        if (guild == null) return null;
        return guild.getRoleById(qotdRole);
    }

    @NotNull
    public String getModeratedNickname() {
        if (moderatedNickname == null) return "Moderated Nickname";
        return moderatedNickname;
    }

    @Nullable
    public StandardGuildMessageChannel getWelcomeChannel() {
        if (welcomeChannel == null) return null;
        final Guild guild = getGuild();
        if (guild == null) return null;
        return guild.getChannelById(StandardGuildMessageChannel.class, welcomeChannel);
    }

    @Nullable
    public CoReactChannel getReactChannel(long channel) {
        return reactChannels.stream()
                .filter(reactChannel -> reactChannel.channel == channel)
                .findFirst()
                .orElse(null);
    }

    @Nullable
    public CoThreadChannel getThreadChannel(long channel) {
        return threadChannels.stream()
                .filter(threadChannel -> threadChannel.channel == channel)
                .findFirst()
                .orElse(null);
    }

    @Nullable
    public CoStickyMessage getStickyMessage(long channel) {
        return stickyMessages.stream()
                .filter(stickyMessage -> stickyMessage.channel == channel)
                .findFirst()
                .orElse(null);
    }

    @Nullable
    public CoSlowmode getSlowmode(long channelId) {
        return slowmodes.stream()
                .filter(slowmode -> slowmode.channel == channelId)
                .findFirst()
                .orElse(null);
    }

    @Nullable
    public CoLimitedMessages getLimitedMessages(long channelId) {
        return limitedMessages.stream()
                .filter(limitedMessage -> limitedMessage.channel == channelId)
                .findFirst()
                .orElse(null);
    }

    public void checkMemberNicknames(@Nullable Collection<String> nicknames) {
        final Guild guild = getGuild();
        if (guild != null) guild.loadMembers().onSuccess(members -> members.forEach(member -> checkMemberNickname(member, nicknames)));
    }

    public void checkMemberNickname(@NotNull Member member, @Nullable Collection<String> nicknames) {
        // Check if member has a whitelisted role
        if (!Collections.disjoint(nicknameWhitelist, member.getRoles().stream()
                .map(Role::getIdLong)
                .toList())) return;

        boolean changed = false;
        String newName = member.getEffectiveName().toLowerCase().trim();

        // Check length
        if (maxNicknameLength != 0 && newName.length() > maxNicknameLength) {
            changed = true;
            newName = newName.substring(0, maxNicknameLength);
        }

        // Check blacklist
        if (nicknames != null) {
            final String moderatedName = getModeratedNickname();
            for (final String nickname : nicknames) if (newName.contains(nickname)) {
                changed = true;
                newName = moderatedName;
                break;
            }
        }

        // Set nickname
        if (changed) member.modifyNickname(newName).queue(s -> {}, f -> {});
    }
}
