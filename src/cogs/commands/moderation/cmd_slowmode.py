import discord
from datetime import datetime, timedelta
from typing import List
from discord import ApplicationContext
from discord.commands.options import Option
from cogs import BaseCog
from discord.ext import tasks


def cog_creator(servers: List[int]):
    class SlowmodeCog(BaseCog):
        def __init__(self, bot) -> None:
            super().__init__(bot)
            self.slowmode_db = self.bot.config.DATABASE["dynamic_slowmode"]
            self.slowmode_map = {}
            self.slowmode_job.start()

        @BaseCog.cslash_command(description="Marks a channel for dynamic slowmode", guild_ids=servers)
        async def slowmode_setup(self, ctx: ApplicationContext, msgs_per_min: Option(int), min_msgs_per_min: Option(int), max_slowmode_time: Option(int), default_slowmode: Option(int)):
            if not self.check_perms(ctx, {"manage_messages": True}):
                await ctx.respond("Sorry, you cannot use this command.", ephemeral=True)
                return

            data = {"channel_id": ctx.channel.id, "amount_of_messages_per_min": msgs_per_min, "minimum_of_messages_per_minute": min_msgs_per_min, "slowmode_time": max_slowmode_time, "defaultslowmode": default_slowmode}
            try:
                find = await self.slowmode_db.find_one({"channel_id": ctx.channel.id})
                if find is None:
                    await self.slowmode_db.insert_one(data)
                    self.slowmode_map[ctx.channel.id] = data
                    await ctx.respond(
                        "Slowmode is now enabled for this channel.",
                        ephemeral=True
                        )
                    return

                await self.slowmode_db.replace_one(find, data)
                self.slowmode_map[ctx.channel.id] = data
                await ctx.respond("Slowmode is now updated for this channel.", ephemeral=True)
            except Exception:
                await ctx.respond("Could not interact with database `dynamic_slowmode`. Please try again after sometime.", ephemeral=True)

        @BaseCog.cslash_command(description="Disables slowmode for a channel", guild_ids=servers)
        async def slowmode_disable(self, ctx: ApplicationContext):
            if not self.check_perms(ctx, {"manage_messages": True}):
                await ctx.respond("Sorry, you cannot use this command.", ephemeral=True)
                return

            try:
                find = self.slowmode_db.find_one({"channel_id": ctx.channel.id})
                if find is None:
                    await ctx.respond("Slowmode is not enabled for this channel.", ephemeral=True)
                    return

                await self.slowmode_db.delete_one(find)
                self.slowmode_map.pop(ctx.channel, None)
                await ctx.respond("Slowmode is now disabled for this channel.", ephemeral=True)
            except Exception:
                await ctx.respond("Could not interact with database `dynamic_slowmode`. Please try again after sometime.", ephemeral=True)

        def cog_unload(self) -> None:
            self.slowmode_job.cancel()

        @tasks.loop(seconds=10.0)
        async def slowmode_job(self):
            delete = []
            for key, val in self.slowmode_map.items():
                channel: discord.TextChannel = self.bot.get_channel(key)
                if channel is None:
                    delete.append(key)
                    continue

                since_last_min = datetime.now() - timedelta(seconds=20)
                try:
                    num_messages = len(await channel.history(limit=100, after=since_last_min).flatten())
                except discord.HTTPException:
                    delete.append(key)
                    continue
                except Exception as e:
                    print(e)
                    continue

                slow_mode_time = val["slowmode_time"]
                msgs_per_min = val["amount_of_messages_per_min"]
                min_msgs_per_min = val["minimum_of_messages_per_minute"]
                default_slowmode = val["defaultslowmode"]

                if channel.slowmode_delay != slow_mode_time:  # if channel is NOT in slowmode
                    if num_messages >= msgs_per_min:  # if the no. messages sent is >= than required
                        await channel.edit(slowmode_delay=slow_mode_time)
                        continue
                    continue

                if num_messages < min_msgs_per_min:  # if the no. messages sent is < than required - 10, setting the lower threshold for slowmode to end
                    await channel.edit(slowmode_delay=default_slowmode)

            for key in delete:
                slowmode_map.pop(key, None)
                try:
                    await self.slowmode_db.delete_one({"channel_id": key})
                except Exception:
                    pass

        @slowmode_job.before_loop
        async def before_slowmode_job(self):
            await self.bot.wait_until_ready()
            try:
                delete = []
                async for result in self.slowmode_db.find({}):
                    channel = self.bot.get_channel(result["channel_id"])
                    if channel is None:
                        delete.append(result)
                        continue
                    self.slowmode_map[channel.id] = result

                for key in delete:
                    await self.slowmode_db.delete_one(key)

            except Exception:
                raise Exception("Could not connect to database `dynamic_slowmode` to fetch slowmode details.")

    return SlowmodeCog
