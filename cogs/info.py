from nextcord import Interaction, slash_command
from nextcord.ext import commands
from bot import client, Global_Report_Channel, Global_Log_Channel

global_report_channel = Global_Report_Channel
channel_id = Global_Log_Channel


class info(commands.Cog):
    def __init__(self, client):
        self.client = client

    @slash_command(description="Sends link to support server")
    async def support(self, interaction: Interaction):
        await interaction.response.send_message('Join our support server https://discord.gg/kaddCVeRj6')

    @slash_command(description="Lists all the servers the bot is in")
    async def servers(self, interaction: Interaction, ephemeral: bool = False):
        bot_owners = [242385234992037888, 273538684526264320]
        if interaction.user.id in bot_owners:
            guilds = client.guilds
            guild_list = []
            for guild in guilds:
                guild_list.append(f"**{guild.name},** id: `{guild.id}`")
            if ephemeral:
                await interaction.send("\n".join(guild_list), ephemeral=True)
            else:
                await interaction.response.send_message("\n".join(guild_list), ephemeral=False)
        else:
            await interaction.response.send_message("You do not have permission to use this command")


def setup(client):
    client.add_cog(info(client))
