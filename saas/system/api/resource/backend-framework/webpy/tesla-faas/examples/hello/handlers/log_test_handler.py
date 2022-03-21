#!/usr/bin/env python
# encoding: utf-8
""" """
from container.webpy.common import RestHandler
from container.webpy.common.decorators import exception_wrapper

import json

__author__ = 'adonis'


class LogHandler(RestHandler):

    @exception_wrapper
    def GET(self):
        self.logger.info("test log, params: %s", self.params)
        return json.dumps({
            'code': 200,
            'message': 'Tesla FaaS Container: hello',
            'data': 'test log, params: %s' % self.params
        })

