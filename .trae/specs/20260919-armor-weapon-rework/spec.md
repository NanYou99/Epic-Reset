# Spec：盔甲套装 × 武器共鸣 × 弓/弩/三叉戟/盾牌互动 全面重写

## 1. 问题 / 用户 / 目标 / 非目标

### 1.1 问题（Problem）
当前 epic-reset 1.21.1 模组的 7 套盔甲（皮革/锁链/铁/金/钻石/下界合金/岩晶）使用旧版定位（夜视/抗性/挖掘加速），且武器、弓、弩、三叉戟、盾牌没有联动机制；用户要求一次性按新的定位表全部重写，并引入**材质共鸣**与**全套盔甲 × 武器互动**两条新触发条件，同时保证乘法、防永久叠加、Lore 直接写入 tooltip、原版护甲值保留这四条硬约束。

### 1.2 目标用户（Users）
- 单人 / 多人联机生存玩家
- 冒险 / RPG 服服主（需要平衡数值、不掉线、不炸服）
- 模组维护者（按 SOLID 分层，单件/套装/共鸣/武器四类逻辑解耦，后续加新套不冲突）

### 1.3 目标（Goals）
G1. 单件词条、2/4 件套装效果按用户新表 1:1 对齐 7 套盔甲（含岩晶保持原定位不改动，因为新表未列岩晶）。
G2. 新增材质共鸣：**穿戴 4 件同材质盔甲 AND 主手持同材质剑/斧** 两条件同时满足才激活对应武器共鸣加成。
G3. 弓、弩、三叉戟、盾牌：**不参与共鸣**，按"当前穿的全套盔甲（count=4）"触发各自的武器互动效果；若无全套（<4），仅武器自带词条生效。
G4. 所有百分比加成采用**乘法计算**（`finalDmg = dmg × (1 + a) × (1 + b) × ...`，非 `1+a+b`）。
G5. 持续类 buff（如金套击杀吸血 5s、金套暴击瞬间吸血 3s、锁链移动回血等）必须做好计时器，不永久叠加（`ensureEffect(duration<40 才续杯)` 或 `lastTriggerTick` 两档互斥）。
G6. 装备 Lore：单件词条 + 2/4 件效果 + 单件/套装描述（如"朴素皮甲，野外求生的基础装备…"）直接写入物品悬浮 tooltip。
G7. **原版盔甲基础护甲值、基础韧性、基础附魔兼容全部保留**，新增词条是额外叠加属性，绝不覆盖原版防御。

### 1.4 非目标（Non-Goals）
NG1. 不新增独立 UI/属性面板/Tab（上一轮已删除）。
NG2. 不新增自定义物品/方块/附魔；全部走原版 Items 盔甲 + 剑/斧/弓/弩/三叉戟/盾牌。
NG3. 不改原版耐久、原版剑斧攻速；仅在服务端战斗事件中做伤害乘/减。
NG4. 不新增任何网络协议（上一轮已删）。
NG5. 不动岩晶套装（RockCrystalArmorSet），用户表未涉及，保持旧行为。

---

## 2. 功能需求（Functional Requirements）

### 2.1 触发条件层
FR1 · 材质共鸣触发判定：
- FR1a. 玩家穿 `count==4` 的同材质盔甲（leather/chainmail/iron/golden/diamond/netherite）。
- FR1b. 主手 `ItemStack` 是对应材质的 SwordItem 或 AxeItem。
- FR1c. **AND 关系**：FR1a ∧ FR1b 才触发该武器/材质对应的共鸣效果；少一者均不触发。
- FR1d. 弓/弩/三叉戟/盾牌 **不会命中 FR1**（这四类独立走 FR2）。

FR2 · 武器互动触发判定：
- FR2a. 玩家穿 `count==4` 的同材质盔甲 → 触发对应武器（弓/弩/三叉戟/盾牌）的盔甲加成互动效果。
- FR2b. `count<4` → 仅保留该武器的"自带词条"（弓远程伤害+5% 等）。
- FR2c. 无论盔甲数量如何，武器自带词条**始终生效**。

