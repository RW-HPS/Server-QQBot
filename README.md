#Server-QQBot

##许可证
项目遵循GNU AFFERO GENERAL PUBLIC LICENSE Version 3

## 介绍
一个使用[Mirai](https://github.com/mamoe/mirai) 框架，运行于[RW-HPS](https://github.com/RW-HPS/RW-HPS) 的QQ机器人插件

## 用法
从release下载并[安装此插件](https://github.com/RW-HPS/RW-HPS/wiki/UserManual) 。启动Server，你可能会等待一些时间用于下载依赖和启动Mirai Console。
当Mirai Console启动成功后在命令行输入 `mirai autoLogin add [qq] [password]` 这将会添加一个自动登录配置。

有关任何Mirai指令与Mirai滑块验证的问题，请参阅[指令集](https://github.com/mamoe/mirai-console/blob/master/docs/BuiltInCommands.md) 或[Bot](https://github.com/mamoe/mirai/blob/dev/docs/Bots.md)

## Server内指令
 - `mirai <command...>` - 在mirai console内执行给定的命令。

## 群内命令
 - `.status` 向群内发送服务器状态，包括当前玩家人数，地图，模式等
 - `.upload [map/all] [path]` 指定type。`map`代表下载一张地图，无须指定后缀，将自动寻找给定`path`中后缀为`.tmx`的文件。并自动下载后缀为`_map.png`作为预览图片。注意，前者没有找到会引发下载失败，而后者不会。
`all`代表下载任何文件，下载的文件将会在Server根目录下的`/data/download`。注意，该操作只能由管理员或群主使用。
 - 更多指令待支持...

## 构建
`clone`这个项目。打开这个项目目录并在你的ide或命令行运行`gradlew jar` 或 `./gradlew jar`

输出的 jar 应该会在工作目录下的 `build/libs/Server-QQBot-x.x.x.jar`  

## 贡献
欢迎提出任何issues或pull request!