import asyncio
import nextcord
from nextcord import Interaction, slash_command
from nextcord.ext import commands, application_checks


class react(commands.Cog):

    def __init__(self, client):
        self.client = client

    @application_checks.has_permissions(manage_messages=True)
    @slash_command()
    async def react(self, interaction: Interaction, emojis, message_id):
        await interaction.response.defer()
        channel = interaction.channel
        msg = await channel.fetch_message(message_id)
        emojilist = emojis.split(' ')
        for emoji in emojilist:
            await msg.add_reaction(emoji)
            await asyncio.sleep(1)
        await interaction.send("added emojis", ephemeral=True)


def setup(client):
    client.add_cog(react(client))
