#!/usr/bin/env python
# encoding: utf-8
""" """
from common.jsonify import jsondumps
from container.webpy.common import RestHandler
from container.webpy.common.decorators import exception_wrapper

__author__ = 'adonis'


class DmHandler(RestHandler):

    @exception_wrapper
    def GET(self):
        self.result['message'] = 'Tesla FaaS Container: hello'
        dm = self.dm(self.db)
        print '-----  dm: ',
        self.result['data'] = list(dm.obj('information_schema.tables').select('TABLE_NAME').execute())
        return jsondumps(self.result)

