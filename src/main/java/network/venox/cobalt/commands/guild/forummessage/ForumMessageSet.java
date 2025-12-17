package network.venox.cobalt.commands.guild.forummessage;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.annotations.UserPermissions;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.internal.utils.Helpers;

import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.ForumMessage;
import network.venox.cobalt.mongo.MongoMessage;
import network.venox.cobalt.mongo.StickyMessage;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.javautilities.manipulation.Mapper;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.LazyEmoji;

import java.util.Optional;


@Command
public class ForumMessageSet extends ApplicationCommand {
    @NotNull private final MongoProvider mongo;

    public ForumMessageSet(@NotNull MongoProvider mongo) {
        this.mongo = mongo;
    }

    @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MESSAGE_MANAGE, Permission.MESSAGE_SEND})
    @JDASlashCommand(
            name = "forummessage",
            subcommand = "set",
            description = "Set the message to be sent whenever a new post is created in a forum channel")
    public void forumMessageSet(@NotNull GuildSlashEvent event,
                                @SlashOption(description = "The channel ID of the forum channel") @NotNull ForumChannel channel,
                                @SlashOption(description = "The message ID to set as the forum message") @NotNull String message) {
        // Get messageId
        final Optional<Long> messageId = Mapper.toLong(message);
        if (messageId.isEmpty()) {
            event.replyEmbeds(LazyEmbed.invalidArgument("message", message).build()).setEphemeral(true).queue();
            return;
        }
        final MessageChannel currentChannel = event.getMessageChannel();
        final long guildId = event.getGuild().getIdLong();

        // Reply
        event.reply(LazyEmoji.YES + " " + Helpers.format(Message.JUMP_URL, guildId, currentChannel.getId(), message) + " has been set as " + channel.getAsMention() + "'s forum message").setEphemeral(true).queue();

        // Upsert new forum message
        currentChannel.retrieveMessageById(messageId.get())
                .queue(sentMessage -> mongo.database.getMagicCollection(ForumMessage.class).findOneAndUpsert(
                        Filters.and(
                                Filters.eq("_id", channel.getIdLong()),
                                Filters.eq(StickyMessage.PROP_GUILD, guildId)),
                        Updates.set(StickyMessage.PROP_MESSAGE, new MongoMessage(sentMessage))));
    }
}
