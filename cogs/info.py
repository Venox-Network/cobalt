from nextcord import Interaction, SlashOption, ChannelType, slash_command, guild, Guild
from nextcord.abc import GuildChannel
import nextcord
from nextcord import Interaction, slash_command
from nextcord.ext import commands, application_checks

from bot import client, CLUSTER, Global_Report_Channel, Global_Log_Channel

global_report_channel = Global_Report_Channel
# global log channel
channel_id = Global_Log_Channel


class info(commands.Cog):

    def __init__(self, client):
        self.client = client

    @slash_command(description="Sends link to support server")
    async def support(self, interaction: Interaction):
        await interaction.response.send_message('Join our support server https://discord.gg/kaddCVeRj6')

    @slash_command(description="Sends the number of guilds Venox is in")
    async def guilds(self, interaction: Interaction):
        r = ""
        for guild in client.guilds:
            r += f"`{guild}` \n"
        await interaction.response.send_message(f"Venox is in {len(client.guilds)} servers")
        await interaction.send(r)


def setup(client):
    client.add_cog(info(client))
