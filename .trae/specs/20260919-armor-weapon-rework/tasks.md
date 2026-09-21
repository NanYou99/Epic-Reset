# Tasks：盔甲×武器共鸣重写 — 实现任务队列

总依赖：`spec.md` 已写；`RockCrystalArmorSet.java` 保持不动（非目标）。

---

## Task 1：新增统计分类 + 乘法累加器（基础数据层，无外部依赖）

**Priority**：high
**Status**：pending
**依赖**：—

### 工作内容
1. 在 `armorset/stat/` 下新建：
   - `StatCategory.java` enum：`MELEE_DMG, RANGED_DMG, CRIT_RATE, CRIT_DMG, ARMOR_PIERCE, DAMAGE_REDUCE, LIFE_STEAL, KNOCKBACK_RESIST, ATTACK_SPEED, ATTACK_RANGE, BURN_DMG, DOT_BLEED, MOVE_SPEED, KNOCKBACK_POWER`（14 个，覆盖 FR2.2-2.5 全部百分比/数值档位）
   - `StatAccumulator.java`：`EnumMap<StatCategory, Double>` 包装器，提供 `add(StatCategory, double pct)` + `merge(EnumMap same)` + `mulReduce()` 返回按类分别相乘后的 `finalFactors`（每类 `product = 1 × (1+a) × (1+b)...`，减伤类走 `product = 1 - (1-a)*(1-b)...`，减伤类单独列 `REDUCE_CATEGORIES` 集合）
2. 在 `armorset/api/` 新增 `IStatModifiable.java`：单件/套装/共鸣/武器自描实现该接口：`Map<StatCategory, Double> getStatMods(ServerPlayer player)` — 用于 ISP 最小化接口。

### 验收 TRs（Task-local）
| ID | 类型 | 内容 | 通过条件 |
|---|---|---|---|
| T1-R1 | rule | 编译通过 | `compileJava compileClientJava exit 0` |
| T1-R2 | rule | 乘法正确性 | 单元场景（不用 JUnit，写 main 或在 LOGGER 里打）：`DAMAGE_REDUCE` 累计 0.05+0.08 → `1 - 0.95*0.92 = 0.126 ≈ 12.6%`（不是 13% 加法）|
| T1-U1 | rubric | 可扩展性（0-2，阈值≥1） | 新增 StatCategory 只需加 enum 值；Accumulator 不改即可支持；2=不改即可 1=需 1 处小改 0=整个类要重写 |

**Completion Evidence**：(填于完成)

---

## Task 2：重写 7 套盔甲单件词条 + 2/4 件效果（除岩晶外全部）

**Priority**：high
**Status**：pending
**依赖**：Task 1

### 工作内容
对 `LeatherArmorSet / ChainmailArmorSet / IronArmorSet / GoldenArmorSet / DiamondArmorSet / NetheriteArmorSet.java` 各文件：
1. 每个 `piece(type, item, perkKey)` 新增第 4 个参数 `EnumMap<StatCategory, Double> pieceMods`（在 `ArmorSet.Builder.piece(...)` 方法扩展签名，保留旧重载给岩晶）
2. `twoPiece(desc, effect)` → 新增重载 `twoPiece(desc, effect, statMods)`；`fourPiece` 同理（statMods 即 G4 的"常驻乘法数值"，effect lambda 是"持续类/概率类触发器"）
3. 按 spec FR2.2/FR2.3 表格把 6 套 × (4 件 + 2/4 件) = 6×6 = 36 个 EnumMap 填好
4. `ArmorSetEffect lambda`：处理概率/计时/持续类触发：
   - 皮靴 8% 受伤移速提升：**不在 tick 中处理**，移到 CombatEvents（LivingHurtCallback）触发；lambda 留空（或留 `registerListener` 注册钩子）
   - 锁链 4 件"移动时缓慢持续回血"：在 tick lambda 中 `if (player.getDeltaMovement().horizontalDistanceSqr() > 0.001 && tickCount % 60 == 0) player.heal(1f)`
   - 铁 4 件 HP<30% 近战：不在 tick 处理，移到 CombatEvents 读取 player hp
   - 金 4 件"击杀生物吸血 5s"：在 LivingDeathCallback（server-side）处理
   - 钻石 4 件"单次大额伤害降低 15%"：在 LivingHurtCallback `amount > player.getMaxHealth()*0.2f` 时再乘 0.85
   - 下界合金 4 件：免疫火焰（`MobEffects.FIRE_RESISTANCE duration=260`），HP>70% 增伤 → CombatEvents 读取
