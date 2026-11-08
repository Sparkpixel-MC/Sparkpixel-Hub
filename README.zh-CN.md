<div align="center">

# ⚡ Sparkpixel-Hub

**驱动 Sparkpixel 网络的大厅插件 —— 现已适配 Paper 1.21.11**

[![Paper](https://img.shields.io/badge/Paper-1.21.11-3b82f6?style=for-the-badge&logo=minecraft&logoColor=white)](https://papermc.io)
[![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org)
[![版本](https://img.shields.io/badge/版本-4.0.0-22c55e?style=for-the-badge)](https://github.com)
[![许可证](https://img.shields.io/badge/许可证-GPL--3.0-red?style=for-the-badge)](LICENSE)

[English](README.md) | **简体中文**

</div>

---

## 📖 简介

**Sparkpixel-Hub** 是为 **Sparkpixel** 网络打造的一款模块化、功能齐全的大厅插件。它为区域化 PvP 竞技场、完整的留存系统、跑酷、任务、饰品以及丰富的引导剧情提供支持，并运行于 **Paper 1.21.11**，采用 **Adventure / MiniMessage** 文本引擎。

> 基于 Java 21 构建，面向规模化设计，专为现代化 Minecraft 服务器体验而调优。

---

## 🚀 功能特性

- 🧩 **模块化配置** —— 拆分的 `modules/*.yml` 文件，自动迁移缺失的配置项
- 🌐 **客户端语言本地化** —— 消息来自 `lang/en_us.yml` 与 `lang/zh_cn.yml`
- ⚔️ **区域化 PvP 竞技场** —— 完全可配置的装备套装
- 🎫 **LuckPerms VIP 弹跳床** —— VIP 专属发射台集成
- 🎆 **加入特效** —— 烟花、音效与粒子
- 🔊 **可配置的公告音效**
- 🖥️ **现代化菜单** —— 基于 Adventure 构建
- 🎯 **留存系统** —— 主菜单、每日签到、任务、跑酷、成就、收集品、商店、排行榜、活动与指南
- 📜 **完整引导剧情** —— 详见 [GUIDE_STORY.md](GUIDE_STORY.md)
- ♟️ **SparkChess 兼容** —— 棋盘上不放置弹跳床、游戏过程中隐藏大厅物品、棋盘交互绕过大厅保护
- 🛡️ **大厅工具箱** —— 大厅保护、出生点、建造模式、发射台、二段跳、全息文字、玩家隐藏、传送弓等

👉 完整功能列表与配置说明请查阅 [FEATURES.md](FEATURES.md)。

---

## 🔧 构建

```bash
./gradlew build
```

**输出：**

```text
build/libs/Sparkpixel-Hub-4.0.0.jar
```

---

## ⌨️ 命令

| 命令 | 说明 |
| --- | --- |
| `/sparkpixelhub` · `/sphub` | 主命令 |
| `/sparkpixelhub reload` | 重载配置、菜单、模块与语言 |
| `/sparkpixelhub open <menu>` | 打开自定义菜单 |
| `/sparkpixelhub pvp pos1\|pos2\|info` | 配置 PvP 区域 |
| `/sparkpixelhub trampoline` | 以管理员身份放置弹跳床 |
| `/sparkpixelhub menu` | 打开大厅菜单 |
| `/sparkpixelhub parkour` | 开始 / 结束跑酷 |
| `/sparkpixelhub feedback <text>` | 提交反馈 |
| `/sparkpixelhub hologram ...` | 管理全息文字 |

---

## 🧱 技术栈

| 组件 | 版本 / 详情 |
| --- | --- |
| **Minecraft** | Paper 1.21.11 |
| **Java** | 21 |
| **插件版本** | 4.0.0 |
| **文本引擎** | Adventure / MiniMessage |
| **权限前缀** | `sparkpixelhub.*` |

---

## 📄 许可证

本项目采用 **GNU General Public License v3.0** 许可证。
详情请见 [LICENSE](LICENSE)。

<div align="center">

*为 Sparkpixel 网络精心打造 ⚡*

</div>