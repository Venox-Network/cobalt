package network.venox.cobalt.listeners;

import io.github.freya022.botcommands.api.core.annotations.BEventListener;
import io.github.freya022.botcommands.api.core.service.annotations.BService;

import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;

import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.CornerCreator;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;


@BService
public class ChannelListener {
    @NotNull private final MongoProvider mongo;

    public ChannelListener(@NotNull MongoProvider mongo) {
        this.mongo = mongo;
    }

    @BEventListener
    public void onChannelDelete(@Nonnull ChannelDeleteEvent event) {
        mongo.database.getMagicCollection(CornerCreator.class).deleteOne("_id", event.getChannel().getIdLong());
    }
}
