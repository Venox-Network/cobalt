package network.venox.cobalt.mongo;

import com.freya02.botcommands.api.components.Components;
import com.freya02.botcommands.api.utils.ButtonContent;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.objects.CoModmail;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.javautilities.manipulation.Mapper;

import xyz.srnyx.lazylibrary.LazyEmoji;

import java.awt.*;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


//TODO
public class Modmail {
    private static final Duration EXPIRE_TIME = Duration.ofDays(1);
    @NotNull public static final String AUTHOR_URL = "https://venox.network/modmailmessage/";
    @NotNull public static final String BUTTON_CLOSE_PRIVATE = "CoModmail.button.close.private";
    @NotNull public static final String BUTTON_CLOSE_THREAD = "CoModmail.button.close.thread";
    @NotNull public static final String BUTTON_DELETE = "CoModmail.button.delete";

    @BsonId public ObjectId id;
    @BsonProperty("user") public long userId;
    @BsonProperty("thread") public long threadId;
    public Boolean closed;
    @BsonProperty("dm_start_message") public long dmStartMessageId;
    @BsonProperty("expire_warning_sent") public Boolean expireWarningSent;

    @BsonIgnore @Nullable private Long futureStart;
    @BsonIgnore @Nullable private ScheduledFuture<?> future;

    public Modmail() {}

    public Modmail(long userId, long threadId, long dmStartMessageId, boolean closed, boolean expireWarningSent) {
        this.userId = userId;
        this.threadId = threadId;
        this.dmStartMessageId = dmStartMessageId;
        this.closed = closed;
        this.expireWarningSent = expireWarningSent;
    }

    @NotNull
    public CacheRestAction<User> getUser(@NotNull JDA jda) {
        return jda.retrieveUserById(userId);
    }

    @Nullable
    public ThreadChannel getThread(@NotNull Cobalt cobalt) {
        final Guild guild = cobalt.config.getGuild();
        if (guild == null) return null;
        final ThreadChannel thread = guild.getThreadChannelById(threadId);
        if (thread != null && (thread.isArchived() || thread.isLocked())) return null;
        return thread;
    }

    @NotNull
    private RestAction<PrivateChannel> getPrivateChannel(@NotNull JDA jda) {
        return getUser(jda).flatMap(User::openPrivateChannel);
    }

    @Nullable
    public MessageEmbed getUserEmbed(@NotNull JDA jda, @NotNull String description, long messageId) {
        final User user = getUser(jda).complete();
        return new EmbedBuilder()
                .setColor(Color.GREEN)
                .setAuthor(user.getName(), AUTHOR_URL + messageId, user.getEffectiveAvatarUrl())
                .setDescription(description)
                .build();
    }

    @NotNull
    public MessageEmbed getModeratorEmbed(@NotNull User moderator, @NotNull String description, long messageId) {
        return new EmbedBuilder()
                .setColor(Color.BLUE)
                .setAuthor(moderator.getName(), AUTHOR_URL + messageId, moderator.getEffectiveAvatarUrl())
                .setDescription(description)
                .build();
    }

    public void close(@NotNull Cobalt cobalt, @Nullable User closer) {
        closed = true;
        final ThreadChannel thread = getThread(cobalt);
        if (thread != null) thread.retrieveMessageById(thread.getIdLong())
                .flatMap(msg -> msg.editMessageComponents(ActionRow.of(getDeleteButton())))
                .flatMap(msg -> thread.sendMessage("\uD83D\uDD12 " + (closer == null ? "The modmail thread has expired" : "**`" + closer.getName() + "`** has closed the modmail thread") + "!"))
                .flatMap(msg -> thread.getManager().setArchived(true))
                .queue();
        getPrivateChannel(cobalt.jda)
                .flatMap(privateChannel -> privateChannel.sendMessage("\uD83D\uDD12 " + (closer == null ? "Your modmail thread has expired" : "**`" + closer.getName() + "`** has closed your modmail thread") + "!")
                        .flatMap(msg -> privateChannel.retrieveMessageById(dmStartMessageId)))
                .flatMap(msg -> msg.editMessageComponents(java.util.List.of()))
                .queue();
    }

