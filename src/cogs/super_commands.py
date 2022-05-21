from typing import List
from discord import ApplicationContext
import discord
from discord.ext.commands import Cog
from discord.commands.options import Option
from . import BaseCog

def cog_creator(servers: List[int]):
    class SuperCog(BaseCog):

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
            reason: Option(str)=None
        ):
            if not (ctx.user.id in (self.bot.config.OWNERS)):
                await ctx.respond(f"Sorry, you cannot use this command.", ephemeral=True)
                return

            #member: discord.Member = member

            failed = []

            try:
                await member.send(f"You have been kicked from **all** Venox Network Servers, for `{reason}`. Responsible owner: `{ctx.user.name}#{ctx.user.discriminator}`")
            except Exception:
                pass

            for guild in self.bot.guilds:

                guild_member = guild.get_member(member.id)
                if guild_member is None:
                    continue

                try:
                    await guild_member.kick(reason=reason)
                except Exception:
                    failed.append(guild.name)

            await self.bot.log_msg(f"`{member.name}#{member.discriminator}` has been kicked from **all** Venox Network Servers, for `{reason}`. Responsible owner: `{ctx.user.name}#{ctx.user.discriminator}`" + (("\n\nFailed to kick user in guilds: \n" + ", ".join(failed)) if failed else ""))
            await ctx.respond(f"`{member.mention}` has been kicked from **all** Venox Network Servers, for `{reason}`")

        @BaseCog.cslash_command(
            description="Bans a member from all guilds Venox moderates in",
            guild_ids=servers
        )
        async def super_ban(
            self,
            ctx: ApplicationContext,
            member: Option(discord.Member),
            reason: Option(str)=None
        ):
            if not (ctx.user.id in (self.bot.config.OWNERS)):
                await ctx.respond(f"Sorry, you cannot use this command.", ephemeral=True)
                return

            #member: discord.Member = member

            failed = []

            try:
                await self.super_ban_db.insert_one({'banned_member_id': member.id, 'banned_member_name': member.name, 'superban_user': ctx.user.name})
            except Exception:
                ctx.respond("Could not interract with database `superbanids`. Please try again after sometime.", ephemeral=True)
                return

            try:
                await member.send(f"You have been banned from **all** Venox Network Servers, for `{reason}`. Responsible owner: `{ctx.user.name}#{ctx.user.discriminator}`")
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

            await self.bot.log_msg(f"`{member.name}#{member.discriminator}` has been ***SUPER BANNED***, for `{reason}`. Responsible owner: `{ctx.user.name}#{ctx.user.discriminator}`" + ("\n\nFailed to ban user in guilds: \n" + ", ".join(failed)) if failed else "")
            await ctx.respond(f"`{member.mention}` has been ***SUPER BANNED***, for `{reason}`")

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
                ctx.respond(f"User with the id `{user_id}` not found.", ephemeral=True)
                return

            try:
                await self.super_ban_db.delete_one({'banned_member_id': user_id})
            except Exception:
                ctx.respond("Could not interract with database `superbanids`. Please try again after sometime.", ephemeral=True)
                return

            failed = []

            for guild in self.bot.guilds:
                try:
                    guild.unban(user)
                except Exception:
                    failed.append(guild.name)

            await self.bot.log_msg(f"`{user.name}#{user.discriminator}` has been ***SUPER UN BANNED***. Responsible owner: `{ctx.user.name}#{ctx.user.discriminator}`" + ("\n\nFailed to un-ban user in guilds: \n" + ", ".join(failed)) if failed else "")
            await ctx.respond(f"`{user.mention}` has been ***SUPER UN BANNED***")

        @Cog.listener("on_member_join")
        async def ban_member_on_join(self, member: discord.Member):
            try:
                ban_count = await self.super_ban_db.count_documents({'banned_member_id': member.id})
            except Exception:
                ban_count = 0

            if ban_count == 0:
                return

            try:
                await member.send(f"You have been banned from `{member.guild.name}`, as this is a part of the Vnox Network, and you have previously been SUPER BANNED by one of the owners of Venox.")
            except Exception:
                pass

            try:
                await member.ban(reason="User was SUPER BANNED")
            except Exception:
                self.bot.log_msg(f"Could not ***SUPER BAN*** `{member.name}#{member.discriminator}` on join, on server: {member.guild.name}")

    return SuperCog