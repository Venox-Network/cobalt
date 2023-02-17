package network.venox.cobalt.commands;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.utility.CoUtilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@CommandMarker @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MANAGE_THREADS})
public class ThreadCmd extends ApplicationCommand {
    @Dependency private Cobalt cobalt;

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "thread",
            description = "Commands to manage the current guild's auto-thread")
    public void onCommand(@NotNull GuildSlashEvent event,
                          @AppOption(description = "The channel to set the thread to") @Nullable TextChannel channel) {
        if (channel == null) channel = event.getChannel().asTextChannel();
        final boolean enabled = cobalt.data.getGuild(event.getGuild()).toggleThreadChannel(channel.getIdLong());
        event.reply("Auto-threading for " + channel.getAsMention() + " has been **" + CoUtilities.formatBoolean(enabled) + "**").setEphemeral(true).queue();
    }
}
