package omegacoreeverywhere.ships;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.EngagementResultForFleetAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

/**
 * A ship disabled or destroyed with Armament Support System (Omega) loses the Omega core in it, as
 * Special Hullmod Upgrades does with its own cores, unless that mod's "keep the items" setting is on.
 */
public final class OmegaCoreLoss extends BaseCampaignEventListener {

    private OmegaCoreLoss() {
        super(false);
    }

    /** Called on every game load. Transient, so it leaves with the loaded game. */
    public static void install() {
        Global.getSector().addTransientListener(new OmegaCoreLoss());
    }

    @Override
    public void reportPlayerEngagement(EngagementResultAPI result) {
        if (OmegaArmamentSupport.coreSurvivesLoss()) return;
        EngagementResultForFleetAPI player = result.didPlayerWin() ? result.getWinnerResult() : result.getLoserResult();
        if (player == null) return;
        for (FleetMemberAPI member : player.getDestroyed()) loseCore(member);
        for (FleetMemberAPI member : player.getDisabled()) loseCore(member);
    }

    private static void loseCore(FleetMemberAPI member) {
        if (member == null) return;
        ShipVariantAPI variant = member.getVariant();
        if (variant != null && variant.getNonBuiltInHullmods().contains(OmegaArmamentSupport.ID)) {
            variant.removeMod(OmegaArmamentSupport.ID);
        }
    }
}
