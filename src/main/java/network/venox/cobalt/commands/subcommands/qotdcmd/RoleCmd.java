package network.venox.cobalt.commands.subcommands.qotdcmd;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import network.venox.cobalt.command.CoCommand;
import network.venox.cobalt.command.CoSubCommand;
import network.venox.cobalt.data.CoGuild;
import network.venox.cobalt.events.CoSlashCommandInteractionEvent;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;


public class RoleCmd extends CoSubCommand {
    public RoleCmd(@NotNull CoCommand parent) {
        super(parent);
    }

    @Override @NotNull
    public String description() {
        return "Sets the role that will be pinged for QOTD";
    }

    @Override @NotNull
    public List<OptionData> options() {
        return Collections.singletonList(new OptionData(OptionType.ROLE, "role", "The role to ping for QOTD. Leave empty to remove QOTD"));
    }

    @Override
    public void onCommand(@NotNull CoSlashCommandInteractionEvent event) {
        final Guild guild = event.getGuild();
        final CoGuild coGuild = event.getCoGuild();
        if (guild == null || coGuild == null) return;
        final OptionMapping roleOption = event.getOption("role");

        // Remove the QOTD role
        if (roleOption == null) {
            coGuild.qotdRole = null;
            event.reply("The QOTD role for **" + guild.getName() + "** has been removed").setEphemeral(true).queue();
            return;
        }

        // Set the QOTD role
        final Role role = roleOption.getAsRole();
        coGuild.qotdRole = role.getIdLong();
        event.reply("The QOTD role for **" + guild.getName() + "** has been set to " + role.getAsMention()).setEphemeral(true).queue();
    }
}
