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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.javautilities.MiscUtility;
import xyz.srnyx.javautilities.manipulation.Mapper;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.utility.LazyUtilities;

import java.util.*;
import java.util.stream.Collectors;


@CommandMarker
public class ServersCmd extends ApplicationCommand {
    @NotNull private static final String AC_SERVERS_USER = "ServersCmd.serversCommand.user";

    @Dependency private Cobalt bot;

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "servers",
            description = "Lists all servers the bot is in")
    public void serversCommand(@NotNull GlobalSlashEvent event,
                               @AppOption(description = "The ID of the user to get mutual servers of", autocomplete = AC_SERVERS_USER) @Nullable String user) {
        if (!bot.config.checkIsOwner(event)) return;
        final JDA jda = event.getJDA();

        // Get entity & guilds
        String entity = jda.getSelfUser().getAsTag();
        List<Guild> guilds = bot.jda.getGuilds();
        if (user != null) {
            final Optional<Long> userId = Mapper.toLong(user);
            if (userId.isEmpty()) {
                event.replyEmbeds(LazyEmbed.invalidArgument("user", user).build(bot)).setEphemeral(true).queue();
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
        event.replyEmbeds(embed.build(bot)).setEphemeral(true).queue();
    }

    @AutocompletionHandler(name = AC_SERVERS_USER) @NotNull
    public List<Command.Choice> acServersUser(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!bot.isOwner(event.getUser().getIdLong())) return List.of();
        final Guild guild = event.getGuild();
        return guild == null ? List.of() : LazyUtilities.sortChoicesFuzzy(event, guild.getMembers().stream()
                .filter(member -> !member.getUser().isBot())
                .map(member -> new Command.Choice(member.getUser().getAsTag(), member.getIdLong()))
                .sorted(Comparator.comparing(choice -> choice.getName().toLowerCase()))
                .toList());
    }
}
