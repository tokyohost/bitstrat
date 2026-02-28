一、先确认当前 swap 状态
```free -h```


如果看到：

```Swap: 0B   0B   0B```


说明还没开启。

也可以：

```swapon --show```

二、创建 swapfile（推荐方案）
1️⃣ 选择 swap 大小（经验值）
内存	swap 建议
≤ 8G	2–4G
16G	4–8G
≥ 32G	4–16G

JVM / Node / Docker 机器建议 ≥ 8G

下面以 8G swap 为例：

2️⃣ 创建 swap 文件
```sudo fallocate -l 8G /swapfile```


如果 fallocate 不可用：

```sudo dd if=/dev/zero of=/swapfile bs=1M count=8192```

3️⃣ 设置权限（非常重要）
```sudo chmod 600 /swapfile```


验证：

```ls -lh /swapfile```
# -rw------- 1 root root 8.0G /swapfile

4️⃣ 格式化为 swap
````sudo mkswap /swapfile````

5️⃣ 启用 swap
````sudo swapon /swapfile````

6️⃣ 验证
````free -h````


应看到：

Swap: 8.0G  0B  8.0G

三、设置开机自动启用（⚠️ 必做）

```编辑 /etc/fstab：```

```sudo vim /etc/fstab```


追加一行：

```/swapfile none swap sw 0 0```


保存退出。

四、优化 swap 使用策略（强烈建议）
1️⃣ 调整 swappiness（推荐 10–20）
```cat /proc/sys/vm/swappiness```


默认是 60，太激进。

临时修改：

```sudo sysctl vm.swappiness=10```


永久生效：

```sudo vim /etc/sysctl.conf```


加一行：

```vm.swappiness=10```

2️⃣ 降低 IO 抖动（服务器推荐）
```sudo sysctl vm.vfs_cache_pressure=50```

五、验证 swap 是否真的在用
```watch -n 1 free -h```


或者：

```vmstat 1```
