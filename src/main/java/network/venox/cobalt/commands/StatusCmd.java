package network.venox.cobalt.commands;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.managers.Presence;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.command.CoExecutableCommand;
import network.venox.cobalt.events.CoSlashCommandInteractionEvent;

import org.jetbrains.annotations.NotNull;

import java.util.List;


public class StatusCmd extends CoExecutableCommand {
    public StatusCmd(@NotNull Cobalt cobalt) {
        super(cobalt);
    }

    @Override @NotNull
    public String description() {
        return "Sets the custom status of the bot";
    }

    @Override @NotNull
    public List<OptionData> options() {
        return List.of(
                new OptionData(OptionType.STRING, "status", "The status to set", false),
                new OptionData(OptionType.STRING, "type", "The type of status to set", false)
                        .addChoice("Playing ...", "PLAYING")
                        .addChoice("Listening to ...", "LISTENING")
                        .addChoice("Watching ...", "WATCHING"));
    }

    @Override
    public void onCommand(@NotNull CoSlashCommandInteractionEvent event) {
        final OptionMapping statusOption = event.getOption("status");
        final OptionMapping typeOption = event.getOption("type");

        // Reset the status
        if (statusOption == null && typeOption == null) {
            cobalt.startStatuses();
            cobalt.data.global.activity = null;
            event.reply("Reset the status to the pre-defined random ones").setEphemeral(true).queue();
            return;
        }
        final Presence presence = event.getJDA().getPresence();

        // Get the status
        final String status;
        if (statusOption == null) {
            final Activity activity = presence.getActivity();
            if (activity == null) {
                event.reply("You must specify a status!").setEphemeral(true).queue();
                return;
            }
            status = activity.getName();
        } else {
            status = statusOption.getAsString();
        }

        // Get the type
        Activity.ActivityType type = Activity.ActivityType.PLAYING;
        if (typeOption == null) {
            final Activity activity = presence.getActivity();
            if (activity != null) type = activity.getType();
        } else {
            type = Activity.ActivityType.valueOf(typeOption.getAsString());
        }
        final Activity activity = Activity.of(type, status);

        // Stop the random statuses
        if (cobalt.statusTask != null) {
            cobalt.statusTask.cancel(false);
            cobalt.statusTask = null;
        }

        // Set the status
        presence.setActivity(activity);
        cobalt.data.global.activity = activity;

        // Reply
        event.reply("Set the status to `" + type.name() + " " + status + "`").setEphemeral(true).queue();

        // Log
        cobalt.config.sendLog("status", "**Status:** " + status + "\n**Type:** `" + type.name() + "`\n**User:** " + event.getUser().getAsMention());
    }
}
