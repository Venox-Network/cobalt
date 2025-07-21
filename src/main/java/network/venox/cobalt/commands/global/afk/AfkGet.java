package network.venox.cobalt.commands.global.afk;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import net.dv8tion.jda.api.entities.User;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.mongo.CoUser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.LazyEmoji;


@CommandMarker
public class AfkGet extends ApplicationCommand {
    @Dependency private Cobalt bot;

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "afk",
            subcommand = "get",
            description = "Get the AFK status of a user")
    public void get(@NotNull GlobalSlashEvent event,
                    @AppOption(description = "The user to get the AFK status of") @Nullable User user) {
        final User author = event.getUser();
        if (user == null) user = author;
        final String entity = user == author ? "You are" : user.getAsMention();

        // AFK
        if (!bot.mongo.getMagicCollection(CoUser.class).findOne("_id", user.getIdLong())
                .map(coUser -> coUser.afk)
                .orElse(false)) {
            event.reply(LazyEmoji.YES + " " + entity + " is AFK").setEphemeral(true).queue();
            return;
        }

        // Not AFK
        event.reply(LazyEmoji.NO + " " + entity + " is not AFK").setEphemeral(true).queue();
    }
}
