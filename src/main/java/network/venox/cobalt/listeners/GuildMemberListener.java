package network.venox.cobalt.listeners;

import io.github.freya022.botcommands.api.core.annotations.BEventListener;
import io.github.freya022.botcommands.api.core.service.annotations.BService;

import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;

import network.venox.cobalt.CoConfig;
import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.Server;

import org.jetbrains.annotations.NotNull;


@BService
public record GuildMemberListener(@NotNull CoConfig config, @NotNull MongoProvider mongo) {
    @BEventListener
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        mongo.database.getMagicCollection(Server.class)
                .findOne("_id", event.getGuild().getIdLong())
                .ifPresent(server -> server.sendWelcomeMessage(config, event.getUser()));
    }
}
