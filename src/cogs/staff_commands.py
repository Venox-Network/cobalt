from datetime import datetime, timedelta
from typing import List
from discord import ApplicationContext
import discord
import humanfriendly
from . import BaseCog
from discord.commands.options import Option


def cog_creator(servers: List[int]):
    class StaffCog(BaseCog):

        def __init__(self, bot) -> None:
            super().__init__(bot)
            self.warn_collection = self.bot.config.DATABASE["warns"]

        @BaseCog.cslash_command(
            description="Bans a member from the guild",
            guild_ids=servers
        )
        async def ban(
                self,
                ctx: ApplicationContext,
                member: Option(discord.Member),
                reason: Option(str) = None
        ):
            # member: discord.Member = member

            required_perms = {"ban_members": True}

            if not self.check_perms(ctx, required_perms, member):
                await ctx.respond(f"Sorry, you cannot use this command.", ephemeral=True)
                return

            try:
                await member.send(
                    f"You have been banned from `{ctx.guild.name}` for `{reason}`. Responsible moderator: `{ctx.user.name}#{ctx.user.discriminator}`")
            except Exception:
                pass

            await member.ban(reason=reason)
            await self.bot.log_msg(
                f"`{member.name}#{member.discriminator}` has been banned from `{ctx.guild.name}`, for reason: `{reason}`. Responsible moderator: `{ctx.user.name}#{ctx.user.discriminator}`")
            await ctx.respond(f"'{member.mention}' has been banned for `{reason}`", ephemeral=True)

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

        @BaseCog.cslash_command(
            description="Mutes a member using the timeout function",
            guild_ids=servers
        )
        async def mute(
                self,
                ctx: ApplicationContext,
                member: Option(discord.Member),
                time: Option(str),
                reason: Option(str) = None
        ):
            # member: discord.Member = member

            required_perms = {"moderate_members": True}

            if not self.check_perms(ctx, required_perms, member):
                await ctx.respond(f"Sorry, you cannot use this command.", ephemeral=True)
                return

            try:
                time_readable = humanfriendly.parse_timespan(time)
            except Exception:
                await ctx.respond("Cannot parse time, please retry the command.", ephemeral=True)

            time_final = timedelta(seconds=time_readable)
            time_unix = int((datetime.now() + time_final).timestamp())

            await member.timeout_for(timedelta(seconds=time_readable), reason=reason)

            try:
                await member.send(
                    f"You have been muted in `{ctx.guild.name}`, till: <t:{time_unix}>, for `{reason}`. Responsible moderator: Responsible moderator: `{ctx.user.name}#{ctx.user.discriminator}`")
            except Exception:
                pass

            await self.bot.log_msg(
                f"`{member.name}#{member.discriminator}` has been muted in `{ctx.guild.name}`, till: <t:{time_unix}>, for reason: `{reason}`. Responsible moderator: `{ctx.user.name}#{ctx.user.discriminator}`")
            await ctx.respond(f"'{member.mention}' has been muted, till: <t:{time_unix}>, for `{reason}`",
                              ephemeral=True)

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
                await ctx.respond(f"Sorry, you cannot use this command.", ephemeral=True)
                return

            if member.timed_out:
                await member.edit(communication_disabled_until=None, reason=reason)
            else:
                await ctx.respond(f"'{member.mention}' has not been muted to be unmuted", ephemeral=True)
                return

            try:
                await member.send(
                    f"You have been unmuted in `{ctx.guild.name}`, for `{reason}`. Responsible moderator: `{ctx.user.name}#{ctx.user.discriminator}`")
            except Exception:
                pass

            await self.bot.log_msg(
                f"`{member.name}#{member.discriminator}` has been unmuted in `{ctx.guild.name}`, for reason: `{reason}`. Responsible moderator: `{ctx.user.name}#{ctx.user.discriminator}`")
            await ctx.respond(f"'{member.mention}' has been unmuted, for `{reason}`", ephemeral=True)

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
                await ctx.respond(f"Sorry, you cannot use this command.", ephemeral=True)
                return

            try:
                if member is not None:
                    await ctx.channel.purge(limit=amount, check=(lambda message: message.author == member))
                    await ctx.respond(f"Purged `{amount}` message(s) sent by '{member.mention}' in this channel.",
                                      ephemeral=True)
                    return

                await ctx.channel.purge(limit=amount)
                await ctx.respond(f"Purged `{amount}` message(s) from this channel", ephemeral=True)
            except Exception:
                await ctx.respond("Could not purge all the messages from this channel", ephemeral=True)

        @BaseCog.cslash_command(
            description="Leaves a particular server",
            guild_ids=servers
        )
        async def leave_server(
                self,
                ctx: ApplicationContext,
                guild_id: Option(int)
        ):

            if not (ctx.user.id in (self.bot.config.OWNERS)):
                await ctx.respond(f"Sorry, you cannot use this command.", ephemeral=True)
                return

            guild = self.bot.get_guild(guild_id)
            if guild is None:
                await ctx.respond(f"Couldn't find the guild with id of `{guild_id}`", ephemeral=True)
                return

            try:
                await guild.leave()
            except Exception:
                await ctx.respond(f"Failed to leave guild `{guild.name}`", ephemeral=True)
                return

            await self.bot.config.DATABASE["new_nicknames"].delete_many({"guild_id": guild.id})
            await self.bot.config.DATABASE["nicknames"].delete_many({"guild_id": guild.id})

            await self.bot.config.DATABASE["report_channels"].delete_many({"guildid": guild.id})

            await self.bot.config.DATABASE["warns"].delete_many({"warn_guild": guild.id})

            await self.bot.log_msg(
                f"Left guild `{guild.name}`. Responsible owner: `{ctx.user.name}#{ctx.user.discriminator}`")
            await ctx.respond(f"Left guild `{guild.name}`", ephemeral=True)

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
                await ctx.respond(f"Sorry, you cannot use this command.", ephemeral=True)
                return

            today_date = datetime.now().strftime("%Y-%m-%d")

            try:
                await self.warn_collection.insert_one(
                    {"warn_guild": ctx.guild.id, "memberid": member.id, "membername": member.name,
                     "guildname": ctx.guild.name, "reason": reason, "date": today_date, "moderator": ctx.user.name})
                count = int(
                    await self.warn_collection.count_documents({"warn_guild": ctx.guild.id, "memberid": member.id}))
            except Exception:
                await ctx.respond("Could not interract with database `warns`. Please try again after sometime.",
                                  ephemeral=True)
                return

            last_digit = count % 10

            if last_digit == 1:
                count = str(count) + "st"
            elif last_digit == 2:
                count = str(count) + "nd"
            elif last_digit == 2:
                count = str(count) + "rd"
            else:
                count = str(count) + "th"

            await ctx.respond(f"{member.mention} has been warned for `{reason}`. This is their `{count}` warning.")

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
                await ctx.respond(f"Sorry, you cannot use this command.", ephemeral=True)
                return

            try:
                documents = self.warn_collection.find({"warn_guild": ctx.guild_id, "memberid": member.id})
                count = int(
                    await self.warn_collection.count_documents({"warn_guild": ctx.guild.id, "memberid": member.id}))
            except Exception:
                await ctx.respond("Could not interract with database `warns`. Please try again after sometime.",
                                  ephemeral=True)
                return

            embed = discord.Embed(title=f"Warns for {member.name}:",
                                  description=f"`{member.name}` has `{count}` warns.")

            async for document in documents:
                embed.add_field(name="Warn: ",
                                value=f"**Date:** `{document['date']}`\n**Reason:** `{document['reason']}`\n**Moderator:** `{document['moderator']}`")

            await ctx.respond(embed=embed)

    return StaffCog
