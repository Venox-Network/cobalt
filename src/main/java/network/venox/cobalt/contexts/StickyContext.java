package network.venox.cobalt.contexts;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.context.annotations.JDAMessageCommand;
import com.freya02.botcommands.api.application.context.message.GuildMessageEvent;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.CoGuild;
import network.venox.cobalt.data.objects.CoMessage;
import network.venox.cobalt.data.objects.CoStickyMessage;

import org.jetbrains.annotations.NotNull;


@CommandMarker @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MESSAGE_MANAGE, Permission.MESSAGE_SEND})
public class StickyContext extends ApplicationCommand {
    @Dependency private Cobalt cobalt;

    @JDAMessageCommand(
            scope = CommandScope.GUILD,
            name = "Sticky message")
    public void stickyContext(@NotNull GuildMessageEvent event) {
        final MessageChannelUnion channelUnion = event.getChannel();
        if (channelUnion == null) return;
        final TextChannel channel = channelUnion.asTextChannel();
        final Guild guild = event.getGuild();
        final CoGuild coGuild = cobalt.data.getGuild(guild);
        final Message message = event.getTarget();

        CoStickyMessage stickyMessage = coGuild.getStickyMessage(channel.getIdLong());
        if (stickyMessage != null) {
            // Edit existing sticky message
            stickyMessage.delete(guild);
            stickyMessage.message = new CoMessage(message);
            stickyMessage.current = message.getIdLong();
        } else {
            // Set new sticky message
            stickyMessage = new CoStickyMessage(cobalt, channel.getIdLong(), new CoMessage(message), null);
            coGuild.stickyMessages.add(stickyMessage);
        }

        // Send & reply
        stickyMessage.send(guild);
        event.reply(message.getJumpUrl() + " has been set as " + channel.getAsMention() + "'s sticky message").setEphemeral(true).queue();
    }
}
