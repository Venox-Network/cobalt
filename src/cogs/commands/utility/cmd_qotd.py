import datetime
from time import strptime
from typing import List
from discord import ApplicationContext
import discord
from discord.utils import get
from discord.ext.commands import Cog
from discord.commands.options import Option
from cogs import BaseCog
from discord.ext import tasks


def cog_creator(servers: List[int]):
    class Qotd(BaseCog):

        def __init__(self, bot) -> None:
            super().__init__(bot)
            self.qotd_db = self.bot.config.DATABASE["qotds"]

            self.activity_job.start()

        @BaseCog.cslash_command(
            description='Adds a qotd to queue',
            guild_ids=servers)
        async def qotd(self, ctx: ApplicationContext, question: str):
            role = discord.utils.get(ctx.guild.roles, id=873791606673780787)
            if role not in ctx.user.roles:
                await ctx.respond('You do not have permission to use this command.')
                return
            documents = await self.qotd_db.count_documents({})
            await self.qotd_db.insert_one({'id': int(documents) + 1, 'question': question, 'used': False})
            await ctx.respond(f"Added question: `{question}`")
    
        @BaseCog.cslash_command(
            description="List qotds",
            guild_ids=servers,
        )
        async def qotd_list(self, ctx):
            em=discord.Embed(title='Qotds', description='List of all qotds')
            qotds = self.qotd_db.find({})
            async for qotd in qotds:
                em.add_field(name=qotd['id'], value=qotd['question'])
            await ctx.respond(embed=em)

        @tasks.loop(minutes=1)
        async def activity_job(self):
            now = datetime.datetime.utcnow()
            if now.hour == 17 and now.minute == 1:
                used_res = await self.qotd_db.count_documents({'used': True})
                res = await self.qotd_db.find_one({'used': False})
                if res is None:
                    # qotd manager chat id is 891404641277984788
                    qotd_manager_channel = await self.bot.fetch_channel(891404641277984788)
                    # qotd manage role id is 891405322105811004 if it is not this when i make pr let me know
                    await qotd_manager_channel.send(f'⚠️ **We are out of questions!** <@&891405322105811004> `{int(used_res)}` backups left')
                    res = await self.qotd_db.find_one({'used': True})
                    if res is not None:
                        self.qotd_db.delete_one({'id': res['id'], 'used': True})
                        for guild in self.bot.guilds:
                            for channel in guild.channels:
                                if channel.name == 'qotd':
                                    await channel.send(res['question'])
                        return
                    return
                self.qotd_db.update_one({ 'id' : res['id'] },{ '$set': { 'used' : True } })
                for guild in self.bot.guilds:
                    for channel in guild.channels:
                        if channel.name == 'qotd':
                            await channel.send(res['question'])
                            print('sent in channel')
            
    return Qotd
