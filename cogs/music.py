import nextcord
from nextcord import Interaction, SlashOption, ChannelType, slash_command
from nextcord.abc import GuildChannel
from nextcord.ext import commands
import wavelink


class music(commands.Cog):

    def __init__(self, bot):
        self.bot = bot
        bot.loop.create_task(self.node_connect())

    async def node_connect(self):
        await self.bot.wait_until_ready()
        await wavelink.NodePool.create_node(bot=self.bot,
                                            host='lavalink.houseofgamers.xyz',
                                            port=2333,
                                            password='lavalink')

    @commands.Cog.listener()
    async def on_wavelink_node_ready(self, node: wavelink.Node):
        print(f"node {node.identifier} is ready!!!")

    @slash_command()
    async def play(self, interaction: Interaction,
                   channel: GuildChannel = SlashOption(channel_types=[ChannelType.voice],
                                                       description="Voice channel countrol"), *,
                   search: str = SlashOption(description='Song name')):
        search = await wavelink.YouTubeTrack.search(query=search, return_first=True)
        if not interaction.guild.voice_client:
            vc: wavelink.Player = await channel.connect(cls=wavelink.Player)
        elif not getattr(interaction.user.voice, "channel", None):
            return await interaction.response.send_message("Join a voice channel  first")
        else:
            vc: wavelink.Player() = interaction.guild.voice_client

        if vc.queue.is_empty and not vc.is_playing():
            await vc.play(search)
            await interaction.guild.change_voice_state(channel=channel, self_mute=False, self_deaf=True)
            await interaction.send(f"Now playing `{search.title}`")
        else:
            await vc.queue.put_wait(search)
            await interaction.guild.change_voice_state(channel=channel, self_mute=False, self_deaf=True)
            await interaction.response.send_message(f"added `{search.title}` to the queue")
        vc.interaction = interaction
        setattr(vc, "loop", False)

    @slash_command()
    async def pause(self, interaction: Interaction):
        if not interaction.guild.voice_client:
            return await interaction.send("Your not playing any music")
        elif not getattr(interaction.user.voice, "channel", None):
            return await interaction.send("Join a voice channel  first")
        else:
            vc: wavelink.Player() = interaction.guild.voice_client

        await vc.pause()
        await interaction.send("Paused the music")

    @slash_command()
    async def resume(self, interaction: Interaction):
        if not interaction.guild.voice_client:
            return await interaction.send("No music is paused")
        elif not getattr(interaction.user.voice, "channel", None):
            return await interaction.send("Join a voice channel  first")
        else:
            vc: wavelink.Player() = interaction.guild.voice_client

        await vc.resume()
        await interaction.send("Resumed the music :>")

    @slash_command()
    async def stop(self, interaction: Interaction):
        if not interaction.guild.voice_client:
            return await interaction.send("No music is playing")
        elif not getattr(interaction.user.voice, "channel", None):
            return await interaction.send("Join a voice channel first")
        else:
            vc: wavelink.Player() = interaction.guild.voice_client

        setattr(vc, "loop", False)
        await vc.stop()
        await interaction.send("Stopped the music D:")

    @slash_command()
    async def skip(self, interaction: Interaction):
        if not interaction.guild.voice_client:
            return await interaction.send("No music is playing")
        elif not getattr(interaction.user.voice, "channel", None):
            return await interaction.send("Join a voice channel first")
        else:
            vc: wavelink.Player() = interaction.guild.voice_client

        setattr(vc, "loop", False)
        await vc.stop()
        await interaction.send("Skipped the music B)")

    @slash_command()
    async def disconnect(self, interaction: Interaction):
        if not interaction.guild.voice_client:
            return await interaction.send("No music is playing")
        elif not getattr(interaction.user.voice, "channel", None):
            return await interaction.send("Join a voice channel first")
        else:
            vc: wavelink.Player() = interaction.guild.voice_client

        await vc.disconnect()
        await interaction.send("Disconnected")

    @commands.Cog.listener()
    async def on_wavelink_track_end(self, player: wavelink.Player, track: wavelink.Track, reason="None"):
        interaction = player.interaction
        vc: player = interaction.guild.voice_client

        if vc.loop:
            return await vc.play(track)

        try:
            next_song = vc.queue.get()
            await vc.play(next_song)
            await interaction.send(f"Now playing `{next_song.title}`")
        except:
            await vc.disconnect()

    @slash_command()
    async def loop(self, interaction: Interaction):
        if not interaction.guild.voice_client:
            return await interaction.send("No music is playing")
        elif not getattr(interaction.user.voice, "channel", None):
            return await interaction.send("Join a voice channel  first")
        else:
            vc: wavelink.Player() = interaction.guild.voice_client
        try:
            vc.loop ^= True
        except Exception:
            setattr(vc, "loop", False)

        if vc.loop:
            return await interaction.send("Loop is now enabled")
        else:
            return await interaction.send("Loop is now disabled")

    @slash_command()
    async def queue(self, interaction: Interaction):
        if not interaction.guild.voice_client:
            return await interaction.send("No music is playing")
        elif not getattr(interaction.user.voice, "channel", None):
            return await interaction.send("Join a voice channel first")
        else:
            vc: wavelink.Player() = interaction.guild.voice_client

        if vc.queue.is_empty and not vc.is_playing:
            return await interaction.send("Queue is empty")
        else:
            embed = nextcord.Embed(title="Queue")
            song_count = 0
            queue = vc.queue.copy()
            for song in queue:
                song_count += 1
                embed.add_field(name=f"Song Num {song_count}", value=f"{song.title}")
            await interaction.send(embed=embed)


def setup(client):
    client.add_cog(music(client))
