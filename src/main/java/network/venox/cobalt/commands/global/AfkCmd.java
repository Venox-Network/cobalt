package network.venox.cobalt.commands.global;

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
public class AfkCmd extends ApplicationCommand {
    @Dependency private Cobalt bot;

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "afk",
            subcommand = "get",
            description = "Get the AFK status of a user")
    public void get(@NotNull GlobalSlashEvent event,
                    @AppOption(description = "The user to get the AFK status of") @Nullable User user) {
        final User author = event.getUser();
        if (user == null) user = author;
        final String entity = user == author ? "You are" : user.getAsMention();

        // AFK
        if (!bot.dataManager.mongo.getMagicCollection(CoUser.class).findOne("_id", user.getIdLong())
                .map(CoUser::afk)
                .orElse(false)) {
            event.reply(LazyEmoji.YES + " " + entity + " is AFK").setEphemeral(true).queue();
            return;
        }

        // Not AFK
        event.reply(LazyEmoji.NO + " " + entity + " is not AFK").setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "afk",
            subcommand = "enable",
            description = "Enable your AFK status")
    public void enable(@NotNull GlobalSlashEvent event,
                       @AppOption(description = "OWNER | The user to toggle AFK for") @Nullable User user) {
        if (user != null && !bot.config.checkIsOwner(event)) return;
        final User author = event.getUser();
        if (user == null) user = author;
        bot.dataManager.mongo.getMagicCollection(CoUser.class).upsertOne(
                Filters.eq("_id", user.getIdLong()),
                Updates.set(CoUser.PROP_AFK, true));
        event.reply(LazyEmoji.YES + " " + (user == author ? "You" : user.getAsMention()) + " are now AFK").setEphemeral(true).queue();
    }

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
        bot.dataManager.mongo.getMagicCollection(CoUser.class).updateOne(
                Filters.eq("_id", user.getIdLong()),
                Updates.set(CoUser.PROP_AFK, false));
        event.reply(LazyEmoji.YES + " " + (user == author ? "You" : user.getAsMention()) + " is no longer AFK").setEphemeral(true).queue();
    }
}
