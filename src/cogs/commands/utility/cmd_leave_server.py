from typing import List
from discord import ApplicationContext
from cogs import BaseCog
from discord.commands.options import Option


def cog_creator(servers: List[int]):
    class Leave_Server(BaseCog):

        def __init__(self, bot) -> None:
            super().__init__(bot)
            self.warn_collection = self.bot.config.DATABASE["warns"]

        @BaseCog.cslash_command(
            description="Leaves a particular server",
            guild_ids=servers
        )
        async def leave_server(
                self,
                ctx: ApplicationContext,
                guild_id: Option(int)
        ):

            if not (ctx.user.id in (self.bot.config.OWNERS)):
                await ctx.respond("Sorry, you cannot use this command.", ephemeral=True)
                return

            guild = self.bot.get_guild(guild_id)
            if guild is None:
                await ctx.respond(f"Couldn't find the guild with id of `{guild_id}`", ephemeral=True)
                return

            try:
                await guild.leave()
            except Exception:
                await ctx.respond(f"Failed to leave guild `{guild.name}`", ephemeral=True)
                return

            await self.bot.config.DATABASE["new_nicknames"].delete_many({"guild_id": guild.id})
            await self.bot.config.DATABASE["nicknames"].delete_many({"guild_id": guild.id})

            await self.bot.config.DATABASE["report_channels"].delete_many({"guildid": guild.id})

            await self.bot.config.DATABASE["warns"].delete_many({"warn_guild": guild.id})

            await self.bot.log_msg(
                f"Left guild `{guild.name}`. Responsible owner: `{ctx.user.name}#{ctx.user.discriminator}`")
            await ctx.respond(f"Left guild `{guild.name}`", ephemeral=True)

    return Leave_Server
