# Sparkpixel-Hub 功能清单

> 适用版本：**Paper 26.3**（Minecraft Java 26.3）  
> 插件版本：**4.0.0**  
> 构建基线：Java 25、Paper API `26.3.build.231-stable`

---

## 1. 版本与品牌

- 插件名：**Sparkpixel-Hub**
- Java 包名：`top.sparkpixel.hub`
- 主类：`SparkpixelHubPlugin`
- 主命令：`/sparkpixelhub`、`/sphub`
- 权限前缀：`sparkpixelhub.*`
- 数据目录：`plugins/Sparkpixel-Hub/`
- 不保留旧权限、数据目录或命令兼容层
- 保留 GPL 版权声明和原始项目署名（法律要求），但插件品牌、包名、命令、权限、消息、配置和文档全部使用 Sparkpixel-Hub

---

## 2. 配置文件结构

```
plugins/Sparkpixel-Hub/
├── config.yml
├── modules/
│   ├── announcements.yml
│   ├── anti-wdl.yml
│   ├── build-mode.yml
│   ├── double-jump.yml
│   ├── drift.yml
│   ├── join-effects.yml
│   ├── join-items.yml
│   ├── join.yml
│   ├── launchpad.yml
│   ├── player-hider.yml
│   ├── pvp.yml
│   ├── static-time.yml
│   ├── teleportation-bow.yml
│   ├── trampoline.yml
│   └── world-settings.yml
├── lang/
│   ├── en_us.yml
│   └── zh_cn.yml
├── commands.yml
├── data.yml
└── menus/
    └── serverselector.yml
```

- `config.yml` 只保留核心设置
- `modules/*.yml` 每个功能一个文件，启动时合并为统一配置
- `lang/*.yml` 根据客户端语言加载 MiniMessage 消息
- 旧配置中的废弃段会在迁移时删除

---

## 3. 多语言

- 通过 `Player#locale()` 识别客户端语言
- 查找顺序：
  1. 完全匹配的 `lang/<locale>.yml`
  2. 同语言文件
  3. `language.fallback`
  4. `language.default`
- 当前内置 `en_us` 与 `zh_cn`
- 所有消息使用 **MiniMessage**，同时兼容旧 `&` 颜色代码
- 示例：

```yaml
PREFIX: "<gradient:#9863E7:#545eb6><bold>Sparkpixel-Hub</bold></gradient> <dark_gray>▏"
NO_PERMISSION: "%prefix% <red>你没有权限执行此操作。"
```

---

## 4. PvP 区域系统

### 规则

- PvP 只在配置的立方体区域内开启
- 进入区域：
  - 保存玩家当前背包
  - 清空背包
  - 自动发放配置好的 PvP 装备
  - ActionBar 提示
- 离开区域：
  - 移除 PvP 装备
  - 恢复进入前背包
  - ActionBar 提示
- 区域外：
  - 不持有任何 PvP 物品
  - 无法自行开启 PvP
  - 玩家之间伤害无效
- 区域内的攻击要求攻击者和被攻击者都在 PvP 状态
- 支持近战与弹射物
- 死亡、重生、传送、换世界、退出服务器都会正确清理状态

### 装备配置

PvP 装备完全由 `modules/pvp.yml` 定义，支持：

- 热键栏槽位
- 头盔、胸甲、护腿、靴子
- 副手
- 材质、数量
- MiniMessage 名称和 lore
- 附魔
- 可选的物品移动保护

### 圈定区域

管理员命令：

```text
/sparkpixelhub pvp pos1
/sparkpixelhub pvp pos2
/sparkpixelhub pvp info
```

坐标会写回 `modules/pvp.yml`。

---

## 5. VIP 蹦床（LuckPerms）

- 与 LuckPerms 联动，支持配置允许的权限组
- 未安装 LuckPerms 时回退到 `sparkpixelhub.trampoline.use`
- VIP 通过专属物品使用
- 管理员可通过 `/sparkpixelhub trampoline` 或物品使用
- 非 VIP 无法使用物品或命令
- 使用后在玩家前方地面上方生成配置好的蹦床结构
- 不会替换地面实体方块
- 玩家踩上后被弹飞
- 播放可配置的烟花起飞音效
- ActionBar 提示
- 结构到期后自动恢复原始方块

结构通过“图案 + 方块映射”配置，可自由设计。

---

## 6. 漂流

`modules/drift.yml` 提供服务器权威的河道漂流：

- 根据原版水流等级梯度自动计算前进方向
- 水流拐弯时逐 tick 重新计算，船会自然转向
- 速度高于原版船，可配置
- 玩家上船后取消客户端载具输入，WASD 不再能操纵船
- 安装 ProtocolLib 时，每个 tick 向骑手发送服务器权威位置，避免松开按键后船“瞬移回服务器位置”
- 需要安装 **ProtocolLib** 才能完全屏蔽客户端载具输入；未安装时回退到周期性位置同步
- 支持 `/sparkpixelhub drift`（权限 `sparkpixelhub.drift.use`，默认允许）生成专属漂流船
- 漂流船使用持久化数据标记，只对本插件的船生效

### 为什么之前会回弹

只取消客户端载具包并不够：客户端仍会在本地预测船向前移动，而服务端船的位置没有更新，等玩家松开按键时客户端收到服务端真实位置，就会“回弹”。  
本实现同时做两件事：

1. 持续在服务端设置船的速度和朝向
2. 通过 ProtocolLib 每 tick 发送 `ENTITY_TELEPORT` 强制客户端对齐服务端位置

