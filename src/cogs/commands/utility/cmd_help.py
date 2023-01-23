import discord
import datetime
from typing import List
from cogs import BaseCog


def cog_creator(servers: List[int]):
    class HelpCog(BaseCog):
        def __init__(self, bot) -> None:
            super().__init__(bot)

        @BaseCog.cslash_command(description='Shows all commands', guild_ids=servers)
        async def help(self, ctx):
            embed = discord.Embed(title='Commands', description='Shows all commands', colour=0x2c6dbf, timestamp=datetime.datetime.now())
            commands = ctx.bot.walk_application_commands()
            for command in commands:
                description = getattr(command, 'description')
                embed.add_field(name = command, value=description)
            embed.set_footer(text="Venox Network", icon_url='https://images-ext-1.discordapp.net/external/Ku-I5AZNKBTGjAQ2TRpUsTUiUKPVa7GKyidFvaQikik/%3Fsize%3D1024/https/cdn.discordapp.com/icons/879734848946847774/40e90339ddce11f97924e85c6e901b0c.png?width=491&height=491')
            await ctx.respond(embed=embed)

    return HelpCog
    