5. `RockCrystalArmorSet.java` **不改**；保留旧 perk 与旧效果。

### 验收 TRs
| ID | 类型 | 内容 | 通过条件 |
|---|---|---|---|
| T2-R1 | rule | 6 套共 36 条 statMods 完整无误 | 代码 review：Leather/Iron 头胸腹腿 2/4 件全部有对应 double 值，与 spec FR2.2/FR2.3 一一对应 |
| T2-R2 | rule | 岩晶零改动 | `git diff RockCrystalArmorSet.java` 输出为空（或 IDE diff 无修改）|
| T2-R3 | rule | 编译通过 | `compileJava exit 0` |
| T2-U1 | rubric | 数值乘法/加法分离（0-2，阈值≥1） | 2=36 个 EnumMap 全是 pct 0.01 单位，没有任何地方 `dmg += pct`（加法误用）；1=1-2 处可接受警告 |

**Completion Evidence**：(填于完成)

---

## Task 3：盔甲单件属性生效（把 ArmorSet.pieceMods 注入玩家）

**Priority**：high
**Status**：pending
**依赖**：Task 2

### 工作内容
1. 新增 `ArmorSetApplier.java`（在 `armorset/`）：
   - `collectPieceMods(player): EnumMap<StatCategory, Double>`：遍历 4 件盔甲，`mgr.getSetByItem(piece)` 拿套装 → 拿该 type pieceMods → 合并入结果
   - `collectSetBonusMods(player): EnumMap<StatCategory, Double>`：2 件 → 取 twoPiece statMods；4 件 → 加 fourPiece statMods（累加进同一个 EnumMap）
   - 统一对外 `collectArmorMods(player) = merge(collectPieceMods, collectSetBonusMods)`
2. 单件属性注入**不**通过 Mixin 改 ArmorItem（因为原版 ArmorItem.getAttributeModifiers 返回 ImmutableMultimap，不改），改为通过 Fabric `EntityAttributeModificationCallback`（1.21 提供的"盔甲穿上时加修饰符"回调）加上 AttributeModifier（走 Transient，脱下移除）；**穿甲%、减伤% 这类非原版 Attribute 的 StatCategory** 不走 Attribute，保留在 ArmorSetApplier.EnumMap 里给 CombatEvents 使用（SRP：原版 Attribute 系统用于 MC 原生属性，自定义百分比用 EnumMap）。
3. `ServerPlayerTickHandler`：保留现有 for (sets) safeApply 逻辑（用于 FR10 持续类移动回血、免疫火焰续杯），不要改动 safeApply 部分。

### 验收 TRs
| ID | 类型 | 内容 | 通过条件 |
|---|---|---|---|
| T3-R1 | rule | 原版 ARMOR / ARMOR_TOUGHNESS / KNOCKBACK_RESISTANCE 值未被覆盖 | 穿全套钻石胸甲（原版 defense=8, toughness=3）后 `player.getAttributeValue(Attributes.ARMOR)` = 原版 4 件总值 |
| T3-R2 | rule | 单件/套装 statMods 能被合并 | 代码里 `ArmorSetApplier.collectArmorMods` 返回包含 DAMAGE_REDUCE / MELEE_DMG 等 key |
| T3-R3 | rule | 编译通过 | `compileJava exit 0` |
| T3-U1 | rubric | 分层纯度（0-2，阈值≥1） | 2=ArmorSetApplier 只负责"取 mods"，无任何战斗伤害逻辑 |

**Completion Evidence**：(填于完成)

---

## Task 4：新增剑/斧材质共鸣（Material × WeaponType 双维度注册 + 触发）

