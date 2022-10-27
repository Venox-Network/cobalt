import discord

from datetime import datetime
from typing import List
from cogs import BaseCog
from discord import ApplicationContext
from discord.commands.options import Option


def cog_creator(servers: List[int]):
    class All_Warns(BaseCog):

        def __init__(self, bot) -> None:
            super().__init__(bot)
            self.warn_collection = self.bot.config.DATABASE["warns"]

        @BaseCog.cslash_command(
            description="Warns a member",
            guild_ids=servers
        )
        async def warn(
                self,
                ctx: ApplicationContext,
                member: Option(discord.Member),
                reason: Option(str) = None
        ):

            # member: discord.Member = member

            required_perms = {"moderate_members": True}

            if not self.check_perms(ctx, required_perms, member):
                await ctx.respond(
                    "Sorry, you cannot use this command.",
                    ephemeral=True
                    )
                return

            today_date = datetime.now().strftime("%Y-%m-%d")

            try:
                await self.warn_collection.insert_one(
                    {"warn_guild": ctx.guild.id, "memberid": member.id, "membername": member.name,
                     "guildname": ctx.guild.name, "reason": reason, "date": today_date, "moderator": ctx.user.name})
                count = int(
                    await self.warn_collection.count_documents({"warn_guild": ctx.guild.id, "memberid": member.id}))
            except Exception:
                await ctx.respond(
                    "Could not interract with database `warns`. Please try again after sometime.",
                    ephemeral=True
                    )
                return

            last_digit = count % 10
            if last_digit == 1:
                count = str(count) + "st"
            elif last_digit == 2:
                count = str(count) + "nd"
            elif last_digit == 3:
                count = str(count) + "rd"
            else:
                count = str(count) + "th"

            await ctx.respond(
                f"{member.mention} has been warned for `{reason}`."
                f" This is their `{count}` warning."
                )

        @BaseCog.cslash_command(
            description="Gets all the warn info of a member",
            guild_ids=servers
        )
        async def warns(
                self,
                ctx: ApplicationContext,
                member: Option(discord.Member)
        ):

            member: discord.Member = member

            await ctx.defer(ephemeral=False)

            required_perms = {"moderate_members": True}

            if not self.check_perms(ctx, required_perms, member):
                await ctx.respond(
                    "Sorry, you cannot use this command.",
                    ephemeral=True)
                return

            try:
                documents = self.warn_collection.find({"warn_guild": ctx.guild_id, "memberid": member.id})
                count = int(
                    await self.warn_collection.count_documents({"warn_guild": ctx.guild.id, "memberid": member.id}))
            except Exception as e:
                await ctx.respond(
                    f"Could not interract with database `warns`. With error {e}.",
                    ephemeral=True)
                return

            embed = discord.Embed(title=f"Warns for {member.name}:",
                                  description=f"`{member.name}` has `{count}` warns.")

            async for document in documents:
                embed.add_field(name="Warn: ",
                                value=f"**Date:** `{document['date']}`\n**Reason:** `{document['reason']}`\n**Moderator:** `{document['moderator']}`")

            await ctx.respond(
                embed=embed
                )

    return All_Warns
