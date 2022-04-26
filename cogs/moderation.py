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


class moderation(commands.Cog):

    def __init__(self, client):
        self.client = client

    @application_checks.has_permissions(manage_guild=True)
    @slash_command(description="Setup the report command")
    async def reportsetup(self, interaction: Interaction, report_channel_id):
        ctxchannel = interaction.channel.id
        channelfletched = await client.fetch_channel(ctxchannel)
        await channelfletched.send("ran")
        try:
            ctxguild_id = str(interaction.guild.id)
            data = {"_id": ctxguild_id, "guildid": ctxguild_id, "reports_id": report_channel_id}
            await collection.insert_one(data)
            channelfletched.send("inserted")
            await interaction.send("Report channel setup!")
        except:
            getting_replaced = await collection.find_one({"_id": ctxguild_id})
            for data in getting_replaced:
                await channelfletched.send(data)
            await channelfletched.send("found existed")
            await collection.replace_one(getting_replaced, data)
            await channelfletched.send("replaced")
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

    @application_checks.has_permissions(moderate_members=True)
    @slash_command(description="Warn a member for there wrong doing")
    async def warn(self, interaction: Interaction, member: nextcord.User, reason=None):
        from datetime import datetime
        id = member.id
        member_name = member.name
        ctx_guild_id = interaction.guild.id
        guildname = interaction.guild.name
        count_done = 1

        date = datetime.now().strftime("%Y-%m-%d")
        await warn_collection.insert_one({"warn_guild": ctx_guild_id, "memberid": id, "reason": reason, "date": date})
        count_done = await warn_collection.count_documents({"warn_guild": ctx_guild_id, "memberid": id})
        await interaction.send(f"`{member}` has been warned for `{reason}` this is warning number `{count_done}`")

    @application_checks.has_permissions(moderate_members=True)
    @slash_command(description="See a members warns")
    async def warns(self, interaction: Interaction, member: nextcord.User):
        ctx_guild_id = interaction.guild.id
        id = member.id
        warn_counts = await warn_collection.count_documents({"warn_guild": ctx_guild_id, "memberid": id})
        embed = nextcord.Embed(title=f"Warns for {member.name}:", description=f"`{member.name}` has `{warn_counts}` warns")

        cursor = warn_collection.find({"warn_guild": ctx_guild_id, "memberid": id})
        async for document in cursor:
            datelocal = document["date"]
            reasonlocal = document["reason"]
            embed.add_field(name="Warn: ", value=f"Date: `{datelocal}` Reason: `{reasonlocal}`")
        await interaction.send(embed=embed)

    @slash_command(description="Mute a member using the timeout function")
    @application_checks.has_permissions(moderate_members=True)
    async def mute(self, interaction: Interaction, member: nextcord.User, time, *, reason='None'):
        time = humanfriendly.parse_timespan(time)
        await member.edit(timeout=nextcord.utils.utcnow() + datetime.timedelta(seconds=time))
        log_channel = await client.fetch_channel(channel_id)
        await log_channel.send(f" `{member}` has been muted `{time}` for reason `{reason}`")
        await member.send(f"You have been muted for **{time}** for the reason **{reason}** in **{interaction.guild.name}**")
        await interaction.response.send_message(f" `{member}` has been muted `{time}` for reason `{reason}`")

    @application_checks.has_permissions(moderate_members=True)
    @slash_command(description="Un timeouts a member")
    async def unmute(self, interaction: Interaction, member: nextcord.Member):

        await member.edit(timeout=nextcord.utils.utcnow() + datetime.timedelta(seconds=1))
        log_channel = await client.fetch_channel(channel_id)
        await log_channel.send(f" `{member}` has been unmuted")
        await member.send(f"you have been unmuted")
        await interaction.response.send_message(f" {member} has been unmuted")

    @application_checks.has_permissions(kick_members=True)
    @slash_command(description="Kick a member")
    async def kick(self, interaction: Interaction, member: nextcord.User, *, reason=None):
        await member.kick(reason=reason)
        log_channel = await client.fetch_channel(channel_id)
        await log_channel.send(f" `{member}` has been kicked for reason `{reason}`")
        await member.send(f"You have been kicked for {reason}")
        await interaction.response.send_message(f"{member} has been kicked for {reason}")

    @application_checks.has_permissions(ban_members=True)
    @slash_command(description="Bans a member from the guild")
    async def ban(self, interaction: Interaction, member: nextcord.User, *, reason=None):
        await member.ban(reason=reason)
        log_channel = await client.fetch_channel(channel_id)
        await log_channel.send(f" `{member}` has been banned for reason `{reason}`")
        await member.send(f"You have been banned for {reason}")
        await interaction.response.send_message(f'{member} has been banned for {reason}')

    @slash_command(description="Sends link to support server")
    async def support(self, interaction: Interaction):
        await interaction.response.send_message('Join our support server https://discord.gg/kaddCVeRj6')

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

    @slash_command(description="Sends the number of guilds Venox is in")
    async def guilds(self, interaction: Interaction):
        await interaction.response.send_message(f"Venox is in {len(client.guilds)} servers")

    @application_checks.has_permissions(manage_messages=True)
    @slash_command(
        description="With a member it checks the last defined amount of messages and purges any that was sent by user")
    async def purge(self, interaction: Interaction, amount: int = 5, member: nextcord.User = "None"):
        if member != "None":
            await interaction.channel.purge(limit=amount, check=lambda message: message.author == member)
            await interaction.send(
                f"Checked the last `{amount}` messages to see if any is from `{member}` and purged it if found any")
        else:
            await interaction.channel.purge(limit=amount)
            await interaction.send(f"Purged that last {amount} of messages")


def setup(client):
    client.add_cog(moderation(client))