### 2.2 盔甲单件词条（7 套 × 4 件 = 28 条）
- 皮套：头 1%减伤 / 胸 2%减伤 / 腿 1%暴击率 / 靴 8%概率受伤短移速提升。
- 锁链套：头 2%攻速 / 胸 3%击退抗性 / 腿 1%穿甲 / 靴 2%攻速。
- 铁套：头 3%暴伤 / 胸 3%减伤 / 腿 3%近战伤害 / 靴 1%吸血。
- 金套：头 3%暴击率 / 胸 4%暴伤 / 腿 2%暴击率 / 靴 2%穿甲。
- 钻石套：头 5%击退抗性 / 胸 4%减伤 / 腿 4%暴伤 / 靴 1%吸血。
- 下界合金套：头 3%暴击率 / 胸 5%减伤 / 腿 4%近战伤害 / 靴 3%穿甲。
- 岩晶套：旧值保持不变（NG5）。

### 2.3 盔甲 2/4 件套装效果（7 套 × 2 = 14 条）
- 皮套 2 件：减伤+5%；4 件：减伤+8%、暴击率+4%、吸血 2%、受伤 15% 移速 buff。
- 锁链 2 件：攻速+8%、击退抗性+10%；4 件：攻速+12%、暴击率+6%、穿甲+5%、移动中缓慢回血。
- 铁 2 件：近战伤害+10%、减伤+7%；4 件：近战+15%、减伤+10%、暴伤+12%、吸血+3%、HP<30% 额外+8%近战。
- 金 2 件：暴击率+10%、暴伤+15%；4 件：暴击率+14%、暴伤+25%、穿甲+8%、击杀生物获得 5%吸血 5s。
- 钻石 2 件：减伤+14%、击退抗性+20%；4 件：减伤+20%、击退抗性+30%、暴伤+18%、吸血+4%、单次大额伤害减 15%。
- 下界合金 2 件：近战+18%、减伤+16%；4 件：近战+25%、减伤+24%、暴击率+12%、暴伤+30%、吸血+6%、穿甲+12%、击退抗性+35%、免疫火焰、HP>70% 额外+10%近战。
- 岩晶 2/4 件：旧值保持不变（NG5）。

### 2.4 剑/斧材质共鸣（6 材质 × 2 武器 = 12 条 × 自带词条 2）
- 木剑自带：近战+2%、暴击率+1%；皮4+木剑共鸣：吸血+1%、横扫范围增大。
- 石剑自带：近战+3%、穿甲+1%；锁链4+石剑共鸣：攻速+3%、移动攻击不减速。
- 铁剑自带：近战+4%、暴伤+4%；铁4+铁剑共鸣：HP<30%再多+5%近战、横扫伤害提升。
- 金剑自带：暴击率+4%、暴伤+6%；金4+金剑共鸣：暴击瞬间 3%吸血 3s。
- 钻石剑自带：近战+5%、吸血+2%；钻石4+钻石剑共鸣：大额受击触发反击伤害。
- 下界合金剑自带：近战+7%、暴击率+3%、穿甲+3%；下界合金4+合金剑共鸣：HP>70% 增伤 10%→18%、攻击带微量火焰伤害。
- 木斧～下界合金斧（6 套自带词条 + 6 套共鸣）按原文 1:1。

### 2.5 弓/弩/三叉戟/盾牌 × 盔甲 6 套互动（4 武器 × 6 套 = 24 条 + 4 条自带）
**弓**：自带远程+5%
- 皮4：拉弓移速惩罚降低
- 锁链4：满蓄力箭暴击率+5%
- 铁4：命中减速敌人
- 金4：远程暴伤+12%
- 钻石4：受击临时弓伤害提升
- 下界合金4：满蓄力箭附带火焰、远程伤害+8%

**弩**：自带远程+8%、穿甲+4%
- 皮4：装填移速惩罚降低
- 锁链4：命中降低敌人击退抗性
- 铁4：命中附加流血（DoT）
- 金4：弩暴击重置装填 CD
- 钻石4：持弩状态减伤+3%
- 下界合金4：弩箭带火焰、可穿透 1 名敌人

