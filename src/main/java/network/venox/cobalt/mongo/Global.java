package network.venox.cobalt.mongo;

import network.venox.cobalt.Cobalt;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import org.jetbrains.annotations.NotNull;


public class Global {
    @NotNull public static final String PROP_QOTW_COUNT = "qotw_count";

    @NotNull public static final String ID_VALUE = "global";

    @BsonId public String id; // Should always be ID_VALUE
    @BsonProperty(PROP_QOTW_COUNT) public int qotwCount;

    @NotNull
    public static Global getGlobal(@NotNull Cobalt bot) {
        return bot.dataManager.mongo.getMagicCollection(Global.class).findOne("_id", ID_VALUE).orElse(new Global());
    }
}
