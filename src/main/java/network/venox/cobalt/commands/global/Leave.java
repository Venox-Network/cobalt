package network.venox.cobalt.commands.global;

import io.github.freya022.botcommands.api.commands.application.CommandScope;
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData;
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;

import network.venox.cobalt.CoConfig;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.javautilities.manipulation.Mapper;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.LazyLibrary;

import java.util.List;


@io.github.freya022.botcommands.api.commands.annotations.Command
public class Leave {
    @NotNull private static final String AC_LEAVE_SERVER = "LeaveCmd.leaveCommand.server";

    @NotNull private final LazyLibrary library;
    @NotNull private final CoConfig config;

    public Leave(@NotNull LazyLibrary library, @NotNull CoConfig config) {
        this.library = library;
        this.config = config;
    }

    @TopLevelSlashCommandData(scope = CommandScope.GLOBAL)
    @JDASlashCommand(
            name = "leave",
            description = "OWNER | Leaves the specified server")
    public void leaveCommand(@NotNull GlobalSlashEvent event,
                             @SlashOption(description = "The ID of the server to leave", autocomplete = AC_LEAVE_SERVER) @NotNull String server) {
        if (!config.checkIsOwner(event)) return;
        final Long serverId = Mapper.toLong(server).orElse(null);
        if (serverId == null) {
            event.replyEmbeds(LazyEmbed.invalidArgument("server", server).build()).setEphemeral(true).queue();
            return;
        }
        final Guild guild = event.getJDA().getGuildById(serverId);
        if (guild == null) {
            event.replyEmbeds(LazyEmbed.invalidArgument("server", server).build()).setEphemeral(true).queue();
            return;
        }

        // Leave guild
        guild.leave().queue();

        // Message
        event.replyEmbeds(new LazyEmbed()
                .setTitle("%type%Left server")
                .setDescription("Left server **" + guild.getName() + "** (`" + serverId + "`)")
                .build()).setEphemeral(true).queue();

        // Log
        config.guild.sendLog("left guild", "**Guild:** " + guild.getName() + " (`" + serverId + "`)\n**Executor:** " + event.getUser().getAsMention());
    }

    @AutocompleteHandler(AC_LEAVE_SERVER) @NotNull
    public List<Command.Choice> onAutoCompleteServer(@NotNull CommandAutoCompleteInteractionEvent event) {
        return !library.isOwner(event.getUser().getIdLong()) ? List.of() : event.getJDA().getGuilds().stream()
                .map(guild -> new Command.Choice(guild.getName(), guild.getId()))
                .toList();
    }
}
