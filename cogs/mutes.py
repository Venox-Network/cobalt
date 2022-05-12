import datetime
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

global_report_channel = Global_Report_Channel
# global log channel
channel_id = Global_Log_Channel


class mutes(commands.Cog):

    def __init__(self, client):
        self.client = client

    @slash_command(description="Mute a member using the timeout function")
    @application_checks.has_permissions(moderate_members=True)
    async def mute(self, interaction: Interaction, member: nextcord.User, time, *, reason='None'):
        time = humanfriendly.parse_timespan(time)
        await member.edit(timeout=nextcord.utils.utcnow() + datetime.timedelta(seconds=time))
        log_channel = await client.fetch_channel(channel_id)
        await log_channel.send(f" `{member}` has been muted `{time}` for reason `{reason}`")
        await member.send(
            f"You have been muted for **{time}** for the reason **{reason}** in **{interaction.guild.name}**")
        await interaction.response.send_message(f" `{member}` has been muted `{time}` for reason `{reason}`")

    @application_checks.has_permissions(moderate_members=True)
    @slash_command(description="Un timeouts a member")
    async def unmute(self, interaction: Interaction, member: nextcord.Member):
        await member.edit(timeout=nextcord.utils.utcnow() + datetime.timedelta(seconds=1))
        log_channel = await client.fetch_channel(channel_id)
        await log_channel.send(f" `{member}` has been unmuted")
        await member.send(f"you have been unmuted")
        await interaction.response.send_message(f" {member} has been unmuted")


def setup(client):
    client.add_cog(mutes(client))
