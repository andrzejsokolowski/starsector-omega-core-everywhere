package omegacoreeverywhere.ships;

import java.awt.Color;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import omegacoreeverywhere.Safe;

/**
 * Armament Support System (Omega) on a section other than the main one, put there by the main
 * section's hull mod (as Special Hullmod Upgrades' module extensions are): the same weapon bonuses,
 * no Omega core of its own, no deployment cost. Hidden.
 */
public class OmegaArmamentSupportModule extends BaseHullMod {

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        Safe.run("Armament Support System (Omega) on a section", () -> {
            // A section that carries the full hull mod itself already has these bonuses.
            if (stats.getVariant() != null && stats.getVariant().hasHullMod(OmegaArmamentSupport.ID)) return;
            OmegaArmamentSupport.applyWeaponStats(stats, id);
        });
    }

    @Override
    public boolean affectsOPCosts() {
        return true;
    }

    @Override
    public Color getNameColor() {
        return OmegaArmamentSupport.OMEGA_COLOR;
    }
}
