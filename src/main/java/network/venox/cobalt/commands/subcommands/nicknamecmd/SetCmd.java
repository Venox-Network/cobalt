package network.venox.cobalt.commands.subcommands.nicknamecmd;

import net.dv8tion.jda.api.entities.Guild;
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


public class SetCmd extends CoSubCommand {
    public SetCmd(@NotNull CoCommand parent) {
        super(parent);
    }

    @Override @NotNull
    public String description() {
        return "Sets the nickname a user will get when they use a blacklisted phrase in their nickname";
    }

    @Override @NotNull
    public List<OptionData> options() {
        return Collections.singletonList(new OptionData(OptionType.STRING, "nickname", "The nickname you want to set", false));
    }

    @Override
    public void onCommand(@NotNull CoSlashCommandInteractionEvent event) {
        final Guild guild = event.getGuild();
        final CoGuild coGuild = event.getCoGuild();
        if (guild == null || coGuild == null) return;
        final OptionMapping nicknameOption = event.getOption("nickname");

        // Set moderatedNickname
        coGuild.moderatedNickname = nicknameOption == null ? null : nicknameOption.getAsString();

        // Reply
        event.reply("Set the moderated nickname to `" + coGuild.getModeratedNickname() + "` for **" + guild.getName() + "**").setEphemeral(true).queue();
    }
}
