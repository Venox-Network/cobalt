from typing import List
from discord import ApplicationContext
import discord
from discord.ext.commands import Cog
from src.cogs import BaseCog
from discord.ext import tasks


def cog_creator(servers: List[int]):
    class AutoThreadCog(BaseCog):

        def __init__(self, bot) -> None:
            super().__init__(bot)
            self.thread_channel_collections = self.bot.config.DATABASE["threadedchannels"]
            self.thread_guild_map = {}
            self.thread_loop.start()

        def cog_unload(self) -> None:
            self.thread_loop.stop()

        @tasks.loop(hours=1.0)
        async def thread_loop(self):
            try:
                self.thread_guild_map = {}
                delete = []
                async for result in self.thread_channel_collections.find({}):
                    # data = {"guild_id": ctx.guild.id, "guildname": ctx.guild.name, "channel_db_id": ctx.channel.id}
                    guild = self.bot.get_guild(result["guild_id"])
                    if guild is None:
                        delete.append(result)
                        continue

                    channel = guild.get_channel(result["channel_db_id"])
                    if channel is None:
                        delete.append(result)
                        continue

                    self.thread_guild_map[guild.id] = channel.id

                for key in delete:
                    await self.thread_channel_collections.delete_one(key)

            except Exception:
                self.bot.log_msg("Could not connect to database `threadedchannels` to fetch slowmode details.", True)

        @thread_loop.before_loop
        async def on_start(self):
            await self.bot.wait_until_ready()

        @Cog.listener()
        async def on_message(self, message: discord.Message):

            if message.author.bot:
                return

            try:
                if message.channel.id not in self.thread_guild_map.values():
                    return

                await message.create_thread(name=message.content[:15] + ("..." if len(message.content) > 15 else ""))
            except Exception:
                pass

        @Cog.listener()
        async def on_guild_channel_delete(self, channel: discord.TextChannel):

            if not isinstance(channel, discord.TextChannel):
                return

            try:

                if channel.guild.id in self.thread_guild_map.keys():
                    await self.thread_channel_collections.delete_many({"channel_db_id": channel.id})
                    self.thread_guild_map.pop(channel.guild.id, None)
            except Exception as e:
                await self.bot.log_msg(
                    f"Error while deleting channel: `{channel.name}`[{channel.id}] from DB `threadedchannels`\n\n{str(e)}",
                    True)

        @BaseCog.cslash_command(
            descripton="Makes a thread from a message",
            guild_ids=servers
        )
        async def auto_thread(
                self,
                ctx: ApplicationContext
        ):

            required_perms = {"manage_messages": True}

            if not self.check_perms(ctx, required_perms):
                await ctx.respond(f"Sorry, you cannot use this command.", ephemeral=True)
                return

            try:
                result = await self.thread_channel_collections.find_one({"guild_id": ctx.guild.id})
                data = {"guild_id": ctx.guild.id, "guildname": ctx.guild.name, "channel_db_id": ctx.channel.id}
                if result is None:
                    await self.thread_channel_collections.insert_one(data)
                    self.thread_guild_map[ctx.guild.id] = ctx.channel.id
                    await ctx.respond("Auto Threading is now enabled for this channel.", ephemeral=True)
                    return

                if result["channel_db_id"] == ctx.channel.id:
                    await self.thread_channel_collections.delete_one(result)
                    self.thread_guild_map.pop(ctx.guild.id, None)
                    await ctx.respond("Auto Threading is now disabled for this channel.", ephemeral=True)
                    return

                prev_channel = self.bot.get_channel(result["channel_db_id"])

                await self.thread_channel_collections.replace_one(result, data)
                self.thread_guild_map[ctx.guild.id] = ctx.channel.id
                await ctx.respond(f"Auto Threading is now enabled for this channel" + (
                    f", and is now disabled in {prev_channel.mention}." if prev_channel is not None else "."),
                                  ephemeral=True)

            except Exception:
                await ctx.respond(
                    "Could not interract with database `threadedchannels`. Please try again after sometime.",
                    ephemeral=True)

    return AutoThreadCog
