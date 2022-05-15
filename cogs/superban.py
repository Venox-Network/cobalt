import datetime, os, nextcord, asyncio, humanfriendly, motor.motor_asyncio
import pymongo as pymongo
from nextcord import Interaction, SlashOption, ChannelType, slash_command, guild, Guild
from nextcord.abc import GuildChannel
from nextcord.ext import commands, application_checks

from bot import client, CLUSTER, Global_Report_Channel, Global_Log_Channel

global_report_channel = Global_Report_Channel
# global log channel
channel_id = Global_Log_Channel

cluster_local = CLUSTER
cluster = motor.motor_asyncio.AsyncIOMotorClient(cluster_local)
db = cluster["VenoxDB"]
superbandb = db["superbanids"]

class superban(commands.Cog):

    def __init__(self, client):
        self.client = client


    @application_checks.has_permissions(ban_members=True)
    @slash_command(description="Bans a member from all guilds Venox is in")
    async def superban(self, interaction: Interaction, member: nextcord.User, *, reason="No reason given"):
        # srnyx and chrizs id
        bot_owners = [242385234992037888, 273538684526264320]
        if interaction.user.id in bot_owners:
            await superbandb.insert_one({'banned_member_id': member.id, 'banned_member_name': member.name, 'superban_user': interaction.user.name})
            member_id = member.id
            for g in client.guilds:
                if m := await g.fetch_member(member_id):
                    await m.ban()
                    await member.send(f"You have been banned from all Venox Network servers for `{reason}`")
                    log_channel = await client.fetch_channel(channel_id)
                    await log_channel.send(f"`{member.name}` has been superbanned for `{reason}`")
                    await interaction.response.send_message("User was super banned for `" + reason + "`")
        else:
            await interaction.response.send_message("You do not have permission to run this command")

    @application_checks.has_permissions(ban_members=True)
    @slash_command(description="Unbans a member from all guilds Venox is in")
    async def superunban(self, interaction: Interaction, member, *, reason="No reason given"):
        # srnyx and chrizs id
        bot_owners = [242385234992037888, 273538684526264320]
        if interaction.user.id in bot_owners:
            obj = nextcord.Object(int(member))
            try:
                await superbandb.delete_one({'banned_member_id': member})
            except Exception as e:
                await interaction.send(f"Could not remove user from db `{e}`")
            for g in client.guilds:
                try:
                    await g.unban(obj)
                except Exception as e:
                    pass

            log_channel = await client.fetch_channel(channel_id)
            await log_channel.send(f"`{member}` has been superunbanned for `{reason}`")
            await interaction.send("User was un-superbanned for `" + reason + "`")
        else:
            await interaction.response.send_message("You do not have permission to run this command")

    @commands.Cog.listener()
    async def on_member_join(self, member):
        count = await superbandb.count_documents({'banned_member_id': member.id})
        print(count)
        if count == 0:
            return
        else:
            await member.send("You are superbanned")
            await member.ban(reason="User is superbanned")


def setup(client):
    client.add_cog(superban(client))
