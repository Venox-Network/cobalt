package network.venox.cobalt.utility;

import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.utils.MiscUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;


public class CoMapper {
    @Nullable
    public static Integer toInt(@Nullable Object object) {
        if (object == null) return null;
        return handleException(() -> Integer.parseInt(object.toString()));
    }

    @Nullable
    public static Double toDouble(@Nullable Object object) {
        if (object == null) return null;
        return handleException(() -> Double.parseDouble(object.toString()));
    }

    @Nullable
    public static Long toLong(@Nullable Object object) {
        if (object == null) return null;
        return handleException(() -> Long.parseLong(object.toString()));
    }

    @Nullable
    public static Long parseSnowflake(@NotNull Object object) {
        return handleException(() -> MiscUtil.parseSnowflake(object.toString()));
    }

    @Nullable
    public static UserSnowflake toUserSnowflake(@Nullable Object object) {
        if (object == null) return null;
        final Long snowflake = parseSnowflake(object);
        if (snowflake == null) return null;
        return handleException(() -> UserSnowflake.fromId(snowflake));
    }

    @Nullable
    public static <R> R handleException(@NotNull Supplier<R> supplier) {
        return handleException(supplier, null);
    }

    @Nullable
    public static <R> R handleException(@NotNull Supplier<R> supplier, @Nullable R def) {
        try {
            return supplier.get();
        } catch (final Exception e) {
            return def;
        }
    }

    private CoMapper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
