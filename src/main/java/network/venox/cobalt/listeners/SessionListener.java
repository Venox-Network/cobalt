package network.venox.cobalt.listeners;

import net.dv8tion.jda.api.events.session.ShutdownEvent;

import network.venox.cobalt.CoListener;
import network.venox.cobalt.Cobalt;

import org.jetbrains.annotations.NotNull;


public class SessionListener extends CoListener {
    public SessionListener(@NotNull Cobalt cobalt) {
        super(cobalt);
    }

    @Override
    public void onShutdown(@NotNull final ShutdownEvent event) {
        cobalt.data.save();
    }
}
