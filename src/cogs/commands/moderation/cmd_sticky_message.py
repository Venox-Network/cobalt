from typing import List
from discord import ApplicationContext
import discord
from discord.ext.commands import Cog
from discord.commands.options import Option
from cogs import BaseCog
from discord.ext import tasks


# https://discord.com/channels/<guild_id>/<channel_id>/<message_id>

def cog_creator(servers: List[int]):
    class StickyCog(BaseCog):

        def __init__(self, bot) -> None:
            super().__init__(bot)
            self.sticky_collections = self.bot.config.DATABASE["sticky_messages"]
            self.message_channel_map = {}
            self.sticky_loop.start()

        def cog_unload(self) -> None:
            self.sticky_loop.stop()

        @tasks.loop(hours=1.0)
        async def sticky_loop(self):
            try:
                delete = []
                async for result in self.sticky_collections.find({}):

                    # data = {"channel_id": ctx.channel.id, "message_id": message.id, "guild_id": ctx.guild.i}
                    channel = self.bot.get_channel(result["channel_id"])
                    if channel is None:
                        delete.append(result)
                        continue

                    try:
                        msg = await channel.fetch_message(result["message_id"])
                    except Exception:
                        delete.append(result)
                        continue

                    self.message_channel_map[channel.id] = msg.id

                for key in delete:
                    await self.sticky_collections.delete_one(key)

            except Exception:
                await self.bot.log_msg(
                    "Could not connect to database `sticky_messages` to fetch sticky message details.",
                                 True)

        @sticky_loop.before_loop
        async def on_start(self):
            await self.bot.wait_until_ready()

        @Cog.listener()
        async def on_message(self, message: discord.Message):

            if message.author.bot:
                return

            try:

                if message.channel.id not in self.message_channel_map.keys():
                    return

                msg = await message.channel.fetch_message(self.message_channel_map[message.channel.id])
                if msg is None:
                    await self.sticky_collections.delete_many({"channel_id": message.channel.id})
                    return

                # find prev message sent by me
                async for prev_msg in message.channel.history(limit=50):
                    if prev_msg.author.id == self.bot.user.id:
                        await prev_msg.delete()
                        break

                await message.channel.send(msg.content)

            except Exception as e:
                print(e)
                self.bot.log_msg(f"Error with sticky message {e}")

        @Cog.listener()
        async def on_guild_channel_delete(self, channel: discord.TextChannel):

            if not isinstance(channel, discord.TextChannel):
                return

            try:

                if channel.id in self.message_channel_map.keys():
                    await self.sticky_collections.delete_many({"channel_id": channel.id})
                    self.message_channel_map.pop(channel.id, None)
            except Exception as e:
                await self.bot.log_msg(
                    f"Error while deleting channel: `{channel.name}`[{channel.id}] from DB `sticky_messages`\n\n{str(e)}",
                    True)

        @staticmethod
        def generate_message_link(message: discord.Message):
            return f"https://discord.com/channels/{message.guild.id}/{message.channel.id}/{message.id}"

        @BaseCog.cslash_command(
            descripton="Mark a message as sticky.",
            guild_ids=servers
        )
        async def stick_msg(
                self,
                ctx: ApplicationContext,
                message_id: Option(str)
        ):

            required_perms = {"manage_messages": True}

            if not self.check_perms(ctx, required_perms):
                await ctx.respond("Sorry, you cannot use this command.", ephemeral=True)
                return

            message_id = int(message_id)
            message = await ctx.channel.fetch_message(message_id)
            if message is None:
                await ctx.respond(
                    f"The message `{message_id}` does not exist. Please try again with a different message id.",
                    ephemeral=True)
                return

            try:
                result = await self.sticky_collections.find_one({"channel_id": ctx.channel.id})
                data = {"channel_id": ctx.channel.id, "message_id": message.id, "guild_id": ctx.guild.id}
                if result is None:
                    await self.sticky_collections.insert_one(data)
                    self.message_channel_map[ctx.channel.id] = message.id
                    await ctx.respond("This message is now marked as sticky.", ephemeral=True)
                    return

                if result["message_id"] == message.id:
                    await self.sticky_collections.delete_one(result)
                    self.message_channel_map.pop(ctx.channel.id, None)
                    await ctx.respond("The message is now marked as non sticky.", ephemeral=True)
                    return

                prev_msg = await ctx.channel.fetch_message(result["message_id"])

                await self.sticky_collections.replace_one(result, data)
                self.message_channel_map[ctx.channel.id] = message.id
                await ctx.respond(f"'Sticky msg' is now transfered to {self.generate_message_link(message)} " + (
                    f", from {self.generate_message_link(prev_msg)}." if prev_msg is not None else "."), ephemeral=True)

            except Exception as e:
                print(e)
                await ctx.respond(
                    f"Could not interract with database `sticky_messages` with error: {e}",
                    ephemeral=True)

        @BaseCog.cslash_command(
            description="Get the sticky message of the channel.",
            guild_ids=servers
        )
        async def get_sticky_msg(
                self,
                ctx: ApplicationContext
        ):

            required_perms = {"manage_messages": True}

            if not self.check_perms(ctx, required_perms):
                await ctx.respond(
                    "Sorry, you cannot use this command.",
                    ephemeral=True)
                return

            try:
                result = await self.sticky_collections.find_one({"channel_id": ctx.channel.id})
                if result is None:
                    await ctx.respond(
                        "No sticky message is set for this channel.",
                        ephemeral=True)
                    return

                message = await ctx.channel.fetch_message(result["message_id"])
                if message is None:
                    await self.sticky_collections.delete_many({"channel_id": ctx.channel.id})
                    self.message_channel_map.pop(ctx.channel.id, None)
                    await self.sticky_collections.delete_many({"channel_id": ctx.channel.id})
                    await ctx.respond("The sticky message is deleted. Please try again.", ephemeral=True)
                    return

                await ctx.respond(
                    f"The sticky message is set to {self.generate_message_link(message)}.",
                                  ephemeral=True)

            except Exception:
                await ctx.respond(
                    "Could not interract with database `sticky_messages`. Please try again after sometime.",
                    ephemeral=True)

    return StickyCog