**Priority**：high
**Status**：pending
**依赖**：Task 3

### 工作内容
1. 新增 `weapon.resonance` package：
   - `MaterialKey.java` enum：`LEATHER, CHAINMAIL, IRON, GOLDEN, DIAMOND, NETHERITE`（6 个）
   - `WeaponType.java` enum：`SWORD, AXE, BOW, CROSSBOW, TRIDENT, SHIELD`
   - `ResonanceEntry.java` record：`MaterialKey mat; WeaponType type; Map<StatCategory, Double> mods; BiConsumer<ServerPlayer, LivingEntity> triggerOnHit; BiConsumer<ServerPlayer, DamageSource> triggerOnHurt;`（trigger 可 null）
   - `ResonanceManager.java` 单例：
     - 注册表 `EnumMap<MaterialKey, EnumMap<WeaponType, ResonanceEntry>>`
     - `register(ResonanceEntry e)`：塞进两个维度
     - `getActive(player): ResonanceEntry`：判定 count=4 → MaterialKey；主手 Item → WeaponType；两者都命中才返回
2. 注册 12 条（6 材 × 2 武）：按 FR2.4 表格
   - 木剑 `(LEATHER, SWORD)`：mods = LIFE_STEAL(0.01) + ATTACK_RANGE(0.2? 横扫增大)；triggerOnHit = `player.addEffect(SWEEPING_EDGE)` 等价（实际通过 Mixin SwordItem sweepRadius，C4 允许最多 1 个 Mixin）
   - 铁剑 `(IRON, SWORD)`：triggerOnHit = `if (hp < 0.3*maxHp) mods extra MELEE_DMG(0.05)`（mods 动态，把 triggerOnHit 改为 modifyStatMods 函数即可，Entry 加一个 `Function<ServerPlayer, Map<SC, Double>> extraMods`)
3. 注意：弓/弩/三叉戟/盾牌 **不**注册到 ResonanceManager（走 Task 5 的 WeaponInteractManager）。

### 验收 TRs
| ID | 类型 | 内容 | 通过条件 |
|---|---|---|---|
| T4-R1 | rule | ResonanceManager 注册条目数 = 12 | `ResonanceManager.getInstance().getRegisteredCount() == 12` |
| T4-R2 | rule | 触发判定 1（穿全甲+主手对）返回非 null | 构造 Mock：4 件皮 + 木剑 → getActive != null |
| T4-R3 | rule | 触发判定 2（穿 3 件甲 + 木剑）返回 null | count=3 → getActive == null |
| T4-R4 | rule | 触发判定 3（全皮甲 + 弓）返回 null | 主手 BOW 不在 Sword/Axe → getActive == null |
| T4-R5 | rule | 编译通过 | `compileJava exit 0` |
| T4-U1 | rubric | OCP（0-2，阈值≥2） | 2=新增一套共鸣 = 1 个 register() 调用，不改 ResonanceManager 判定；1=改判定；0=必须改 if/else |

**Completion Evidence**：(填于完成)

---

## Task 5：弓/弩/三叉戟/盾牌 × 6 套盔甲互动（无共鸣，按全套盔甲触发）

**Priority**：high
**Status**：pending
**依赖**：Task 3

### 工作内容
1. 新增 `weapon.interact` package：
   - `WeaponInteractEntry.java` record：`WeaponType wt; MaterialKey fullArmorMat; Map<StatCategory, Double> mods; BiConsumer<ServerPlayer, LivingEntity> onHit; BiConsumer<ServerPlayer, LivingEntity> onShoot; BiConsumer<ServerPlayer, Float> onBlockSuccess;`
   - `WeaponInteractManager.java` 单例：
     - 注册 `EnumMap<WeaponType, EnumMap<MaterialKey, WeaponInteractEntry>>`
     - 对每个 BOW/CROSSBOW/TRIDENT/SHIELD × 6 Mat 共 24 条注册
     - 自描武器词条：`Map<WeaponType, Map<SC, Double>> intrinsicMods`（单独方法 `getIntrinsicMods(wt)`）
