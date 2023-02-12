package network.venox.cobalt.command;

import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import network.venox.cobalt.Cobalt;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;


public abstract class CoParentCommand extends CoCommand {
    public CoParentCommand(@NotNull Cobalt cobalt) {
        super(cobalt);
    }

    @Override @NotNull
    public SlashCommandData toSlashCommandData() {
        return super.toSlashCommandData().addSubcommands(subCommands().stream()
                .map(CoSubCommand::toSubcommandData)
                .toList());
    }

    @NotNull
    public abstract Collection<CoSubCommand> subCommands();
}
