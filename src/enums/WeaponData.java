package enums;

/**
 * 武器数据枚举 —— 集中管理所有防御塔的属性数据。
 * 包含：价格、解锁等级、伤害、射速、范围、攻击类型等。
 */
public enum WeaponData {

    ARROW_TOWER(
        "基础箭塔", "🏹",
        50,     // 价格
        1,      // 解锁等级
        12,     // 伤害
        0.8,    // 射速（秒/发）
        2.67,   // 范围（格） - 约160像素
        "单体",  // 攻击类型
        "#64c864",
        "最基础的防御塔，射速快但伤害较低。适合早期清怪，性价比极高。",
        "Lv.1→Lv.2: 伤害+30% 射速+10%\nLv.2→Lv.3: 伤害+30% 范围+0.5"
    ),

    CANNON_TOWER(
        "重装炮塔", "💣",
        100,    // 价格
        2,      // 解锁等级
        35,     // 伤害
        2.0,    // 射速（秒/发）
        2.17,   // 范围（格） - 约130像素
        "范围",  // 攻击类型
        "#c86432",
        "发射重型炮弹，对范围内所有敌人造成溅射伤害。对付成群怪物效果拔群。",
        "Lv.1→Lv.2: 伤害+30% 爆炸范围+20\nLv.2→Lv.3: 伤害+30% 射速+10%"
    ),

    SLOW_TOWER(
        "寒冰减速塔", "❄️",
        150,     // 价格
        3,      // 解锁等级
        6,      // 伤害
        1.2,    // 射速（秒/发）
        2.0,    // 范围（格） - 约120像素
        "减速",  // 攻击类型
        "#6496ff",
        "发射寒冰弹，命中后降低怪物移动速度。战术型防御塔，配合其他塔使用效果更佳。",
        "Lv.1→Lv.2: 减速效果+20% 伤害+30%\nLv.2→Lv.3: 减速持续+50% 范围+0.5"
    ),

    LIGHTNING_TOWER(
        "连锁闪电塔", "⚡",
        260,    // 价格
        5,      // 解锁等级
        24,     // 伤害
        1.0,    // 射速（秒/发）
        3.0,    // 范围（格） - 约180像素
        "多目标", // 攻击类型
        "#9664ff",
        "释放连锁闪电，同时攻击多个目标。对付密集怪物群时伤害爆炸。",
        "Lv.1→Lv.2: 连锁次数+1 伤害+30%\nLv.2→Lv.3: 连锁次数+1 范围+0.5"
    ),

    NUKE_TOWER(
        "终极核弹塔", "🌋",
        500,    // 价格
        8,      // 解锁等级
        180,    // 伤害
        4.0,    // 射速（秒/发）
        4.67,   // 范围（格） - 约280像素
        "全屏",  // 攻击类型
        "#ff4444",
        "终极武器！发射毁灭性核弹，对全屏敌人造成巨额伤害。冷却时间较长，但一击必杀。",
        "Lv.1→Lv.2: 伤害翻倍 冷却-20%\nLv.2→Lv.3: 伤害翻倍 范围+1.0"
    );

    // ===== 属性字段 =====
    private final String name;
    private final String icon;
    private final int price;
    private final int unlockLevel;
    private final int damage;
    private final double fireRate;
    private final double range;
    private final String targetType;
    private final String color;
    private final String detailDesc;
    private final String upgradePath;

    WeaponData(String name, String icon, int price, int unlockLevel,
               int damage, double fireRate, double range, String targetType,
               String color, String detailDesc, String upgradePath) {
        this.name = name;
        this.icon = icon;
        this.price = price;
        this.unlockLevel = unlockLevel;
        this.damage = damage;
        this.fireRate = fireRate;
        this.range = range;
        this.targetType = targetType;
        this.color = color;
        this.detailDesc = detailDesc;
        this.upgradePath = upgradePath;
    }

    // ===== Getter 方法 =====
    public String getName() { return name; }
    public String getIcon() { return icon; }
    public int getPrice() { return price; }
    public int getUnlockLevel() { return unlockLevel; }
    public int getDamage() { return damage; }
    public double getFireRate() { return fireRate; }
    public double getRange() { return range; }
    public String getTargetType() { return targetType; }
    public String getColor() { return color; }
    public String getDetailDesc() { return detailDesc; }
    public String getUpgradePath() { return upgradePath; }

    /** 获取对应的 TowerType 枚举 */
    public TowerType toTowerType() {
        switch (this) {
            case ARROW_TOWER:     return TowerType.ARROW;
            case CANNON_TOWER:    return TowerType.CANNON;
            case SLOW_TOWER:      return TowerType.SLOW;
            case LIGHTNING_TOWER: return TowerType.LIGHTNING;
            case NUKE_TOWER:      return TowerType.NUKE;
            default:              return TowerType.ARROW;
        }
    }

    /** 获取 HUD 按钮索引（与 HudPanel 中的顺序一致） */
    public int getHudIndex() {
        switch (this) {
            case ARROW_TOWER:     return 0;
            case CANNON_TOWER:    return 1;
            case SLOW_TOWER:      return 3;
            case LIGHTNING_TOWER: return 4;
            case NUKE_TOWER:      return 5;
            default:              return 0;
        }
    }

    /** 根据 HUD 索引获取 WeaponData */
    public static WeaponData fromHudIndex(int hudIndex) {
        switch (hudIndex) {
            case 0: return ARROW_TOWER;
            case 1: return CANNON_TOWER;
            case 3: return SLOW_TOWER;
            case 4: return LIGHTNING_TOWER;
            case 5: return NUKE_TOWER;
            default: return ARROW_TOWER;
        }
    }
}
