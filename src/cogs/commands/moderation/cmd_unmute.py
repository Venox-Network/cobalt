from typing import List
from discord import ApplicationContext
import discord
from cogs import BaseCog
from discord.commands.options import Option


def cog_creator(servers: List[int]):
    class Unmute(BaseCog):

        def __init__(self, bot) -> None:
            super().__init__(bot)
            self.warn_collection = self.bot.config.DATABASE["warns"]

        @BaseCog.cslash_command(
            description="Unmutes a member using the timeout function",
            guild_ids=servers
        )
        async def unmute(
                self,
                ctx: ApplicationContext,
                member: Option(discord.Member),
                reason: Option(str) = None
        ):
            # member: discord.Member = member

            required_perms = {"moderate_members": True}

            if not self.check_perms(ctx, required_perms, member):
                await ctx.respond("Sorry, you cannot use this command.", ephemeral=True)
                return

            if member.timed_out:
                await member.edit(communication_disabled_until=None, reason=reason)
            else:
                await ctx.respond(f"'{member.mention}' has not been muted to be unmuted", ephemeral=True)
                return

            try:
                await member.send(
                    f"You have been unmuted in `{ctx.guild.name}`, for `{reason}`. Responsible moderator: `{ctx.user.name}#{ctx.user.discriminator}`")
            except Exception as e:
                await ctx.respond(
                    f"Failed to dm member with error {e}",
                    ephemeral=True
                    )

            await self.bot.log_msg(
                f"`{member.name}#{member.discriminator}` has been unmuted in `{ctx.guild.name}`, for reason: `{reason}`. Responsible moderator: `{ctx.user.name}#{ctx.user.discriminator}`"
                )
            await ctx.respond(
                f"'{member.mention}' has been unmuted, for `{reason}`",
                ephemeral=True
                )

    return Unmute
