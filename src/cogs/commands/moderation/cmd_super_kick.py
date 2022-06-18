from typing import List
from discord import ApplicationContext
import discord
from discord.commands.options import Option
from cogs import BaseCog


def cog_creator(servers: List[int]):
    class SuperKick(BaseCog):

        def __init__(self, bot) -> None:
            super().__init__(bot)
            self.super_ban_db = self.bot.config.DATABASE["superbanids"]

        @BaseCog.cslash_command(
            description="Kicks a member from all guilds Venox moderates in",
            guild_ids=servers
        )
        async def super_kick(
                self,
                ctx: ApplicationContext,
                member: Option(discord.Member),
                reason: Option(str) = None
        ):
            if not (ctx.user.id in (self.bot.config.OWNERS)):
                await ctx.respond(
                    "Sorry, you cannot use this command.",
                    ephemeral=True
                    )
                return

            # member: discord.Member = member

            failed = []

            try:
                await member.send(
                    f"You have been kicked from **all** Venox Network Servers, for `{reason}`. Responsible owner: `{ctx.user.name}#{ctx.user.discriminator}`"
                    )
            except Exception as e:
                await ctx.respond(
                    f"Failed to dm member with error: {e}",
                    ephemeral=True
                    )

            for guild in self.bot.guilds:

                guild_member = guild.get_member(member.id)
                if guild_member is None:
                    continue

                try:
                    await guild_member.kick(reason=reason)
                except Exception:
                    failed.append(guild.name)

            await self.bot.log_msg(
                f"`{member.name}#{member.discriminator}` has been kicked from **all** Venox Network Servers, for `{reason}`. Responsible owner: `{ctx.user.name}#{ctx.user.discriminator}`" + (
                    ("\n\nFailed to kick user in guilds: \n" + ", ".join(failed)) if failed else ""))
            await ctx.respond(
                f"`{member.mention}` has been kicked from **all** Venox Network Servers, for `{reason}`"
                )


    return SuperKick
