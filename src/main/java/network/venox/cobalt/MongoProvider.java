package network.venox.cobalt;

import io.github.freya022.botcommands.api.core.service.annotations.BService;

import network.venox.cobalt.mongo.*;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.LazyLibrary;

import xyz.srnyx.magicmongo.MagicDatabase;
import xyz.srnyx.magicmongo.SingleMongo;

import java.util.Map;


@BService
public class MongoProvider {
    @NotNull public final MagicDatabase database;

    public MongoProvider(@NotNull LazyLibrary library) {
        final String url = library.fileSettings.file.yaml.node("mongo").getString();
        if (url == null) throw new IllegalArgumentException("MongoDB URL not found in config!");
        database = new SingleMongo(url).database.loadMagicCollections(Map.of(
                "auto_slowmodes", AutoSlowmode.class,
                "auto_threads", AutoThread.class,
                "users", CoUser.class,
                "limited_messages", LimitedMessages.class,
                "locks", Lock.class,
                "react_channels", ReactChannel.class,
                "servers", Server.class,
                "sticky_messages", StickyMessage.class));
    }
}
