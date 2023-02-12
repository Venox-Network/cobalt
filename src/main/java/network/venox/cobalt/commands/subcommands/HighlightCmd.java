package network.venox.cobalt.commands.subcommands;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.command.CoParentCommand;
import network.venox.cobalt.command.CoSubCommand;
import network.venox.cobalt.commands.subcommands.highlightcmd.AddCmd;
import network.venox.cobalt.commands.subcommands.highlightcmd.ListCmd;
import network.venox.cobalt.commands.subcommands.highlightcmd.RemoveCmd;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;


public class HighlightCmd extends CoParentCommand {
    public HighlightCmd(@NotNull Cobalt cobalt) {
        super(cobalt);
    }

    @Override @NotNull
    public String description() {
        return "Manages your highlights";
    }

    @Override @NotNull
    public Collection<CoSubCommand> subCommands() {
        return List.of(
                new AddCmd(this),
                new ListCmd(this),
                new RemoveCmd(this));
    }

    @Override
    public boolean ownerOnly() {
        return false;
    }
}
