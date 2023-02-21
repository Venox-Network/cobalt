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
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.objects.CoEmbed;
import network.venox.cobalt.utility.CoUtilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


@CommandMarker
public class ServersCmd extends ApplicationCommand {
    @NotNull private static final String AC_SERVERS_USER = "ServersCmd.serversCommand.user";

    @Dependency private Cobalt cobalt;

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "servers",
            description = "Lists all servers the bot is in")
    public void serversCommand(@NotNull GlobalSlashEvent event,
                          @AppOption(description = "The ID of the user to get mutual servers of", autocomplete = AC_SERVERS_USER) @Nullable String user) {
        if (!cobalt.config.checkIsOwner(event)) return;

        // Get guilds
        String object = "Cobalt";
        List<Guild> guilds = cobalt.jda.getGuilds();
        if (user != null) {
            final User userJda = CoUtilities.getUser(event, user);
            if (userJda == null) return;
            object = userJda.getAsTag();
            guilds = userJda.getMutualGuilds();
        }

        // Send embed
        final CoEmbed embed = cobalt.messages.getEmbed("command", "list-servers")
                .replace("%object%", object);
        guilds.stream()
                .sorted((o1, o2) -> Integer.compare(o2.getMemberCount(), o1.getMemberCount()))
                .forEach(guild -> embed.addField(guild.getName(), "**ID:** `" + guild.getId() + "`\n**Members:** " + guild.getMemberCount(), true));
        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

    @AutocompletionHandler(name = AC_SERVERS_USER) @NotNull
    public List<Command.Choice> acServersUser(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!cobalt.config.isOwner(event.getUser())) return List.of();
        return CoUtilities.acGuildMembers(event);
    }
}
