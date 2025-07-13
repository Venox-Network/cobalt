package network.venox.cobalt.listeners;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import network.venox.cobalt.CoListener;
import network.venox.cobalt.Cobalt;
import network.venox.cobalt.mongo.*;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.javautilities.StringUtility;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.LazyEmoji;

import xyz.srnyx.magicmongo.MagicCollection;

import java.util.*;
import java.util.stream.Collectors;


public class MessageListener extends CoListener {
    public MessageListener(@NotNull Cobalt cobalt) {
        super(cobalt);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        final User author = event.getAuthor();
        if (author.isBot() || author.isSystem()) return;
        final ChannelType channelType = event.getChannel().getType();
        if (channelType == ChannelType.PRIVATE) return;
        final GuildMessageChannel channel = event.getGuildChannel();
        final long channelId = channel.getIdLong();
        final Message message = event.getMessage();

        // Slowmode
        if (channelType == ChannelType.TEXT) bot.dataManager.mongo.getMagicCollection(AutoSlowmode.class)
                .findOne("_id", channel.getIdLong())
                .ifPresent(slowmode -> slowmode.setSlowmode(bot, (TextChannel) channel));

        // React channel
        bot.dataManager.mongo.getMagicCollection(ReactChannel.class)
                .findOne("_id", channelId)
                .ifPresent(reactChannel -> reactChannel.addReactions(message));

        // Auto-thread channel
        bot.dataManager.mongo.getMagicCollection(AutoThread.class)
                .findOne("_id", channelId)
                .ifPresent(threadChannel -> threadChannel.createThread(bot, message));

        // Sticky message
        bot.dataManager.mongo.getMagicCollection(StickyMessage.class)
                .findOne("_id", channelId)
                .ifPresent(stickyMessage -> stickyMessage.send(bot, channel));

        // Limited messages
        bot.dataManager.mongo.getMagicCollection(LimitedMessages.class)
                .findOne("_id", channelId)
                .ifPresent(limitedMessages -> limitedMessages.processMessage(message));

        // AFK (disable)
        final long authorId = author.getIdLong();
        final MagicCollection<CoUser> userCollection = bot.dataManager.mongo.getMagicCollection(CoUser.class);
        final CoUser coUser = userCollection.findOneAndUpdate(
                Filters.and(
                        Filters.eq("_id", authorId),
                        Filters.eq(CoUser.PROP_AFK, true)),
                Updates.unset(CoUser.PROP_AFK));
        if (coUser != null && coUser.afk) message.reply(":wave: **Welcome back,** you are no longer AFK!").queue();

        final Guild guild = event.getGuild();
        if (!guild.getSelfMember().hasPermission(channel, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY)) return;
        final long guildId = guild.getIdLong();
        final String authorName = author.getName();
        final String content = message.getContentRaw();
        final String contentLower = content.toLowerCase().replaceAll("https?://(?:www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,63}\\.[a-zA-Z0-9()]{1,6}\\b[-a-zA-Z0-9()@:%_+.~#?&/=]{0,63}", "");
        final boolean checkHighlights = content.length() < 500;
        final String jumpUrl = message.getJumpUrl();
        final Set<Long> mentions = message.getMentions().getUsers().stream()
                .map(ISnowflake::getIdLong)
                .collect(Collectors.toSet());
        final LazyEmbed embedForFactory = new LazyEmbed()
                .setAuthor(authorName, "https://discord.com/users/" + authorId, author.getEffectiveAvatarUrl())
                .setFooter("#" + channel.getName() + " in " + guild.getName(), guild.getIconUrl())
                .setTimestamp(message.getTimeCreated());
        final LazyEmbed.Factory embedFactory = embedForFactory.toFactory();
        final long now = System.currentTimeMillis();
        final long newCooldown = now + CoUser.HIGHLIGHT_TIME;

        // Get recent message history
        final List<Message> history = new ArrayList<>(message.getChannel().getHistoryBefore(message, 4).complete().getRetrievedHistory());
        Collections.reverse(history);
        for (final Message msg1 : history) embedForFactory.addField(new MessageEmbed.Field(msg1.getAuthor().getName(), StringUtility.shorten(msg1.getContentRaw(), 1024), false));

        for (final CoUser otherCoUser : userCollection.find(Filters.or( // If other features added to this loop, update this filter!
                Filters.eq(CoUser.PROP_AFK, true),
                Filters.and(
                        Filters.exists(CoUser.PROP_HIGHLIGHTS),
                        Filters.ne(CoUser.PROP_HIGHLIGHTS, Collections.emptyList()))))) {
            if (otherCoUser.id == authorId) continue;

            // Check if mentioned
            if (mentions.stream().anyMatch(streamId -> streamId == otherCoUser.id)) {
                // AFK alert
                if (otherCoUser.afk) message.reply(LazyEmoji.WARNING + " <@" + otherCoUser.id + "> is currently AFK!")
                        .setAllowedMentions(Collections.emptyList())
                        .queue();
                continue;
            }

            // Highlights
            if (checkHighlights && otherCoUser.highlights != null) guild.retrieveMemberById(otherCoUser.id).queue(coMember -> {
                // Check if they can see channel and history
                if (!coMember.hasPermission(channel, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY)) return;

                // Check if in audio channel
                final GuildVoiceState voiceState = coMember.getVoiceState();
                if (voiceState != null && voiceState.inAudioChannel()) return;

                // Check cooldown
                final Map<Long, Long> cooldowns = bot.dataManager.highlightCooldowns.get(otherCoUser.id);
                if (cooldowns != null) {
                    final Long cooldown = cooldowns.get(guildId);
                    if (cooldown != null) {
                        if (cooldown - now > 0) return;
                        cooldowns.remove(guildId);
                    }
                }

                // Check highlights
                for (final String highlight : otherCoUser.highlights) {
                    final int index = contentLower.indexOf(highlight);
                    if (index == -1) continue;

                    // Add to cooldowns
                    bot.dataManager.highlightCooldowns
                            .computeIfAbsent(otherCoUser.id, k -> new HashMap<>())
                            .put(guildId, newCooldown);

                    final LazyEmbed embed = embedFactory.newEmbed().setTitle(highlight, jumpUrl);

                    // Highlighted message
                    embed.addField(authorName, StringUtility.shorten(content.substring(0, index) +
                            "[" + content.substring(index, index + highlight.length()) + "](" + jumpUrl + ")" +
                            content.substring(index + highlight.length()), 1024), false);

                    // Send embed in DMs
                    coMember.getUser().openPrivateChannel()
                            .flatMap(privateChannel -> privateChannel.sendMessageEmbeds(embed.build(bot)))
                            .queue();
                    break;
                }
            });
        }
    }
}