2. 注册 24 条 + 4 条 intrinsic：按 FR2.5 表格原文
3. 特殊"穿透 1 名敌人"（下界合金弩）、"暴击重置装填 CD"（金弩）、"格挡反火伤"（下界合金盾）这类非 StatCategory 行为，写进 onShoot / onBlockSuccess / onHit BiConsumer，不要硬塞进 CombatEvents。

### 验收 TRs
| ID | 类型 | 内容 | 通过条件 |
|---|---|---|---|
| T5-R1 | rule | WeaponInteractManager 注册条目：intrinsic=4 + interaction=24，合计 28 | `getIntrinsicCount()==4 && getInteractCount()==24` |
| T5-R2 | rule | 无全套甲时 intrinsicMods 仍生效，互动 mods 不生效 | `count=3` → `getActiveInteractions(bow, mat).size() == 0`；`getIntrinsicMods(bow)` 有 RANGED_DMG=0.05 |
| T5-R3 | rule | 全套金甲 + 弩：暴击重置装填 CD | 在 CrossbowItemMixin（C4 允许 2 个，累计 2 个上限）判定 `getActiveInteractions != null && isCrit && onShoot.accept(player, target)` 重置 crossbow 装填 |
| T5-R4 | rule | 编译通过 | `compileJava exit 0` |
| T5-U1 | rubric | 与 ResonanceManager 边界清晰（0-2，阈值≥2） | 2=代码上 ResonanceManager 只有 Sword/Axe；WeaponInteractManager 只有另外四种，零交叉；1=有一个 if 混用；0=两个类互相 import |

**Completion Evidence**：(填于完成)

---

## Task 6：战斗事件总控（CombatEvents）— 乘法聚合 + 命中触发

**Priority**：high
**Status**：pending
**依赖**：Task 3 + 4 + 5

### 工作内容
1. 新建 `event/CombatEvents.java`：通过 Fabric API 注册以下事件（若 API 没对应回调则做最少 Mixin ≤ 2 个，累计 Task4/SwordItem sweep + Task5/CrossbowItem reload + 这里，单类需控制总 Mixin ≤ 4）：
   - `LivingAttackCallback` / `AttackEntityCallback`：攻击前，收集 `player + mods`
   - `LivingHurtCallback`：受伤前（减伤、穿甲、反击、大额伤害-15%）
   - `LivingDeathCallback`：击杀触发（金套 4 件吸血 5s）
   - `UseEntityCallback`（格挡成功）：盾牌 block 成功后触发 `onBlockSuccess`
2. 每次攻击/受伤流程：
   a. 取 `ArmorSetApplier.collectArmorMods(player)` → modsA
   b. 主手武器 → `ResonanceManager.getActive(player).map(e->e.mods()).orElse(empty)` → modsR
   c. 主手武器 → `WeaponInteractManager.getIntrinsicMods(wt)` + `getActiveInteractions(player)` → modsI + modsW
   d. `total = merge(modsA, modsR, modsI, modsW)`
   e. `factors = StatAccumulator.mulReduce(total)` 得到每类因子（`MELEE_FACTOR, CRIT_PROBABILITY, CRIT_MULTIPLIER, PIERCE_RATIO, REDUCE_RATIO, LIFE_STEAL_RATIO, KB_RESIST_FACTOR`）
   f. **暴击判定**：`random.nextFloat() < CRIT_PROBABILITY → isCrit=true → dmg *= CRIT_MULTIPLIER`
   g. **穿甲**：伤害先 `target armor *= (1 - PIERCE_RATIO)`（临时 AttributeModifier，立刻移除；保证原版 ARMOR 不永久变化）
   h. **减伤**：`dmg *= (1 - REDUCE_RATIO)`
   i. **近战伤害**：`dmg *= MELEE_FACTOR`（远程走 RANGED_FACTOR）
   j. **吸血**：命中后 `player.heal(dmg_dealt * LIFE_STEAL_RATIO)`（避免超过 `player.getMaxHealth`，上限截断）
   k. **击退抗性**：`player.attributes.KB_RESIST.getValue() * KB_RESIST_FACTOR`（通过 `addTransientModifier`）
