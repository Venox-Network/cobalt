package network.venox.cobalt;

import io.github.freya022.botcommands.api.core.service.annotations.BService;

import network.venox.cobalt.mongo.*;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.javautilities.MapGenerator;

import xyz.srnyx.lazylibrary.LazyLibrary;

import xyz.srnyx.magicmongo.MagicDatabase;
import xyz.srnyx.magicmongo.SingleMongo;

import java.util.List;


@BService
public class MongoProvider {
    @NotNull public final MagicDatabase database;

    public MongoProvider(@NotNull LazyLibrary library) {
        final String url = library.fileSettings.file.yaml.node("mongo").getString();
        if (url == null) throw new IllegalArgumentException("MongoDB URL not found in config!");
        database = new SingleMongo(url).database.loadMagicCollections(MapGenerator.HASH_MAP.mapOf(
                List.of("auto_slowmodes", "auto_threads", "corners", "corner_creators", "users", "forum_messages", "limited_messages", "locks", "react_channels", "servers", "sticky_messages"),
                List.of(AutoSlowmode.class, AutoThread.class, Corner.class, CornerCreator.class, CoUser.class, ForumMessage.class, LimitedMessages.class, Lock.class, ReactChannel.class, Server.class, StickyMessage.class)));
    }
}
