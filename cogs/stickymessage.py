import motor.motor_asyncio
import nextcord
from nextcord import Interaction, slash_command
from nextcord.ext import commands, application_checks

from bot import Global_Report_Channel, Global_Log_Channel, CLUSTER

global_report_channel = Global_Report_Channel
channel_id = Global_Log_Channel

cluster_local = CLUSTER

cluster = motor.motor_asyncio.AsyncIOMotorClient(cluster_local)
db = cluster["VenoxDB"]
sticky_messages = db["sticky_messages"]


class stickymessage(commands.Cog):
    def __init__(self, client):
        self.client = client

    @application_checks.has_permissions(manage_messages=True)
    @slash_command(name="stickysetup", description="Creates a sticky message")
    async def stickysetup(self, interaction: Interaction, channel_db_id, *, stickymessage, stickytitle="Sticky title"):

        await interaction.response.defer()
        data = {"_id": channel_id, "guild_id": interaction.guild.id, "guildname": interaction.guild.name,
                "channel_db_id": int(channel_db_id), "stickymessage": stickymessage, "stickytitle": stickytitle}
        try:
            await sticky_messages.insert_one(data)
            await interaction.send("Successfully added the channel to the database")
        except:
            getting_replaced = await sticky_messages.find_one({"_id": channel_db_id})
            await sticky_messages.replace_one(getting_replaced, data)
            await interaction.send("Successfully updated the channel in the database")

    @slash_command(name="stickyremove", description="Removes a sticky message")
    @application_checks.has_permissions(manage_messages=True)
    async def stickyremove(self, interaction: Interaction, channel_db_id):
        await interaction.response.defer()
        data = {"_id": channel_id, "guild_id": interaction.guild.id, "guildname": interaction.guild.name,
                "channel_db_id": int(channel_db_id)}
        try:
            await sticky_messages.delete_one(data)
            await interaction.send("Successfully removed the channel from the database")
        except:
            await interaction.send("Channel not found in the database")

    @commands.Cog.listener()
    async def on_message(self, message):
        # ignores bots
        if message.author.bot:
            return
        # ignores messages that are not in the database
        results = await sticky_messages.count_documents({"channel_db_id": message.channel.id})
        if results == 1:
            # check if the message was sent by the bot
            await message.channel.purge(limit=10, check=lambda message: message.author.id == self.client.user.id)
            resultsfind = await sticky_messages.find_one({"channel_db_id": message.channel.id})
            em = nextcord.Embed(title=resultsfind["stickytitle"], color=0x0070c0, description=resultsfind["stickymessage"])
            await message.channel.send(embed=em)
        else:
            return


def setup(client):
    client.add_cog(stickymessage(client))