因此客户端无法用 WASD 覆盖船的位置。

---

## 7. 加入特效

`modules/join-effects.yml` 支持：

- 加入音效
- 烟花
- 粒子
- 仅首次加入
- 自定义烟花类型、颜色、闪烁、拖尾、数量
- 自定义粒子类型、数量、范围和速度

插件不再发送加入/退出聊天消息。

---

## 8. 定时公告

- 定时轮播
- 支持所需在线人数
- 支持全局音效
- 支持单条公告覆盖音效
- 使用 MiniMessage
- 音效使用命名空间键，例如：

```yaml
sound:
  enabled: true
  key: "minecraft:block.note_block.pling"
  volume: 1.0
  pitch: 1.0
```

---

## 9. 现代 GUI

- Adventure Component 标题
- MiniMessage 物品名称和 lore
- `rows` 行数配置
- 自动填充物
- 槽位/多槽位
- 权限要求
- 点击动作
- 刷新支持
- 玩家头颅 / Base64 / HeadDatabase
- 重写后的服务器选择菜单

菜单文件位于 `menus/*.yml`，菜单 ID 即文件名。

---

## 10. 其他现有功能

### 大厅与世界

- 大厅出生点 `/setlobby`、`/lobby`
- 固定世界时间
- 弹射板
- 二段跳
- 建造模式 `/buildmode`
- 世界保护：
  - 禁止摔落、溺水、火焰、虚空伤害
  - 禁止天气变化、生物生成
  - 禁止物品丢弃/拾取
  - 禁止方块破坏/放置/交互
  - 保护展示框、画、告示牌、船、矿车等
- 防 World Downloader
- 隐身 `/vanish`
- 方向性传送弓
- 全息投影管理

### 玩家

- 加入治疗、灭火、清空背包
- 自定义加入物品
- 玩家隐藏器
- 快捷栏槽位
- 飞行 `/fly`
- 游戏模式命令 `/gamemode`、`/gms`、`/gmc`、`/gma`、`/gmsp`

### 菜单

- 服务器选择器
- 自定义菜单
- 菜单内动作执行

### 框架

- 模块化配置
- 动作系统
- PlaceholderAPI
- HeadDatabase
- 自定义命令
- 权限系统
- 配置迁移

---

## 11. 已移除功能

为避免与现有系统重复，以下功能已移除：

- **TAB 栏修改**：不再调用任何 Tab 列表 Header/Footer API
- **聊天管理**：脏话过滤、聊天锁定、命令屏蔽、`/lockchat`、`/clearchat`
- **加入/退出消息**：不再设置 Join/Quit 消息
- **计分板**：模块、命令、权限、配置和消息全部删除
- **旧更新检查器**：不再请求旧 GitHub 仓库
- **旧 bStats 项目**：不再向旧项目上报

---

## 12. 命令与权限

| 命令 | 说明 | 权限 |
| --- | --- | --- |
| `/sparkpixelhub`、`/sphub` | 主命令 | 按子命令 |
| `/sparkpixelhub reload` | 重载 | `sparkpixelhub.command.reload` |
| `/sparkpixelhub info` | 信息 | `sparkpixelhub.command.help` |
| `/sparkpixelhub open <menu>` | 打开菜单 | `sparkpixelhub.command.openmenu` |
| `/sparkpixelhub pvp ...` | PvP 区域 | `sparkpixelhub.command.pvp` |
| `/sparkpixelhub trampoline` | 放置蹦床 | `sparkpixelhub.command.trampoline` |
| `/sparkpixelhub drift` | 生成漂流船 | `sparkpixelhub.drift.use` |
| `/sparkpixelhub hologram ...` | 全息投影 | `sparkpixelhub.command.holograms` |
| `/gamemode`, `/gms`, `/gmc`, `/gma`, `/gmsp` | 游戏模式 | `sparkpixelhub.command.gamemode` |
| `/fly [玩家]` | 飞行 | `sparkpixelhub.command.fly` / `.others` |
| `/setlobby` | 设置出生点 | `sparkpixelhub.command.setlobby` |
| `/lobby` | 返回大厅 | — |
| `/vanish` | 隐身 | `sparkpixelhub.command.vanish` |
| `/buildmode` | 建造模式 | `sparkpixelhub.command.buildmode` / `.others` |

---

## 13. 动作类型

配置格式：`[ACTION] 参数`

| 动作 | 说明 |
| --- | --- |
| `[MESSAGE]` | 向玩家发送消息 |
| `[BROADCAST]` | 全服广播 |
| `[TITLE]` | 标题：`主标题;副标题;淡入;停留;淡出` |
| `[ACTIONBAR]` | ActionBar 消息 |
| `[SOUND]` | 播放音效 |
| `[COMMAND]` | 玩家身份执行命令 |
| `[CONSOLE]` | 控制台身份执行命令 |
| `[GAMEMODE]` | 切换游戏模式 |
| `[PROXY]` / `[BUNGEE]` | 发送到其他服务器 |
| `[EFFECT]` | 药水效果 |
| `[MENU]` | 打开菜单 |
| `[CLOSE]` | 关闭界面 |

---

## 14. 构建验证

```bash
./gradlew build
```

产物：

```text
build/libs/Sparkpixel-Hub-4.0.0.jar
```

本次已完成编译和 ShadowJar 打包验证，未在真实 Paper 26.3 服务端进行游戏内回归测试。