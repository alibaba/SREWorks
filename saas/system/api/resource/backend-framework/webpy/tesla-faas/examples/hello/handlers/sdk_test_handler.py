#!/usr/bin/env python
# encoding: utf-8
""" """
import web
import json

from container.webpy.common import RestHandler
from container.webpy.common.decorators import exception_wrapper

__author__ = 'adonis'


class SdkHandler(RestHandler):
    TEST_IP = '11.239.167.164'

    @exception_wrapper
    def GET(self):
        tesla_ctx = web.ctx.tesla
        config = tesla_ctx.config
        sdk_client = tesla_ctx.tesla_sdk_client
        # channel = Channel(
        #     sdk_client,
        #     poll_interval=1,
        #     key=config['tesla_sdk']['channel_key'],
        #     secret=config['tesla_sdk']['channel_secret']
        # )
        channel = tesla_ctx.tesla_sdk_channel
        resp = channel.run_command('echo 123', ip=[self.TEST_IP, ])
        # 检查结果是否执行成功
        if not resp.is_success():
            print '--- channel error: ', resp.message, resp.code, resp.data
            data = resp.message
        else:
            data = resp.data
        return json.dumps({
            'code': 200,
            'message': 'Tesla FaaS Container: hello, skd.channel test',
            'data': data
        })

