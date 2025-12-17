package network.venox.cobalt.mongo;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import org.jetbrains.annotations.NotNull;


public class ForumMessage {
    @NotNull public static final String PROP_GUILD = "guild";
    @NotNull public static final String PROP_MESSAGE = "message";

    @BsonId public long channel;
    @BsonProperty(PROP_GUILD) public long guild;
    @BsonProperty(PROP_MESSAGE) public MongoMessage message;
}
