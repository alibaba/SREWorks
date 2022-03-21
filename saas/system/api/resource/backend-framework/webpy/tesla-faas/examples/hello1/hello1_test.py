#!/usr/bin/env python
# encoding: utf-8
""" """
__author__ = 'adonis'

import json


class HelloHandler(object):

    def GET(self):
        return json.dumps({
            'code': 200,
            'message': 'Tesla FaaS Container: hello1',
            'data': []
        })

