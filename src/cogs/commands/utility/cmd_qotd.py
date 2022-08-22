import asyncio
import datetime
from time import strptime, time
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

        qotd = discord.SlashCommandGroup("qotd", "Commands related to qotd.")

        @qotd.command(
            description='Adds a qotd to queue',
            guild_ids=servers
            )
        async def add(self, ctx: ApplicationContext, question: str):
            # qotd manager role is 891405322105811004
            role = discord.utils.get(ctx.guild.roles, id=891405322105811004)
            if role not in ctx.user.roles:
                await ctx.respond(
                    'You do not have permission to use this command.',
                    ephemeral=True
                    )
                return
            documents = self.qotd_db.find({})
            id=0
            async for doc in documents:
                if doc['id'] >= id:
                    id = doc['id']
            await self.qotd_db.insert_one({'id': int(id) + 1, 'question': question, 'used': False, 'user': str(ctx.user)})
            await ctx.respond(f"Added question: `{question}`")
    
        @qotd.command(
            description="List qotds",
            guild_ids=servers,
        )
        async def list(self, ctx):
            em=discord.Embed(title='QOTDs', description='List of all QOTDs', colour=0x0070c0)
            qotds = self.qotd_db.find({})
            async for qotd in qotds:
                em.add_field(name=qotd['id'], value=f"\"{qotd['question']}\" - {qotd['user']}")
            await ctx.respond(embed=em)

        @tasks.loop(minutes=1)
        async def activity_job(self):
            now = datetime.datetime.utcnow()
            if now.hour == 21 and now.minute == 1:
                used_res = await self.qotd_db.count_documents({'used': True})
                res = await self.qotd_db.find_one({'used': False})
                if res is None:
                    # qotd manager chat id is 891404641277984788
                    qotd_manager_channel = await self.bot.fetch_channel(891404641277984788)
                    # qotd manage role id is 891405322105811004 if it is not this when i make pr let me know
                    await qotd_manager_channel.send(f'⚠️ **We are out of questions!** <@&891405322105811004> `{int(used_res) -1}` backups left')
                    res = await self.qotd_db.find_one({'used': True})
                    if res is not None:
                        now = datetime.datetime.now()
                        self.qotd_db.delete_one({'id': res['id'], 'used': True})
                        for guild in self.bot.guilds:
                            for channel in guild.channels:
                                if channel.name == 'qotd':
                                    message = await channel.send(f"{res['question']} \n(You can also answer in your server's general channel by appending `qotd` to your message!)")
                                    message = self.bot.get_message(message.id)
                                    await message.create_thread(name=f"QOTD {now.month}-{now.day}-{now.year}")
                                    await asyncio.sleep(1)
                    return
                self.qotd_db.update_one({ 'id' : res['id'] },{ '$set': { 'used' : True } })
                for guild in self.bot.guilds:
                    for channel in guild.channels:
                        if channel.name == 'qotd':
                            message = await channel.send(res['question'])
                            message = self.bot.get_message(message.id)
                            await message.create_thread(name=f"QOTD {now.month}-{now.day}-{now.year}")
                            await asyncio.sleep(1)
    return Qotd
