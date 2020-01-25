# 肺炎最新疫情情报系统

获取丁香医生的数据，通过邮件发送的方式定时推送到指定的邮箱账户，支持群发。

数据来源：[丁香医生](https://3g.dxy.cn/newh5/view/pneumonia)

## 配置说明

```yaml
custom:
  url: https://3g.dxy.cn/newh5/view/pneumonia # 数据来源
  from: xx@163.com # 负责发送的邮箱
  to:
    - xx@qq.com # 接收邮件的邮箱
  cron: 0/30 * * * * ? # 执行任务的频率
  path: /tmp/mail/ # 临时文件目录
  fileName: new-title.txt # 用于判断是否是最新一条数据
  userAgent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.117 Safari/537.36 # 请求数据的用户环境
```

## 配置启动方式

编辑启动文件：`sudo vim /etc/systemd/system/mail.service`

把下面的内容复制里面，再指定Jar包路径`例如：/data/service/mail/mail.jar`

```shell script
Description=Email Service
Documentation=http://codedream.xin
After=network-online.target
Wants=network-online.target

[Service]
Type=simple
ExecStart=/usr/bin/java -server -Xms256m -Xmx256m -jar Jar包路径
ExecStop=/bin/kill -s QUIT $MAINPID
Restart=always
StandOutput=syslog

StandError=inherit

[Install]
WantedBy=multi-user.target
```

启动、重启、停止之类的命令：

```shell script
# 修改 service 文件之后需要刷新 Systemd
sudo systemctl daemon-reload

# 使 mail 开机自启
sudo systemctl enable mail

# 启动 mail
sudo service mail start

# 重启 mail
sudo service mail restart

# 停止 mail
sudo service mail stop

# 查看 mail 的运行状态
sudo service mail status
```

> 由于丁香医生的网页结构经常发生变更，就不进行版本发布，需要自己Clone代码进行打包。