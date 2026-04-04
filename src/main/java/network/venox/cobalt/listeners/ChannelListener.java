package network.venox.cobalt.listeners;

import com.mongodb.client.model.Filters;

import io.github.freya022.botcommands.api.core.annotations.BEventListener;
import io.github.freya022.botcommands.api.core.service.annotations.BService;

import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;

import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.Corner;
import network.venox.cobalt.mongo.CornerCreator;
import network.venox.cobalt.mongo.ThreadMessage;

import org.bson.conversions.Bson;

import org.jetbrains.annotations.NotNull;


@BService
public class ChannelListener {
    @NotNull private final MongoProvider mongo;

    public ChannelListener(@NotNull MongoProvider mongo) {
        this.mongo = mongo;
    }

    @BEventListener
    public void onChannelDelete(@NotNull ChannelDeleteEvent event) {
        final Bson filter = Filters.eq("_id", event.getChannel().getIdLong());

        // CornerCreator
        mongo.database.getMagicCollection(CornerCreator.class).deleteOne(filter);
        // Corner
        mongo.database.getMagicCollection(Corner.class).deleteOne(filter);
        // ThreadMessage
        mongo.database.getMagicCollection(ThreadMessage.class).deleteOne(filter);
    }
}
