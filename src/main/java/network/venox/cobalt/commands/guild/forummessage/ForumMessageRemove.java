package network.venox.cobalt.commands.guild.forummessage;

import com.mongodb.client.model.Filters;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.annotations.UserPermissions;
import io.github.freya022.botcommands.api.commands.application.CommandScope;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;

import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.ForumMessage;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.LazyEmoji;


@Command
public class ForumMessageRemove {
    @NotNull private final MongoProvider mongo;

    public ForumMessageRemove(@NotNull MongoProvider mongo) {
        this.mongo = mongo;
    }

    @TopLevelSlashCommandData(scope = CommandScope.GUILD)
    @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MESSAGE_MANAGE, Permission.MESSAGE_SEND})
    @JDASlashCommand(
            name = "forummessage",
            subcommand = "remove",
            description = "Remove the forum message of a forum channel")
    public void forumMessageRemove(@NotNull GuildSlashEvent event,
                                   @SlashOption(description = "The channel ID of the forum channel") @NotNull ForumChannel channel) {
        final String channelMention = channel.getAsMention();

        // Delete forum message if it exists
        if (mongo.database.getMagicCollection(ForumMessage.class).deleteOne(Filters.eq("_id", channel.getIdLong())).getDeletedCount() == 0) {
            event.replyEmbeds(LazyEmbed.invalidArgument("channel", channelMention + " does not have a forum message set").build()).setEphemeral(true).queue();
            return;
        }

        // Reply
        event.reply(LazyEmoji.YES + " " + channelMention + "'s forum message has been removed").setEphemeral(true).queue();
    }
}
