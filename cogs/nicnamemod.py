import nextcord
from nextcord import Interaction, SlashOption, ChannelType, slash_command
from nextcord.abc import GuildChannel
from nextcord.ext import commands

from bot import client

guildid = 873789547362803755


class nicnamemod(commands.Cog):

    def __init__(self, bot):
        self.client = client


    @slash_command(guild_ids=[guildid])


    @commands.Cog.listener()
    async def on_member_update(self, before, after):
        banned_nicks = ["h",
                        "i",
                        "x"]

        try:

            for nick in banned_nicks:
                print(nick)
                if nick in after.nick:
                    await after.edit(nick=before.nick)
                else:
                    return
        except:
            print("No nicnam    e detected")


def setup(client):
    client.add_cog(nicnamemod(client))
