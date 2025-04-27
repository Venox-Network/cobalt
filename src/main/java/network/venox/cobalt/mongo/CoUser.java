package network.venox.cobalt.mongo;

import net.suuft.libretranslate.Language;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;


public class CoUser {
    @NotNull public static final String PROP_AFK = "afk";
    @NotNull public static final String PROP_LANGUAGE = "language";
    @NotNull public static final String PROP_HIGHLIGHTS = "highlights";

    public static final long HIGHLIGHT_TIME = 300000; // in milliseconds, 5 minutes

    @BsonId public long id;
    @BsonProperty(PROP_AFK) @Nullable public Boolean afk;
    @BsonProperty(PROP_LANGUAGE) @Nullable public Language language;
    @BsonProperty(PROP_HIGHLIGHTS) @Nullable public Set<String> highlights;

    public boolean afk() {
        return afk != null && afk;
    }

    @NotNull
    public Language language() {
        return language == null ? Language.ENGLISH : language;
    }

    @NotNull
    public Set<String> highlights() {
        return highlights == null ? Set.of() : highlights;
    }
}