**三叉戟**：自带近战+4%、投掷+6%、穿甲+2%
- 皮4：水下移速提升、投掷冷却缩短
- 锁链4：命中减速 2s、投掷返还少量耐久
- 铁4：持三叉戟近战、HP<30% 投掷伤害+6%
- 金4：投掷暴击、暴伤+15%
- 钻石4：手持三叉戟减伤+4%
- 下界合金4：近战/投掷命中附灼烧、水下攻击额外+10%

**盾牌**：自带减伤+3%、击退抗性+5%、成功格挡减伤小幅
- 皮4：格挡成功小幅移速提升 2s
- 锁链4：格挡成功小幅缩短盾牌 CD
- 铁4：格挡成功下一次近战+7%
- 金4：格挡成功下次攻击暴击率+6%
- 钻石4：格挡大额伤害时额外减伤+8%
- 下界合金4：格挡成功反弹少量火焰伤害给攻击者；格挡期间额外减伤+5%

### 2.6 Lore / Tooltip 直接写入
FR6 · 所有盔甲物品悬浮 tooltip（`ClientTooltipHandler`）追加三个区块：
- 单件词条（按 FR2.2）
- 套装进度（X/4）+ 激活效果（[2件]/[4件]），并追加"套装Lore"描述（如"朴素皮甲，野外求生的基础装备…"）

FR7 · 所有剑/斧/弓/弩/三叉戟/盾牌悬浮 tooltip（扩展 ClientTooltipHandler 的匹配范围）追加三个区块：
- 武器自带词条
- 材质共鸣（仅剑/斧）：若玩家穿全套对应盔甲 → 亮绿色显示共鸣已激活，否则灰字
- 武器互动效果（仅弓/弩/三叉戟/盾牌）：穿对应全套盔甲时显示激活条
- 每件武器的"Lore"描述段（如"粗木短剑，荒野求生的简易武器。皮革套装共鸣：生存者的原始搏斗。"）

### 2.7 战斗事件层（计算与触发）
FR8 · 所有数值计算必须走服务端战斗事件（LivingHurtCallback / LivingAttackCallback / EntityAttackServerCallback 系列 Fabric API），客户端绝不处理伤害数值。

FR9 · 乘法定律：在每次计算前，先把同一类加成（例如所有"近战伤害+"）收集成 List<Double>，然后 `mul = list.stream().reduce(1d, (a,b)->a*(1+b))`；最终 `finalDmg = baseDmg * mul`；对减伤则 `finalDmg = baseDmg * (1-list.stream().reduce(1d,(a,b)->a*(1-b)))`。

FR10 · 持续类 buff（击杀吸血、暴击吸血、格挡移速等）：
- 短 buff 用 `ensureEffect(duration, amplifier)` 且仅 `current==null OR current.getDuration() < 10 (半秒) OR current.amplifier < target` 时重新 add；
- 长冷却触发（例如钻石套"单次大额伤害减15%"）用 `player.getPersistentData().putLong("epicreset:lastXxxTick", tick)` 判定，避免每秒触发。

---

## 3. 非功能需求（Non-Functional Requirements）

NFR1 · 分层架构（SRP + OCP + ISP）：
- `armorset.api`：IArmorSetProvider / ArmorSetEffect（已存在）
- `armorset.stat`（新）：`StatCategory` 枚举（MELEE_DMG / RANGED_DMG / CRIT_RATE / CRIT_DMG / ARMOR_PIERCE / DAMAGE_REDUCE / LIFE_STEAL / KNOCKBACK_RESIST / ATTACK_SPEED / ATTACK_RANGE / BURN），`StatAccumulator`（纯乘法累积器，无状态）
- `armorset.set`（改造）：每件 Piece 除 perkKey 外加 `getPieceStatMods(): Map<StatCategory, Double>`；two/four 加 `getArmorSetStatMods(count): Map<StatCategory, Double>`
- `weapon.resonance`（新）：`ResonanceManager` 单例，`computeResonance(player): (weaponType, material) -> Map<StatCategory, Double> + trigger lambdas`
- `weapon.interact`（新）：`WeaponInteractManager` 单例，`computeBow/Crossbow/Trident/Shield(player)`
- `event`（改造）：`CombatEvents`（新），`ServerPlayerTickHandler`（仅保套装 tick 触发的缓慢效果）
- `client.event`（改造）：`ClientTooltipHandler`（支持武器自描 + 共鸣段 + Lore 段）

