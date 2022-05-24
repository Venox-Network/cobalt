from typing import List
from discord import ApplicationContext
import discord
from discord.commands.options import Option
from cogs import BaseCog


def cog_creator(servers: List[int]):
    class SuperUnban(BaseCog):

        def __init__(self, bot) -> None:
            super().__init__(bot)
            self.super_ban_db = self.bot.config.DATABASE["superbanids"]

        @BaseCog.cslash_command(
            description="Unbans a member from all guilds Venox moderates in",
            guild_ids=servers
        )
        async def super_unban(
                self,
                ctx: ApplicationContext,
                user_id: Option(int),
        ):
            if not (ctx.user.id in (self.bot.config.OWNERS)):
                await ctx.respond(f"Sorry, you cannot use this command.", ephemeral=True)
                return

            user: discord.User = self.bot.get_user(user_id)
            if user is None:
                await ctx.respond(f"User with the id `{user_id}` not found.", ephemeral=True)
                return

            try:
                await self.super_ban_db.delete_one({'banned_member_id': user_id})
            except Exception:
                await ctx.respond("Could not interract with database `superbanids`. Please try again after sometime.",
                            ephemeral=True)
                return

            failed = []

            for guild in self.bot.guilds:
                try:
                    guild.unban(user)
                except Exception:
                    failed.append(guild.name)

            await self.bot.log_msg(
                f"`{user.name}#{user.discriminator}` has been ***SUPER UN BANNED***. Responsible owner: `{ctx.user.name}#{ctx.user.discriminator}`" + (
                            "\n\nFailed to un-ban user in guilds: \n" + ", ".join(failed)) if failed else "")
            await ctx.respond(f"`{user.mention}` has been ***SUPER UN BANNED***")

    return SuperUnban
