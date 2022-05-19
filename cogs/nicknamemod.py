import nextcord
from nextcord.ext import commands, application_checks
import motor.motor_asyncio
from bot import CLUSTER
from nextcord import Interaction, slash_command

cluster_local = CLUSTER

cluster = motor.motor_asyncio.AsyncIOMotorClient(cluster_local)
db = cluster["VenoxDB"]
nicknames = db["nicknames"]
new_nicknames = db["new_nicknames"]


class nicknamemod(commands.Cog):
    def __init__(self, client):
        self.client = client

    @application_checks.has_permissions(administrator=True)
    @slash_command(description="Sets new nickname for vialator")
    async def blacklistnewnick(self, interaction: Interaction, *, newnickname):
        await interaction.response.defer()
        results = await new_nicknames.find_one({"_id": interaction.guild.id})
        if results is None:
            await new_nicknames.insert_one({"_id": interaction.guild.id, "new_nickname": newnickname})
            await interaction.send("New nickname set")
        else:
            await new_nicknames.update_one({"_id": interaction.guild.id}, {"$set": {"new_nickname": newnickname}})
            await interaction.send("New nickname set")

    @application_checks.has_permissions(administrator=True)
    @slash_command(description="Adds a nickname to the blacklist")
    async def blacklist_add(self, interaction: Interaction, *, nickname):
        await interaction.response.defer()
        data = {"_id": interaction.guild.id, "nicknames": [nickname], "guild_id": interaction.guild.id}
        count = await nicknames.count_documents({"_id": interaction.guild.id})
        results = await nicknames.find_one({"_id": interaction.guild.id})

        # inserts the new nickname into the database
        if count == 0:
            await nicknames.insert_one(data)
            await interaction.send("Added nickname to blacklist")
        else:
            if nickname in results["nicknames"]:
                await interaction.send("Nickname already in blacklist")
            else:
                await nicknames.update_one({"_id": interaction.guild.id}, {"$push": {"nicknames": nickname}})
                await interaction.send("Added nickname to blacklist")

    @application_checks.has_permissions(administrator=True)
    @slash_command(description="Removes a nickname from the blacklist")
    async def blacklist_remove(self, interaction: Interaction, *, nickname):
        results = await nicknames.find_one({"_id": interaction.guild.id})
        if nickname in results["nicknames"]:
            await nicknames.update_one({"_id": interaction.guild.id}, {"$pull": {"nicknames": nickname}})
            await interaction.send("Removed nickname from blacklist")
        else:
            await interaction.send("Nickname not in blacklist")

    @application_checks.has_permissions(administrator=True)
    @slash_command(description="Lists all nicknames in the blacklist")
    async def blacklist_list(self, interaction: Interaction):
        await interaction.response.defer()
        results = await nicknames.find_one({"_id": interaction.guild.id})
        if results is None:
            await interaction.send("No nicknames in blacklist")
        else:
            await interaction.send("Nicknames in blacklist: " + str(results["nicknames"]))

    @application_checks.has_permissions(administrator=True)
    @slash_command(description="Checks if a member has a nickname in the blacklist")
    async def blacklist_check(self, interaction: Interaction, member: nextcord.User):
        await interaction.response.defer()
        results = await nicknames.find_one({"_id": interaction.guild.id})
        if results is None:
            await interaction.send("No nicknames in blacklist")
        else:
            if member.display_name in results["nicknames"]:
                await interaction.send("Nickname in blacklist")
            else:
                await interaction.send("Nickname not in blacklist")

    @commands.Cog.listener()
    async def on_member_update(self, before, after):
        if before.display_name != after.display_name:
            results = await nicknames.find_one({"_id": after.guild.id})
            if results is None:
                return
            if after.display_name in results["nicknames"]:
                await after.edit(nick="Please don't use this nickname")

    @commands.Cog.listener()
    async def on_member_join(self, member):
        results = await nicknames.find_one({"_id": member.guild.id})
        if results is None:
            return
        if member.display_name in results["nicknames"]:
            results = await new_nicknames.find_one({"_id": member.guild.id})
            if results is not None:
                await member.edit(nick=results["new_nickname"])
            

def setup(client):
    client.add_cog(nicknamemod(client))
