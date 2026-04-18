package network.venox.cobalt;

import org.bson.types.ObjectId;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.javautilities.MiscUtility;

import java.util.Optional;


public class CoUtility {
    @NotNull
    public static Optional<ObjectId> toObjectId(@Nullable Object object) {
        return object != null ? MiscUtility.handleException(() -> new ObjectId(object.toString())) : Optional.empty();
    }
}
