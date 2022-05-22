from typing import Callable, List
import discord
from discord.ext import commands
from config import Config

class Bot(commands.Bot):
    def __init__(
        self, 
        command_prefix=commands.bot.when_mentioned, 
        help_command=commands.bot._default,
        conf: Config=None,
        **options
        ):
        if conf is None:
            raise ValueError("Config isnt passed to 'discord_bot.Bot'")

        self.config = conf
        self.log_channel = None
        self.report_channel = None
        self.views = []

        self.debug_servers = self.config.debug_servers

        super().__init__(command_prefix, help_command, **options)

    async def log_msg(self, message: str, should_print: bool=False):
        await self.log_channel.send(message)
        if should_print: print(f"LOGS: {message}\n----------")

    async def on_ready(self) -> None:
        self.log_channel = self.get_channel(self.config.GLOBAL_LOG_CHANNEL)
        if self.log_channel is None:
            raise ValueError("Logs channel Not Found")

        self.report_channel = self.get_channel(self.config.GLOBAL_REPORT_CHANNEL)
        if self.report_channel is None:
            raise ValueError("Report channel Not Found")

        view = discord.ui.View(
            *self.views,
            timeout=None
        )
        self.add_view(view)

        await self.log_msg(f"Bot logged in as {self.user}", True)

    def add_cogs(self, *list: List[Callable]):
        for cog_creator in list:
            self.add_cog( (cog_creator(self.debug_servers))(self))
            # (cog_creator(self.debug_servers)) => Cog class
            # ^^ (self)