    public void delete(@NotNull Cobalt cobalt, @NotNull User deleter) {
        // Delete thread
        final ThreadChannel thread = getThread(cobalt);
        if (thread != null) thread.delete().queue();
        getPrivateChannel(cobalt.jda)
                .flatMap(privateChannel -> privateChannel.sendMessage(LazyEmoji.TRASH + "️ **`" + deleter.getName() + "`** has deleted your modmail thread!")
                        .flatMap(msg -> privateChannel.retrieveMessageById(dmStartMessageId)))
                .flatMap(msg -> msg.editMessageComponents(java.util.List.of()))
                .queue();

        // Remove from database
        cobalt.dataManager.mongo.getMagicCollection(Modmail.class).deleteOne("_id", id);
    }

    //TODO
    public void checkExpiration(@NotNull Cobalt cobalt) {
        // First check
        if (futureStart == null) {
            // Expire warning already sent
            if (Boolean.TRUE.equals(expireWarningSent)) {
                close(cobalt, null);
                return;
            }

            // Expire warning not yet sent
            scheduleExpireWarning(cobalt, null);
            return;
        }

        // Not the first check
        final long timeSinceFutureScheduled = System.currentTimeMillis() - futureStart;
        if (timeSinceFutureScheduled < EXPIRE_TIME.toMillis()) return;
        if (Boolean.TRUE.equals(expireWarningSent)) {
            close(cobalt, null);
            return;
        }
        scheduleExpireWarning(cobalt, EXPIRE_TIME.toMillis() - timeSinceFutureScheduled);
    }

    //TODO
    public void scheduleExpireWarning(@NotNull Cobalt cobalt, @Nullable Long delay) {
        if (delay == null) delay = EXPIRE_TIME.toMillis();
        if (future != null) future.cancel(false);
        futureStart = System.currentTimeMillis();
        future = Cobalt.SCHEDULED_EXECUTOR_SERVICE.schedule(() -> {
            final ThreadChannel thread = getThread(cobalt);
            if (Boolean.TRUE.equals(closed) || thread == null) return;

            // Check if user was the last to talk
            try {
                if (Boolean.TRUE.equals(userTalkedLast(cobalt).get())) return;
            } catch (final InterruptedException | ExecutionException e) {
                e.printStackTrace();
                if (e instanceof InterruptedException) Thread.currentThread().interrupt();
                return;
            }

            // Schedule close
            future = Cobalt.SCHEDULED_EXECUTOR_SERVICE.schedule(() -> {
                if (Boolean.FALSE.equals(closed)) try {
                    if (Boolean.FALSE.equals(userTalkedLast(cobalt).get())) close(cobalt, null);
                } catch (final InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    if (e instanceof InterruptedException) Thread.currentThread().interrupt();
                }
            }, EXPIRE_TIME.toMillis(), TimeUnit.MILLISECONDS);

            // Send warning
            expireWarningSent = true;
            getPrivateChannel(cobalt.jda)
                    .flatMap(privateChannel -> privateChannel.sendMessage(LazyEmoji.WARNING + " If we don't hear from you soon, your modmail thread will be closed! <@" + userId + ">"))
                    .flatMap(msg -> thread.sendMessage(LazyEmoji.WARNING + " User has been sent an expiration warning. If they don't respond soon, the thread will be closed."))
                    .queue();
        }, delay, TimeUnit.MILLISECONDS);
    }

    @NotNull
    private CompletableFuture<Boolean> userTalkedLast(@NotNull Cobalt cobalt) {
        final ThreadChannel thread = getThread(cobalt);
        if (thread == null) return CompletableFuture.completedFuture(false);
        return thread.getIterableHistory()
                .takeAsync(10)
                .thenApply(messages -> {
                    for (final Message message : messages) {
                        if (!message.getAuthor().isBot()) return false;
                        if (getModmailMessageId(message) != null) return true;
                    }
                    return false;
                });
    }

    // STATIC METHODS

