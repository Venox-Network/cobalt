package network.venox.cobalt.events;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;


public class GuildVoiceJoinEvent extends GuildVoiceUpdateEvent {
    public GuildVoiceJoinEvent(@NotNull JDA api, long responseNumber, @NotNull Member member, @Nullable AudioChannel previous) {
        super(api, responseNumber, member, previous);
    }

    public GuildVoiceJoinEvent(@NotNull GuildVoiceUpdateEvent event) {
        this(event.getJDA(), event.getResponseNumber(), event.getMember(), event.getChannelLeft());
    }

    @Override @Nullable
    public AudioChannelUnion getChannelLeft() {
        return null;
    }

    @Override @NotNull
    public AudioChannelUnion getChannelJoined() {
        return Objects.requireNonNull(super.getChannelJoined());
    }

    @Override @Nullable
    public AudioChannel getOldValue() {
        return null;
    }

    @Override @NotNull
    public AudioChannel getNewValue() {
        return Objects.requireNonNull(super.getNewValue());
    }
}
