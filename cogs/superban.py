import datetime
import os
import nextcord
import pymongo as pymongo
import asyncio
from nextcord import Interaction, SlashOption, ChannelType, slash_command, guild, Guild
from nextcord.abc import GuildChannel
import os
import humanfriendly
import motor.motor_asyncio
import nextcord
from nextcord import Interaction, slash_command
from nextcord.ext import commands, application_checks

from bot import client, CLUSTER, Global_Report_Channel, Global_Log_Channel

cluster_local = CLUSTER

cluster = motor.motor_asyncio.AsyncIOMotorClient(cluster_local)
db = cluster["VenoxDB"]
collection = db["report_channels"]
warn_collection = db["warns"]
global_report_channel = Global_Report_Channel
# global log channel
channel_id = Global_Log_Channel


class superban(commands.Cog):

    def __init__(self, client):
        self.client = client


    @application_checks.has_permissions(ban_members=True)
    @slash_command(description="Bans a member from all guilds Venox is in")
    async def superban(self, interaction: Interaction, member: nextcord.User, *, reason="No reason given"):
        # srnyx and chrizs id
        bot_owners = ["242385234992037888", "273538684526264320"]
        if interaction.user.id in bot_owners:
            member_id = member.id
            for g in client.guilds:
                if m := await g.fetch_member(member_id):
                    await m.ban()
                    member.send(f"You have been banned from all venox servers for reason {reason}")
                    log_channel = await client.fetch_channel(channel_id)
                    await log_channel.send(f" `{member}` has been superbanned for reason `{reason}`")
                    await interaction.response.send_message("User was super banned for `" + reason + "`")
        else:
            await interaction.response.send_message("You are not the almighty Srnyx or Chriz you cannot do this")

    @application_checks.has_permissions(ban_members=True)
    @slash_command(description="Bans a member from all guilds Venox is in")
    async def superunban(self, interaction: Interaction, member: nextcord.User, *, reason="No reason given"):
        # srnyx and chrizs id
        bot_owners = ["242385234992037888", "273538684526264320"]
        if interaction.user.id in bot_owners:
            member_id = member.id
            for g in client.guilds:
                if m := await g.fetch_member(member_id):
                    await m.unban()
                    member.send(f"You have been unbanned from all venox servers for reason {reason}")
                    log_channel = await client.fetch_channel(channel_id)
                    await log_channel.send(f" `{member}` has been superunbanned for reason `{reason}`")
                    await interaction.response.send_message("User was super unbanned for `" + reason + "`")
        else:
            await interaction.response.send_message("You are not the almighty Srnyx or Chriz you cannot do this")


def setup(client):
    client.add_cog(superban(client))
