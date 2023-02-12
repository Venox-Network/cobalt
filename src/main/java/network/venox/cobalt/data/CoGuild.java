package network.venox.cobalt.data;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import network.venox.cobalt.CoFile;
import network.venox.cobalt.data.objects.*;
import network.venox.cobalt.utility.CoMapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.NodeStyle;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class CoGuild {
    @NotNull private final JDA jda;
    public final long guildId;
    @NotNull private final CoFile file;

    public boolean globalBans = true;
    @Nullable public Long qotdChannel = null;
    @Nullable public Long qotdRole = null;
    @NotNull public final Set<CoReactChannel> reactChannels = new HashSet<>();
    @NotNull public final Set<Long> threadChannels = new HashSet<>();
    @NotNull public final Set<CoStickyMessage> stickyMessages = new HashSet<>();
    @NotNull public final Set<String> nicknameBlacklist = new HashSet<>();
    @Nullable public String moderatedNickname = null;
    @NotNull public final Set<CoSlowmode> slowmodes = new HashSet<>();

    public CoGuild(@NotNull JDA jda, long guildId) {
        this.jda = jda;
        this.guildId = guildId;
        this.file = new CoFile("data/guilds/" + guildId, NodeStyle.FLOW, false);

        // Load
        try {
            load();
        } catch (final SerializationException e) {
            e.printStackTrace();
        }
    }

    public CoGuild(@NotNull Guild guild) {
        this(guild.getJDA(), guild.getIdLong());
    }

    public void load() throws SerializationException {
        final Guild guild = getGuild();
        if (guild == null) return;

        // enabled
        this.globalBans = file.yaml.node("global-bans").getBoolean(true);

        // qotdChannel
        final long qotdChannelId = file.yaml.node("qotd-channel").getLong();
        if (qotdChannelId != 0) this.qotdChannel = qotdChannelId;

        // qotdRole
        final long qotdRoleId = file.yaml.node("qotd-role").getLong();
        if (qotdRoleId != 0) this.qotdRole = qotdRoleId;

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
        final List<Long> threadChannelList = file.yaml.node("thread-channels").getList(Long.class);
        if (threadChannelList != null) threadChannels.addAll(threadChannelList);

        // stickyMessages
        for (final ConfigurationNode node : file.yaml.node("sticky-messages").childrenMap().values()) {
            final Long id = CoMapper.toLong(node.key());
            if (id == null) continue;
            final ConfigurationNode messageNode = node.node("message");
            final CoStickyMessage stickyMessage = new CoStickyMessage(id,
                    new CoMessage(messageNode.node("content").getString(), messageNode.node("embeds").childrenMap().values().stream()
                            .map(CoEmbed::new)
                            .toList()),
                    node.node("current").getLong());
            if (stickyMessage.getChannel(guild) != null) stickyMessages.add(stickyMessage);
        }

        // nicknameBlacklist
        for (final ConfigurationNode node : file.yaml.node("nickname-blacklist").childrenList()) {
            final String nickname = node.getString();
            if (nickname != null) nicknameBlacklist.add(nickname);
        }

        // moderatedNickname
        this.moderatedNickname = file.yaml.node("moderated-nickname").getString();

        // slowmodes
        for (final ConfigurationNode node : file.yaml.node("slowmodes").childrenMap().values()) {
            final Long channelId = CoMapper.toLong(node.key());
            if (channelId == null) continue;
            final CoSlowmode slowmode = new CoSlowmode(channelId, node.node("minimum").getInt(), node.node("maximum").getInt());
            if (slowmode.getChannel(guild) != null) slowmodes.add(slowmode);
        }
    }

    public void save() throws SerializationException {
        final Guild guild = getGuild();
        if (guild == null) return;

        // globalBans
        file.yaml.node("global-bans").set(globalBans ? null : false);

        // qotdChannel
        file.yaml.node("qotd-channel").set(getQotdChannel() == null ? null : qotdChannel);

        // qotdRole
        file.yaml.node("qotd-role").set(getQotdRole() == null ? null : qotdRole);

        // reactChannels
        final ConfigurationNode reactChannelsNode = file.yaml.node("react-channels");
        reactChannelsNode.set(null);
        for (final CoReactChannel reactChannel : reactChannels) if (reactChannel.getChannel(guild) != null) reactChannelsNode.appendListNode().set(reactChannel.toMap());

        // threadChannels
        final ConfigurationNode threadChannelsNode = file.yaml.node("thread-channels");
        threadChannelsNode.set(null);
        for (final Long threadChannel : threadChannels) if (guild.getTextChannelById(threadChannel) != null) threadChannelsNode.appendListNode().set(threadChannel);

        // stickyMessages
        final ConfigurationNode stickyMessagesNode = file.yaml.node("sticky-messages");
        stickyMessagesNode.set(null);
        for (final CoStickyMessage stickyMessage : stickyMessages) if (stickyMessage.getChannel(guild) != null) stickyMessagesNode.node(stickyMessage.channel).set(stickyMessage.toMap());

        // nicknameBlacklist
        final ConfigurationNode nicknameBlacklistNode = file.yaml.node("nickname-blacklist");
        nicknameBlacklistNode.set(null);
        for (final String nickname : nicknameBlacklist) nicknameBlacklistNode.appendListNode().set(nickname);

        // moderatedNickname
        file.yaml.node("moderated-nickname").set(moderatedNickname);

        // slowmodes
        final ConfigurationNode slowmodesNode = file.yaml.node("slowmodes");
        slowmodesNode.set(null);
        for (final CoSlowmode slowmode : slowmodes) if (slowmode.getChannel(guild) != null) slowmodesNode.node(slowmode.channelId).set(slowmode.toMap());

        // SAVE FILE
        file.save();
    }

    @Nullable
    public Guild getGuild() {
        return jda.getGuildById(guildId);
    }

    @Nullable
    public TextChannel getQotdChannel() {
        if (qotdChannel == null) return null;
        final Guild guild = getGuild();
        if (guild == null) return null;
        return guild.getTextChannelById(qotdChannel);
    }

    @Nullable
    public Role getQotdRole() {
        if (qotdRole == null) return null;
        final Guild guild = getGuild();
        if (guild == null) return null;
        return guild.getRoleById(qotdRole);
    }

    @Nullable
    public CoReactChannel getReactChannel(long channel) {
        return reactChannels.stream()
                .filter(reactChannel -> reactChannel.channel == channel)
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

    @NotNull
    public String getModeratedNickname() {
        if (moderatedNickname == null) return "Moderated Nickname";
        return moderatedNickname;
    }

    @Nullable
    public CoSlowmode getSlowmode(long channelId) {
        return slowmodes.stream()
                .filter(slowmode -> slowmode.channelId == channelId)
                .findFirst()
                .orElse(null);
    }

    /**
     * Toggles a channel as a thread channel
     *
     * @param   channel the channel to toggle
     * @return          true if the channel was added, false if it was removed
     */
    public boolean toggleThreadChannel(long channel) {
        if (threadChannels.contains(channel)) {
            threadChannels.remove(channel);
            return false;
        } else {
            threadChannels.add(channel);
            return true;
        }
    }
}
