package network.venox.cobalt.commands.guild.welcome;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.annotations.UserPermissions;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;

import net.dv8tion.jda.api.Permission;

import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.Server;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.LazyEmoji;


@Command
public class WelcomeRemove {
    @NotNull private final MongoProvider mongo;

    public WelcomeRemove(@NotNull MongoProvider mongo) {
        this.mongo = mongo;
    }

    @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MANAGE_SERVER})
    @JDASlashCommand(
            name = "welcome",
            subcommand = "remove",
            description = "Remove the welcome channel")
    public void welcomeCommand(@NotNull GuildSlashEvent event) {
        mongo.database.getMagicCollection(Server.class).upsertOne(
                Filters.eq("_id", event.getGuild().getIdLong()),
                Updates.unset(Server.PROP_WELCOME_CHANNEL));
        event.reply(LazyEmoji.YES + " Welcome channel has been removed").setEphemeral(true).queue();
    }
}
