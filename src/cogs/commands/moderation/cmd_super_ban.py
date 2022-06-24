from typing import List
from discord import ApplicationContext
import discord
from discord.ext.commands import Cog
from discord.commands.options import Option
from cogs import BaseCog


def cog_creator(servers: List[int]):
    class SuperBan(BaseCog):

        def __init__(self, bot) -> None:
            super().__init__(bot)
            self.super_ban_db = self.bot.config.DATABASE["superbanids"]

        @BaseCog.cslash_command(
            description="Bans a member from all guilds Venox moderates in",
            guild_ids=servers
        )
        async def super_ban(
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
                await self.super_ban_db.insert_one(
                    {'banned_member_id': member.id, 'banned_member_name': member.name, 'superban_user': ctx.user.name})
            except Exception as e:
                await ctx.respond(
                    f"Could not interract with database `superbanids`. With error {e}.",
                    ephemeral=True
                    )
                return

            try:
                await member.send(
                    f"You have been banned from **all** Venox Network Servers,"\
                    " for `{reason}`."\
                    "Responsible owner: `{ctx.user.name}#{ctx.user.discriminator}`")
            except Exception:
                pass

            for guild in self.bot.guilds:

                guild_member = guild.get_member(member.id)
                if guild_member is None:
                    continue

                try:
                    await guild_member.ban(reason=reason)
                except Exception:
                    failed.append(guild.name)

            await self.bot.log_msg(
                f"`{member.name}#{member.discriminator}` has been ***SUPER BANNED***, for `{reason}`. Responsible owner: `{ctx.user.name}#{ctx.user.discriminator}`" + (
                "\n\nFailed to ban user in guilds: \n" + ", ".join(failed)) if failed else "")
            await ctx.respond(
                f"`{member.mention}` has been ***SUPER BANNED***, for `{reason}`"
                )

        @Cog.listener("on_member_join")
        async def ban_member_on_join(self, member: discord.Member):
            try:
                ban_count = await self.super_ban_db.count_documents({'banned_member_id': member.id})
            except Exception:
                ban_count = 0

            if ban_count == 0:
                return

            try:
                await member.send(
                    f"You have been banned from `{member.guild.name}`, as this is a part of the Venox Network, and you have previously been SUPER BANNED by one of the owners of Venox Network.")
            except Exception as e:
                self.bot.log_msg(
                    f"Failed to dm {member.name} with error: {e}"
                )

            try:
                await member.ban(reason="User was SUPER BANNED")
            except Exception:
                self.bot.log_msg(
                    f"Could not ***SUPER BAN*** `{member.name}#{member.discriminator}` on join, on server: {member.guild.name}")

    return SuperBan
