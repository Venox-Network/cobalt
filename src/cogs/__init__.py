import discord
from typing import Callable
from discord import ApplicationContext, Member
from discord.ext.commands import Cog, slash_command, guild_only
from discord_bot import Bot

class BaseCog(Cog):
    def __init__(self, bot) -> None:
        self.bot:Bot = bot

    @staticmethod
    def cslash_command(**kwargs):
        def decorator(func:Callable):
            return (slash_command(**kwargs))(guild_only()(func))
        return decorator

    @staticmethod
    def check_perms(ctx: ApplicationContext, perms:dict, user2: Member=None, bot_force:bool=False):
        if user2 and user2.bot and (not bot_force):
            return False

        guild = ctx.guild
        bot = guild.me if guild is not None else ctx.bot.user
        if BaseCog.perm_loop(perms, ctx.channel.permissions_for(ctx.author)) or BaseCog.perm_loop(perms, ctx.channel.permissions_for(bot)):
            return False

        if user2 is not None:
            if ctx.author.top_role.position <= user2.top_role.position:
                return False

        return True

    @staticmethod
    def perm_loop(perms: dict, permissions) -> bool:
        for perm, value in perms.items():
            if getattr(permissions, perm) != value:
                return True

        return False

    def register_views(self, view: discord.ui.View):
        self.bot.views.append(view)
