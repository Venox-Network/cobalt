package network.venox.cobalt.commands.guild.muterole;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.annotations.UserPermissions;
import io.github.freya022.botcommands.api.commands.application.CommandScope;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData;

import net.dv8tion.jda.api.Permission;

import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.Server;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.LazyEmoji;


@Command
public class MuteRoleGet {
    @NotNull private final MongoProvider mongo;

    public MuteRoleGet(@NotNull MongoProvider mongo) {
        this.mongo = mongo;
    }

    @TopLevelSlashCommandData(scope = CommandScope.GUILD)
    @UserPermissions({Permission.MANAGE_ROLES, Permission.MODERATE_MEMBERS})
    @JDASlashCommand(
            name = "muterole",
            subcommand = "get",
            description = "Get the mute role for the guild")
    public void getCommand(@NotNull GuildSlashEvent event) {
        final Long muteRole = mongo.database.getMagicCollection(Server.class)
                .findOne("_id", event.getGuild().getIdLong())
                .map(server -> server.muteRole)
                .orElse(null);

        // No mute role
        if (muteRole == null) {
            event.reply(LazyEmoji.NO + " There isn't a mute role configured!").setEphemeral(true).queue();
            return;
        }

        // Get mute role
        event.reply(LazyEmoji.YES + " <@&" + muteRole + "> is the current mute role").setEphemeral(true).queue();
    }
}
