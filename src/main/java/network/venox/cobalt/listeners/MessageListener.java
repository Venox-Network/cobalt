package network.venox.cobalt.listeners;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import io.github.freya022.botcommands.api.core.annotations.BEventListener;
import io.github.freya022.botcommands.api.core.service.annotations.BService;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.*;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.javautilities.MiscUtility;
import xyz.srnyx.javautilities.StringUtility;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.LazyEmoji;
import xyz.srnyx.lazylibrary.LazyLibrary;
import xyz.srnyx.lazylibrary.utility.LazyUtilities;

import xyz.srnyx.magicmongo.MagicCollection;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@BService
public class MessageListener {
    @NotNull private final MongoProvider mongo;

    public MessageListener(@NotNull MongoProvider mongo) {
        this.mongo = mongo;
    }

    @BEventListener
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        final User author = event.getAuthor();
        final ChannelType channelType = event.getChannel().getType();
        if (channelType == ChannelType.PRIVATE) return;
        final boolean isTextChannel = channelType == ChannelType.TEXT;
        final GuildMessageChannel channel = event.getGuildChannel();
        final long channelId = channel.getIdLong();
        final Message message = event.getMessage();

        // Send lock sticky message if no chatting for 5 minutes
        boolean isLocked = false;
        if (isTextChannel) {
            // End existing scheduler
            final ScheduledFuture<?> scheduler = Lock.LOCK_FUTURES.get(channelId);
            if (scheduler != null) {
                scheduler.cancel(false);
                Lock.LOCK_FUTURES.remove(channelId);
            }
            // Start new scheduler
            final Lock lock = mongo.database.getMagicCollection(Lock.class)
                    .findOne("_id", channelId)
                    .orElse(null);
            isLocked = lock != null;
            if (isLocked) Lock.LOCK_FUTURES.put(channelId, MiscUtility.IO_SCHEDULER.schedule(() -> {
                try {
                    lock.replaceStickyMessage(mongo, channel).queue(null, LazyUtilities.IGNORE_MAX_MESSAGE_PINS);
                } catch (final Exception e) {
                    LazyLibrary.LOGGER.error("Failed to send lock sticky message", e);
                }
            }, 1, TimeUnit.MINUTES));
        }

        // React channel
        mongo.database.getMagicCollection(ReactChannel.class)
                .findOne("_id", channelId)
                .ifPresent(reactChannel -> reactChannel.addReactions(message));

        // Sticky message
        if (!isLocked) mongo.database.getMagicCollection(StickyMessage.class)
                .findOne("_id", channelId)
                .ifPresent(stickyMessage -> stickyMessage.send(mongo, channel));

        if (author.isSystem()) return;

        // Auto-thread channel
        if (!isLocked) mongo.database.getMagicCollection(AutoThread.class)
                .findOne("_id", channelId)
                .ifPresent(threadChannel -> threadChannel.createThread(mongo, message));

        if (author.isBot()) return;

        // Slowmode
        final long authorId = author.getIdLong();
        final long now = System.currentTimeMillis();
        if (isTextChannel) {
            final TextChannel textChannel = (TextChannel) channel;
            if (textChannel.getSlowmode() != 0) mongo.database.getMagicCollection(AutoSlowmode.class)
                    .findOne("_id", channel.getIdLong())
                    .ifPresent(slowmode -> {
                        AutoSlowmode.ACTIVE_USERS
                                .computeIfAbsent(channelId, _ -> new HashMap<>())
                                .put(authorId, now);
                        slowmode.setSlowmode(mongo, textChannel);
                    });
        }

        // Limited messages
        mongo.database.getMagicCollection(LimitedMessages.class)
                .findOne("_id", channelId)
                .ifPresent(limitedMessages -> limitedMessages.processMessage(message));

        // AFK (disable)
        final MagicCollection<CoUser> userCollection = mongo.database.getMagicCollection(CoUser.class);
        final CoUser coUser = userCollection.findOneAndUpdate(
                Filters.and(
                        Filters.eq("_id", authorId),
                        Filters.eq(CoUser.PROP_AFK, true)),
                Updates.unset(CoUser.PROP_AFK));
        if (coUser != null && coUser.afk) message.reply(":wave: **Welcome back,** you are no longer AFK!").queue();

        // Update highlights cooldown
        final Guild guild = event.getGuild();
        final long guildId = guild.getIdLong();
        final long newCooldown = now + CoUser.HIGHLIGHT_TIME;
        CoUser.HIGHLIGHT_COOLDOWNS
                .computeIfAbsent(authorId, _ -> new HashMap<>())
                .put(guildId, newCooldown);

