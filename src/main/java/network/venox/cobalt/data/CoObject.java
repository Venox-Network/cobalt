package network.venox.cobalt.data;

import org.jetbrains.annotations.NotNull;

import java.util.Map;


public interface CoObject {
    @NotNull
    Map<String, Object> toMap();
}
