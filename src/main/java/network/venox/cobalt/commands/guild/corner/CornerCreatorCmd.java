package network.venox.cobalt.commands.guild.corner;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.annotations.UserPermissions;
import io.github.freya022.botcommands.api.commands.application.CommandScope;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData;

import net.dv8tion.jda.api.Permission;

import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.CornerCreator;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.LazyEmoji;


@Command
public class CornerCreatorCmd {
    @NotNull private final MongoProvider mongo;

    public CornerCreatorCmd(@NotNull MongoProvider mongo) {
        this.mongo = mongo;
    }

    @TopLevelSlashCommandData(scope = CommandScope.GUILD)
    @UserPermissions(Permission.MANAGE_CHANNEL)
    @JDASlashCommand(
            name = "corner",
            subcommand = "creator",
            description = "Create a new Corner Creator channel")
    public void cornerSet(@NotNull GuildSlashEvent event) {
        event.deferReply(true).queue();
        event.getGuild().createVoiceChannel("Corner Creator")
                .flatMap(channel -> {
                    mongo.database.getMagicCollection(CornerCreator.class).insertOne(new CornerCreator(channel));
                    return event.getHook().editOriginal(LazyEmoji.YES + " Created new corner creator channel " + channel.getAsMention());
                }).queue();
    }
}
