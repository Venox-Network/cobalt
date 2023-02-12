package network.venox.cobalt.commands.subcommands;

import network.venox.cobalt.command.CoParentCommand;
import network.venox.cobalt.command.CoSubCommand;
import network.venox.cobalt.Cobalt;
import network.venox.cobalt.commands.subcommands.reactcmd.SetCmd;
import network.venox.cobalt.commands.subcommands.reactcmd.UnsetCmd;

import org.jetbrains.annotations.NotNull;

import java.util.List;


public class ReactCmd extends CoParentCommand {
    public ReactCmd(@NotNull Cobalt cobalt) {
        super(cobalt);
    }

    @Override @NotNull
    public String description() {
        return "Manages settings for reaction channels for the current guild";
    }

    @Override @NotNull
    public List<CoSubCommand> subCommands() {
        return List.of(
                new SetCmd(this),
                new UnsetCmd(this));
    }
}
