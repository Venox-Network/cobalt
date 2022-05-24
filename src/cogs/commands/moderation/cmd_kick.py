from typing import List
from discord import ApplicationContext
import discord
from src.cogs import BaseCog
from discord.commands.options import Option


def cog_creator(servers: List[int]):
    class Kick(BaseCog):

        def __init__(self, bot) -> None:
            super().__init__(bot)
            self.warn_collection = self.bot.config.DATABASE["warns"]

        @BaseCog.cslash_command(
            description="Kick a member",
            guild_ids=servers
        )
        async def kick(
                self,
                ctx: ApplicationContext,
                member: Option(discord.Member),
                reason: Option(str) = None,
        ):
            # member: discord.Member = member

            required_perms = {"kick_members": True}

            if not self.check_perms(ctx, required_perms, member):
                await ctx.respond(f"Sorry, you cannot use this command.", ephemeral=True)
                return

            try:
                await member.send(
                    f"You have been kicked from `{ctx.guild.name}` for `{reason}`. Responsible moderator: Responsible moderator: `{ctx.user.name}#{ctx.user.discriminator}`")
            except Exception:
                pass

            await member.ban(reason=reason)
            await self.bot.log_msg(
                f"`{member.name}#{member.discriminator}` has been kicked from `{ctx.guild.name}`, for reason: `{reason}`. Responsible moderator: `{ctx.user.name}#{ctx.user.discriminator}`")
            await ctx.respond(f"'{member.mention}' has been kicked for `{reason}`", ephemeral=True)

    return Kick
