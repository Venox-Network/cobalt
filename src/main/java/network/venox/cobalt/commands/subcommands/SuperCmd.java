package network.venox.cobalt.commands.subcommands;

import network.venox.cobalt.command.CoParentCommand;
import network.venox.cobalt.command.CoSubCommand;
import network.venox.cobalt.Cobalt;
import network.venox.cobalt.commands.subcommands.supercmd.BanCmd;
import network.venox.cobalt.commands.subcommands.supercmd.KickCmd;
import network.venox.cobalt.commands.subcommands.supercmd.UnbanCmd;

import org.jetbrains.annotations.NotNull;

import java.util.List;


public class SuperCmd extends CoParentCommand {
    public SuperCmd(@NotNull Cobalt cobalt) {
        super(cobalt);
    }

    @Override @NotNull
    public String description() {
        return "Super punishments that affects all Venox servers";
    }

    @Override @NotNull
    public List<CoSubCommand> subCommands() {
        return List.of(
                new BanCmd(this),
                new KickCmd(this),
                new UnbanCmd(this));
    }
}
