package network.venox.cobalt.commands.subcommands;

import network.venox.cobalt.command.CoParentCommand;
import network.venox.cobalt.command.CoSubCommand;
import network.venox.cobalt.Cobalt;
import network.venox.cobalt.commands.subcommands.qotdcmd.*;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;


public class QotdCmd extends CoParentCommand {
    public QotdCmd(@NotNull Cobalt cobalt) {
        super(cobalt);
    }

    @Override @NotNull
    public String description() {
        return "QOTD management commands";
    }

    @Override @NotNull
    public List<CoSubCommand> subCommands() {
        return Arrays.asList(
                new AddCmd(this),
                new ChannelCmd(this),
                new ListCmd(this),
                new RemoveCmd(this),
                new RoleCmd(this));
    }
}
