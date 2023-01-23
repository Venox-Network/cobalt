import discord
import humanfriendly
from datetime import datetime, timedelta
from typing import List
from discord import ApplicationContext
from cogs import BaseCog
from discord.commands.options import Option


def cog_creator(servers: List[int]):
    class Mute(BaseCog):

        def __init__(self, bot) -> None:
            super().__init__(bot)
            self.warn_collection = self.bot.config.DATABASE["warns"]

        @BaseCog.cslash_command(
            description="Mutes a member using the timeout function",
            guild_ids=servers
        )
        async def mute(self, ctx: ApplicationContext, member: Option(discord.Member), time: Option(str), reason: Option(str) = None):
            if not self.check_perms(ctx, {"moderate_members": True}, member):
                await ctx.respond("Sorry, you cannot use this command.", ephemeral=True)
                return

            try:
                time_readable = humanfriendly.parse_timespan(time)
            except Exception:
                await ctx.respond(
                    "Cannot parse time, please retry the command.",
                    ephemeral=True
                    )

            time_final = timedelta(seconds=time_readable)
            time_unix = int((datetime.now() + time_final).timestamp())

            await member.timeout_for(timedelta(seconds=time_readable), reason=reason)

            try:
                await member.send(f"You have been muted in `{ctx.guild.name}`, till: <t:{time_unix}>, for `{reason}`. Responsible moderator: Responsible moderator: `{ctx.user.name}#{ctx.user.discriminator}`")
            except Exception:
                pass

            await self.bot.log_msg(f"`{member.name}#{member.discriminator}` has been muted in `{ctx.guild.name}`, till: <t:{time_unix}>, for reason: `{reason}`. Responsible moderator: `{ctx.user.name}#{ctx.user.discriminator}`", True)
            await ctx.respond(f"'{member.mention}' has been muted, till: <t:{time_unix}>, for `{reason}`", ephemeral=True)

    return Mute
