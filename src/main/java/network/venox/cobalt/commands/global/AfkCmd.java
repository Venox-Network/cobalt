package network.venox.cobalt.commands.global;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import net.dv8tion.jda.api.entities.User;

import network.venox.cobalt.Cobalt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.LazyEmoji;


@CommandMarker
public class AfkCmd extends ApplicationCommand {
    @Dependency private Cobalt bot;

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "afk",
            subcommand = "get",
            description = "Get the AFK status of a user")
    public void get(@NotNull GlobalSlashEvent event,
                    @AppOption(description = "The user to get the AFK status of") @Nullable User user) {
        final User eventUser = event.getUser();
        final String entity = user == null || user == eventUser ? "You are" : user.getAsMention() + " is";
        if (user == null) user = eventUser;
        if (!bot.oldData.getUser(user).afk()) {
            event.reply(LazyEmoji.NO + " " + entity + " not AFK").setEphemeral(true).queue();
            return;
        }
        event.reply(LazyEmoji.YES + " " + entity + " AFK").setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "afk",
            subcommand = "enable",
            description = "Enable your AFK status")
    public void set(@NotNull GlobalSlashEvent event,
                    @AppOption(description = "The user to toggle AFK for (permission required)") @Nullable User user) {
        if (user != null && !bot.config.checkIsOwner(event)) return;
        bot.oldData.getUser(user == null ? event.getUser() : user).afk = true;
        event.reply(LazyEmoji.YES + " " + (user == null ? "You" : user.getAsMention()) + " are now AFK").setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "afk",
            subcommand = "disable",
            description = "Disable your AFK status")
    public void disable(@NotNull GlobalSlashEvent event,
                        @AppOption(description = "The user to toggle AFK for (permission required)") @Nullable User user) {
        if (user != null && !bot.config.checkIsOwner(event)) return;
        bot.oldData.getUser(user == null ? event.getUser() : user).afk = false;
        event.reply(LazyEmoji.YES + " " + (user == null ? "You" : user.getAsMention()) + " is no longer AFK").setEphemeral(true).queue();
    }
}
