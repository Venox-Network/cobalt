import random
import discord

from discord.ext import tasks
from cogs import BaseCog


def cog_creator(servers: List[int]):
    class TimeJob(BaseCog):
        
        def __init__(self, bot) -> None:
            super().__init__(bot)
            # 1 = playing
            # 2 = listening
            # 3 = watching
            self.activity_job.start()

        def cog_unload(self) -> None:
            self.activity_job.stop()

        @tasks.loop(seconds=25.0)
        async def activity_job(self):
            statuses = [
                ["over chriz.cf", 3],
                ["srnyx.xyz/pl", 2],
                [f"{len(self.bot.guilds)} servers", 3],
                ["with pack.srnyx.xyz", 1],
                ["on play.venox.network", 1],
                ["chriz.cf/playlist", 2],
                ["over srnyx.xyz/discord", 3],
                ["over venox.network", 3],
                ["over simpearth.xyz/discord", 3],
                ["over events.red", 3],
                ["over lasertag.venox.network", 3],
                ["on lasertag.venox.network", 1]
            ]
            current_status = random.choice(statuses)
            await self.bot.change_presence(activity=discord.Activity(name=current_status[0], type=current_status[1]))

        @activity_job.before_loop
        async def before_activity_job(self):
            await self.bot.wait_until_ready()

    return TimeJob
