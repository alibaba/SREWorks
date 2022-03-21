#!/usr/bin/env python
# encoding: utf-8
""" """
from common.jsonify import jsondumps
from container.webpy.common import RestHandler
from container.webpy.common.decorators import exception_wrapper
from ..models.mysql_test_model import MysqlTestModel

__author__ = 'adonis'


class MysqlHandler(RestHandler):

    @exception_wrapper
    def GET(self):
        self.result['message'] = 'Tesla FaaS Container: hello'
        self.result['data'] = MysqlTestModel().get_db_time()
        return jsondumps(self.result)

