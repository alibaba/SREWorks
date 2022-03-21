#!/usr/bin/python

import sys
import os

env_key_list = [word.strip() for word in sys.argv[1].split(',')]

content = open('Dockerfile.tpl').read()

env_dist = os.environ

for env_key in env_key_list:
    env_value = env_dist.get(env_key)
    content = content.replace("${%s}" % env_key, env_value)

os.write(os.open('Dockerfile', os.O_RDWR | os.O_CREAT), content)

