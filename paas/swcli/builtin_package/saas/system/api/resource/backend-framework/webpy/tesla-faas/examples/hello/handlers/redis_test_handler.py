#!/usr/bin/env python
# encoding: utf-8
""" """
import web

from container.webpy.common import RestHandler
from container.webpy.common.decorators import exception_wrapper

__author__ = 'adonis'


class RedisHandler(RestHandler):
    TEST_KEY = 'tesla-faas-test'

    @exception_wrapper
    def GET(self):
        tesla_ctx = web.ctx.tesla
        redis_pool = tesla_ctx.redis_pool
        try:
            redis_conn = self.r
            # other ways
            # redis_conn = redis.Redis(connection_pool=redis_pool, socket_timeout=3)
            # redis_conn = self.factory.get_redis_conn_wrapper()
            redis_conn.set(self.TEST_KEY, 'Tesla FaaS redis test: set/get()')
            data = redis_conn.get(self.TEST_KEY)
        except Exception as e:
            data = e.__str__()
        return self.jsondumps({
            'code': 200,
            'message': 'Tesla FaaS Container: hello',
            'data': data
        })

