-- 创建 nacos 用户
CREATE USER IF NOT EXISTS 'nacos'@'%' IDENTIFIED BY '552bbc5f9db1';

-- 创建业务用户
CREATE USER IF NOT EXISTS 'bitstrat'@'%' IDENTIFIED BY '699f9c48';

-- 授权
GRANT ALL PRIVILEGES ON nacos.* TO 'nacos'@'%';
GRANT ALL PRIVILEGES ON bitstrat.* TO 'bitstrat'@'%';

-- 刷新权限
FLUSH PRIVILEGES;
