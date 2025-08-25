package network.venox.cobalt.commands.global.afk;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;

import net.dv8tion.jda.api.entities.User;

import network.venox.cobalt.CoConfig;
import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.CoUser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.LazyEmoji;


@Command
public class AfkDisable extends ApplicationCommand {
    @NotNull private final CoConfig config;
    @NotNull private final MongoProvider mongo;

    public AfkDisable(@NotNull CoConfig config, @NotNull MongoProvider mongo) {
        this.config = config;
        this.mongo = mongo;
    }

    @JDASlashCommand(
            name = "afk",
            subcommand = "disable",
            description = "Disable your AFK status")
    public void disable(@NotNull GlobalSlashEvent event,
                        @SlashOption(description = "OWNER | The user to toggle AFK for") @Nullable User user) {
        if (user != null && !config.checkIsOwner(event)) return;
        final User author = event.getUser();
        if (user == null) user = author;
        mongo.database.getMagicCollection(CoUser.class).updateOne(
                Filters.eq("_id", user.getIdLong()),
                Updates.set(CoUser.PROP_AFK, false));
        event.reply(LazyEmoji.YES + " " + (user == author ? "You" : user.getAsMention()) + " is no longer AFK").setEphemeral(true).queue();
    }
}
