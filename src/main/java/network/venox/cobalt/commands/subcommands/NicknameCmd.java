package network.venox.cobalt.commands.subcommands;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.command.CoParentCommand;
import network.venox.cobalt.command.CoSubCommand;
import network.venox.cobalt.commands.subcommands.nicknamecmd.AddCmd;
import network.venox.cobalt.commands.subcommands.nicknamecmd.ListCmd;
import network.venox.cobalt.commands.subcommands.nicknamecmd.RemoveCmd;
import network.venox.cobalt.commands.subcommands.nicknamecmd.SetCmd;
import network.venox.cobalt.events.CoSlashCommandInteractionEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class NicknameCmd extends CoParentCommand {
    public NicknameCmd(@NotNull Cobalt cobalt) {
        super(cobalt);
    }

    @Override @NotNull
    public String description() {
        return "Commands to manage the nickname blacklist for the guild";
    }

    @Override @NotNull
    public List<CoSubCommand> subCommands() {
        return List.of(
                new AddCmd(this),
                new ListCmd(this),
                new RemoveCmd(this),
                new SetCmd(this));
    }

    @Nullable
    public Set<String> getNicknames(@NotNull CoSlashCommandInteractionEvent event) {
        final OptionMapping nicknamesOption = event.getOption("nicknames");
        if (nicknamesOption == null) return null;
        final OptionMapping delimiterOption = event.getOption("delimiter");
        final String nicknamesString = nicknamesOption.getAsString();

        // Get questions
        Set<String> nicknames = Collections.singleton(nicknamesString);
        if (delimiterOption != null) nicknames = Arrays.stream(nicknamesString.split(Pattern.quote(delimiterOption.getAsString()))).collect(Collectors.toSet());
        return nicknames.stream()
                .map(nickname -> nickname.toLowerCase().trim())
                .collect(Collectors.toSet());
    }
}
