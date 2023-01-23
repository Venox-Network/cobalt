from typing import List
from discord import ApplicationContext
from cogs import BaseCog


def cog_creator(servers: List[int]):
    class Eval(BaseCog):
        def __init__(self, bot) -> None:
            super().__init__(bot)

        @BaseCog.cslash_command(description="Runs code in eval", guild_ids=servers)
        async def exec(self, ctx: ApplicationContext, code):
            if ctx.user.id not in self.bot.config.OWNERS:
                await ctx.respond("Sorry, you cannot use this command.", ephemeral=True)
                return

            try:
                results = exec(code)
                await ctx.respond(f"The code executed and returned: `{results}`")
            except Exception as e:
                await ctx.respond(f"The code ran with error: `{e}`")

        
        @BaseCog.cslash_command(description="Runs code in eval", guild_ids=servers)
        async def eval(self, ctx: ApplicationContext, code):
            if ctx.user.id not in self.bot.config.OWNERS:
                await ctx.respond("Sorry, you cannot use this command.", ephemeral=True)
                return

            try:
                results = eval(code)
                await ctx.respond(f"the code executed and returned: `{results}`")
            except Exception as e:
                await ctx.respond(f"The code ran with error: `{e}`")

    return Eval
