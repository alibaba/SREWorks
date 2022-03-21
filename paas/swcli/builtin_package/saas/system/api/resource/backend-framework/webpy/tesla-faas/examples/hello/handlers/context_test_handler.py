#!/usr/bin/env python
# encoding: utf-8
""" """
from container.webpy.common.decorators import exception_wrapper

__author__ = 'adonis'
import web
import json


class ContextHandler(object):
    """
    请求上下文的内容存放在web.ctx.tesla / web.ctx.factory / web.ctx.db 中，
    具体格式如下:

    web.ctx.tesla 格式如下:
    {
        'config': context_manager.config,
        'db': context_manager.db,
        'dbs': context_manager.dbs,
        'logger': context_manager.logger,
        'tesla_sdk_client': context_manager.tesla_sdk,
        'tesla_sdk': context_manager.tesla_sdk,
        'tesla_sdk_channel': context_manager.tesla_sdk_channel,
        'redis_pool': context_manager.redis_pool,
        'factory': context_manager.factory,
        # 兼容老的代码
        'clusterinfo_source': '',
        'http_opts': {
            'cors_wl': '*'
        },
        'db_opts': {},
        'request_id': '',
    }

    其他上下文内容:

    # 兼容老的bcc中的使用方式
    web.ctx.factory = context_manager.factory
    # 兼容老的代码
    web.ctx.server = ''
    web.ctx.db = web.ctx.tesla.db

    """

    @exception_wrapper
    def GET(self):
        tesla_ctx = web.ctx.tesla
        print tesla_ctx['config']
        return json.dumps({
            'code': 200,
            'message': 'Tesla FaaS Container: hello',
            'data': tesla_ctx['config'],
            'trace_id': tesla_ctx['trace_id']
        })

