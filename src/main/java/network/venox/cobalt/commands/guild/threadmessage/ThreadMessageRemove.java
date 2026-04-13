package network.venox.cobalt.commands.guild.threadmessage;

import com.mongodb.client.model.Filters;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.annotations.UserPermissions;
import io.github.freya022.botcommands.api.commands.application.CommandScope;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer;

import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.ThreadMessage;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.emoji.LazyEmoji;


@Command
public class ThreadMessageRemove {
    @NotNull private final MongoProvider mongo;

    public ThreadMessageRemove(@NotNull MongoProvider mongo) {
        this.mongo = mongo;
    }

    @TopLevelSlashCommandData(scope = CommandScope.GUILD)
    @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MESSAGE_MANAGE, Permission.MESSAGE_SEND})
    @JDASlashCommand(
            name = "threadmessage",
            subcommand = "remove",
            description = "Remove the thread message of a thread channel")
    public void threadMessageRemove(@NotNull GuildSlashEvent event,
                                   @SlashOption(description = "The channel ID of the thread channel") @NotNull IThreadContainer channel) {
        final String channelMention = channel.getAsMention();

        // Delete thread message if it exists
        if (mongo.database.getMagicCollection(ThreadMessage.class).deleteOne(Filters.eq("_id", channel.getIdLong())).getDeletedCount() == 0) {
            event.replyEmbeds(LazyEmbed.invalidArgument("channel", channelMention + " does not have a thread message set").build()).setEphemeral(true).queue();
            return;
        }

        // Reply
        event.reply(LazyEmoji.YES + " " + channelMention + "'s thread message has been removed").setEphemeral(true).queue();
    }
}