NFR2 · OCP：新增套装/新增武器 → 只新增对应 set 类 + 在 ResonanceManager/WeaponInteractManager 注册映射，不修改 CombatEvents。

NFR3 · 性能：StatAccumulator 每次战斗最多遍历 7 套套装 + 4 件单件 + 1 武器，总遍历 ≤ 12 次 Map.Entry；无额外内存分配（全部用 EnumMap，size <= StatCategory.values.length）。

NFR4 · 线程安全：仅服务端主线程（ServerTick / LivingHurtCallback）访问，不涉及 volatile；玩家 PDC 写操作按 tick 限流。

NFR5 · 兼容性：不 mixin 原版 LivingEntity hurt 方法、不 mixin ItemStack hurtEnemy 方法，全部走 Fabric 官方 API（fabric-lifecycle-events-v1、fabric-entity-events-v1 的 LivingHurtCallback / LivingAttackCallback / AttackEntityCallback）；若 Fabric API 没有直接回调，再做最少 Mixin。

---

## 4. 约束 / 依赖 / 假设 / 开放问题

### 4.1 依赖（Dependency）
- `fabric-api`：`fabric-lifecycle-events-v1`（已在 fabric.mod.json 中），`fabric-entity-events-v1`（必须在 fabric.mod.json 的 `depends` 中存在，没有则补）
- 原版 `EnchantmentHelper`、`DamageSource`、`LivingEntity` 1.21.1 Mojang Mappings
- 原版 `Items`（全部 6 材质剑/斧/弓/弩/三叉戟/盾牌 + 24 件原版盔甲 + 岩晶 4 件自定义）

### 4.2 约束（Constraint）
C1. 原版基础盔甲值（`ArmorMaterial` 提供的 defense / toughness / knockbackResistance）**不可覆盖**；任何减伤/穿甲必须通过服务端 hurt 事件的 `setAmount(newAmount)` 或返回值实现。
C2. 百分比一律写成 0.01 的倍数（5% = 0.05），**不用整数 percent 字段**；避免 `5 + 10 = 15%` 加法误解。
C3. 所有 trigger（受伤概率移速提升、击杀 buff、暴击 buff 等）必须记录"上次触发 tick"，防止高 tickRate 服务器无限触发。
C4. 新增 Mixin 数量 ≤ 2（尽量 0；实在需要 Mixin 才加，例如 `SwordItem#sweepDamage` 扩展横扫范围）。
C5. `Player#getPersistentData` 前缀统一 `epicreset:` 命名空间，绝不污染其他模组。

### 4.3 假设（Assumption）
A1. Fabric API `LivingHurtCallback`（1.21）签名 = `boolean onHurt(LivingEntity entity, DamageSource source, float amount)`，返回 true 取消，或通过它对 amount 乘后写入（新版签名若为 `float modifyHurtDamage(...)` 按最新，以 1.21.1 loom 反查 jar 为准）。
A2. 剑横扫攻击可通过原版 `SwordItem#hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker)` 前后的 `target.level() instanceof ServerLevel && target.hurtMarked` 判断横扫是否发生；必要时 Mixin `SwordItem#sweep` 仅修改半径的 float 值。
A3. 原版击退抗性 = `Entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE).getValue`；新增"击退抗性 %" 走 `AttributeInstance.addTransientModifier(Modifier)`，tick 结束时移除（避免永久叠加，满足 FR10）。

### 4.4 开放问题（Open Question，需用户确认）
Q1. 岩晶套装（RockCrystalArmorSet）：用户新表未列出，NG5 默认保留旧值。是否需要为岩晶套也增加"岩晶剑/斧/弓/弩/三叉戟/盾牌"？如果 ModItems 里没有这 6 件物品 → 保持 NG5，不做。
Q2. "穿甲%" 数值效果：在 MC 原版中 "穿甲 = 无视护甲" 常见实现是把 target 的 `Attributes.ARMOR` 临时降低 X%，然后再还原（通过 AttributeModifier 瞬时）。是否采用这个实现？（默认：是）
Q3. 弓"拉弓移速惩罚降低"、弩"装填移速惩罚降低"：原版 1.21 中弓拉满/弩装填时 SlowEffect 是原版行为吗？若原版不是通过 SlowEffect，需要 Mixin `BowItem#releaseUsing` 前的速度计算。（默认：Mixin 1 个，违反 C4 的上限 2 可接受）

