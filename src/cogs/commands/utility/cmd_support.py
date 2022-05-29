from typing import List
from discord import ApplicationContext
from cogs import BaseCog


def cog_creator(servers: List[int]):
    class Support(BaseCog):

        @BaseCog.cslash_command(
            description="Sends link to support server",
            guild_ids=servers
        )
        async def support(self, ctx: ApplicationContext):
            await ctx.respond("[Join our support server](https://discord.gg/kaddCVeRj6)\nhttps://discord.gg/kaddCVeRj6")
    return Support
