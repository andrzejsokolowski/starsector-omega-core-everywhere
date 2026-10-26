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

/**
 * Exotica Technologies' Subsumed Alpha Core, with an Omega core: an automated ship takes the core in
 * for extra bandwidth, outside the exotic limit. Installed only by the player, with an Omega core.
 * Computer fleets never get it, and it never drops.
 */
public class SubsumedOmegaCore extends Exotic {

    public static final float BANDWIDTH = 150f;

    public SubsumedOmegaCore(String key, JSONObject settings) {
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
    public boolean canApply(FleetMemberAPI member, ShipModifications mods) {
        if (member.getFleetData() == null || member.getFleetData().getFleet() == null) return false;
        return member.getOwner() == 0 && super.canApply(member, mods);
    }

    @Override
    public boolean countsTowardsExoticLimit(FleetMemberAPI member) {
        return false;
    }

    @Override
    public float getExtraBandwidth(FleetMemberAPI member, ShipModifications mods, ExoticData exoticData) {
        return BANDWIDTH * getPositiveMult(member, mods, exoticData);
    }

    @Override
    public void modifyToolTip(TooltipMakerAPI tooltip, UIComponentAPI title, FleetMemberAPI member,
                              ShipModifications mods, ExoticData exoticData, boolean expand) {
        if (!expand) return;
        OmegaExotics.addPara(tooltip, title, "Increases bandwidth by %s. %s.", Misc.getHighlightColor(),
                Math.round(getExtraBandwidth(member, mods, exoticData)) + " TB/s",
                "Does not count towards exotic limit");
    }
}
