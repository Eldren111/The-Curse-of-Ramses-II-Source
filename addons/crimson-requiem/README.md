# 葬神录附属

新增最终 Boss「葬神剑主 · 无赦」，当前实现前五套技能，独立星空战场无需安装其他 Boss 模组。
实体、刷怪蛋、配置和测试说明见 [docs/wushe.md](docs/wushe.md)。

`ramesses_requiem` 是 Ramesses' Curse 的可选内容附属，适用 Minecraft 1.20.1 / Forge 47。
本体不依赖附属；附属依赖本体 1.2.6beta 或更新的兼容版本。

## 安装

- 只需要本体：安装 `ramesses_curse-1.2.6beta.jar` 和本体原有前置。
- 需要绯黯归墟：再安装 `ramesses_requiem-1.0.0.jar`，客户端和服务器均需安装。
- 附属有独立创造标签页「葬神录」，包含剑、索魂附魔书、万剑归宗附魔书。
- 升级时替换旧本体 JAR，不要同时保留旧版。旧版内置了剑，不能与新附属混装。

剑、附魔、实体、效果、伤害类型及资源仍使用原来的 `ramesses_curse:` 注册 ID，以保留现有物品的身份。
从旧版升级时，同时装入新版本体和附属，可以继续读取原来的剑与附魔。
只装本体时不会注册这些内容；在有剑的存档中移除附属后保存，可能丢失相应物品和实体。
原配置文件名及玩家冷却 NBT 键不变。

## 代码与资源

本目录单独存放剑及后续技能的 Java、Mixin、网络消息、注册表、配置和资源。
本体 `src/main/` 不再包含剑逻辑。

| 路径 | 用途 |
| --- | --- |
| `src/main/java/com/eldren/wutao/ramesses_requiem/` | 附属所有 Java 逻辑 |
| `RequiemAddon.java` | 附属入口、独立创造页、配置与兼容接口注册 |
| `RequiemItems.java` / `RequiemEntities.java` / `RequiemEffects.java` / `ModEnchantments.java` | 附属内容注册 |
| `RequiemNetwork.java` | 独立网络通道 `ramesses_requiem:main` |
| `RequiemClientSetup.java` | 客户端实体渲染器注册 |
| `src/main/resources/ramesses_requiem.mixins.json` | 附属独立 Mixin 配置 |
| `src/main/resources/assets/ramesses_curse/` | 剑模型、贴图、特效、着色器和翻译 |
| `src/main/resources/data/` | 伤害类型、伤害标签及 Better Combat 适配 |

以上 Java 文件均在 Java 根包目录内。贴图的完整项目相对路径为：

`addons/crimson-requiem/src/main/resources/assets/ramesses_curse/textures/item/crimson_requiem.png`

本体仅通过通用 `DynastyCombatHooks` 接收附属注册的词条、沉默和净化豁免；不加载或引用附属类。
附属复用本体的动态字体 API 和封印规则。这些依赖均从附属指向本体。

## 构建和开发运行

在仓库根目录运行，使用根 Gradle Wrapper 和 Forge 工具链。根构建脚本引入本目录的 `build.gradle`，
附属使用独立 `requiem` source set 编译、打包和重混淆，不会合入本体 JAR。

```powershell
# 同时构建本体与附属
.\gradlew.bat build --offline

# 仅构建附属（自动编译需要的本体类）
.\gradlew.bat requiemJar --offline

# 本地默认：本体 + 附属（IDE 的 Gradle runClient 按钮同样生效）
.\gradlew.bat runClient --offline
.\gradlew.bat runGameTestServer --offline

# 临时关闭附属，只加载本体
.\gradlew.bat -PwithRequiem=false runClient --offline
.\gradlew.bat -PwithRequiem=false runGameTestServer --offline
```

项目的 `gradle.properties` 设置了 `withRequiem=true`，命令行 `-PwithRequiem=false` 可覆盖此默认值。
这只影响开发环境启动，发布时本体和附属仍是独立 JAR，可选择是否安装附属。

产物分别在根目录 `build/libs/ramesses_curse-1.2.6beta.jar` 和
`addons/crimson-requiem/build/libs/ramesses_requiem-1.0.0.jar`。
两种测试安装使用独立世界：`run-gametest/base/` 与 `run-gametest/requiem/`，避免切换附属时互相改写注册表快照。
本地证书检查有问题时，可沿用项目原有参数 `'-Dnet.minecraftforge.gradle.check.certs=false'`。

## 保留的功能

主副手近战、蒸汽十字剑气、回旋投剑与槽位固定、索魂、万剑归宗、冥焰、裂宙终裁、
实时生命/幸运成长伤害、正常掉落归属、动态 tooltip 及 Better Combat 资源全部归入附属。
临时测试指令 `/requiem_reset` 仍保留，需要权限等级 2，仅重置 Shift+右键冷却。
移除附属时，不会加载此指令、网络消息或剑的 Mixin。

完整操作和机制说明见仓库根目录的 [docs/crimson-requiem.md](../../docs/crimson-requiem.md)。

## 拆分验证

本体单独加载通过 1 项 GameTest；本体与附属同时加载通过 10 项 GameTest。
包含原有 8 项成长伤害、主副手攻击、槽位保留、万剑归宗奖励和裂宙终裁测试，
另检查普通武器不获得豁免，以及附属旧 ID、两本书、独立标签页和词条兼容接口。
这次验证覆盖专用测试服务器和构建产物；尚未在拆分后的客户端进行视觉或联机验收。
