package omegacoreeverywhere.exotica;

import java.awt.Color;
import java.util.Map;

import org.json.JSONObject;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;

import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.exotics.Exotic;
import exoticatechnologies.modifications.exotics.ExoticData;
import exoticatechnologies.util.StringUtils;
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
        OmegaExotics.takeCore(fleet, market);
        return true;
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
        StringUtils.getTranslation(getKey(), "longDescription")
                .format("bandwidthIncrease", getExtraBandwidth(member, mods, exoticData))
                .format("smallReduction", OmegaSubcoreHullmod.REDUCTION_SMALL)
                .format("medReduction", OmegaSubcoreHullmod.REDUCTION_MEDIUM)
                .format("largeReduction", OmegaSubcoreHullmod.REDUCTION_LARGE)
                .format("fghtrReduction", OmegaSubcoreHullmod.REDUCTION_FIGHTER)
                .format("bmberReduction", OmegaSubcoreHullmod.REDUCTION_BOMBER)
                .addToTooltip(tooltip, title);
        if (OmegaSubcoreHullmod.hasConflict(member.getVariant())) {
            StringUtils.getTranslation(getKey(), "conflictDetected").addToTooltip(tooltip);
        }
    }
}
