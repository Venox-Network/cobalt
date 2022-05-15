from nextcord import Interaction, slash_command
from nextcord.ext import commands
from bot import client, Global_Report_Channel, Global_Log_Channel

global_report_channel = Global_Report_Channel
channel_id = Global_Log_Channel


class leaveserver(commands.Cog):
    def __init__(self, client):
        self.client = client

    @slash_command(description="Leaves a specific server")
    async def leaveserver(self, interaction: Interaction, guildid):
        bot_owners = [242385234992037888, 273538684526264320, 491266830674034699]
        guildint = int(guildid)
        if interaction.user.id in bot_owners:
            to_leave = client.get_guild(guildint)
            if to_leave is not None:
                await to_leave.leave()
                await interaction.send(f"Left `{to_leave.name}`")


def setup(client):
    client.add_cog(leaveserver(client))
