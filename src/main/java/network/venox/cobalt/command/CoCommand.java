package network.venox.cobalt.command;

import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import network.venox.cobalt.Cobalt;

import org.jetbrains.annotations.NotNull;


public abstract class CoCommand {
    @NotNull protected final Cobalt cobalt;

    protected CoCommand(@NotNull Cobalt cobalt) {
        this.cobalt = cobalt;
    }

    @NotNull
    public SlashCommandData toSlashCommandData() {
        return Commands.slash(name(), description());
    }

    @NotNull
    public String fullName() {
        return name();
    }

    @NotNull
    public String name() {
        return getClass().getSimpleName().toLowerCase().replace("cmd", "");
    }

    @NotNull
    public abstract String description();

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean ownerOnly() {
        return true;
    }
}
