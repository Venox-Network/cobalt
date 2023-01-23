import discord
from typing import List
from discord import ApplicationContext
from cogs import BaseCog
from discord.commands.options import Option


def cog_creator(servers: List[int]):
    class Kick(BaseCog):
        def __init__(self, bot) -> None:
            super().__init__(bot)

        @BaseCog.cslash_command(description="Kick a member", guild_ids=servers)
        async def kick(self, ctx: ApplicationContext, member: Option(discord.Member), reason: Option(str) = None,):
            if not self.check_perms(ctx, {"kick_members": True}, member):
                await ctx.respond("Sorry, you cannot use this command.", ephemeral=True)
                return

            try:
                await member.send(f"You have been kicked from `{ctx.guild.name}` for `{reason}`. Responsible moderator: Responsible moderator: `{ctx.user.name}#{ctx.user.discriminator}`")
            except Exception:
                pass

            await member.kick(reason=reason)
            await self.bot.log_msg(f"`{member.name}#{member.discriminator}` has been kicked from `{ctx.guild.name}`, for reason: `{reason}`. Responsible moderator: `{ctx.user.name}#{ctx.user.discriminator}`", True)
            await ctx.respond(f"'{member.mention}' has been kicked for `{reason}`", ephemeral=True)

    return Kick
