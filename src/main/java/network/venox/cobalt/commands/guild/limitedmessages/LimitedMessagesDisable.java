package network.venox.cobalt.commands.guild.limitedmessages;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.ChannelTypes;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import com.mongodb.client.model.Filters;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import network.venox.cobalt.Cobalt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.LazyEmoji;


@CommandMarker @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MESSAGE_MANAGE})
public class LimitedMessagesDisable extends ApplicationCommand {
    @Dependency private Cobalt bot;

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "limitedmessages",
            subcommand = "disable",
            description = "Disable limited messages in a channel")
    public void disableLimitedMessagesCommand(@NotNull GuildSlashEvent event,
                                              @AppOption(description = "The channel to disable limited messages in") @ChannelTypes({ChannelType.TEXT, ChannelType.NEWS}) @Nullable GuildChannel channel) {
        if (channel == null) channel = event.getChannel().asGuildMessageChannel();

        // Check permissions
        if (!event.getMember().hasPermission(channel, Permission.MANAGE_CHANNEL, Permission.MESSAGE_MANAGE)) {
            event.replyEmbeds(LazyEmbed.noPermission().build(bot)).setEphemeral(true).queue();
            return;
        }

        // Delete
        final network.venox.cobalt.mongo.LimitedMessages existing = bot.mongo.getMagicCollection(network.venox.cobalt.mongo.LimitedMessages.class).findOneAndDelete(Filters.eq("_id", channel.getIdLong()));
        if (existing == null) {
            event.reply(LazyEmoji.NO + " " + channel.getAsMention() + " doesn't have a per-user message limit!").setEphemeral(true).queue();
            return;
        }

        // Reply (removed)
        event.reply(LazyEmoji.YES + " Disabled per-user message limit in " + channel.getAsMention()).setEphemeral(true).queue();
    }
}
