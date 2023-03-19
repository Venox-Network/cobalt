package network.venox.cobalt.commands.global;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.managers.Presence;

import network.venox.cobalt.Cobalt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;


@CommandMarker
public class StatusCmd extends ApplicationCommand {
    @Dependency private Cobalt cobalt;

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "status",
            description = "Sets the custom status of the bot")
    public void statusCommand(@NotNull GlobalSlashEvent event,
                              @AppOption(description = "The status to set") @Nullable String status,
                              @AppOption(description = "The type of status to set") @Nullable String type) {
        if (!cobalt.config.checkIsOwner(event)) return;

        // Reset the status
        if (status == null && type == null) {
            cobalt.startStatuses();
            cobalt.data.global.activity = null;
            event.reply("Reset the status to the pre-defined random ones").setEphemeral(true).queue();
            return;
        }
        final Presence presence = event.getJDA().getPresence();

        // Get the status
        final String statusValue;
        if (status == null) {
            final Activity activity = presence.getActivity();
            if (activity == null) {
                event.reply("You must specify a status!").setEphemeral(true).queue();
                return;
            }
            statusValue = activity.getName();
        } else {
            statusValue = status;
        }

        // Get the type
        Activity.ActivityType typeValue = Activity.ActivityType.PLAYING;
        if (type == null) {
            final Activity activity = presence.getActivity();
            if (activity != null) typeValue = activity.getType();
        } else {
            typeValue = Activity.ActivityType.valueOf(type);
        }
        final Activity activity = Activity.of(typeValue, statusValue);

        // Stop the random statuses
        if (cobalt.statusTask != null) {
            cobalt.statusTask.cancel(false);
            cobalt.statusTask = null;
        }

        // Set the status
        presence.setActivity(activity);
        cobalt.data.global.activity = activity;

        // Reply
        event.reply("Set the status to `" + typeValue.name() + " " + statusValue + "`").setEphemeral(true).queue();

        // Log
        cobalt.config.sendLog("status", "**Status:** " + statusValue + "\n**Type:** `" + typeValue.name() + "`\n**User:** " + event.getUser().getAsMention());
    }

    @Override @NotNull
    public List<Command.Choice> getOptionChoices(@Nullable Guild guild, @NotNull CommandPath commandPath, int optionIndex) {
        if (optionIndex == 1) return List.of(
                new Command.Choice("Playing ...", "PLAYING"),
                new Command.Choice("Listening to ...", "LISTENING"),
                new Command.Choice("Watching ...", "WATCHING"));
        return Collections.emptyList();
    }
}
