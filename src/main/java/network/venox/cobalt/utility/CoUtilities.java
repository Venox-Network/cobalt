package network.venox.cobalt.utility;

import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.autocomplete.AutocompleteAlgorithms;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import network.venox.cobalt.data.objects.CoEmbed;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;


public class CoUtilities {
    @Nullable
    public static User getUser(@NotNull GlobalSlashEvent event, @NotNull String user) {
        final Long userId = CoMapper.toLong(user);
        if (userId == null) {
            event.replyEmbeds(CoEmbed.invalidArgument(user).build()).setEphemeral(true).queue();
            return null;
        }
        return event.getJDA().retrieveUserById(userId).complete();
    }

    @NotNull
    public static String formatBoolean(boolean bool, @NotNull String enabled, @NotNull String disabled) {
        return bool ? enabled : disabled;
    }

    @NotNull
    public static String formatBoolean(boolean bool) {
        return formatBoolean(bool, "enabled", "disabled");
    }

    @NotNull
    public static List<Command.Choice> sortChoicesFuzzy(@NotNull CommandAutoCompleteInteractionEvent event, @NotNull Collection<Command.Choice> collection) {
        final OptionType type = event.getFocusedOption().getType();
        return AutocompleteAlgorithms.fuzzyMatching(collection, Command.Choice::getName, event).stream()
                .map(result -> {
                    final Command.Choice choice = result.getReferent();
                    return getChoice(type, choice.getName(), choice.getAsString());
                })
                .toList();
    }
    
    @Nullable
    private static Command.Choice getChoice(@NotNull OptionType type, @NotNull String name, @NotNull String value) {
		return switch (type) {
			case STRING -> new Command.Choice(name, value);
			case INTEGER -> {
                final Long valueLong = CoMapper.toLong(value);
                yield valueLong == null ? null : new Command.Choice(name, valueLong);
			}
			case NUMBER -> {
                final Double valueDouble = CoMapper.toDouble(value);
                yield valueDouble == null ? null : new Command.Choice(name, valueDouble);
			}
			default -> throw new IllegalArgumentException("Invalid autocompletion option type: " + type);
		};
    }

    public static List<Command.Choice> acGuildMembers(@NotNull CommandAutoCompleteInteractionEvent event) {
        final Guild guild = event.getGuild();
        if (guild == null) return List.of();
        return sortChoicesFuzzy(event, guild.getMembers().stream()
                .filter(member -> !member.getUser().isBot())
                .map(member -> new Command.Choice(member.getUser().getAsTag(), member.getIdLong()))
                .sorted(Comparator.comparing(choice -> choice.getName().toLowerCase()))
                .toList());
    }

    public static void dynamicReact(@NotNull Message message) {
        for (final String emoji : message.getContentRaw().split("\\s+")) message.addReaction(Emoji.fromFormatted(emoji)).queue(s -> {}, f -> {});
    }

    private CoUtilities() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
