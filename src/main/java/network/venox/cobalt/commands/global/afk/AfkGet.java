package network.venox.cobalt.commands.global.afk;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.CommandScope;
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData;

import net.dv8tion.jda.api.entities.User;

import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.CoUser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.LazyEmoji;


@Command
public class AfkGet {
    @NotNull private final MongoProvider mongo;

    public AfkGet(@NotNull MongoProvider mongo) {
        this.mongo = mongo;
    }

    @TopLevelSlashCommandData(scope = CommandScope.GLOBAL)
    @JDASlashCommand(
            name = "afk",
            subcommand = "get",
            description = "Get the AFK status of a user")
    public void get(@NotNull GlobalSlashEvent event,
                    @SlashOption(description = "The user to get the AFK status of") @Nullable User user) {
        final User author = event.getUser();
        if (user == null) user = author;
        final String entity = user == author ? "You are" : user.getAsMention();

        // AFK
        if (!mongo.database.getMagicCollection(CoUser.class).findOne("_id", user.getIdLong())
                .map(coUser -> coUser.afk)
                .orElse(false)) {
            event.reply(LazyEmoji.YES + " " + entity + " is AFK").setEphemeral(true).queue();
            return;
        }

        // Not AFK
        event.reply(LazyEmoji.NO + " " + entity + " is not AFK").setEphemeral(true).queue();
    }
}
