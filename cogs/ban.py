from nextcord import Interaction, SlashOption, ChannelType, slash_command, guild, Guild
from nextcord.abc import GuildChannel
import motor.motor_asyncio
import nextcord
from nextcord import Interaction, slash_command
from nextcord.ext import commands, application_checks

from bot import client, Global_Report_Channel, Global_Log_Channel

global_report_channel = Global_Report_Channel
# global log channel
channel_id = Global_Log_Channel


class ban(commands.Cog):

    def __init__(self, client):
        self.client = client

    @application_checks.has_permissions(ban_members=True)
    @slash_command(description="Bans a member from the guild")
    async def ban(self, interaction: Interaction, member: nextcord.User, *, reason=None):
        await member.ban(reason=reason)
        log_channel = await client.fetch_channel(channel_id)
        await log_channel.send(f" `{member}` has been banned for reason `{reason}`")
        await member.send(f"You have been banned for {reason}")
        await interaction.response.send_message(f'{member} has been banned for {reason}')


def setup(client):
    client.add_cog(ban(client))