3. 触发节流（FR10）：
   - 皮靴受伤 8% 移速：`lastLeatherBootsMoveTick` PDC；若 `tick - last < 60` → 跳过
   - 钻石套单次大额伤害：`lastDiamondBigHitTick`；若 `tick - last < 20` → 跳过
   - 击杀 buff：`lastKillBuffTick`；若 `tick - last < 20` → 跳过
4. **原版护甲保留验证**：穿甲结束后立即 `target.getAttribute(Attributes.ARMOR).removeModifier(PIERCE_UUID)`（UUID 固定）

### 验收 TRs
| ID | 类型 | 内容 | 通过条件 |
|---|---|---|---|
| T6-R1 | rule | 乘法独立（spec.R7） | 穿铁 4 件 + 铁剑 + 铁共鸣 → 日志打 MELEE_FACTOR = `1.03 * 1.10 * 1.15 * 1.04 * 1.05 = 1.428...`（允许 ±0.01）|
| T6-R2 | rule | 金套 4 件击杀吸血 5s 不永久叠加（spec.R8） | 连续 3 次击杀，latest.log `addEffect(LIFE_STEAL_BUFF)` 只出现 1 次（或 duration 在 90-110tick 范围内续杯，不超过 120） |
| T6-R3 | rule | 原版 ARMOR 不被永久覆盖（spec.R10） | 战斗后 `player.getAttribute(Attributes.ARMOR).getModifiers().stream().noneMatch(m -> m.id() == PIERCE_UUID)` |
| T6-R4 | rule | 岩晶套装战斗行为不变 | 穿岩晶套 → CombatEvents 中 modsA 只含有旧版 ArmorItemMixin 注入的（即没有新条目）|
| T6-R5 | rule | 编译通过 | `compileJava exit 0` |
| T6-U1 | rubric | 乘法隔离（0-2，阈值≥2） | 2=代码里没有 `dmg += pct`，全部是 `dmg *= (1 ± p)`；1=2 处以下加法；0=存在核心加法路径 |
| T6-U2 | rubric | 触发节流（0-2，阈值≥2） | 2=4 类触发全部有 lastTick PDC；日志里无重复触发刷屏；1=1 处漏节流 |

**Completion Evidence**：(填于完成)

---

## Task 7：客户端 Tooltip 重写（支持盔甲/武器 6 类，加套装 Lore + 共鸣/互动段）

**Priority**：medium
**Status**：pending
**依赖**：Task 2 + 4 + 5

### 工作内容
1. 扩写 `client.event.ClientTooltipHandler.register()`：
   - 原只匹配 `ArmorItem && IArmorSetProvider` → 扩展两个分支：
     a. 盔甲分支：保留原"单件 + 进度 + 激活"，在最后追加"套装Lore"翻译段（`armorset.<id>.lore`）
     b. 武器分支（SwordItem / AxeItem / BowItem / CrossbowItem / TridentItem / ShieldItem）：
        i. 武器自带词条（翻译键 `weapon.<item_reg_name>.intrinsic`，按 FR2.4-2.5 写多行）
        ii. 剑/斧：共鸣段（`weapon.<item_reg_name>.resonance` + 激活状态亮绿/灰字；判定：`countEquipped(客户端玩家, mat)`）
        iii. 弓/弩/三叉戟/盾牌：互动段（`weapon.<item_reg_name>.interact.<mat>` 6 条，对应 mat 激活则亮绿；每条前面加 "[皮套]/[锁链套]/..." 等标识）
        iv. 武器 Lore 段（`weapon.<item_reg_name>.lore`）
2. 为了不使 ClientTooltipHandler 过长（ISP），新建 `client.event.WeaponTooltipRenderer`：武器 tooltip 专用；ClientTooltipHandler 只分发分支。

