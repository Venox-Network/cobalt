import datetime
import motor.motor_asyncio
from nextcord import SlashOption, ChannelType
from nextcord.abc import GuildChannel
from nextcord import Interaction, slash_command
from nextcord.ext import commands, application_checks
from bot import CLUSTER

cluster_local = CLUSTER

cluster = motor.motor_asyncio.AsyncIOMotorClient(cluster_local)
db = cluster["VenoxDB"]
dynamicslowmodedb = db["dynamic_slowmode"]


class dynamicslowmode(commands.Cog):
    def __init__(self, client):
        self.client = client

    @application_checks.has_permissions(manage_messages=True)
    @slash_command(
        description="Sets a slowmode that chnages depednign on how many messages were sent in the last minute")
    async def slowmodesetup(self, interaction: Interaction, amount_of_messages_per_min: int,
                            dynamic_max_slowmode_time: int, defaultslowmode: int,  channel: GuildChannel = SlashOption(channel_types=[ChannelType.text])):
        data = {"_id": channel.id, "channel_id": channel.id, "amount_of_messages_per_min": amount_of_messages_per_min,
                                "slowmode_time": dynamic_max_slowmode_time, "defaultslowmode": defaultslowmode}
        try:
            await dynamicslowmodedb.insert_one(data)
            await interaction.send("Slowmode set up")
        except:
            await dynamicslowmodedb.find_one_and_replace({"_id": channel.id}, data),
            await interaction.send("Dynamic slow-mode updated")

    @application_checks.has_permissions(manage_messages=True)
    @slash_command(
        description="Removes dynamic slowmode")
    async def removeslowmode(self, interaction: Interaction, channel: GuildChannel = SlashOption(channel_types=[ChannelType.text])):
        data = {"_id": channel.id}
        try:
            await dynamicslowmodedb.delete_one(data)
            await interaction.send(f"Slowmode removed for {channel.name}")
        except Exception as e:
            await interaction.send(f"Could not remove `{e}`")

    @commands.Cog.listener()
    async def on_message(self, message):
        if message.author.bot:
            return
        # db calls
        try:
            resultsfind = await dynamicslowmodedb.find_one({"channel_id": message.channel.id})
            amount_of_messages_per_min = resultsfind["amount_of_messages_per_min"]
            slowmode_time = resultsfind["slowmode_time"]
            channel_id_db = resultsfind["channel_id"]
            defaultslowmode = resultsfind["defaultslowmode"]
        except:
            return
        # checks the amount of messages per minute

        # gets the current time
        my_datetime = datetime.datetime.now()
        my_datetime_sec = my_datetime - datetime.timedelta(seconds=60)
        messages = await message.channel.history(limit=100, after=my_datetime_sec).flatten()
        if message.channel.id == channel_id_db:
            # checks if more messages was sent than the dynamic speed is
            if len(messages) >= amount_of_messages_per_min:
                # checks if the slowmode is already set
                if message.channel.slowmode_delay != slowmode_time:
                    # sets the slowmode
                    await message.channel.edit(slowmode_delay=slowmode_time)

            else:
                if message.channel.slowmode_delay != defaultslowmode:
                    # sets the slowmode
                    await message.channel.edit(slowmode_delay=defaultslowmode)


def setup(client):
    client.add_cog(dynamicslowmode(client))
