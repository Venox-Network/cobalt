from typing import List
from discord import ApplicationContext
import discord
from discord.commands.options import Option
from cogs import BaseCog


def cog_creator(servers: List[int]):
    class Eval(BaseCog):

        def __init__(self, bot) -> None:
            super().__init__(bot)
            self.super_ban_db = self.bot.config.DATABASE["superbanids"]

        @BaseCog.cslash_command(
            description="Runs code in eval",
            guild_ids=servers
        )
        async def exec(
                self,
                ctx: ApplicationContext,
                code
        ):
            if not (ctx.user.id in (self.bot.config.OWNERS)):
                await ctx.respond(
                    "Sorry, you cannot use this command.",
                    ephemeral=True
                    )
                return

            # member: discord.Member = member
            try:
                results = exec(code)
                await ctx.respond(f"the code executed and returned: `{results}`")
            except Exception as e:
                await ctx.respond(f"The code ran with error: `{e}`")

        
        @BaseCog.cslash_command(
            description="Runs code in eval",
            guild_ids=servers
        )
        async def eval(
                self,
                ctx: ApplicationContext,
                code
        ):
            if not (ctx.user.id in (self.bot.config.OWNERS)):
                await ctx.respond(
                    "Sorry, you cannot use this command.",
                    ephemeral=True
                    )
                return

            # member: discord.Member = member
            try:
                results = eval(code)
                await ctx.respond(f"the code executed and returned: `{results}`")
            except Exception as e:
                await ctx.respond(f"The code ran with error: `{e}`")

    return Eval
