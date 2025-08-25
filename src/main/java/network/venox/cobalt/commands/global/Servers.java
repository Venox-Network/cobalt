package network.venox.cobalt.commands.global;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.CommandScope;
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData;
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;

import network.venox.cobalt.CoConfig;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.javautilities.MiscUtility;
import xyz.srnyx.javautilities.manipulation.Mapper;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.LazyLibrary;
import xyz.srnyx.lazylibrary.utility.LazyUtilities;

import java.util.*;
import java.util.stream.Collectors;


@Command
public class Servers extends ApplicationCommand {
    @NotNull private static final String AC_SERVERS_USER = "ServersCmd.serversCommand.user";

    @NotNull private final LazyLibrary library;
    @NotNull private final CoConfig config;

    public Servers(@NotNull LazyLibrary library, @NotNull CoConfig config) {
        this.library = library;
        this.config = config;
    }

    @TopLevelSlashCommandData(scope = CommandScope.GLOBAL)
    @JDASlashCommand(
            name = "servers",
            description = "Lists all servers the bot is in")
    public void serversCommand(@NotNull GlobalSlashEvent event,
                               @SlashOption(description = "The ID of the user to get mutual servers of", autocomplete = AC_SERVERS_USER) @Nullable String user) {
        if (!config.checkIsOwner(event)) return;
        final JDA jda = event.getJDA();

        // Get entity & guilds
        String entity = jda.getSelfUser().getAsTag();
        List<Guild> guilds = event.getJDA().getGuilds();
        if (user != null) {
            final Optional<Long> userId = Mapper.toLong(user);
            if (userId.isEmpty()) {
                event.replyEmbeds(LazyEmbed.invalidArgument("user", user).build()).setEphemeral(true).queue();
                return;
            }
            final User userEntity = jda.retrieveUserById(userId.get()).complete();
            entity = userEntity.getAsTag();
            guilds = jda.getGuilds().stream()
                    .filter(guild -> MiscUtility.handleException(() -> guild.retrieveMember(userEntity).complete()).isPresent())
                    .toList();
        }
        final Map<Guild, Integer> guildsMap = guilds.stream()
                .map(guild -> Map.entry(guild, guild.getMemberCount()))
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, HashMap::new));

        // Send embed
        final LazyEmbed embed = new LazyEmbed()
                .setTitle(entity + " servers")
                .setDescription("**Total servers:** " + guildsMap.size() + "\n**Total members:** " + guildsMap.values().stream()
                        .mapToInt(Integer::intValue)
                        .sum());
        guildsMap.forEach((guild, members) -> embed.addField(guild.getName(), "**ID:** `" + guild.getId() + "`\n**Members:** " + members, true));
        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

    @AutocompleteHandler(AC_SERVERS_USER) @NotNull
    public List<Choice> acServersUser(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!library.isOwner(event.getUser().getIdLong())) return List.of();
        final Guild guild = event.getGuild();
        return guild == null ? List.of() : LazyUtilities.sortChoicesFuzzy(event, guild.getMembers().stream()
                .filter(member -> !member.getUser().isBot())
                .map(member -> new Choice(member.getUser().getAsTag(), member.getIdLong()))
                .sorted(Comparator.comparing(choice -> choice.getName().toLowerCase()))
                .toList());
    }
}
