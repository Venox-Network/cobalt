import discord
from typing import List
from discord import ApplicationContext
from discord.ext.commands import Cog
from discord.commands.options import Option
from cogs import BaseCog


def cog_creator(servers: List[int]):
    class GlobalChatCog(BaseCog):
        def __init__(self, bot) -> None:
            super().__init__(bot)
            self.global_chat = self.bot.config.DATABASE["global_chat"]

        @BaseCog.cslash_command(description="Sets up cross-channel chat", guild_ids=servers)
        async def channel_add(self, ctx: ApplicationContext, channel1: Option(str), channel2: Option(str)):
            if not self.check_perms(ctx, {"administrator": True}):
                await ctx.respond("Sorry, you cannot use this command.", ephemeral=True)
                return

            try:
                await ctx.response.defer(ephemeral=False)
                result = await self.global_chat.find_one({"channel1": channel1, "channel2": channel2})
                if result is None:
                    data = {"channel1": int(channel1), "channel2": int(channel2)}
                    await self.global_chat.insert_one(data)
                    await ctx.respond("Channel added to database", ephemeral=False)
                    return

                if channel1 in result["channel1"] or channel2 in result["channel2"]:
                    await ctx.respond("That channel is already in the blacklist.", ephemeral=False)
                    return

            except Exception as e:
                await ctx.respond("Could not interract with database `global chat`. Please try again after sometime.", ephemeral=True)
                print(e)
                

        @BaseCog.cslash_command(description="Removes a channel to the database", guild_ids=servers)
        async def channel_remove(self, ctx: ApplicationContext, channel1: Option(str), channel2: Option(str)):
            if not self.check_perms(ctx, {"administrator": True}):
                await ctx.respond("Sorry, you cannot use this command.", ephemeral=True)
                return

            try:
                data = {"channel1": int(channel1), "channel2": int(channel2)}
                data2 = {"channel2": int(channel1), "channel1": int(channel2)}
                await ctx.response.defer(ephemeral=False)
                result = await self.global_chat.find_one(data)
                if result is None:
                    result2 = await self.global_chat.find_one(data2)
                    if result2 is None:
                        await ctx.respond("Those channels are not in db")
                        return
                    await self.global_chat.delete_one(data2)
                    await ctx.respond("Removed channels from database")
                    return
                await self.global_chat.delete_one(data)
                await ctx.respond("Removed channels from database")
                return


            except Exception as e:
                await ctx.respond("Could not interract with database `global chat`. With error: {e}", ephemeral=True)
                print(e)
                await self.bot.log_msg(f"Error with connecting global chat with error: `{e}`", should_print=True)

        @Cog.listener()
        async def on_message(self, message: discord.Message):
            if message.author.bot:
                return

            try:
                result = await self.global_chat.find_one({"channel1": message.channel.id})
                result2 = await self.global_chat.find_one({"channel2": message.channel.id})

                if result is not None:
                    channel = result["channel2"]
                    discord_channel = self.bot.get_channel(channel)
                    try:
                        replied_message_id = message.reference.message_id
                    except AttributeError:
                        embed = discord.Embed(title=f"Cross chat to: \#{message.channel.name}")
                        embed.add_field(name=f"{message.author}:", value=f"{message.content}")
                        await discord_channel.send(embed=embed)
                        return
                    replied_message = await message.channel.fetch_message(replied_message_id)
                    embed = discord.Embed(title=f"Cross chat to: \#{message.channel.name}")
                    embed.set_footer(text=f"Replying to: {replied_message.author}: {replied_message.clean_content}", icon_url=message.author.avatar.url)
                    embed.add_field(name=f"{message.author}:", value=f"{message.content}")
                    await discord_channel.send(embed=embed)

                if result2 is not None:
                    channel = result2["channel1"]
                    discord_channel = self.bot.get_channel(channel)
                    try:
                        replied_message_id = message.reference.message_id
                    except AttributeError:
                        embed = discord.Embed(title=f"Cross chat to: \#{message.channel.name}")
                        embed.add_field(name=f"{message.author}:", value=f"{message.content}")
                        await discord_channel.send(embed=embed)
                        return
                    replied_message = await message.channel.fetch_message(replied_message_id)
                    embed = discord.Embed(title=f"Cross chat to: \#{message.channel.name}")
                    embed.set_footer(text=f"Replying to: {replied_message.author}: {replied_message.clean_content}", icon_url=replied_message.author.avatar.url)
                    embed.add_field(name=f"{message.author}:", value=f"{message.content}")
                    await discord_channel.send(embed=embed)
            except Exception as e:
                print(e)
                
    return GlobalChatCog
