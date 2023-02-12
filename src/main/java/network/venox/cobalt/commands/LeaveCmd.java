package network.venox.cobalt.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.objects.CoEmbed;
import network.venox.cobalt.command.CoExecutableCommand;
import network.venox.cobalt.events.CoCommandAutoCompleteInteractionEvent;
import network.venox.cobalt.events.CoSlashCommandInteractionEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class LeaveCmd extends CoExecutableCommand {
    public LeaveCmd(@NotNull Cobalt cobalt) {
        super(cobalt);
    }

    @Override @NotNull
    public String description() {
        return "Leaves the specified server";
    }

    @Override @NotNull
    public List<OptionData> options() {
        return Collections.singletonList(new OptionData(OptionType.STRING, "server", "The ID of the server to leave", true, true));
    }

    @Override
    public void onCommand(@NotNull CoSlashCommandInteractionEvent event) {
        final OptionMapping serverOption = event.getOption("server");
        if (serverOption == null) {
            event.replyEmbeds(CoEmbed.invalidArgument("server").build()).setEphemeral(true).queue();
            return;
        }

        final Guild guild = cobalt.jda.getGuildById(serverOption.getAsLong());
        if (guild == null) {
            event.replyEmbeds(CoEmbed.invalidArgument(serverOption.getAsString()).build()).setEphemeral(true).queue();
            return;
        }

        guild.leave().queue();

        // Message
        event.replyEmbeds(new CoEmbed(CoEmbed.Type.SUCCESS)
                .setTitle("%type%Left server")
                .setDescription("Left server **" + guild.getName() + "** (`" + guild.getId() + "`)")
                .build()).setEphemeral(true).queue();

        // Log
        cobalt.config.sendLog("left guild", "**Guild:** " + guild.getName() + " (`" + guild.getId() + "`)\n**Executor:** " + event.getUser().getAsMention());
    }

    @Override @Nullable
    public Collection<Command.Choice> onAutoComplete(@NotNull CoCommandAutoCompleteInteractionEvent event) {
        if (event.getFocusedOption().getName().equals("server")) return cobalt.jda.getGuilds().stream()
                .map(guild -> new Command.Choice(guild.getName(), guild.getId()))
                .collect(Collectors.toSet());
        return null;
    }
}
