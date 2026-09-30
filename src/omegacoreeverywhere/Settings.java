package omegacoreeverywhere;

import lunalib.lunaSettings.LunaSettings;

/** LunaLib values, read live. A missing value falls back to the default in LunaSettings.csv. */
final class Settings {

    static final boolean DEFAULT_DROPS_ENABLED = true;
    static final float DEFAULT_DROP_CHANCE = 0.33f;

    private Settings() {}

    static boolean dropsEnabled() {
        Boolean value = LunaSettings.getBoolean(OmegaCoreEverywherePlugin.MOD_ID, "oce_drops_enabled");
        return value == null ? DEFAULT_DROPS_ENABLED : value;
    }

    static float dropChance() {
        Double value = LunaSettings.getDouble(OmegaCoreEverywherePlugin.MOD_ID, "oce_drop_chance");
        if (value == null || value.isNaN()) return DEFAULT_DROP_CHANCE;
        return (float) Math.max(0d, Math.min(1d, value));
    }
}
