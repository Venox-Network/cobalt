package network.venox.cobalt.commands.global.afk;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import net.dv8tion.jda.api.entities.User;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.mongo.CoUser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.LazyEmoji;


@CommandMarker
public class AfkDisable extends ApplicationCommand {
    @Dependency private Cobalt bot;

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "afk",
            subcommand = "disable",
            description = "Disable your AFK status")
    public void disable(@NotNull GlobalSlashEvent event,
                        @AppOption(description = "OWNER | The user to toggle AFK for") @Nullable User user) {
        if (user != null && !bot.config.checkIsOwner(event)) return;
        final User author = event.getUser();
        if (user == null) user = author;
        bot.mongo.getMagicCollection(CoUser.class).updateOne(
                Filters.eq("_id", user.getIdLong()),
                Updates.set(CoUser.PROP_AFK, false));
        event.reply(LazyEmoji.YES + " " + (user == author ? "You" : user.getAsMention()) + " is no longer AFK").setEphemeral(true).queue();
    }
}
