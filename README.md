# 513保卫战

一个基于 JavaFX 的塔防游戏。

## 游戏特色

- 🏹 5种防御塔：箭塔、炮塔、寒冰塔、闪电塔、核弹塔
- 👾 多种怪物类型：普通僵尸、速跑兵、坦克怪、Boss
- 🌊 动态波次成长系统：怪物血量、速度、金币奖励随波次递增
-  商城系统：购买解锁新防御塔，数据持久化保存
-  排行榜系统：记录历史最高分，支持本地 JSON 存储
-  60FPS 流畅游戏循环

## 环境要求

- JDK 17+
- JavaFX SDK 21

## 快速开始

### 1. 配置 JavaFX SDK

将 JavaFX SDK 21 解压到项目根目录，确保路径为 `javafx-sdk-21/`。

### 2. 编译

```bash
javac -d out -cp "javafx-sdk-21/lib/*" -sourcepath src src/main/*.java src/entity/*.java src/entity/monster/*.java src/entity/tower/*.java src/enums/*.java src/map/*.java src/wave/*.java
```

### 3. 运行

双击 `start.bat` 或执行：

```bash
java --module-path "javafx-sdk-21/lib" --add-modules javafx.controls,javafx.graphics -cp out main.Main
```

## 项目结构

```
513war/
├── src/
│   ├── main/              # 主程序
│   │   ├── Main.java      # 入口
│   │   ├── GamePanel.java # 游戏主面板
│   │   ├── HudPanel.java  # HUD 界面
│   │   ├── ShopOverlay.java   # 商城
│   │   ├── MainMenu.java      # 主菜单
│   │   ├── WaveManager.java   # 波次管理
│   │   ├── ScoreManager.java  # 排行榜数据
│   │   └── ...
│   ├── entity/            # 游戏实体
│   │   ├── monster/       # 怪物类
│   │   ├── tower/         # 防御塔类
│   │   └── Projectile.java # 子弹
│   ├── enums/             # 枚举类型
│   ├── map/               # 地图系统
│   ── wave/              # 波次配置
├── resources/             # 资源文件
│   └── styles/            # CSS 样式
├── out/                   # 编译输出
└── start.bat              # 启动脚本
```

## 防御塔数据

| 塔类型 | 价格 | 伤害 | 射速(秒) | 范围(像素) | DPS |
|--------|------|------|----------|-----------|-----|
| 🏹 基础箭塔 | 50 | 12 | 0.8 | 160 | 15.0 |
| 💣 重装炮塔 | 100 | 35 | 2.0 | 130 | 17.5 |
| ❄️ 寒冰减速塔 | 150 | 6 | 1.2 | 120 | 5.0 |
| ⚡ 连锁闪电塔 | 260 | 24 | 1.0 | 180 | 24.0 |
| 🌋 终极核弹塔 | 500 | 180 | 4.0 | 280 | 45.0 |

## 波次成长公式

- **怪物血量**：`HP = HP初始 × (1 + (Wave-1) × 0.25)` — 每波+25%
- **怪物速度**：每5波+10%，封顶1.5倍
- **金币奖励**：`Gold = Gold初始 × (1 + (Wave-1) × 0.1)` — 每波+10%

## 许可证

MIT License
