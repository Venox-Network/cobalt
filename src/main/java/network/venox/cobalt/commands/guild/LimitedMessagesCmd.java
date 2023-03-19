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

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.CoGuild;
import network.venox.cobalt.data.objects.CoLimitedMessages;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;


@CommandMarker @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MESSAGE_MANAGE})
public class LimitedMessagesCmd extends ApplicationCommand {
    @Dependency private Cobalt cobalt;

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "limitedmessages",
            subcommand = "list",
            description = "List all channels with limited messages")
    public void listLimitedMessagesCommand(@NotNull GuildSlashEvent event) {
        final Set<CoLimitedMessages> limitedMessages = cobalt.data.getGuild(event.getGuild()).limitedMessages;

        // Check if empty
        if (limitedMessages.isEmpty()) {
            event.reply("No channels have a per-user message limit").setEphemeral(true).queue();
            return;
        }

        // Reply
        final StringBuilder sb = new StringBuilder();
        for (final CoLimitedMessages limitedMessage : limitedMessages) sb.append("<#").append(limitedMessage.channel).append(">: `").append(limitedMessage.limit).append("`").append("\n");
        event.reply(sb.toString()).setEphemeral(true).queue();
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
        final Guild guild = event.getGuild();
        final CoGuild coGuild = cobalt.data.getGuild(event.getGuild());

        // Edit existing limitedMessages and reply
        final CoLimitedMessages existing = coGuild.getLimitedMessages(channel.getIdLong());
        if (existing != null) {
            existing.limit = limit;
            event.reply("Updated per-user message limit in " + channel.getAsMention() + " to `" + limit + "`").setEphemeral(true).queue();
            existing.checkAllUsers(guild);
            return;
        }

        // Add new limitedMessages and reply
        coGuild.limitedMessages.add(new CoLimitedMessages(channel.getIdLong(), limit, null, null));
        event.reply("Set per-user message limit in " + channel.getAsMention() + " to `" + limit + "`").setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "limitedmessages",
            subcommand = "disable",
            description = "Disable limited messages in a channel")
    public void disableLimitedMessagesCommand(@NotNull GuildSlashEvent event,
                                              @AppOption(description = "The channel to disable limited messages in") @ChannelTypes({ChannelType.TEXT, ChannelType.NEWS}) @Nullable GuildChannel channel) {
        if (channel == null) channel = event.getChannel().asGuildMessageChannel();
        final CoGuild guild = cobalt.data.getGuild(event.getGuild());

        // Check existing
        final CoLimitedMessages existing = guild.getLimitedMessages(channel.getIdLong());
        if (existing == null) {
            event.reply(channel.getAsMention() + " doesn't have a per-user message limit").setEphemeral(true).queue();
            return;
        }

        // Remove from limitedMessages and reply
        guild.limitedMessages.remove(existing);
        event.reply("Disabled per-user message limit in " + channel.getAsMention()).setEphemeral(true).queue();
    }
}
