package network.venox.cobalt.commands;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.application.slash.autocomplete.annotations.AutocompletionHandler;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.objects.CoEmbed;
import network.venox.cobalt.utility.CoMapper;

import org.jetbrains.annotations.NotNull;

import java.util.List;


@CommandMarker
public class LeaveCmd extends ApplicationCommand {
    @NotNull private static final String AC_LEAVE_SERVER = "LeaveCmd.leaveCommand.server";

    @Dependency private Cobalt cobalt;

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "leave",
            description = "Leaves the specified server")
    public void leaveCommand(@NotNull GlobalSlashEvent event,
                          @AppOption(description = "The ID of the server to leave", autocomplete = AC_LEAVE_SERVER) @NotNull String server) {
        if (!cobalt.config.checkIsOwner(event)) return;
        final Long serverId = CoMapper.toLong(server);
        if (serverId == null) {
            event.replyEmbeds(CoEmbed.invalidArgument(server).build()).setEphemeral(true).queue();
            return;
        }
        final Guild guild = cobalt.jda.getGuildById(serverId);
        if (guild == null) {
            event.replyEmbeds(CoEmbed.invalidArgument(server).build()).setEphemeral(true).queue();
            return;
        }

        // Leave guild
        guild.leave().queue();

        // Message
        event.replyEmbeds(new CoEmbed(CoEmbed.Type.SUCCESS)
                .setTitle("%type%Left server")
                .setDescription("Left server **" + guild.getName() + "** (`" + serverId + "`)")
                .build()).setEphemeral(true).queue();

        // Log
        cobalt.config.sendLog("left guild", "**Guild:** " + guild.getName() + " (`" + serverId + "`)\n**Executor:** " + event.getUser().getAsMention());
    }

    @AutocompletionHandler(name = AC_LEAVE_SERVER) @NotNull
    public List<Command.Choice> onAutoCompleteServer(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!cobalt.config.owners.contains(event.getUser().getIdLong())) return List.of();
        return cobalt.jda.getGuilds().stream()
                .map(guild -> new Command.Choice(guild.getName(), guild.getId()))
                .toList();
    }
}
