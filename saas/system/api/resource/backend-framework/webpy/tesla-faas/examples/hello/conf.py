#!/usr/bin/env python
# encoding: utf-8
""" """
__author__ = "adonis"

py_conf_key = "py_conf_value"

py_conf_list_key = [1, 2, 3]

# 默认数据库配置
# BaseHandler/BaseModel.db
database = {
    "host": "rm-tatodps-pe-db.mysql.rdstest.tbsite.net",
    "port": 3306,
    "user": "brain_test",
    "passwd": "brain_test555",
    "db": "brain_test",
    "pool_size": 50
}

# 多数据库配置, 复数形式
# BaseHandler/BaseModel.dbs.test1
databases = {
    "test1": {
        "host": "rm-tatodps-pe-db.mysql.rdstest.tbsite.net",
        "port": 3306,
        "user": "brain_test",
        "passwd": "brain_test555",
        "db": "brain_test",
        "pool_size": 50
    },
    "test2": {
        "host": "rm-tatodps-pe-db.mysql.rdstest.tbsite.net",
        "port": 3306,
        "user": "brain_test",
        "passwd": "brain_test555",
        "db": "brain_test",
        "pool_size": 50
    }
}
