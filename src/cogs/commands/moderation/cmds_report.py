from typing import List
from discord import ApplicationContext
import discord
from discord.ext.commands import Cog
from discord.commands.options import Option
from cogs import BaseCog


def cog_creator(servers: List[int]):
    class ReportCog(BaseCog):

        def __init__(self, bot) -> None:
            super().__init__(bot)
            self.report_collection = self.bot.config.DATABASE["report_channels"]

        @Cog.listener()
        async def on_guild_channel_delete(self, channel: discord.TextChannel):

            if not isinstance(channel, discord.TextChannel):
                return

            try:
                await self.report_collection.delete_many({"reports_id": channel.id})
            except Exception as e:
                print(e)

        @BaseCog.cslash_command(
            description="Setup the report command",
            guild_ids=servers
        )
        async def report_setup(
                self,
                ctx: ApplicationContext,
                report_channel: Option(discord.TextChannel),
        ):

            report_channel: discord.TextChannel = report_channel

            required_perms = {"manage_guild": True}

            if not self.check_perms(ctx, required_perms):
                await ctx.respond(
                    "Sorry, you cannot use this command.",
                    ephemeral=True
                    )
                return

            data = {"guildid": ctx.guild.id, "reports_id": report_channel.id}
            try:
                try:
                    await self.report_collection.insert_one(data)
                    await ctx.respond(
                        f"Report channel setup and linked to '{report_channel.mention}'",
                        ephemeral=True
                        )

                except Exception:
                    replace = await self.report_collection.find_one({"guildid": ctx.guild.id})
                    await self.report_collection.replace_one(replace, data)

                    await ctx.respond(
                        f"Replaced previous report channel and linked new channel to '{report_channel.mention}'",
                        ephemeral=True)

            except Exception:
                await ctx.respond(
                    "Could not interract with database `report_channels`. Please try again after sometime and mention the issue to any staff member.",
                    ephemeral=True)

        @BaseCog.cslash_command(
            description="Report a user",
            guild_ids=servers
        )
        async def report(
                self,
                ctx: ApplicationContext,
                member: Option(discord.Member),
                reason: Option(str)
        ):

            member: discord.Member = member

            try:
                local_report_channel_id = (await self.report_collection.find_one({"guildid": ctx.guild.id}))[
                    "reports_id"]
            except Exception:
                await ctx.respond(
                    "Could not interract with database `report_channels`. Please try again after sometime and mention the issue to any staff member.",
                    ephemeral=True)
                return

            local_report_channel = ctx.guild.get_channel(local_report_channel_id)
            if local_report_channel is None:
                await self.bot.log_msg(
                    f"`{ctx.user}` tried to report on `{ctx.guild.name}`,"
                    f" local report channel for `{ctx.guild.name}` not found."
                    )
                await ctx.respond(
                    "Could not send report. Please mention ask any staff member for further help.",
                    ephemeral=True
                    )
                return

            await local_report_channel.send(
                f"`{ctx.user.name}#{ctx.user.discriminator}` has reported `{member.name}`, in channel `{ctx.channel.name}`, for reason {reason}")
            await self.bot.report_channel.send(
                f"`{ctx.user.name}#{ctx.user.discriminator}` has reported `{member.name}` in `{ctx.guild.name}`, in channel `{ctx.channel.name}`, for reason {reason}")
            await ctx.respond(
                f"Reported user {member.mention} for: `{reason}`",
                ephemeral=True
                )

    return ReportCog
