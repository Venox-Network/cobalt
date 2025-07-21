package network.venox.cobalt.commands.guild.limitedmessages;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import com.mongodb.client.model.Filters;

import net.dv8tion.jda.api.Permission;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.mongo.LimitedMessages;
import org.jetbrains.annotations.NotNull;


import xyz.srnyx.lazylibrary.LazyEmoji;

import java.util.List;


@CommandMarker @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MESSAGE_MANAGE})
public class LimitedMessagesList extends ApplicationCommand {
    @Dependency private Cobalt bot;

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "limitedmessages",
            subcommand = "list",
            description = "List all channels with limited messages")
    public void listLimitedMessagesCommand(@NotNull GuildSlashEvent event) {
        final List<LimitedMessages> limitedMessages = bot.mongo.getMagicCollection(network.venox.cobalt.mongo.LimitedMessages.class).findMany(Filters.eq(network.venox.cobalt.mongo.LimitedMessages.PROP_GUILD, event.getGuild().getIdLong()));

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
