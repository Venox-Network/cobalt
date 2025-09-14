package network.venox.cobalt.commands.guild.corner;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import net.dv8tion.jda.api.Permission;

import network.venox.cobalt.Cobalt;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.LazyEmoji;


@CommandMarker @UserPermissions(Permission.MANAGE_CHANNEL)
public class CornerCreator extends ApplicationCommand {
    @Dependency private Cobalt bot;

    @JDASlashCommand(
            name = "corner",
            subcommand = "creator",
            description = "Create a new Corner Creator channel")
    public void cornerSet(@NotNull GuildSlashEvent event) {
        event.deferReply(true)
                .flatMap(hook -> event.getGuild().createVoiceChannel("Corner Creator")
                        .flatMap(channel -> {
                            bot.mongo.getMagicCollection(network.venox.cobalt.mongo.CornerCreator.class).insertOne(new network.venox.cobalt.mongo.CornerCreator(channel));
                            return hook.editOriginal(LazyEmoji.YES + " Created new corner creator channel " + channel.getAsMention());
                        }))
                .queue();
    }
}
