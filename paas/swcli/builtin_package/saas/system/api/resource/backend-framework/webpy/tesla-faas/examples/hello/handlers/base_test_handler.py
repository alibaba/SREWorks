#!/usr/bin/env python
# encoding: utf-8
""" """
from container.webpy.common import BaseHandler

__author__ = 'adonis'


class TestBaseHandler(BaseHandler):

    def test(self, params):
        return params

    def echo(self, params):
        return {
            'params': params,
            'body': self.body,
            'json_body': self.json_body,
        }

    def test_model(self, params):
        pass

