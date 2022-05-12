import datetime
import pymongo as pymongo
import asyncio
from nextcord import Interaction, SlashOption, ChannelType, slash_command, guild, Guild
from nextcord.abc import GuildChannel
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


class warnings(commands.Cog):

    def __init__(self, client):
        self.client = client

    @application_checks.has_permissions(moderate_members=True)
    @slash_command(description="Warns a member")
    async def warn(self, interaction: Interaction, member: nextcord.User, reason=None):
        from datetime import datetime
        id = member.id
        member_name = member.name
        ctx_guild_id = interaction.guild.id
        guildname = interaction.guild.name
        count_done = 1

        date = datetime.now().strftime("%Y-%m-%d")
        await warn_collection.insert_one({"warn_guild": ctx_guild_id, "memberid": id, "membername": member_name, "guildname": guildname, "reason": reason, "date": date, "moderator": interaction.user.name})
        count_done = await warn_collection.count_documents({"warn_guild": ctx_guild_id, "memberid": id})
        await interaction.send(f"`{member}` has been warned for `{reason}` this is warning number `{count_done}`")

    @application_checks.has_permissions(moderate_members=True)
    @slash_command(description="Shows a member's warnings")
    async def warns(self, interaction: Interaction, member: nextcord.User):
        ctx_guild_id = interaction.guild.id
        id = member.id
        warn_counts = await warn_collection.count_documents({"warn_guild": ctx_guild_id, "memberid": id})
        embed = nextcord.Embed(title=f"Warns for {member.name}:", description=f"`{member.name}` has `{warn_counts}` warns")

        cursor = warn_collection.find({"warn_guild": ctx_guild_id, "memberid": id})
        async for document in cursor:
            datelocal = document["date"]
            reasonlocal = document["reason"]
            rmoderator = document["moderator"]
            embed.add_field(name="Warn: ", value=f"**Date:** `{datelocal}` **Reason:** `{reasonlocal}` **Moderator:** `{rmoderator}`")
        await interaction.send(embed=embed)


def setup(client):
    client.add_cog(warnings(client))
