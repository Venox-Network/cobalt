package network.venox.cobalt.utility;

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
    public static Long toLong(@Nullable Object object) {
        if (object == null) return null;
        return handleException(() -> Long.parseLong(object.toString()));
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
