/******************************************/
/*   数据库全名 = tesla_authproxy   */
/*   表名称 = ta_user   */
/*   OXS环境数据库登录认证方式初始化管理员用户  初始密码从外部传入 */
/******************************************/

update ta_user set login_pwd = md5('${ADMIN_INIT_PASSWORD}') where login_name = 'admin' and login_pwd = '25d55ad283aa400af464c76d713c07ad';
