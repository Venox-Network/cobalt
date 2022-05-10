from nextcord import Interaction, SlashOption, ChannelType, slash_command, guild, Guild
from nextcord.abc import GuildChannel
import nextcord
from nextcord import Interaction, slash_command
from nextcord.ext import commands, application_checks

from bot import client, CLUSTER, Global_Report_Channel, Global_Log_Channel



class purge(commands.Cog):

    def __init__(self, client):
        self.client = client




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
    client.add_cog(purge(client))
