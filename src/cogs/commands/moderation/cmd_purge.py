from typing import List
from discord import ApplicationContext
import discord
from cogs import BaseCog
from discord.commands.options import Option


def cog_creator(servers: List[int]):
    class Purge(BaseCog):

        def __init__(self, bot) -> None:
            super().__init__(bot)
            self.warn_collection = self.bot.config.DATABASE["warns"]

        @BaseCog.cslash_command(
            description="Purges messages by deafult. If 'member' is provided, it will purge messages by that member only",
            guild_ids=servers
        )
        async def purge(
                self,
                ctx: ApplicationContext,
                amount: Option(int),
                member: Option(discord.Member) = None
        ):
            # member: discord.Member = member

            required_perms = {"manage_messages": True}

            if not self.check_perms(ctx, required_perms, member, True):
                await ctx.respond(
                    "Sorry, you cannot use this command.",
                    ephemeral=True
                    )
                return

            try:
                if member is not None:
                    await ctx.channel.purge(limit=amount, check=(lambda message: message.author == member))
                    await ctx.respond(
                        f"Purged `{amount}` message(s) sent by '{member.mention}' in this channel.",
                        ephemeral=True
                        )
                    return

                await ctx.channel.purge(limit=amount)
                await ctx.respond(
                    f"Purged `{amount}` message(s) from this channel",
                    ephemeral=True
                    )
            except Exception:
                await ctx.respond(
                    "Could not purge all the messages from this channel",
                    ephemeral=True
                    )

    return Purge
