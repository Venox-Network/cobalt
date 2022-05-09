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


class kick(commands.Cog):

    def __init__(self, client):
        self.client = client

    @application_checks.has_permissions(kick_members=True)
    @slash_command(description="Kick a member")
    async def kick(self, interaction: Interaction, member: nextcord.User, *, reason=None):
        await member.kick(reason=reason)
        log_channel = await client.fetch_channel(channel_id)
        await log_channel.send(f" `{member}` has been kicked for reason `{reason}`")
        try:
            await member.send(f"You have been kicked for **{reason}**")
        except:
            await interaction.send(f"Failed to DM **{member}**")
        await interaction.response.send_message(f"**{member}** has been kicked for **{reason}**")


def setup(client):
    client.add_cog(kick(client))
