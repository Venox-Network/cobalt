import asyncio
from typing import List
from discord import ApplicationContext
from discord.commands.options import Option
from . import BaseCog


def cog_creator(servers: List[int]):
    class FunCog(BaseCog):
        
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

            required_perms = {"manage_messages":True}

            if not self.check_perms(ctx, required_perms):
                await ctx.respond(f"Sorry, you cannot use this command.", ephemeral=True)
                return

            try:
                message_id = int(message_id)
            except Exception:
                await ctx.respond("Please enter a valid number", ephemeral=True)

            message = await ctx.fetch_message(message_id)
            if message == None:
                ctx.respond(f"Message with id `{message_id}` not found.", ephemeral=True)
                return

            await ctx.defer(ephemeral=True)

            for emoji in emojis.split(" "):
                await message.add_reaction(emoji)
                await asyncio.sleep(1) # To not trigger auto spam filter by discord.
            
            await ctx.respond("Added emojis", ephemeral=True)
    
    return FunCog