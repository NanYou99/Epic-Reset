package com.nanyou.epicreset.armorset;

import com.nanyou.epicreset.armorset.api.ArmorSetEffect;
import com.nanyou.epicreset.armorset.stat.StatCategory;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;

import java.util.*;

public class ArmorSet {

    private final String id;
    private final String translationKey;
    private final Map<ArmorItem.Type, Item> pieces;
    private final Map<ArmorItem.Type, String> piecePerks;
    private final Map<ArmorItem.Type, Map<StatCategory, Double>> pieceStatMods;
    private final String twoPieceDesc;
    private final String fourPieceDesc;
    private final ArmorSetEffect twoPieceEffect;
    private final ArmorSetEffect fourPieceEffect;
    private final Map<StatCategory, Double> twoPieceStatMods;
    private final Map<StatCategory, Double> fourPieceStatMods;

    private ArmorSet(Builder builder) {
        this.id = builder.id;
        this.translationKey = builder.translationKey;
        this.pieces = Collections.unmodifiableMap(new EnumMap<>(builder.pieces));
        this.piecePerks = Collections.unmodifiableMap(new EnumMap<>(builder.piecePerks));
        this.pieceStatMods = Collections.unmodifiableMap(new EnumMap<>(builder.pieceStatMods));
        this.twoPieceDesc = builder.twoPieceDesc;
        this.fourPieceDesc = builder.fourPieceDesc;
        this.twoPieceEffect = builder.twoPieceEffect;
        this.fourPieceEffect = builder.fourPieceEffect;
        this.twoPieceStatMods = builder.twoPieceStatMods == null
                ? Map.of() : Collections.unmodifiableMap(new EnumMap<>(builder.twoPieceStatMods));
        this.fourPieceStatMods = builder.fourPieceStatMods == null
                ? Map.of() : Collections.unmodifiableMap(new EnumMap<>(builder.fourPieceStatMods));
    }

    public String getId() { return id; }
    public String getTranslationKey() { return translationKey; }
    public Map<ArmorItem.Type, Item> getPieces() { return pieces; }
    public String getPiecePerk(ArmorItem.Type type) { return piecePerks.get(type); }
    public Map<StatCategory, Double> getPieceStatMods(ArmorItem.Type type) {
        return pieceStatMods.getOrDefault(type, Map.of());
    }
    public String getTwoPieceDesc() { return twoPieceDesc; }
    public String getFourPieceDesc() { return fourPieceDesc; }
    public ArmorSetEffect getTwoPieceEffect() { return twoPieceEffect; }
    public ArmorSetEffect getFourPieceEffect() { return fourPieceEffect; }
    public Map<StatCategory, Double> getTwoPieceStatMods() { return twoPieceStatMods; }
    public Map<StatCategory, Double> getFourPieceStatMods() { return fourPieceStatMods; }

    public boolean containsItem(Item item) {
        return pieces.containsValue(item);
    }

    public int countEquipped(Iterable<Item> wornArmor) {
        int count = 0;
        for (Item armor : wornArmor) {
            if (containsItem(armor)) count++;
        }
        return count;
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public static class Builder {
        private final String id;
        private String translationKey;
        private final Map<ArmorItem.Type, Item> pieces = new EnumMap<>(ArmorItem.Type.class);
        private final Map<ArmorItem.Type, String> piecePerks = new EnumMap<>(ArmorItem.Type.class);
        private final Map<ArmorItem.Type, Map<StatCategory, Double>> pieceStatMods = new EnumMap<>(ArmorItem.Type.class);
        private String twoPieceDesc;
        private String fourPieceDesc;
        private ArmorSetEffect twoPieceEffect = (p, c) -> {};
        private ArmorSetEffect fourPieceEffect = (p, c) -> {};
        private Map<StatCategory, Double> twoPieceStatMods;
        private Map<StatCategory, Double> fourPieceStatMods;

        private Builder(String id) {
            this.id = id;
            this.translationKey = "armorset." + id;
        }

        public Builder translationKey(String key) {
            this.translationKey = key;
            return this;
        }

        public Builder piece(ArmorItem.Type type, Item item, String perkKey) {
            return piece(type, item, perkKey, null);
        }

        public Builder piece(ArmorItem.Type type, Item item, String perkKey,
                             Map<StatCategory, Double> statMods) {
            this.pieces.put(type, item);
            if (perkKey != null) this.piecePerks.put(type, perkKey);
            if (statMods != null) this.pieceStatMods.put(type, new EnumMap<>(statMods));
            return this;
        }

        public Builder twoPiece(String descKey, ArmorSetEffect effect) {
            return twoPiece(descKey, effect, null);
        }

        public Builder twoPiece(String descKey, ArmorSetEffect effect,
                                Map<StatCategory, Double> statMods) {
            this.twoPieceDesc = descKey;
            if (effect != null) this.twoPieceEffect = effect;
            if (statMods != null) this.twoPieceStatMods = new EnumMap<>(statMods);
            return this;
        }

        public Builder fourPiece(String descKey, ArmorSetEffect effect) {
            return fourPiece(descKey, effect, null);
        }

        public Builder fourPiece(String descKey, ArmorSetEffect effect,
                                 Map<StatCategory, Double> statMods) {
            this.fourPieceDesc = descKey;
            if (effect != null) this.fourPieceEffect = effect;
            if (statMods != null) this.fourPieceStatMods = new EnumMap<>(statMods);
            return this;
        }

        public ArmorSet build() {
            if (pieces.size() != 4) {
                throw new IllegalStateException("套装 [" + id + "] 必须注册4件盔甲，当前=" + pieces.size());
            }
            return new ArmorSet(this);
        }
    }
}
