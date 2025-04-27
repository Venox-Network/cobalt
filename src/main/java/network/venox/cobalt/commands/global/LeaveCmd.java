package network.venox.cobalt.commands.global;

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

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.javautilities.manipulation.Mapper;

import xyz.srnyx.lazylibrary.LazyEmbed;

import java.util.List;


@CommandMarker
public class LeaveCmd extends ApplicationCommand {
    @NotNull private static final String AC_LEAVE_SERVER = "LeaveCmd.leaveCommand.server";

    @Dependency private Cobalt bot;

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "leave",
            description = "Leaves the specified server")
    public void leaveCommand(@NotNull GlobalSlashEvent event,
                          @AppOption(description = "The ID of the server to leave", autocomplete = AC_LEAVE_SERVER) @NotNull String server) {
        if (!bot.config.checkIsOwner(event)) return;
        final Long serverId = Mapper.toLong(server).orElse(null);
        if (serverId == null) {
            event.replyEmbeds(LazyEmbed.invalidArgument("server", server).build(bot)).setEphemeral(true).queue();
            return;
        }
        final Guild guild = bot.jda.getGuildById(serverId);
        if (guild == null) {
            event.replyEmbeds(LazyEmbed.invalidArgument("server", server).build(bot)).setEphemeral(true).queue();
            return;
        }

        // Leave guild
        guild.leave().queue();

        // Message
        event.replyEmbeds(new LazyEmbed()
                .setTitle("%type%Left server")
                .setDescription("Left server **" + guild.getName() + "** (`" + serverId + "`)")
                .build(bot)).setEphemeral(true).queue();

        // Log
        bot.config.guild.sendLog("left guild", "**Guild:** " + guild.getName() + " (`" + serverId + "`)\n**Executor:** " + event.getUser().getAsMention());
    }

    @AutocompletionHandler(name = AC_LEAVE_SERVER) @NotNull
    public List<Command.Choice> onAutoCompleteServer(@NotNull CommandAutoCompleteInteractionEvent event) {
        return !bot.isOwner(event.getUser().getIdLong()) ? List.of() : bot.jda.getGuilds().stream()
                .map(guild -> new Command.Choice(guild.getName(), guild.getId()))
                .toList();
    }
}
