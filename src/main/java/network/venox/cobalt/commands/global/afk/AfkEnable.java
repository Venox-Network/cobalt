package network.venox.cobalt.commands.global.afk;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;

import net.dv8tion.jda.api.entities.User;

import network.venox.cobalt.CoConfig;
import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.CoUser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.emoji.LazyEmoji;


@Command
public class AfkEnable {
    @NotNull private final CoConfig config;
    @NotNull private final MongoProvider mongo;

    public AfkEnable(@NotNull CoConfig config, @NotNull MongoProvider mongo) {
        this.config = config;
        this.mongo = mongo;
    }

    @JDASlashCommand(
            name = "afk",
            subcommand = "enable",
            description = "Enable your AFK status")
    public void enable(@NotNull GlobalSlashEvent event,
                       @SlashOption(description = "OWNER | The user to toggle AFK for") @Nullable User user) {
        if (user != null && !config.checkIsOwner(event)) return;
        final User author = event.getUser();
        if (user == null) user = author;
        mongo.database.getMagicCollection(CoUser.class).upsertOne(
                Filters.eq("_id", user.getIdLong()),
                Updates.set(CoUser.PROP_AFK, true));
        event.reply(LazyEmoji.YES + " " + (user == author ? "You" : user.getAsMention()) + " are now AFK").setEphemeral(true).queue();
    }
}
