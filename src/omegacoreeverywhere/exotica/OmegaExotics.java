package omegacoreeverywhere.exotica;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.util.Misc;

import exoticatechnologies.modifications.ShipModLoader;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.refit.RefitButtonAdderKt;
import exoticatechnologies.util.Utilities;
import omegacoreeverywhere.ships.OmegaArmamentSupport;
import omegacoreeverywhere.ships.OmegaSubcoreHullmod;

/**
 * What the two Omega exotics share. This package is the only code that names Exotica Technologies
 * classes: Exotica itself loads the exotics from data/config/exotics.json, and nothing else may call
 * in here unless Exotica is enabled.
 */
public final class OmegaExotics {

    public static final String EXOTICA_ID = "exoticatechnologies";
    public static final String OMEGA_SUBCORE = "OmegaSubcore";

    static final Color COLOR = OmegaArmamentSupport.OMEGA_COLOR;

    private OmegaExotics() {}

    static boolean hasCoreOrChip(String exoticKey, MarketAPI market) {
        return Utilities.hasExoticChipOrItemInCargo(exoticKey, Commodities.OMEGA_CORE, market);
    }

    static boolean canAfford(CampaignFleetAPI fleet, MarketAPI market) {
        return Utilities.hasItemInFleetCargoOrMarketStorageCargo(fleet, market, Commodities.OMEGA_CORE);
    }

    /** Takes the core from the fleet's cargo first, then from the colony's storage. */
    static void takeCore(CampaignFleetAPI fleet, MarketAPI market) {
        if (Utilities.hasItem(fleet.getCargo(), Commodities.OMEGA_CORE)) {
            Utilities.takeItemQuantity(fleet.getCargo(), Commodities.OMEGA_CORE, 1f);
        } else {
            Utilities.takeItemQuantity(Misc.getStorageCargo(market), Commodities.OMEGA_CORE, 1f);
        }
    }

    static Map<String, Float> oneCore() {
        Map<String, Float> cost = new HashMap<>();
        cost.put(Commodities.OMEGA_CORE, 1f);
        return cost;
    }

    /** The hidden hull mod that carries the Omega Subcore's ordnance discounts, on the ship and its refit copy. */
    static void addCarrier(FleetMemberAPI member) {
        for (ShipVariantAPI variant : variants(member)) {
            if (!variant.hasHullMod(OmegaSubcoreHullmod.ID)) variant.addPermaMod(OmegaSubcoreHullmod.ID);
        }
    }

    static void removeCarrier(FleetMemberAPI member) {
        for (ShipVariantAPI variant : variants(member)) variant.removePermaMod(OmegaSubcoreHullmod.ID);
    }

    private static ShipVariantAPI[] variants(FleetMemberAPI member) {
        ShipVariantAPI refit = RefitButtonAdderKt.checkRefitVariant(member);
        if (refit == member.getVariant()) return new ShipVariantAPI[] {member.getVariant()};
        return new ShipVariantAPI[] {member.getVariant(), refit};
    }

    /** For the cleanup of a carrier left behind when the exotic is gone. */
    public static boolean hasOmegaSubcore(FleetMemberAPI member) {
        ShipModifications mods = ShipModLoader.get(member, member.getVariant());
        return mods != null && mods.getExotics().hasExotic(OMEGA_SUBCORE);
    }
}
