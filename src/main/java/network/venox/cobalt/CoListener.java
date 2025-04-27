package network.venox.cobalt;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.LazyListener;


public class CoListener extends LazyListener {
    @NotNull protected final Cobalt bot;

    public CoListener(@NotNull Cobalt bot) {
        this.bot = bot;
    }
}
