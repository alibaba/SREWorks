#!/usr/bin/env python
# encoding: utf-8
""" """
import json

from container.webpy.common.decorators import exception_wrapper

__author__ = 'adonis'


class HelloHandler(object):
    """
    Raw webpy style, for demo

    Please use RestHandler/BaseHandler for ease.
    """

    @exception_wrapper
    def GET(self):
        return json.dumps({
            'code': 200,
            'message': 'Tesla FaaS Container: hello',
            'data': []
        })

