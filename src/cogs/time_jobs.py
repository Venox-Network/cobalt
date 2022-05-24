from typing import List
from discord.ext import tasks
from src.cogs import BaseCog
from discord import Game, Activity, ActivityType

def cog_creator(servers: List[int]):
    class TimeJob(BaseCog):
        
        def __init__(self, bot) -> None:
            super().__init__(bot)
            task_description = (("Vanadium SMP", "game"), (f"{len(self.bot.guilds)} servers", "watch"))
            self.task = [Game(i[0]) if i[1] == "game" else Activity(type=ActivityType.watching, name=i[0]) for i in task_description]
            self.index = False

            self.activity_job.start()

        def cog_unload(self) -> None:
            self.activity_job.stop()

        @tasks.loop(seconds=5.0)
        async def activity_job(self):
            if self.index:
                await self.bot.change_presence(activity=self.task[0])
                self.index = False
            else:
                await self.bot.change_presence(activity=self.task[1])
                self.index = True

        @activity_job.before_loop
        async def before_activity_job(self):
            await self.bot.wait_until_ready()

    return TimeJob