    @NotNull
    public static RestAction<Message> sendModmailConfirmation(@NotNull Cobalt cobalt, @NotNull User user, @Nullable Message message) {
        final long userId = user.getIdLong();
        final MessageCreateBuilder builder = message == null ? new MessageCreateBuilder() : MessageCreateBuilder.fromMessage(message);
        final String content = message == null ? "*Opened via `/support`*" : builder.getContent();

        final MessageCreateBuilder confirmation = new MessageCreateBuilder();
        confirmation.setContent("You are about to open a new modmail thread with the Venox Network staff.\nAre you sure you want to continue?");
        confirmation.addActionRow(
                Components.successButton(buttonEvent -> {
                    final ForumChannel forumChannel = cobalt.config.getGuildModmail();
                    if (forumChannel == null) {
                        buttonEvent.reply(LazyEmoji.NO + " Modmail is not configured!").setEphemeral(true).queue();
                        return;
                    }

                    final User userButton = buttonEvent.getUser();
                    final String mutualGuilds = buttonEvent.getJDA().getMutualGuilds(userButton).stream()
                            .map(Guild::getName)
                            .collect(Collectors.joining("\n- "));
                    builder.setContent("**User:** " + userButton.getAsMention() + " `" + userId + "`\n**Mutual servers:** \n- " + mutualGuilds + "\n**Message:**\n" + content);
                    buttonEvent.deferEdit()
                            .flatMap(hook -> hook.editOriginalComponents(java.util.List.of())
                                    .flatMap(msg -> forumChannel.createForumPost(userButton.getName(), MessageCreateData.fromContent("Getting the mods!")))
                                    .flatMap(post -> {
                                        final CoModmail modmail = new CoModmail(cobalt, userId, post.getThreadChannel().getIdLong(), buttonEvent.getMessageIdLong(), false, false, null);
                                        cobalt.dataManager.mongo.getMagicCollection(CoModmail.class).collection.insertOne(modmail);
                                        modmail.scheduleExpireWarning(null);
                                        builder.addActionRow(getCloseButton(BUTTON_CLOSE_THREAD), getDeleteButton());
                                        return hook.editOriginal(LazyEmoji.YES + " **Modmail thread created!** Any messages you send here will now be sent to the moderators.")
                                                .setActionRow(getCloseButton(BUTTON_CLOSE_PRIVATE))
                                                .flatMap(msg -> post.getMessage().editMessage("<@&" + cobalt.config.guildMod + ">"));
                                    }))
                            .flatMap(msg -> msg.editMessage(MessageEditBuilder.fromCreateData(builder.build()).build()))
                            .queue();
                }).build(LazyEmoji.YES_CLEAR.getEmoji()),
                Components.dangerButton(buttonEvent -> buttonEvent.getMessage().delete().queue()).build(LazyEmoji.NO_CLEAR_DARK.getEmoji()));
        return user.openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage(confirmation.build()));
    }

    @NotNull
    private static net.dv8tion.jda.api.interactions.components.buttons.Button getCloseButton(@NotNull String name) {
        return Components.dangerButton(name).build(new ButtonContent("Close", Emoji.fromUnicode("\uD83D\uDD12")));
    }

    @NotNull
    private static Button getDeleteButton() {
        return Components.dangerButton(BUTTON_DELETE).build(new ButtonContent("Delete", LazyEmoji.TRASH_CLEAR_DARK.getEmoji()));
    }

    @Nullable
    private static Long getModmailMessageId(@NotNull Message message) {
        if (!message.getAuthor().isBot()) return null;
        final List<MessageEmbed> embeds = message.getEmbeds();
        if (embeds.isEmpty()) return null;
        final MessageEmbed.AuthorInfo author = embeds.get(0).getAuthor();
        if (author == null) return null;
        final String url = author.getUrl();
        return url == null ? null : Mapper.toLong(url.replace("https://venox.network/modmailmessage/", ""));
    }

    @NotNull
    public static RestAction<Message> getModmailMessage(@NotNull MessageChannel channel, long otherMessage) {
        return channel.getHistory().retrievePast(50)
                .map(messages -> messages.stream()
                        .filter(message -> {
                            if (!message.getAuthor().isBot()) return false;
                            final Long messageId = getModmailMessageId(message);
                            return messageId != null && messageId == otherMessage;
                        })
                        .findFirst()
                        .orElse(null));
    }

    @NotNull
    public static RestAction<Message> getCreateAction(@NotNull MessageChannel channel, @NotNull Message message, @NotNull MessageEmbed embed) {
        final MessageCreateAction action = channel.sendMessage(new MessageCreateBuilder()
                .addEmbeds(embed)
                .addFiles(message.getAttachments().stream()
                        .map(attachment -> FileUpload.fromData(attachment.getProxy().download().join(), attachment.getFileName()))
                        .toList())
                .mentionRepliedUser(false)
                .build());
        final Message referencedMessage = message.getReferencedMessage();
        if (referencedMessage != null) {
            final Long modmailMessageId = getModmailMessageId(referencedMessage);
            return modmailMessageId != null ? action.setMessageReference(modmailMessageId) : getModmailMessage(channel, referencedMessage.getIdLong())
                    .flatMap(msg -> {
                        if (msg == null) return action;
                        return action.setMessageReference(msg.getIdLong());
                    });
        }
        return action;
    }
}
