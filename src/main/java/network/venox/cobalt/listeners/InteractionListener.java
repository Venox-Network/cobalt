package network.venox.cobalt.listeners;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;

import network.venox.cobalt.CoListener;
import network.venox.cobalt.command.CoExecutableCommand;
import network.venox.cobalt.Cobalt;
import network.venox.cobalt.events.CoCommandAutoCompleteInteractionEvent;
import network.venox.cobalt.events.CoSlashCommandInteractionEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class InteractionListener extends CoListener {
    public InteractionListener(@NotNull Cobalt cobalt) {
        super(cobalt);
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        final CoExecutableCommand command = getCommand(event.getFullCommandName());
        if (command != null && command.checkOwner(event)) command.onCommand(new CoSlashCommandInteractionEvent(cobalt, event));
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        // Get command
        final CoExecutableCommand command = getCommand(event.getFullCommandName());
        if (command == null) {
            replyChoicesEmpty(event);
            return;
        }

        // Get options
        final Collection<Command.Choice> options = command.onAutoComplete(new CoCommandAutoCompleteInteractionEvent(cobalt, event));
        if (options == null) {
            replyChoicesEmpty(event);
            return;
        }

        // Get and filter options
        final List<Command.Choice> choices = options.stream()
                .filter(choice -> choice.getName().toLowerCase().contains(event.getFocusedOption().getValue().toLowerCase()))
                .collect(Collectors.toList());
        if (choices.isEmpty()) {
            replyChoicesEmpty(event);
            return;
        }

        // Limit choices to 25
        if (choices.size() > 25) choices.subList(25, choices.size()).clear();

        // Reply choices
        event.replyChoices(choices).queue();
    }

    private void replyChoicesEmpty(@NotNull CommandAutoCompleteInteractionEvent event) {
        event.replyChoices(Collections.emptyList()).queue();
    }

    @Nullable
    private CoExecutableCommand getCommand(@NotNull String name) {
        return cobalt.executableCommands.stream()
                .filter(command -> command.fullName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
