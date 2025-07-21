package network.venox.cobalt.listeners;

import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;

import network.venox.cobalt.CoListener;
import network.venox.cobalt.Cobalt;
import network.venox.cobalt.mongo.Server;

import org.jetbrains.annotations.NotNull;


public class GuildMemberListener extends CoListener {
    public GuildMemberListener(@NotNull Cobalt cobalt) {
        super(cobalt);
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        bot.mongo.getMagicCollection(Server.class)
                .findOne("_id", event.getGuild().getIdLong())
                .ifPresent(server -> server.sendWelcomeMessage(bot, event.getUser()));
    }
}