### 验收 TRs
| ID | 类型 | 内容 | 通过条件 |
|---|---|---|---|
| T7-R1 | rule | 钻石剑 tooltip 出现 4 段（T1 自带 + T2 钻石套共鸣 + T3 Lore + T4 悬浮时按玩家穿甲显示激活） | IDE/游戏内截图文字中包含 4 段标识 |
| T7-R2 | rule | 弓 tooltip 出现 4 段（自带 + 6 条盔甲互动 + Lore） | 6 条"皮套→/锁链套→/..."全 |
| T7-R3 | rule | 盔甲皮靴 tooltip 有套装Lore段："朴素皮甲…自保能力" | 文字命中 |
| T7-R4 | rule | 0 Translation key missing WARN | latest.log 无 |
| T7-R5 | rule | 编译通过 | `compileClientJava exit 0` |
| T7-U1 | rubric | 职责隔离（0-2，阈值≥1） | 2=ClientTooltipHandler 仅分发；WeaponTooltipRenderer 独立；1=混合但方法清晰；0=所有代码堆在一个 300 行文件 |

**Completion Evidence**：(填于完成)

---

## Task 8：zh_cn.json 翻译全量重写（6 盔甲 6 武器 6 互动 + Lore）

**Priority**：high
**Status**：pending
**依赖**：Task 2-5 完成后知道所有 key

### 工作内容
1. 在 zh_cn.json 中补 key 清单：
   - 6 盔甲套：`armorset.<id> = 套装名`；`armorset.<id>.[helmet|chestplate|leggings|boots] = 单件名 / 百分比`；`armorset.<id>.[two|four] = 效果翻译`；`armorset.<id>.lore = 套装Lore`（7×(1+4+2+1) = 56，含岩晶已有保留）
   - 剑/斧 12 件：`weapon.<mat>_sword/axe.intrinsic = 自带词条`；`weapon.<mat>_sword/axe.resonance = 共鸣描述`；`weapon.<mat>_sword/axe.lore = Lore`
   - 弓/弩/三叉戟/盾牌 4 件：`weapon.bow/crossbow/trident/shield.intrinsic` + `weapon.bow.interact.leather/chainmail/iron/golden/diamond/netherite`（6 条）× 4 = 24；再各加 `.lore`
   - 总数估计 ≈ 56(盔甲) + 12*3(剑斧) + 4*(1+24+1)(远程盾) = 56+36+104 = 196 条
2. 旧 `armorset.*` 翻译覆盖（除岩晶外）。

### 验收 TRs
| ID | 类型 | 内容 | 通过条件 |
|---|---|---|---|
| T8-R1 | rule | `compileClientJava exit 0`（json 语法正确）| |
| T8-R2 | rule | 运行 runClient，latest.log 0 "Translation key not found: armorset.*" / "weapon.*" | grep 为 0 |

**Completion Evidence**：(填于完成)

---

## Task 9：编译 + runClient + 关键日志验证（总体验收）

**Priority**：high
**Status**：pending
**依赖**：Task 1-8

### 工作内容
1. `clean compileJava compileClientJava` 两次，BUILD SUCCESSFUL
2. `runClient` 启动 → wait 120s
3. `grep latest.log` 关键字集：
   - `已注册 [7] 套盔甲套装`
   - `[EpicReset] 模组初始化完成 - 已启用盔甲套装系统`
   - `0 ERROR` / `0 Exception` / `0 Caused by` / `0 Mixin transformation failed`
   - 无 `Translation key not found`
   - `ResonanceManager 注册条目 12`、`WeaponInteractManager 注册 intrinsic=4 interact=24`（在 EpicReset init 时打 info）

### 验收 TRs
| ID | 类型 | 内容 | 通过条件 |
|---|---|---|---|
| T9-R1 | rule | 编译 0 error | BUILD SUCCESSFUL exit 0 |
| T9-R2 | rule | runClient exit 0 | exit_code == 0 |
| T9-R3 | rule | 日志 0 ERROR / 0 WARN 翻译缺失 | grep 为 0 |
| T9-R4 | rule | ResonanceManager + WeaponInteractManager 条目数正确 | 日志命中 12 + 4+24=28 |

**Completion Evidence**：(填于完成)

---

## 全局依赖顺序
Task 1 → Task 2 → Task 3 → Task 4 + Task 5（可并行）→ Task 6 → Task 7 + Task 8（可并行）→ Task 9。
