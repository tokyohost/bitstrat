# Ubuntu / Debian
apt install -y apache2-utils

# 生成密码文件
mkdir -p /etc/nginx/auth
htpasswd -c /etc/nginx/auth/kibana.passwd admin

#再加一个用户（不加 -c）：
htpasswd /etc/nginx/auth/kibana.passwd ops

#反代
location / {
        # 基础认证
        auth_basic "kibana Login";
        auth_basic_user_file /etc/nginx/auth/kibana.passwd;

        proxy_pass http://127.0.0.1:5600;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        proxy_http_version 1.1;
        proxy_read_timeout 300s;
        proxy_send_timeout 300s;
    }

location /v3/ {
        proxy_set_header Host $http_host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header REMOTE-HOST $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_pass http://127.0.0.1:8099/v3/;
    }