        // Highlights stuff
        final String authorName = author.getName();
        final String rawContent = message.getContentRaw();
        final int rawLength = rawContent.length();
        final String rawLower = rawContent.toLowerCase();
        final String displayLowerNoURLs = message.getContentDisplay().toLowerCase().replaceAll("https?://(?:www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,63}\\.[a-zA-Z0-9()]{1,6}\\b[-a-zA-Z0-9()@:%_+.~#?&/=]{0,63}", "");
        final String jumpUrl = message.getJumpUrl();
        final Set<Long> mentions = message.getMentions().getUsers().stream()
                .map(ISnowflake::getIdLong)
                .collect(Collectors.toSet());

        // Get highlights embed factory
        final LazyEmbed embedForFactory = new LazyEmbed()
                .setAuthor(authorName, "https://discord.com/users/" + authorId, author.getEffectiveAvatarUrl())
                .setFooter("#" + channel.getName() + " in " + guild.getName(), guild.getIconUrl())
                .setTimestamp(message.getTimeCreated());
        LazyLibrary.LOGGER.info("Requesting past 4 messages for highlights embed...");
        final List<Message> history = new ArrayList<>(message.getChannel().getHistoryBefore(message, 4).complete().getRetrievedHistory());
        Collections.reverse(history);
        for (final Message histMsg : history) embedForFactory.addField(new MessageEmbed.Field(histMsg.getAuthor().getName(), StringUtility.shorten(histMsg.getContentRaw(), MessageEmbed.VALUE_MAX_LENGTH), false));
        final LazyEmbed.Factory embedFactory = embedForFactory.toFactory();

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
            if (otherCoUser.highlights == null || otherCoUser.highlights.isEmpty()) continue;

            // Check cooldown
            final Map<Long, Long> cooldowns = CoUser.HIGHLIGHT_COOLDOWNS.get(otherCoUser.id);
            if (cooldowns != null) {
                final Long cooldown = cooldowns.get(guildId);
                if (cooldown != null) {
                    if (cooldown - now > 0) return;
                    cooldowns.remove(guildId);
                }
            }

            // Check highlights
            for (final String highlight : otherCoUser.highlights) {
                if (!displayLowerNoURLs.contains(highlight)) continue;

                // Get highlight index in raw content
                final int index = rawLower.indexOf(highlight);
                if (index == -1) continue; // Edge case: removing URLs somehow formed/created highlight

                guild.retrieveMemberById(otherCoUser.id).queue(coMember -> {
                    // Check if in audio channel
                    final GuildVoiceState voiceState = coMember.getVoiceState();
                    if (voiceState != null && voiceState.inAudioChannel()) return;

                    // Check if they can see channel and history
                    if (!coMember.hasPermission(channel, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY)) return;

                    // Add to cooldowns
                    CoUser.HIGHLIGHT_COOLDOWNS
                            .computeIfAbsent(otherCoUser.id, _ -> new HashMap<>())
                            .put(guildId, newCooldown);

                    // Create embed
                    final int highlightHash = highlight.hashCode();
                    final LazyEmbed embed = embedFactory.newEmbed()
                            .setColor(new Color( // Generate unique color based on highlight
                                    (highlightHash & 0xFF0000) >> 16,
                                    (highlightHash & 0x00FF00) >> 8,
                                    highlightHash & 0x0000FF))
                            .setTitle(highlight, jumpUrl);

                    // Highlighted message (snippet if too long)
                    final String value;
                    final int highlightLength = highlight.length();
                    final String linkedHighlight = "[" + rawContent.substring(index, index + highlightLength) + "](" + jumpUrl + ")";
                    if (rawLength > MessageEmbed.VALUE_MAX_LENGTH) {
                        final int snippetRadius = (MessageEmbed.VALUE_MAX_LENGTH - linkedHighlight.length() - 6) / 2; // why does it divide by 2?
                        final int start = Math.max(0, index - snippetRadius);
                        final int end = Math.min(rawLength, index + highlightLength + snippetRadius);
                        String snippet = rawContent.substring(start, end);
                        if (start > 0) snippet = "..." + snippet;
                        if (end < rawLength) snippet = snippet + "...";
                        final int snippetIndex = snippet.toLowerCase().indexOf(highlight);
                        value = snippet.substring(0, snippetIndex) + linkedHighlight + snippet.substring(snippetIndex + highlightLength);
                    } else {
                        value = StringUtility.shorten(rawContent.substring(0, index) + linkedHighlight + rawContent.substring(index + highlightLength), MessageEmbed.VALUE_MAX_LENGTH);
                    }
                    embed.addField(authorName, value, false);

                    // Send embed in DMs
                    coMember.getUser().openPrivateChannel()
                            .flatMap(privateChannel -> privateChannel.sendMessageEmbeds(embed.build()))
                            .queue();
                }, LazyUtilities.IGNORE_UNKNOWN_MEMBER);
                break;
            }
        }
    }
}
