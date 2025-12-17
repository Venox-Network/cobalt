package network.venox.cobalt.listeners;

import io.github.freya022.botcommands.api.core.annotations.BEventListener;
import io.github.freya022.botcommands.api.core.service.annotations.BService;

import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;

import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.Corner;
import network.venox.cobalt.mongo.CornerCreator;
import network.venox.cobalt.mongo.ForumMessage;

import org.jetbrains.annotations.NotNull;


@BService
public class ChannelListener {
    @NotNull private final MongoProvider mongo;

    public ChannelListener(@NotNull MongoProvider mongo) {
        this.mongo = mongo;
    }

    @BEventListener
    public void onChannelCreate(@NotNull ChannelCreateEvent event) {
        // ForumMessage
        if (!(event.getChannel() instanceof ThreadChannel threadChannel) || !(threadChannel.getParentChannel() instanceof ForumChannel forumChannel)) return;
        mongo.database.getMagicCollection(ForumMessage.class).findOne("_id", forumChannel.getIdLong())
                .ifPresent(message -> threadChannel.sendMessage(message.message.toBuilder().build()).queue());
    }

    @BEventListener
    public void onChannelDelete(@NotNull ChannelDeleteEvent event) {
        final long channelId = event.getChannel().getIdLong();

        // CornerCreator
        mongo.database.getMagicCollection(CornerCreator.class).deleteOne("_id", channelId);
        // Corner
        mongo.database.getMagicCollection(Corner.class).deleteOne("_id", channelId);
        // ForumMessage
        mongo.database.getMagicCollection(ForumMessage.class).deleteOne("_id", channelId);
    }
}
