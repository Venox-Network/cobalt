package network.venox.cobalt.commands.guild.corner;

import com.mongodb.client.model.Filters;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.components.Buttons;
import io.github.freya022.botcommands.api.components.SelectMenus;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.Corner;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.LazyEmoji;


@Command
public class CornerMenu extends ApplicationCommand {
    @NotNull private final MongoProvider mongo;
    @NotNull private final Buttons buttons;
    @NotNull private final SelectMenus menus;

    public CornerMenu(@NotNull MongoProvider mongo, @NotNull Buttons buttons, @NotNull SelectMenus menus) {
        this.mongo = mongo;
        this.buttons = buttons;
        this.menus = menus;
    }

    @JDASlashCommand(
            name = "corner",
            subcommand = "menu",
            description = "Access the menu for a Corner")
    public void cornerMenu(@NotNull GuildSlashEvent event) {
        // Get Corner
        final GuildMessageChannel channel = event.getGuildChannel();
        final Corner corner = mongo.database.getMagicCollection(Corner.class)
                .findOne(Filters.eq("_id", channel.getIdLong()))
                .orElse(null);
        if (corner == null) {
            event.reply(LazyEmoji.NO + " This command can only be used in a Corner channel!").setEphemeral(true).queue();
            return;
        }

        // Check if Corner owner
        final Member member = event.getMember();
        if (corner.owner != member.getIdLong() && !member.hasPermission(channel, Permission.MANAGE_PERMISSIONS)) {
            event.reply(LazyEmoji.NO + " Only the owner of this Corner can access the menu!").setEphemeral(true).queue();
            return;
        }

        // Reply with menu
        event.replyComponents(corner.getComponents(buttons, menus, channel, null, null, null)).setEphemeral(true).queue();
    }
}
