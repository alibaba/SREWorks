# encoding: utf-8

import requests
import os
import json
import hashlib
import time

def read_routes():
    with open('default_route.json') as f:
        return json.loads(f.read())

def update_route(route):
    update_route_url = '%s/v2/common/gateway/route/%s' % (get_gateway_endpoint(), route['routeId'])
    print("update_route_url: %s" % update_route_url)
    res = requests.put(update_route_url, json=route, headers=gen_auth_headers())
    print(res.content)
    if res.status_code == 200:
        if json.loads(res.content)['code'] == 200:
            return True
    raise RuntimeError('insert route failed, status_code=%s, content=%s' % (res.status_code, res.content))


def insert_route(route):
    insert_route_url = '%s/v2/common/gateway/route' % get_gateway_endpoint()
    print("insert_route_url: %s" % insert_route_url)
    res = requests.post(insert_route_url, json=route, headers=gen_auth_headers())
    print(res.content)
    if res.status_code == 200:
        if json.loads(res.content)['code'] == 200:
            return True
    raise RuntimeError('insert route failed, status_code=%s, content=%s' % (res.status_code, res.content))


def check_route_exist(route_id):
    route_query_url = '%s/v2/common/gateway/route/%s' % (get_gateway_endpoint(), route_id)
    print("route_query_url: %s" % route_query_url)
    res = requests.get(route_query_url, headers=gen_auth_headers())
    print(res.content)
    if res.status_code == 200:
        return json.loads(res.content)['data'] is not None
    raise RuntimeError('check route failed, status_code=%s, content=%s' % (res.status_code, res.content))


def get_gateway_endpoint():
    gateway_endpoint = os.environ.get('ENDPOINT_PAAS_GATEWAY')
    if gateway_endpoint is None:
        raise RuntimeError("ENDPOINT_PAAS_GATEWAY is emtpy.")

    if gateway_endpoint.startswith('http') is False:
        gateway_endpoint = 'http://%s:80' % gateway_endpoint
        #gateway_endpoint = 'http://%s:7002' % gateway_endpoint

    return gateway_endpoint

def get_passwd_hash(user_name, user_passwd):
    key = "%(user_name)s%(local_time)s%(passwd)s" % {
        'user_name': user_name,
        'local_time': time.strftime('%Y%m%d', time.localtime(time.time())),
        'passwd': user_passwd }
    m = hashlib.md5()
    m.update(key)
    return m.hexdigest()

def gen_auth_headers():
    ## 四个header
    return {
        "x-auth-app": os.environ.get('ACCOUNT_SUPER_CLIENT_ID'),
        "x-auth-key": os.environ.get('ACCOUNT_SUPER_CLIENT_SECRET'),
        "x-auth-user": os.environ.get('ACCOUNT_SUPER_ID'),
        "x-auth-passwd": get_passwd_hash(os.environ.get('ACCOUNT_SUPER_ID'), os.environ.get('ACCOUNT_SUPER_SECRET_KEY'))
    }


def import_default_route():
    routes = read_routes()
    print(gen_auth_headers())
    for route in routes:
        if check_route_exist(route['routeId']):
            update_route(route)
        else:
            insert_route(route)


if __name__ == '__main__':
    import_default_route()