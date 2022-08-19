import asyncio
from typing import List
from discord import ApplicationContext
import discord
from discord.commands.options import Option
from cogs import BaseCog
from discord.ext.commands import Cog


def cog_creator(servers: List[int]):
    class React(BaseCog):
        def __init__(self, bot) -> None:
            super().__init__(bot)
            self.react_channel_static = self.bot.config.DATABASE["react_channel_static"]
            self.react_channel_dynamic = self.bot.config.DATABASE["react_channel_dynamic"]
        
        @BaseCog.cslash_command(
            description="Mass react to messages",
            guild_ids=servers
        )
        async def react(
                self,
                ctx: ApplicationContext,
                emojis: Option(str),
                message_id: Option(str)
        ):

            required_perms = {"manage_messages": True}

            if not self.check_perms(ctx, required_perms):
                await ctx.respond(
                    "Sorry, you cannot use this command.",
                    ephemeral=True
                    )
                return

            try:
                message_id = int(message_id)
            except Exception:
                await ctx.respond(
                    "Please enter a valid number",
                    ephemeral=True
                    )

            message = await ctx.fetch_message(message_id)
            if message == None:
                await ctx.respond(
                    f"Message with id `{message_id}` not found.",
                    ephemeral=True
                    )
                return

            await ctx.defer(ephemeral=True)

            for emoji in emojis.split(" "):
                await message.add_reaction(emoji)
                await asyncio.sleep(1)  # To not trigger auto spam filter by discord.

            await ctx.respond(
                "Added emojis",
                ephemeral=True
                )

        @BaseCog.cslash_command(
            description='Makes bot react to all messages in certain channels',
            guild_ids=servers
        )
        async def static_react_channel(self, ctx, channel: discord.TextChannel, emojis: str):
            required_perms = {"manage_messages": True}

            if not self.check_perms(ctx, required_perms):
                await ctx.respond(
                        "Sorry, you cannot use this command.",
                        ephemeral=True
                        )
                return

            emojis_list = emojis.split(' ')
            resullts = await self.react_channel_static.find_one({'channel': channel.id})
            if resullts is None:
                self.react_channel_static.insert_one({'channel': channel.id, 'emojis': list(emojis_list)})
                await ctx.respond('Added channel')
                return
            self.react_channel_static.update_one({ 'channel': channel.id},{ '$set': { 'emojis': list(emojis_list) } })
            await ctx.respond('Updated emojis')
        
        @BaseCog.cslash_command(
            description="Removes static react channel",
            guild_ids=servers
        )
        async def static_react_channel_remove(self, ctx, channel: discord.TextChannel):
            required_perms = {"manage_messages": True}

            if not self.check_perms(ctx, required_perms):
                await ctx.respond(
                        "Sorry, you cannot use this command.",
                        ephemeral=True
                        )
                return
            results =  await self.react_channel_static.find_one({ 'channel': channel.id})
            if results is not None:
                await self.react_channel_static.delete_one({ 'channel': channel.id})
                await ctx.respond('Removed static react channel')
                return
            await ctx.respond('That channel is not in database')

        @Cog.listener()
        async def on_message(self, message):
            try:
                results = await self.react_channel_static.find_one({'channel': message.channel.id})
            except Exception as e:
                print(e)
            if results is None:
                return
            for emoji in list(results['emojis']):
                await message.add_reaction(emoji)
                await asyncio.sleep(1)
            
        @BaseCog.cslash_command(
            description='Sets up or removes channel to react to every emoji in messages',
            guild_ids=servers
        )
        async def dynamic_react_channel(self, ctx, channel: discord.TextChannel):
            required_perms = {"manage_messages": True}

            if not self.check_perms(ctx, required_perms):
                await ctx.respond(
                        "Sorry, you cannot use this command.",
                        ephemeral=True
                        )
                return
            results = await self.react_channel_dynamic.find_one({'channel': channel.id})
            if results is None:
                await self.react_channel_dynamic.insert_one({'channel': channel.id})
                await ctx.respond('Added channel')
                return
            await self.react_channel_dynamic.delete_one({'channel': channel.id})
            await ctx.respond('Removed channel')

        @Cog.listener()
        async def on_message(self, message):
            results = await self.react_channel_dynamic.find_one({'channel': message.channel.id})
            if results is not None:
                line_list = message.content.split('\n')
                message_content = ' '.join(line_list)
                message_list = message_content.split(' ')
                for word in message_list:
                    if not word.isascii() or word.startswith('<') and word.endswith('>'):
                        try:
                            await message.add_reaction(word)
                        except:
                            pass
                        await asyncio.sleep(1)

    return React
