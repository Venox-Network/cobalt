package network.venox.cobalt.commands.guild;

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
import network.venox.cobalt.mongo.LimitedMessages;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.LazyEmoji;

import java.util.List;


@CommandMarker @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MESSAGE_MANAGE})
public class LimitedMessagesCmd extends ApplicationCommand {
    @Dependency private Cobalt bot;

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "limitedmessages",
            subcommand = "list",
            description = "List all channels with limited messages")
    public void listLimitedMessagesCommand(@NotNull GuildSlashEvent event) {
        final List<LimitedMessages> limitedMessages = bot.dataManager.mongo.getMagicCollection(LimitedMessages.class).findMany(Filters.eq(LimitedMessages.PROP_GUILD, event.getGuild().getIdLong()));

        // Check if empty
        if (limitedMessages.isEmpty()) {
            event.reply(LazyEmoji.NO + " No channels have a per-user message limit!").setEphemeral(true).queue();
            return;
        }

        // Reply
        final StringBuilder builder = new StringBuilder();
        for (final LimitedMessages limitedMessage : limitedMessages) builder.append("<#").append(limitedMessage.channel).append(">: `").append(limitedMessage.limit).append("`").append("\n");
        event.reply(builder.toString()).setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "limitedmessages",
            subcommand = "set",
            description = "Only allow a certain amount of messages in a channel")
    public void limitedMessagesCommand(@NotNull GuildSlashEvent event,
                                       @AppOption(description = "The amount of messages to allow") int limit,
                                       @AppOption(description = "The channel to enable limited messages in") @ChannelTypes({ChannelType.TEXT, ChannelType.NEWS}) @Nullable GuildChannel channel) {
        if (channel == null) channel = event.getChannel().asGuildMessageChannel();

        // Edit existing limitedMessages and reply
        final LimitedMessages existing = bot.dataManager.mongo.getMagicCollection(LimitedMessages.class).findOneAndUpdate(
                Filters.and(
                        Filters.eq("_id", channel.getIdLong()),
                        Filters.eq(LimitedMessages.PROP_GUILD, event.getGuild().getIdLong())),
                Updates.set(LimitedMessages.PROP_LIMIT, limit));
        if (existing != null) {
            existing.checkAllUsers(bot.jda);
            event.reply(LazyEmoji.YES + " Updated per-user message limit in " + channel.getAsMention() + " to `" + limit + "`").setEphemeral(true).queue();
            return;
        }

        // Reply (new)
        event.reply(LazyEmoji.YES + " Set per-user message limit in " + channel.getAsMention() + " to `" + limit + "`").setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "limitedmessages",
            subcommand = "disable",
            description = "Disable limited messages in a channel")
    public void disableLimitedMessagesCommand(@NotNull GuildSlashEvent event,
                                              @AppOption(description = "The channel to disable limited messages in") @ChannelTypes({ChannelType.TEXT, ChannelType.NEWS}) @Nullable GuildChannel channel) {
        if (channel == null) channel = event.getChannel().asGuildMessageChannel();

        // Delete
        final LimitedMessages existing = bot.dataManager.mongo.getMagicCollection(LimitedMessages.class).findOneAndDelete(Filters.eq("_id", channel.getIdLong()));
        if (existing == null) {
            event.reply(LazyEmoji.NO + " " + channel.getAsMention() + " doesn't have a per-user message limit!").setEphemeral(true).queue();
            return;
        }

        // Reply (removed)
        event.reply(LazyEmoji.YES + " Disabled per-user message limit in " + channel.getAsMention()).setEphemeral(true).queue();
    }
}
