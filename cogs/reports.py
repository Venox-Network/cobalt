import datetime, os, nextcord, asyncio, humanfriendly, motor.motor_asyncio
import pymongo
from nextcord import Interaction, SlashOption, ChannelType, slash_command, guild, Guild
from nextcord.abc import GuildChannel
from nextcord.ext import commands, application_checks

from bot import client, CLUSTER, Global_Report_Channel, Global_Log_Channel

global_report_channel = Global_Report_Channel
# global log channel
channel_id = Global_Log_Channel


class reports(commands.Cog):

    def __init__(self, client):
        self.client = client

    @application_checks.has_permissions(manage_guild=True)
    @slash_command(description="Setup the report command")
    async def reportsetup(self, interaction: Interaction, report_channel_id):
        ctxchannel = interaction.channel.id
        channelfletched = await client.fetch_channel(ctxchannel)
        try:
            ctxguild_id = str(interaction.guild.id)
            data = {"_id": ctxguild_id, "guildid": ctxguild_id, "reports_id": report_channel_id}
            await collection.insert_one(data)
            await interaction.send("Report channel setup!")
        except:
            getting_replaced = await collection.find_one({"_id": ctxguild_id})
            await collection.replace_one(getting_replaced, data)
            await interaction.send("Replaced report channel")

    @slash_command(description="Report a user and get a response asap")
    async def report(self, interaction: Interaction, member: nextcord.Member, reason=None):
        try:
            # sending to global report channel
            global_channel = await client.fetch_channel(global_report_channel)
            await global_channel.send(
                f"`{interaction.user}` has reported `{member}` in `{interaction.guild.name}` in channel `{interaction.channel.name}` for reason `{reason}`")
            await interaction.send("Reported thank you", ephemeral=True)
            # sending to defined report channel
            ctxguild_id = str(interaction.guild.id)
            results = await collection.find_one({"_id": ctxguild_id})
            server_report_channel = await client.fetch_channel(results["reports_id"])
            await server_report_channel.send(
                f"`{interaction.user}` has reported `{member}`  in channel `{interaction.channel.name}` for reason `{reason}`")
            log_channel = await client.fetch_channel(channel_id)
            await log_channel.send(
                f"`{interaction.user}` has reported `{member}`  in channel `{interaction.channel.name}` for reason `{reason}`")
        except:
            await interaction.send("Report failed please notify staff", ephemeral=True)


def setup(client):
    client.add_cog(reports(client))
