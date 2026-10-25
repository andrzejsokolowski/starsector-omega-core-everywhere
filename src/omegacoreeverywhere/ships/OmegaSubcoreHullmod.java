package omegacoreeverywhere.ships;

import java.awt.Color;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

/**
 * What Exotica Technologies' Omega Subcore exotic does to a ship: cheaper weapons and fighters.
 *
 * Ordnance costs only change in the refit screen through a hull mod, so the exotic puts this hidden
 * one on the ship as a permanent mod and takes it off again when it is removed (as Exotica's own
 * Alpha Subcore does). This class is plain base game code and loads without Exotica; the exotic is
 * in {@code omegacoreeverywhere.exotica}.
 */
public class OmegaSubcoreHullmod extends BaseHullMod {

    public static final String ID = "oce_omega_subcore";

    public static final int REDUCTION_SMALL = 2;
    public static final int REDUCTION_MEDIUM = 4;
    public static final int REDUCTION_LARGE = 8;
    public static final int REDUCTION_FIGHTER = 4;
    public static final int REDUCTION_BOMBER = 8;

    /** Special Hullmod Upgrades' core upgrades run the fire control themselves, as with Exotica's Alpha Subcore. */
    public static final String[] CONFLICTS = {
            OmegaArmamentSupport.SHU_ALPHA, "specialsphmod_beta_core_upgrades", "specialsphmod_gamma_core_upgrades",
            OmegaArmamentSupport.ID};

    public static boolean hasConflict(ShipVariantAPI variant) {
        if (variant == null) return false;
        for (String id : CONFLICTS) {
            if (variant.hasHullMod(id)) return true;
        }
        return false;
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if (hasConflict(stats.getVariant())) return;
        reduce(stats, id, REDUCTION_SMALL, Stats.SMALL_BALLISTIC_MOD, Stats.SMALL_ENERGY_MOD, Stats.SMALL_MISSILE_MOD);
        reduce(stats, id, REDUCTION_MEDIUM, Stats.MEDIUM_BALLISTIC_MOD, Stats.MEDIUM_ENERGY_MOD, Stats.MEDIUM_MISSILE_MOD);
        reduce(stats, id, REDUCTION_LARGE, Stats.LARGE_BALLISTIC_MOD, Stats.LARGE_ENERGY_MOD, Stats.LARGE_MISSILE_MOD);
        reduce(stats, id, REDUCTION_FIGHTER, Stats.FIGHTER_COST_MOD, Stats.INTERCEPTOR_COST_MOD, Stats.SUPPORT_COST_MOD);
        reduce(stats, id, REDUCTION_BOMBER, Stats.BOMBER_COST_MOD);
    }

    private static void reduce(MutableShipStatsAPI stats, String id, int amount, String... mods) {
        for (String mod : mods) stats.getDynamic().getMod(mod).modifyFlat(id, -amount);
    }

    @Override
    public boolean affectsOPCosts() {
        return true;
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        switch (index) {
            case 0: return "" + REDUCTION_SMALL;
            case 1: return "" + REDUCTION_MEDIUM;
            case 2: return "" + REDUCTION_LARGE;
            case 3: return "" + REDUCTION_FIGHTER;
            case 4: return "" + REDUCTION_BOMBER;
            default: return null;
        }
    }

    @Override
    public Color getNameColor() {
        return OmegaArmamentSupport.OMEGA_COLOR;
    }
}
