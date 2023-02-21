package network.venox.cobalt.commands.guild;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.CoGuild;
import network.venox.cobalt.data.objects.CoMessage;
import network.venox.cobalt.data.objects.CoStickyMessage;
import network.venox.cobalt.utility.CoMapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@CommandMarker @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MESSAGE_MANAGE, Permission.MESSAGE_SEND})
public class StickyCmd extends ApplicationCommand {
    @Dependency private Cobalt cobalt;

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "sticky",
            description = "Sticky a message to keep it as the last message in the current channel")
    public void stickyCommand(@NotNull GuildSlashEvent event,
                          @AppOption(description = "The message to sticky. If empty, sticky will be removed") @Nullable String message) {
        final Guild guild = event.getGuild();
        final CoGuild coGuild = cobalt.data.getGuild(guild);
        final TextChannel channel = event.getChannel().asTextChannel();

        // Delete existing sticky message
        if (message == null) {
            final CoStickyMessage stickyMessage = coGuild.getStickyMessage(channel.getIdLong());
            if (stickyMessage != null) {
                stickyMessage.delete(guild);
                coGuild.stickyMessages.remove(stickyMessage);
            }
            event.reply("Sticky message has been removed from " + channel.getAsMention()).setEphemeral(true).queue();
            return;
        }

        // Get messageId
        final Long messageId = CoMapper.toLong(message);
        if (messageId == null) {
            event.reply("Invalid message ID").setEphemeral(true).queue();
            return;
        }

        // Delete existing sticky message
        final CoStickyMessage current = coGuild.getStickyMessage(channel.getIdLong());
        if (current != null) {
            current.delete(guild);
            coGuild.stickyMessages.remove(current);
        }

        // Set new sticky message
        channel.retrieveMessageById(messageId)
                .queue(sentMessage -> {
                    final CoStickyMessage stickyMessage = new CoStickyMessage(cobalt, channel.getIdLong(), new CoMessage(sentMessage), null);
                    coGuild.stickyMessages.add(stickyMessage);
                    event.reply(sentMessage.getJumpUrl() + " has been set as " + channel.getAsMention() + "'s sticky message").setEphemeral(true).queue();
                    stickyMessage.send(guild);
                });
    }
}
