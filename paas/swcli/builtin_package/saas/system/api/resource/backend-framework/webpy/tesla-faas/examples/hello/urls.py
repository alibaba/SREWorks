#!/usr/bin/env python
# encoding: utf-8
""" """
from .handlers import hello_test_handler, context_test_handler, \
    mysql_test_handler, redis_test_handler, sdk_test_handler, \
    factory_test_handler, base_test_handler, log_test_handler, dm_test_handler

__author__ = 'adonis'


urls = (
    r'/', hello_test_handler.HelloHandler,
    r'/hello', hello_test_handler.HelloHandler,

    r'/db', mysql_test_handler.MysqlHandler,
    r'/mysql', mysql_test_handler.MysqlHandler,
    r'/dm', dm_test_handler.DmHandler,
    r'/redis', redis_test_handler.RedisHandler,
    r'/factory', factory_test_handler.FactoryHandler,
    r'/context', context_test_handler.ContextHandler,
    r'/config', context_test_handler.ContextHandler,
    r'/sdk', sdk_test_handler.SdkHandler,
    r'/log', log_test_handler.LogHandler,
    r'/base/(.+)', base_test_handler.TestBaseHandler,
)
