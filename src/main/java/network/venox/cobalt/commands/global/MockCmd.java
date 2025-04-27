package network.venox.cobalt.commands.global;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import network.venox.cobalt.Cobalt;

import org.jetbrains.annotations.NotNull;


@CommandMarker
public class MockCmd extends ApplicationCommand {
    @Dependency private Cobalt cobalt;

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "mock",
            description = "Convert normal text to mock text (randomly capitalize)")
    public void mock(@NotNull GlobalSlashEvent event,
                     @AppOption(description = "The text to mock") @NotNull String text) {
        event.reply(mockify(text)).setEphemeral(true).queue();
    }

    @NotNull
    public static String mockify(@NotNull String text) {
        return text.chars()
                .mapToObj(c -> (char) c)
                .map(c -> Cobalt.RANDOM.nextBoolean() ? Character.toUpperCase(c) : Character.toLowerCase(c))
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }
}
