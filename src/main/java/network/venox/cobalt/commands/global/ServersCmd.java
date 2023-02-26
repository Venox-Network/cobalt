package network.venox.cobalt.commands.global;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.application.slash.autocomplete.annotations.AutocompletionHandler;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.objects.CoEmbed;
import network.venox.cobalt.utility.CoMapper;
import network.venox.cobalt.utility.CoUtilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


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
        final JDA jda = event.getJDA();

        // Get entity & guilds
        String entity = jda.getSelfUser().getAsTag();
        List<Guild> guilds = cobalt.jda.getGuilds();
        if (user != null) {
            final Long userId = CoMapper.toLong(user);
            if (userId == null) {
                event.replyEmbeds(CoEmbed.invalidArgument(user).build()).setEphemeral(true).queue();
                return;
            }
            final User userEntity = jda.retrieveUserById(userId).complete();
            entity = userEntity.getAsTag();
            guilds = userEntity.getMutualGuilds();
        }
        final Map<Guild, Integer> guildsMap = guilds.stream()
                .map(guild -> Map.entry(guild, guild.getMemberCount()))
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, HashMap::new));

        // Send embed
        final CoEmbed embed = cobalt.messages.getEmbed("command.servers")
                .replace("%entity%", entity)
                .replace("%guilds%", guildsMap.size())
                .replace("%members%", guildsMap.values().stream()
                        .mapToInt(Integer::intValue)
                        .sum());
        guildsMap.forEach((guild, members) -> embed.addField(guild.getName(), "**ID:** `" + guild.getId() + "`\n**Members:** " + members, true));
        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

    @AutocompletionHandler(name = AC_SERVERS_USER) @NotNull
    public List<Command.Choice> acServersUser(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!cobalt.config.isOwner(event.getUser())) return List.of();
        return CoUtilities.acGuildMembers(event);
    }
}
