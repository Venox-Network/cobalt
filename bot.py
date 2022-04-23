import json
import os
import wavelink
import nextcord
from nextcord.ext import commands

with open("soontobeconfig.txt") as f:
    TOKEN = f.readlines()[1]
with open("soontobeconfig.txt") as f:
    CLUSTER = f.readlines()[3]
with open("soontobeconfig.txt") as f:
    Global_Report_Channel = f.readlines()[5]
with open("soontobeconfig.txt") as f:
    Global_Log_Channel = f.readlines()[7]

intents = nextcord.Intents.default()
intents.members = True
client = commands.Bot(intents=intents)


@client.event
async def on_ready():
    print("bot is ready")


@commands.Cog.listener()
async def on_wavelink_node_ready(self, node: wavelink.Node):
    print(f"node {node.identifier} is ready!!!")


async def node_connect(self):
    await client.wait_until_ready()
    await wavelink.NodePool.create_node(client=client,
                                        host='kerosine.darrennathanael.com',
                                        port=2124,
                                        password='SleepingOnTrains')


class Confirm(nextcord.ui.View):
    def __init__(self):
        super().__init__()
        self.value = None


async def load(ctx, extension):
    client.load_extension(f'cogs.{extension}')


async def unload(ctx, extension):
    client.unload_extension(f'cogs.{extension}')


for filename in os.listdir('./cogs'):
    if filename.endswith('.py'):
        client.load_extension(f'cogs.{filename[:-3]}')

client.run(TOKEN)
