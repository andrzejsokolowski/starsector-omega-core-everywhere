package omegacoreeverywhere.ships;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;

import omegacoreeverywhere.Safe;

/**
 * Sits on the main section while the other sections carry Armament Support System (Omega)'s hidden
 * copy. Once the hull mod itself is gone, it takes the copies off and then itself (as Special Hullmod
 * Upgrades' utility scripts do). Hidden.
 */
public class OmegaArmamentSupportLink extends BaseHullMod {

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        Safe.run("Armament Support System (Omega) on a section",
                () -> OmegaArmamentSupport.syncSections(ship.getVariant()));
    }
}
