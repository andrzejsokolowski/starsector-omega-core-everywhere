package omegacoreeverywhere.exotica;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
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
 *
 * Players run Exotica 2.0.2 to 2.1.1, and the helpers differ between them (2.0.x has no
 * hasExoticChipOrItemInCargo), so cargo is read with the base game's own calls. The few Exotica
 * calls left exist in every one of those versions, and each still fails safe: on any error it logs
 * once and answers as if the Omega core or exotic were not there, instead of taking the game down.
 */
public final class OmegaExotics {

    public static final String EXOTICA_ID = "exoticatechnologies";
    public static final String OMEGA_SUBCORE = "OmegaSubcore";
    /** Exotica's exotic chip: special item data is the exotic's key, or "key,type". */
    private static final String CHIP_ITEM = "et_exotic";

    static final Color COLOR = OmegaArmamentSupport.OMEGA_COLOR;

    private static final Logger log = Global.getLogger(OmegaExotics.class);
    private static boolean reported;

    private OmegaExotics() {}

    private static void report(Throwable e) {
        if (reported) return;
        reported = true;
        log.error("Omega Core Everywhere: an Exotica Technologies call failed; the Omega exotics act as if absent", e);
    }

    private static CargoAPI storage(MarketAPI market) {
        return market == null ? null : Misc.getStorageCargo(market);
    }

    private static boolean hasCore(CargoAPI cargo) {
        return cargo != null && cargo.getCommodityQuantity(Commodities.OMEGA_CORE) >= 1f;
    }

    private static boolean hasChip(CargoAPI cargo, String exoticKey) {
        if (cargo == null) return false;
        try {
            // Also looks inside Exotica's crates.
            return Utilities.hasExoticChip(cargo, exoticKey);
        } catch (RuntimeException | LinkageError e) {
            report(e);
        }
        for (CargoStackAPI stack : cargo.getStacksCopy()) {
            SpecialItemData data = stack.getSpecialDataIfSpecial();
            if (data == null || !CHIP_ITEM.equals(data.getId()) || data.getData() == null) continue;
            if (data.getData().split(",")[0].equals(exoticKey)) return true;
        }
        return false;
    }

    /** The exotic shows in Exotica's list once an Omega core or its chip is in the fleet or the colony's storage. */
    static boolean hasCoreOrChip(String exoticKey, MarketAPI market) {
        try {
            CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
            CargoAPI own = fleet == null ? null : fleet.getCargo();
            CargoAPI stored = storage(market);
            return hasCore(own) || hasCore(stored) || hasChip(own, exoticKey) || hasChip(stored, exoticKey);
        } catch (RuntimeException e) {
            report(e);
            return false;
        }
    }

    static boolean canAfford(CampaignFleetAPI fleet, MarketAPI market) {
        return hasCore(fleet == null ? null : fleet.getCargo()) || hasCore(storage(market));
    }

    /** Takes the core from the fleet's cargo first, then from the colony's storage. */
    static boolean takeCore(CampaignFleetAPI fleet, MarketAPI market) {
        CargoAPI own = fleet == null ? null : fleet.getCargo();
        CargoAPI from = hasCore(own) ? own : storage(market);
        if (!hasCore(from)) return false;
        from.removeCommodity(Commodities.OMEGA_CORE, 1f);
        return true;
    }

    static Map<String, Float> oneCore() {
        Map<String, Float> cost = new HashMap<>();
        cost.put(Commodities.OMEGA_CORE, 1f);
        return cost;
    }

    /** A paragraph laid out under the exotic's title in Exotica's panel, as Exotica's own texts are. */
    static void addPara(TooltipMakerAPI tooltip, UIComponentAPI title, String format, Color color, String... highlights) {
        tooltip.addPara(format, 0f, color, highlights);
        if (title != null) tooltip.getPrev().getPosition().belowLeft(title, 2f);
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
        ShipVariantAPI refit = member.getVariant();
        try {
            refit = RefitButtonAdderKt.checkRefitVariant(member);
        } catch (RuntimeException | LinkageError e) {
            report(e);
        }
        if (refit == null || refit == member.getVariant()) return new ShipVariantAPI[] {member.getVariant()};
        return new ShipVariantAPI[] {member.getVariant(), refit};
    }

    /**
     * For the cleanup of a carrier left behind when the exotic is gone. When Exotica cannot be asked,
     * the answer is yes, so a working Omega Subcore is never stripped.
     */
    public static boolean hasOmegaSubcore(FleetMemberAPI member) {
        try {
            ShipModifications mods = ShipModLoader.get(member, member.getVariant());
            return mods != null && mods.getExotics().hasExotic(OMEGA_SUBCORE);
        } catch (RuntimeException | LinkageError e) {
            report(e);
            return true;
        }
    }
}
