#!/usr/bin/env python
# encoding: utf-8
""" """
import web

from container.webpy.common import RestHandler
from container.webpy.common.decorators import exception_wrapper

__author__ = 'adonis'


class FactoryHandler(RestHandler):
    TEST_KEY = 'tesla-faas-test'

    @exception_wrapper
    def GET(self):
        # redis_conn = self.r       # easy way
        factory = web.ctx.factory
        redis_conn = factory.get_redis_conn_wrapper()
        redis_conn.set(self.TEST_KEY, 'Tesla FaaS factory test: set/get()')
        data = redis_conn.get(self.TEST_KEY)

        data = factory.hello.test()
        return self.jsondumps({
            'code': 200,
            'message': 'Tesla FaaS Container: hello',
            'data': data
        })

