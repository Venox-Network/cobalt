package network.venox.cobalt.events;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;


public class GuildVoiceLeaveEvent extends GuildVoiceUpdateEvent {
    public GuildVoiceLeaveEvent(@NotNull JDA api, long responseNumber, @NotNull Member member, @Nullable AudioChannel previous) {
        super(api, responseNumber, member, previous);
    }

    public GuildVoiceLeaveEvent(@NotNull GuildVoiceUpdateEvent event) {
        this(event.getJDA(), event.getResponseNumber(), event.getMember(), event.getChannelLeft());
    }

    @Override @NotNull
    public AudioChannelUnion getChannelLeft() {
        return Objects.requireNonNull(super.getChannelLeft());
    }

    @Override @Nullable
    public AudioChannelUnion getChannelJoined() {
        return null;
    }

    @Override @NotNull
    public AudioChannel getOldValue() {
        return Objects.requireNonNull(super.getOldValue());
    }

    @Override @Nullable
    public AudioChannel getNewValue() {
        return null;
    }
}
