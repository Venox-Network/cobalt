import motor.motor_asyncio
from nextcord import Interaction, slash_command
from nextcord.ext import commands, application_checks

from bot import Global_Report_Channel, Global_Log_Channel, CLUSTER

global_report_channel = Global_Report_Channel
channel_id = Global_Log_Channel

cluster_local = CLUSTER

cluster = motor.motor_asyncio.AsyncIOMotorClient(cluster_local)
db = cluster["VenoxDB"]
threadedchannels = db["threadedchannels"]


class autothread(commands.Cog):
    def __init__(self, client):
        self.client = client

    @application_checks.has_permissions(manage_messages=True)
    @slash_command(name="autothread", description="Makes a thread from a message")
    async def autothread(self, interaction: Interaction, channel_db_id, on_or_off: bool):
        if on_or_off == True:
            await interaction.response.defer()
            data = {"_id": channel_id, "guild_id": interaction.guild.id, "guildname": interaction.guild.name,
                    "channel_db_id": int(channel_db_id)}
            try:
                await threadedchannels.insert_one(data)
                await interaction.send("Successfully added the channel to the database")
            except:
                getting_replaced = await threadedchannels.find_one({"_id": channel_db_id})
                await threadedchannels.replace_one(getting_replaced, data)
                await interaction.send("Successfully updated the channel in the database")
        if on_or_off == False:
            await interaction.response.defer()
            data = {"_id": channel_id, "guild_id": interaction.guild.id, "guildname": interaction.guild.name,
                    "channel_db_id": int(channel_db_id)}
            try:
                await threadedchannels.delete_one(data)
                await interaction.send("Successfully removed the channel from the database")
            except:
                await interaction.send("Channel not found in the database")

    @commands.Cog.listener()
    async def on_message(self, message):
        # ignores bots
        if message.author.bot:
            return
        # ignores messages that are not in the database
        results = await threadedchannels.count_documents({"channel_db_id": message.channel.id})
        if results == 1:
            # creates a thread named message content
            await message.create_thread(name=message.content)
        else:
            return


def setup(client):
    client.add_cog(autothread(client))
