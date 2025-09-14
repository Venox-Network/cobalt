package network.venox.cobalt.listeners;

import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;

import network.venox.cobalt.CoListener;
import network.venox.cobalt.Cobalt;
import network.venox.cobalt.mongo.CornerCreator;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;


public class ChannelListener extends CoListener {
    public ChannelListener(@NotNull Cobalt bot) {
        super(bot);
    }

    @Override
    public void onChannelDelete(@Nonnull ChannelDeleteEvent event) {
        bot.mongo.getMagicCollection(CornerCreator.class).deleteOne("_id", event.getChannel().getIdLong());
    }
}
