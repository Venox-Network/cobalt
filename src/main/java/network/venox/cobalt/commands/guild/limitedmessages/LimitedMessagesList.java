package network.venox.cobalt.commands.guild.limitedmessages;

import com.mongodb.client.model.Filters;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.annotations.UserPermissions;
import io.github.freya022.botcommands.api.commands.application.CommandScope;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData;

import net.dv8tion.jda.api.Permission;

import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.LimitedMessages;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.LazyEmoji;

import java.util.List;


@Command
public class LimitedMessagesList {
    @NotNull private final MongoProvider mongo;

    public LimitedMessagesList(@NotNull MongoProvider mongo) {
        this.mongo = mongo;
    }

    @TopLevelSlashCommandData(scope = CommandScope.GUILD)
    @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MESSAGE_MANAGE})
    @JDASlashCommand(
            name = "limitedmessages",
            subcommand = "list",
            description = "List all channels with limited messages")
    public void limitedMessagesList(@NotNull GuildSlashEvent event) {
        final List<LimitedMessages> limitedMessages = mongo.database.getMagicCollection(network.venox.cobalt.mongo.LimitedMessages.class).findMany(Filters.eq(network.venox.cobalt.mongo.LimitedMessages.PROP_GUILD, event.getGuild().getIdLong()));

        // Check if empty
        if (limitedMessages.isEmpty()) {
            event.reply(LazyEmoji.NO + " No channels have a per-user message limit!").setEphemeral(true).queue();
            return;
        }

        // Reply
        final StringBuilder builder = new StringBuilder();
        for (final network.venox.cobalt.mongo.LimitedMessages limitedMessage : limitedMessages) builder.append("<#").append(limitedMessage.channel).append(">: `").append(limitedMessage.limit).append("`").append("\n");
        event.reply(builder.toString()).setEphemeral(true).queue();
    }
}
