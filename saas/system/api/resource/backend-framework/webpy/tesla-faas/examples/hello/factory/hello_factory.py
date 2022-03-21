#!/usr/bin/env python
# encoding: utf-8
""" """
from container.webpy.bcc_factory import BCCFactoryBase

__author__ = 'adonis'


class HelloFactory(BCCFactoryBase):

    @classmethod
    def register(cls):
        return 'hello'

    def test(self):
        return 'HelloFactory'
