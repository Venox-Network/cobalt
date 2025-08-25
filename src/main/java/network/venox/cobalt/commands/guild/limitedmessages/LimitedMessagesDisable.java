package network.venox.cobalt.commands.guild.limitedmessages;

import com.mongodb.client.model.Filters;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.annotations.UserPermissions;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.ChannelTypes;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import network.venox.cobalt.MongoProvider;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.LazyEmoji;


@Command
public class LimitedMessagesDisable extends ApplicationCommand {
    @NotNull private final MongoProvider mongo;

    public LimitedMessagesDisable(@NotNull MongoProvider mongo) {
        this.mongo = mongo;
    }

    @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MESSAGE_MANAGE})
    @JDASlashCommand(
            name = "limitedmessages",
            subcommand = "disable",
            description = "Disable limited messages in a channel")
    public void limitedMessagesDisable(@NotNull GuildSlashEvent event,
                                       @SlashOption(description = "The channel to disable limited messages in") @ChannelTypes({ChannelType.TEXT, ChannelType.NEWS}) @Nullable GuildChannel channel) {
        if (channel == null) channel = event.getChannel().asGuildMessageChannel();

        // Check permissions
        if (!event.getMember().hasPermission(channel, Permission.MANAGE_CHANNEL, Permission.MESSAGE_MANAGE)) {
            event.replyEmbeds(LazyEmbed.noPermission().build()).setEphemeral(true).queue();
            return;
        }

        // Delete
        final network.venox.cobalt.mongo.LimitedMessages existing = mongo.database.getMagicCollection(network.venox.cobalt.mongo.LimitedMessages.class).findOneAndDelete(Filters.eq("_id", channel.getIdLong()));
        if (existing == null) {
            event.reply(LazyEmoji.NO + " " + channel.getAsMention() + " doesn't have a per-user message limit!").setEphemeral(true).queue();
            return;
        }

        // Reply (removed)
        event.reply(LazyEmoji.YES + " Disabled per-user message limit in " + channel.getAsMention()).setEphemeral(true).queue();
    }
}
