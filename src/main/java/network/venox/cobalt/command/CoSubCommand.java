package network.venox.cobalt.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import network.venox.cobalt.data.objects.CoEmbed;

import org.jetbrains.annotations.NotNull;


public abstract class CoSubCommand extends CoExecutableCommand {
    @NotNull private final CoCommand parent;

    public CoSubCommand(@NotNull CoCommand parent) {
        super(parent.cobalt);
        this.parent = parent;
    }

    @NotNull
    public <T> T getParent(@NotNull Class<T> clazz) {
        return clazz.cast(parent);
    }

    public SubcommandData toSubcommandData() {
        return new SubcommandData(name(), description()).addOptions(options());
    }

    @Override @NotNull
    public String fullName() {
        return parent.fullName() + " " + name();
    }

    @Override
    public boolean checkOwner(@NotNull SlashCommandInteractionEvent event) {
        if (!ownerOnly() && !parent.ownerOnly()) return true;
        final boolean isOwner = cobalt.config.owners.contains(event.getUser().getIdLong());
        if (!isOwner) event.replyEmbeds(CoEmbed.NO_PERMISSION.build()).setEphemeral(true).queue();
        return isOwner;
    }
}
