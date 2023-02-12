package network.venox.cobalt.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import network.venox.cobalt.data.objects.CoEmbed;
import network.venox.cobalt.Cobalt;
import network.venox.cobalt.events.CoCommandAutoCompleteInteractionEvent;
import network.venox.cobalt.events.CoSlashCommandInteractionEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;


public abstract class CoExecutableCommand extends CoCommand {
    public CoExecutableCommand(@NotNull Cobalt cobalt) {
        super(cobalt);
    }

    @Override @NotNull
    public SlashCommandData toSlashCommandData() {
        return super.toSlashCommandData().addOptions(options());
    }

    @Override @NotNull
    public String toString() {
        // Create builder with full name
        final StringBuilder builder = new StringBuilder(fullName());
        // Add arguments
        for (final OptionData option : options()) {
            builder.append(" ");
            if (option.isRequired()) {
                builder.append("<").append(option.getName()).append(">"); // Required argument
            } else {
                builder.append("[").append(option.getName()).append("]"); // Optional argument
            }
        }
        // Return
        return builder.toString();
    }

    @NotNull
    public List<OptionData> options() {
        return Collections.emptyList();
    }

    public abstract void onCommand(@NotNull CoSlashCommandInteractionEvent event);

    @Nullable
    public Collection<Command.Choice> onAutoComplete(@NotNull CoCommandAutoCompleteInteractionEvent event) {
        return null;
    }

    public boolean checkOwner(@NotNull SlashCommandInteractionEvent event) {
        if (!ownerOnly()) return true;
        final boolean isOwner = cobalt.config.owners.contains(event.getUser().getIdLong());
        if (!isOwner) event.replyEmbeds(CoEmbed.NO_PERMISSION.build()).setEphemeral(true).queue();
        return isOwner;
    }
}
