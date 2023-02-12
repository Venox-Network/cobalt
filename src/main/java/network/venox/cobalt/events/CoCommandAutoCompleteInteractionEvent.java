package network.venox.cobalt.events;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.CoGuild;
import network.venox.cobalt.data.CoUser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class CoCommandAutoCompleteInteractionEvent extends CommandAutoCompleteInteractionEvent {
    @NotNull private final CoUser coUser;
    @Nullable private final CoGuild coGuild;

    public CoCommandAutoCompleteInteractionEvent(@NotNull Cobalt cobalt, @NotNull CommandAutoCompleteInteractionEvent event) {
        super(event.getJDA(), event.getResponseNumber(), event.getInteraction());

        // coUser
        this.coUser = cobalt.data.getUser(event.getUser());

        // coGuild
        final Guild guild = event.getGuild();
        if (guild != null) {
            this.coGuild = cobalt.data.getGuild(guild);
        } else {
            this.coGuild = null;
        }
    }

    @NotNull
    public CoUser getCoUser() {
        return coUser;
    }

    @Nullable
    public CoGuild getCoGuild() {
        return coGuild;
    }
}
