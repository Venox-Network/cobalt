from typing import List
from discord import ApplicationContext
import discord
from discord.ext.commands import Cog
from discord.commands.options import Option
from src.cogs import BaseCog
import wavelink


def cog_creator(servers: List[int]):
    class MusicCog(BaseCog):
        
        def __init__(self, bot) -> None:
            super().__init__(bot)
            self.bot.loop.create_task(self.connect_nodes())

        async def check_voice(self, ctx: ApplicationContext):
            user_voice_state = ctx.user.voice

            if user_voice_state is None:
                await ctx.respond("Please connect to a VC first before using this command.", ephemeral=True)
                return False

            if user_voice_state.channel != ctx.guild.voice_client.channel:
                await ctx.respond("You are not in the same voice channel as the bot.", ephemeral=True)
                return False

            return True

        async def connect_nodes(self):

            await self.bot.wait_until_ready()
            await wavelink.NodePool.create_node(
                bot=self.bot,
                host=self.bot.config.wavelink_host,
                port=self.bot.config.wavelink_port,
                password=self.bot.config.wavelink_pass,
                https=True
            )

        @Cog.listener()
        async def on_wavelink_node_ready(self, node: wavelink.Node):
            await self.bot.log_msg(f"Wavelink node `{node.identifier}` is ready!", True)

        @Cog.listener()
        async def on_wavelink_track_end(self, voice_chat: wavelink.Player, track: wavelink.Track, reason):

            if getattr(voice_chat, "loop", None) is not None:
                if voice_chat.loop:
                    await voice_chat.play(track)
                    return

            try:
                next_music = voice_chat.queue.get()
                await voice_chat.play(next_music)
                if getattr(voice_chat, "text_channel", None) is not None:
                    text_channel: discord.TextChannel = voice_chat.text_channel
                    await text_channel.send(f"Now playing: `{next_music.title}`")
            except Exception:

                voice_chat.loop = False
                voice_chat.text_channel = None

                await voice_chat.disconnect()

        @BaseCog.cslash_command(
            description="Play music",
            guild_ids=servers
        )
        async def play(
            self,
            ctx: ApplicationContext,
            search: Option(str, "Song Name")
        ):

            try:
                if not ctx.voice_client:

                    user_voice_state = ctx.user.voice

                    if user_voice_state is None:
                        await ctx.respond("Please connect to a VC first before using this command.", ephemeral=True)
                        return

                    channel = user_voice_state.channel

                    voice_chat: wavelink.Player = await channel.connect(cls=wavelink.Player)

                else:
                    voice_chat: wavelink.Player = ctx.voice_client

            except Exception:
                await self.bot.log_msg(f"Could not join vc in `{ctx.guild.name}`")
                await ctx.respond(f"Could not join vc in `{ctx.guild.name}`", ephemeral=True)
                return

            try:

                await ctx.respond(f"Searching for `{search}`...", ephemeral=True)
                query_result = await wavelink.YouTubeTrack.search(search, return_first=True)
                #channel: discord.VoiceChannel = channel
                
                if voice_chat.queue.is_empty and (not voice_chat.is_playing()):
                    await voice_chat.play(query_result)
                    await ctx.respond(f"Now playing: `{query_result.title}`")
                    await ctx.guild.change_voice_state(channel=channel, self_mute=False, self_deaf=True)
                
                else:
                    await voice_chat.queue.put_wait(query_result)
                    await ctx.respond(f"Added `{query_result.title}` to the queue")

            except Exception:
                await self.bot.log_msg(f"Could not play music in vc: `{channel.name}` in `{ctx.guild.name}`")
                await ctx.respond("There was an error while playing music. Please contact the staff.", ephemeral=True)
                if voice_chat.is_connected():
                    voice_chat.loop = False
                    voice_chat.text_channel = None
                    await voice_chat.disconnect()

            voice_chat.loop = False
            voice_chat.text_channel = ctx.channel 

        @BaseCog.cslash_command(
            description="Toggle Music b/w Playing and Paused",
            guild_ids=servers
        )
        async def toggle_music(
            self,
            ctx: ApplicationContext
        ):
            if not ctx.voice_client:
                await ctx.respond("There is no music being played on this server.")
                return

            if not await self.check_voice(ctx):
                return

            voice_chat: wavelink.Player = ctx.voice_client

            if voice_chat.is_paused():
                await voice_chat.resume()
                await ctx.respond("Resumed music.")
            else:
                await voice_chat.pause()
                await ctx.respond("Paused music.")

        @BaseCog.cslash_command(
            description="Stops Music and clears queue",
            guild_ids=servers
        )
        async def stop_music(
            self,
            ctx: ApplicationContext
        ):
            if not ctx.voice_client:
                await ctx.respond("There is no music being played on this server.")
                return

            if not await self.check_voice(ctx):
                return

            required_perms = {"manage_messages":True}

            if not self.check_perms(ctx, required_perms):
                await ctx.respond(f"Sorry, only mods can stop the music.", ephemeral=True)
                return

            voice_chat: wavelink.Player = ctx.voice_client

            voice_chat.queue.clear()
            await voice_chat.stop()
            voice_chat.loop = False
            voice_chat.text_channel = None
            await voice_chat.disconnect()

        @BaseCog.cslash_command(
            description="Skip current song",
            guild_ids=servers
        )
        async def skip_music(
            self,
            ctx: ApplicationContext
        ):
            if not ctx.voice_client:
                await ctx.respond("There is no music being played on this server.")
                return

            if not await self.check_voice(ctx):
                return

            voice_chat: wavelink.Player = ctx.voice_client

            if not voice_chat.is_playing():
                await ctx.respond("There is no music being played to skip.")
                return

            await voice_chat.stop()
            await ctx.respond("Skipped current song.")

        @BaseCog.cslash_command(
            description="Disconnect from voice channel",
            guild_ids=servers
        )
        async def voice_leave(
            self,
            ctx: ApplicationContext
        ):
            if not ctx.voice_client:
                await ctx.respond("There is no music being played on this server.")
                return

            required_perms = {"manage_messages":True}

            if not self.check_perms(ctx, required_perms):
                await ctx.respond(f"Sorry, only mods can disconnect the bot from VCs.", ephemeral=True)
                return

            voice_chat: wavelink.Player = ctx.voice_client

            await voice_chat.stop()
            await voice_chat.disconnect()
            voice_chat.loop = False
            voice_chat.text_channel = None
            await ctx.respond("Disconnected from voice channel.")

        @BaseCog.cslash_command(
            description="Loops over the current music",
            guild_ids=servers
        )
        async def loop_music(
            self,
            ctx: ApplicationContext
        ):
            if not ctx.voice_client:
                await ctx.respond("There is no music being played on this server.")
                return

            if not await self.check_voice(ctx):
                return

            voice_chat: wavelink.Player = ctx.voice_client

            if not voice_chat.is_playing():
                await ctx.respond("There is no music being played to skip.")
                return

            if getattr(voice_chat, "loop", None) is not None:
                voice_chat.loop = not voice_chat.loop
                await ctx.respond("Loop has now been `" + ("enabled" if voice_chat.loop else "disabled") + "`.")
                return
            
            voice_chat.loop = True
            await ctx.respond("Loop has now been `enabled`.")

        @BaseCog.cslash_command(
            description="Show the music queue.",
            guild_ids=servers
        )
        async def queue(
            self,
            ctx: ApplicationContext
        ):
            if not ctx.voice_client:
                await ctx.respond("There is no music being played on this server.")
                return

            voice_chat: wavelink.Player = ctx.voice_client

            if voice_chat.queue.is_empty:
                await ctx.respond("The Queue is currently empty.", ephemeral=True)
                return

            embed = discord.Embed(title="Queue")
            count = 0
            for song in voice_chat.queue:
                count += 1
                embed.add_field(name=f"Song No: {count}", value=song.title)
            
            await ctx.respond(embed=embed)

    return MusicCog