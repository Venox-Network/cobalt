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
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import network.venox.cobalt.Cobalt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.LazyEmoji;


@CommandMarker @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MANAGE_SERVER})
public class WelcomeCmd extends ApplicationCommand {
    @Dependency private Cobalt cobalt;

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "welcome",
            subcommand = "set",
            description = "Set the welcome channel")
    public void welcomeCommand(@NotNull GuildSlashEvent event,
                               @AppOption(description = "The channel to set as welcome channel") @ChannelTypes({ChannelType.TEXT}) @Nullable GuildChannel channel) {
        if (channel == null) channel = event.getChannel().asGuildMessageChannel();
        cobalt.oldData.getGuild(event.getGuild()).welcomeChannel = channel.getIdLong();
        event.reply(LazyEmoji.YES + " Welcome channel has been set to " + channel.getAsMention()).setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "welcome",
            subcommand = "remove",
            description = "Remove the welcome channel")
    public void welcomeCommand(@NotNull GuildSlashEvent event) {
        cobalt.oldData.getGuild(event.getGuild()).welcomeChannel = null;
        event.reply(LazyEmoji.YES + " Welcome channel has been removed").setEphemeral(true).queue();
    }
}
