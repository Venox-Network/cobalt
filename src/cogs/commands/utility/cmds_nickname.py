from typing import List
from discord import ApplicationContext
import discord
from discord.ext.commands import Cog
from discord.commands.options import Option
from cogs import BaseCog


def cog_creator(servers: List[int]):
    class NickNameCog(BaseCog):

        def __init__(self, bot) -> None:
            super().__init__(bot)
            self.nick_names = self.bot.config.DATABASE["nicknames"]
            self.guild_nick_name = self.bot.config.DATABASE["new_nicknames"]
            self.default_nickname = "This (nick)name is banned"

        async def correct_nickname(self, after: discord.Member):
            try:
                # result = await self.guild_nick_name.find_one({"guild_id": before.guild.id})
                nicknames = await self.nick_names.find_one({"guild_id": after.guild.id})
                if nicknames is None:
                    return

                print("Server contains blacklist")

                for blacklisted_name in nicknames["nicknames"]:
                    print("Blacklisted:", blacklisted_name)
                    if blacklisted_name in after.display_name.lower():

                        print("Found, Replacing name:", blacklisted_name)
                        nick_replace = await self.guild_nick_name.find_one({"guild_id": after.guild.id})

                        if nick_replace is None:
                            new_nick = self.default_nickname
                        else:
                            new_nick = nick_replace["new_nickname"]

                        print("New nickname:", new_nick)

                        await after.edit(nick=new_nick)
                        return

            except Exception as e:
                print(e)

        @Cog.listener()
        async def on_member_join(self, member: discord.Member):
            await self.correct_nickname(member)

        @Cog.listener()
        async def on_member_update(self, before: discord.Member, after: discord.Member):
            if before.display_name == after.display_name:
                return

            await self.correct_nickname(after)

        @BaseCog.cslash_command(
            description="Marks name as the nickname to be set for violators",
            guild_ids=servers
        )
        async def setup_nickname(
                self,
                ctx: ApplicationContext,
                new_nickname: Option(str)
        ):

            required_perms = {"administrator": True}

            if not self.check_perms(ctx, required_perms):
                await ctx.respond(
                    "Sorry, you cannot use this command.",
                    ephemeral=True
                    )
                return

            try:
                await ctx.response.defer(ephemeral=True)
                result = await self.guild_nick_name.find_one({"guild_id": ctx.guild.id})
                data = {"guild_id": ctx.guild.id, "new_nickname": new_nickname}
                if result is None:
                    await self.guild_nick_name.insert_one(data)
                    await ctx.respond(
                        f"New Nickname set for this server as `{new_nickname}`",
                        ephemeral=True
                        )
                    return

                await self.guild_nick_name.replace_one(result, data)
                await ctx.respond(
                    f"New Nickname set for this server as `{new_nickname}`",
                    ephemeral=True
                    )
            except Exception:
                await ctx.respond(
                    "Could not interract with database `new_nicknames`. Please try again after sometime.",
                    ephemeral=True
                    )

        @BaseCog.cslash_command(
            description="Adds a nickname to the blacklist",
            guild_ids=servers
        )
        async def blacklist_name(
                self,
                ctx: ApplicationContext,
                name: Option(str)
        ):

            required_perms = {"administrator": True}

            if not self.check_perms(ctx, required_perms):
                await ctx.respond(
                    "Sorry, you cannot use this command.",
                    ephemeral=True
                    )
                return

            try:
                await ctx.response.defer(ephemeral=True)
                # data = {"nicknames": [name], "guild_id": ctx.guild.id}
                result = await self.nick_names.find_one({"guild_id": ctx.guild.id})
                if result is None:
                    data = {"nicknames": [name.lower()], "guild_id": ctx.guild.id}
                    await self.nick_names.insert_one(data)
                    await ctx.respond(
                        "Nickname added to Blacklist.",
                        ephemeral=True
                        )
                    return

                if name in result["nicknames"]:
                    await ctx.respond(
                        "Nickname is already in the blacklist.",
                        ephemeral=True
                        )
                    return

                result["nicknames"].append(name.lower())
                await self.nick_names.replace_one({"guild_id": ctx.guild.id}, result)

            except Exception as e:
                await ctx.respond(
                    "Could not interract with database `nicknames`. Please try again after sometime.",
                    ephemeral=True
                    )
                print(e)

        @BaseCog.cslash_command(
            description="Removes a nickname from the blacklist",
            guild_ids=servers
        )
        async def remove_blacklist_name(
                self,
                ctx: ApplicationContext,
                name: Option(str)
        ):

            required_perms = {"administrator": True}

            if not self.check_perms(ctx, required_perms):
                await ctx.respond(
                    "Sorry, you cannot use this command.",
                    ephemeral=True
                    )
                return

            try:
                await ctx.response.defer(ephemeral=True)
                # data = {"nicknames": [name], "guild_id": ctx.guild.id}
                result = await self.nick_names.find_one({"guild_id": ctx.guild.id})
                if result is None:
                    await ctx.respond(
                        f"Nickname `{name}` is not on the Blacklist",
                        ephemeral=True
                        )

                if name.lower() not in result["nicknames"]:
                    await ctx.respond(
                        f"Nickname `{name}` is not on the Blacklist",
                        ephemeral=True
                        )
                    return

                result["nicknames"].remove(name.lower())
                await self.nick_names.replace_one({"guild_id": ctx.guild.id}, result)
                await ctx.respond(
                    f"Nickname `{name}` is now removed from Blacklist",
                    ephemeral=True
                    )

            except Exception as e:
                await ctx.respond(
                    f"Could not interract with database `nicknames`. With error: {e}",
                    ephemeral=True
                    )

        @BaseCog.cslash_command(
            description="List all the blacklisted nicknames on this server",
            guild_ids=servers
        )
        async def blacklist_nick_list(
                self,
                ctx: ApplicationContext,
                hide: Option(bool) = True
        ):
            required_perms = {"administrator": True}

            if not self.check_perms(ctx, required_perms):
                await ctx.respond(
                    "Sorry, you cannot use this command.",
                    ephemeral=True
                    )
                return

            try:
                await ctx.response.defer(ephemeral=hide)

                result = await self.nick_names.find_one({"guild_id": ctx.guild.id})
                if result is None:
                    await ctx.respond(
                        "No Nicknames blacklisted on this server.",ephemeral=hide
                        )
                    return

                await ctx.respond(
                    "Blacklisted Nicknames:\n" + "\n".join(result["nicknames"])
                    )

            except Exception as e:
                await ctx.respond(
                    f"Could not interract with database `nicknames`. With error: `{e}`",
                    ephemeral=hide
                    )

    return NickNameCog
