package network.venox.cobalt.commands.global;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.components.Components;

import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import network.venox.cobalt.Cobalt;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


@CommandMarker
public class SupportCmd extends ApplicationCommand {
    @Dependency private Cobalt cobalt;

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "support",
            description = "Get Venox Network and/or Cobalt support")
    public void onCommand(@NotNull GlobalSlashEvent event) {
        // Get buttons
        final List<Button> buttons = new ArrayList<>();
        final String server = cobalt.config.guildInvite;
        buttons.add(Components.primaryButton(buttonEvent -> cobalt.data.global.sendModmailConfirmation(buttonEvent.getUser(), new MessageCreateBuilder().setContent("*Opened via `/support`*"))
                .flatMap(message -> buttonEvent.editMessage("**Modmail creation confirmation:** " + message.getJumpUrl()).setComponents(List.of()))
                .queue()).build("Modmail"));
        if (server != null) buttons.add(Button.link(server, "Support Server"));

        // Send message
        event.reply("Do you want to create a new modmail thread or join the support server?\n*If you don't see one of the buttons, it's because that option is not available.*")
                .addActionRow(buttons)
                .setEphemeral(true)
                .queue();
    }
}
