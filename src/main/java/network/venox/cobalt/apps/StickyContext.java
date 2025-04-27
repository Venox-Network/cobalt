package network.venox.cobalt.apps;

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
import network.venox.cobalt.data.objects.CoStickyMessage;
import network.venox.cobalt.mongo.Server;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.LazyMessage;


@CommandMarker @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MESSAGE_MANAGE, Permission.MESSAGE_SEND})
public class StickyContext extends ApplicationCommand {
    @Dependency private Cobalt bot;

    @JDAMessageCommand(
            scope = CommandScope.GUILD,
            name = "Sticky message")
    public void stickyContext(@NotNull GuildMessageEvent event) {
        final MessageChannelUnion channelUnion = event.getChannel();
        if (channelUnion == null) return;
        final TextChannel channel = channelUnion.asTextChannel();
        final Guild guild = event.getGuild();
        final Server server = bot.oldData.getGuild(guild);
        final Message message = event.getTarget();

        CoStickyMessage stickyMessage = server.getStickyMessage(channel.getIdLong());
        if (stickyMessage != null) {
            // Edit existing sticky message
            stickyMessage.delete();
            stickyMessage.message = new LazyMessage(message);
            stickyMessage.current = message.getIdLong();
        } else {
            // Set new sticky message
            stickyMessage = new CoStickyMessage(bot, guild.getIdLong(), channel.getIdLong(), new LazyMessage(message), null);
            server.stickyMessages.add(stickyMessage);
        }

        // Send & reply
        stickyMessage.send();
        event.reply(message.getJumpUrl() + " has been set as " + channel.getAsMention() + "'s sticky message").setEphemeral(true).queue();
    }
}
