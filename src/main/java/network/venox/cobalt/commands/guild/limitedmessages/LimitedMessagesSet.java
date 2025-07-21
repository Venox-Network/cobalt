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
import com.mongodb.client.model.Updates;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import network.venox.cobalt.Cobalt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.LazyEmoji;


@CommandMarker @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MESSAGE_MANAGE})
public class LimitedMessagesSet extends ApplicationCommand {
    @Dependency private Cobalt bot;

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "limitedmessages",
            subcommand = "set",
            description = "Only allow a certain amount of messages in a channel")
    public void limitedMessagesCommand(@NotNull GuildSlashEvent event,
                                       @AppOption(description = "The amount of messages to allow") int limit,
                                       @AppOption(description = "The channel to enable limited messages in") @ChannelTypes({ChannelType.TEXT, ChannelType.NEWS}) @Nullable GuildChannel channel) {
        if (channel == null) channel = event.getChannel().asGuildMessageChannel();

        // Check permissions
        if (!event.getMember().hasPermission(channel, Permission.MANAGE_CHANNEL, Permission.MESSAGE_MANAGE)) {
            event.replyEmbeds(LazyEmbed.noPermission().build(bot)).setEphemeral(true).queue();
            return;
        }

        // Edit existing limitedMessages and reply
        final network.venox.cobalt.mongo.LimitedMessages existing = bot.mongo.getMagicCollection(network.venox.cobalt.mongo.LimitedMessages.class).findOneAndUpdate(
                Filters.and(
                        Filters.eq("_id", channel.getIdLong()),
                        Filters.eq(network.venox.cobalt.mongo.LimitedMessages.PROP_GUILD, event.getGuild().getIdLong())),
                Updates.set(network.venox.cobalt.mongo.LimitedMessages.PROP_LIMIT, limit));
        if (existing != null) {
            existing.checkAllUsers(bot.jda);
            event.reply(LazyEmoji.YES + " Updated per-user message limit in " + channel.getAsMention() + " to `" + limit + "`").setEphemeral(true).queue();
            return;
        }

        // Reply (new)
        event.reply(LazyEmoji.YES + " Set per-user message limit in " + channel.getAsMention() + " to `" + limit + "`").setEphemeral(true).queue();
    }
}
