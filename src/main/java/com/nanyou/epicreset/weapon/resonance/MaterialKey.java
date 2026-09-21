package com.nanyou.epicreset.weapon.resonance;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum MaterialKey {
    LEATHER("leather"),
    CHAINMAIL("chainmail"),
    IRON("iron"),
    GOLDEN("golden"),
    DIAMOND("diamond"),
    NETHERITE("netherite"),
    ROCK_CRYSTAL("rock_crystal");

    private static final Map<String, MaterialKey> BY_SET_ID = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(MaterialKey::setId, Function.identity()));

    private final String setId;

    MaterialKey(String setId) { this.setId = setId; }

    public String setId() { return setId; }

    public static MaterialKey fromSetId(String id) {
        return BY_SET_ID.get(id);
    }
}
