package network.venox.cobalt.mongo;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import space.dynomake.libretranslate.Language;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class CoUser {
    @NotNull public static final String PROP_AFK = "afk";
    @NotNull public static final String PROP_LANGUAGE = "language";
    @NotNull public static final String PROP_HIGHLIGHTS = "highlights";

    public static final long HIGHLIGHT_TIME = 300000; // in milliseconds, 5 minutes
    /**
     * [user ID, [guild ID, next highlight time]]
     */
    @NotNull public static final Map<Long, Map<Long, Long>> HIGHLIGHT_COOLDOWNS = new HashMap<>();

    @BsonId public long id;
    @BsonProperty(PROP_AFK) public boolean afk;
    @BsonProperty(PROP_LANGUAGE) @Nullable public Language language;
    @BsonProperty(PROP_HIGHLIGHTS) @Nullable public Set<String> highlights;
}