---

## 5. 验收标准（Acceptance Criteria）

### 5.1 型为规则（Rule，可客观验证）
- R1：`compileJava compileClientJava` exit 0。
- R2：`runClient` exit 0；latest.log `0 ERROR + 0 Exception + 0 Caused by + 0 Mixin transformation failed`。
- R3：`latest.log` 命中 `"已注册 [7] 套盔甲套装"` + `"模组初始化完成"`。
- R4：穿 4 件皮甲 + 主手木剑 → 触发共鸣（用 debug 日志开 `LOGGER.debug(...)` 在 ResonanceManager 中打），命中 `[共鸣] leather_sword`。
- R5：穿 3 件皮甲 + 木剑（count<4）→ 不触发共鸣（日志无 `[共鸣]`）。
- R6：穿 4 件皮甲 + 主手弓（非剑斧）→ 不触发皮套剑共鸣，但皮套 4 件弓互动（拉弓移速惩罚降低）生效。
- R7：乘法独立验证：近战伤害加成 = 单件 + 2 件 + 4 件 + 铁剑自带 + 共鸣5%，用公式 `1.03 * 1.10 * 1.15 * 1.04 * 1.05` 计算；最终伤害 ≈ 该乘积 × 基伤（允许 ±0.5 浮点误差）。
- R8：金套 4 件击杀吸血 5s → 第一次击杀 addEffect(LIFE_STEAL_BUFF, 100 tick)；连续 3 次击杀，PDC 中 `lastKillLifeStealTick` 只在第 1 次写入，后两次不重复 add（查看日志）。
- R9：岩晶套装 7 件套数不变、原有旧 perk/效果不被改动（通过查看 RockCrystalArmorSet.java diff 无修改）。
- R10：原版钻石胸甲的 defense=8、toughness=3，穿戴后在玩家属性中仍显示原数值（通过 F3 查看 Attributes.ARMOR 等于原版值 + 自定义 ModItems 属性修饰符不出现；或通过 Mixin 断言确认 ArmorItem.constructor 未被修改）。
- R11：ClientTooltipHandler 对钻石剑显示 3 段：(1) 自带词条 (近战+5% / 吸血+2%)；(2) 钻石套共鸣（亮绿或暗灰）；(3) Lore（"坚锐钻石剑…钻石套装共鸣…"）。
- R12：对弓显示 2 段：(1) 自带远程+5%；(2) 对应全套盔甲互动效果（穿全套亮绿，否则暗灰）；(3) Lore。
- R13：zh_cn.json 中新增的翻译 key 无 `Translation key not found`（latest.log 中 grep 无）。

### 5.2 过程量规尺（Rubric，打分式）
- U1 · SOLID 分层（0-2）：
  - 2：单件/套装 stat 用 StatAccumulator 乘法（无耦合）；Resonance / WeaponInteract 通过 Material EnumMap 注册（OCP 可扩展）；CombatEvents 只负责"取累加器 → 乘 → 赋值"，不写死数值。
  - 1：分层正确，但 CombatEvents 中出现少量 if (item == Items.LEATHER_SWORD) 分支。
  - 0：全部 if/else 堆在一个类。
- U2 · 防永久叠加与触发节流（0-2）：
  - 2：每个 trigger 都显式有 `lastXxxTick` PDC 字段 OR `ensureEffect(duration < safetyMargin)` 续杯策略；latest.log grep 无重复"addEffect 每秒超过 1 次"的日志。
  - 1：大部分触发节流，有 1 处小概率重复触发（概率 < 5%）。
  - 0：出现永久 buff（持续时长 > 600s 且无触发冷却）。
- U3 · 乘法正确性（0-2）：
  - 2：所有同类别数值走 `StatAccumulator.mulReduce(1, list)`，代码中 `+` 仅用于 `countEquipped`，绝无 `dmg += dmg*pct`。
  - 1：1-2 处用了加法，但非核心战斗路径。
  - 0：铁套 4 件近战加成（10%+15%）= 25%（加法）。
