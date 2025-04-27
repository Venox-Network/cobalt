package network.venox.cobalt.commands.global;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.components.Components;
import com.freya02.botcommands.api.utils.ButtonContent;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.objects.CoModmail;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


@CommandMarker
public class SupportCmd extends ApplicationCommand {
    @Dependency private Cobalt bot;

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "support",
            description = "Get Venox Network and/or Cobalt support")
    public void onCommand(@NotNull GlobalSlashEvent event) {
        // Get buttons
        final List<Button> buttons = new ArrayList<>();
        buttons.add(Components.primaryButton(buttonEvent -> CoModmail.sendModmailConfirmation(bot, event.getUser(), null)
                .flatMap(message -> buttonEvent.editMessage("**Modmail creation confirmation:** " + message.getJumpUrl()).setComponents(List.of()))
                .queue()).build(new ButtonContent("Modmail", Emoji.fromUnicode("\uD83C\uDFAB"))));
        final String invite = bot.config.guild.invite;
        if (invite != null) buttons.add(Button.link(invite, "Support Server"));

        // Send message
        event.reply("Do you want to create a new modmail thread or join the support server?")
                .addActionRow(buttons)
                .setEphemeral(true)
                .queue();
    }
}
