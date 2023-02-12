package network.venox.cobalt;

import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.jetbrains.annotations.NotNull;


public class CoListener extends ListenerAdapter {
    @NotNull protected final Cobalt cobalt;

    public CoListener(@NotNull Cobalt cobalt) {
        this.cobalt = cobalt;
    }
}
