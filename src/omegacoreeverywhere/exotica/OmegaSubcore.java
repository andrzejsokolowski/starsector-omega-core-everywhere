package omegacoreeverywhere.exotica;

import java.awt.Color;
import java.util.Map;

import org.json.JSONObject;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Misc;

import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.exotics.Exotic;
import exoticatechnologies.modifications.exotics.ExoticData;
import omegacoreeverywhere.ships.OmegaSubcoreHullmod;

/**
 * Exotica Technologies' Alpha Subcore, with an Omega core: extra bandwidth and much cheaper weapons
 * and fighters on any ship. The discounts live in the hidden {@link OmegaSubcoreHullmod}, which this
 * exotic puts on the ship and takes off again. Installed only by the player, with an Omega core.
 * Computer fleets never get it, and it never drops.
 */
public class OmegaSubcore extends Exotic {

    public static final float BANDWIDTH = 150f;

    public OmegaSubcore(String key, JSONObject settings) {
        super(key, settings);
    }

    @Override
    public Color getColor() {
        return OmegaExotics.COLOR;
    }

    @Override
    public boolean getCanDropFromCombat() {
        return false;
    }

    @Override
    public float getSalvageChance(float chanceMult) {
        return 0f;
    }

    @Override
    public float getGenerationChanceMult(FleetMemberAPI member) {
        return 0f;
    }

    @Override
    public boolean shouldShow(FleetMemberAPI member, ShipModifications mods, MarketAPI market) {
        return OmegaExotics.hasCoreOrChip(getKey(), market);
    }

    @Override
    public boolean canAfford(CampaignFleetAPI fleet, MarketAPI market) {
        return OmegaExotics.canAfford(fleet, market);
    }

    @Override
    public boolean removeItemsFromFleet(CampaignFleetAPI fleet, FleetMemberAPI member, MarketAPI market) {
        return OmegaExotics.takeCore(fleet, market);
    }

    @Override
    public Map<String, Float> getResourceCostMap(FleetMemberAPI fm, ShipModifications mods, MarketAPI market) {
        return OmegaExotics.oneCore();
    }

    @Override
    public void onInstall(FleetMemberAPI member) {
        OmegaExotics.addCarrier(member);
    }

    @Override
    public void onDestroy(FleetMemberAPI member) {
        OmegaExotics.removeCarrier(member);
    }

    /** Puts the carrier back if anything took it off while the exotic is still installed. */
    @Override
    public void advanceInCampaign(FleetMemberAPI member, ShipModifications mods, float amount, ExoticData exoticData) {
        if (!member.getVariant().hasHullMod(OmegaSubcoreHullmod.ID)) OmegaExotics.addCarrier(member);
    }

    @Override
    public float getExtraBandwidth(FleetMemberAPI member, ShipModifications mods, ExoticData exoticData) {
        return BANDWIDTH * getPositiveMult(member, mods, exoticData);
    }

    @Override
    public void modifyToolTip(TooltipMakerAPI tooltip, UIComponentAPI title, FleetMemberAPI member,
                              ShipModifications mods, ExoticData exoticData, boolean expand) {
        if (!expand) return;
        OmegaExotics.addPara(tooltip, title,
                "Increases bandwidth by %s. Reduces ordnance point costs for weapons by %s/%s/%s based on weapon"
                        + " size, and for fighters and bombers by %s/%s. Installable only on the main module.",
                Misc.getHighlightColor(),
                Math.round(getExtraBandwidth(member, mods, exoticData)) + " TB/s",
                "" + OmegaSubcoreHullmod.REDUCTION_SMALL, "" + OmegaSubcoreHullmod.REDUCTION_MEDIUM,
                "" + OmegaSubcoreHullmod.REDUCTION_LARGE, "" + OmegaSubcoreHullmod.REDUCTION_FIGHTER,
                "" + OmegaSubcoreHullmod.REDUCTION_BOMBER);
        if (OmegaSubcoreHullmod.hasConflict(member.getVariant())) {
            tooltip.addPara("The subcore is receiving interference from something else installed on the ship,"
                    + " and can't reduce ordnance point costs.", Misc.getNegativeHighlightColor(), 3f);
        }
    }
}
