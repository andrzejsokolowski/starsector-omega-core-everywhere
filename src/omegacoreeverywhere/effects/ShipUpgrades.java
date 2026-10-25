package omegacoreeverywhere.effects;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CharacterDataAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import omegacoreeverywhere.exotica.OmegaExotics;
import omegacoreeverywhere.ships.OmegaArmamentSupport;
import omegacoreeverywhere.ships.OmegaSubcoreHullmod;

/**
 * Upkeep for the ship upgrades that come with Special Hullmod Upgrades and Exotica Technologies.
 *
 * Armament Support System (Omega) is known while its Alpha version is. The knowledge is held in the
 * {@link Ledger}, so a save never names the hull mod among the known ones. The Omega Subcore's hidden
 * carrier is taken off any ship whose exotic is gone (or whose save is played without Exotica).
 * {@link OmegaExotics} is only called while Exotica is enabled.
 */
final class ShipUpgrades {

    private static final int CLEANUP_FRAMES = 60;

    private final boolean shu = Global.getSettings().getModManager().isModEnabled(OmegaArmamentSupport.SHU_MOD_ID);
    private final boolean exotica = Global.getSettings().getModManager().isModEnabled(OmegaExotics.EXOTICA_ID);
    private int frames;

    void tick(Ledger ledger) {
        if (shu) learnWithAlpha(ledger);
        if (++frames < CLEANUP_FRAMES) return;
        frames = 0;
        dropOrphanCarriers();
    }

    private static void learnWithAlpha(Ledger ledger) {
        CharacterDataAPI data = Global.getSector().getCharacterData();
        if (!data.knowsHullMod(OmegaArmamentSupport.SHU_ALPHA)) return;
        if (!data.knowsHullMod(OmegaArmamentSupport.ID)) data.addHullMod(OmegaArmamentSupport.ID);
        ledger.hold(data, OmegaArmamentSupport.ID, () -> data.removeHullMod(OmegaArmamentSupport.ID));
    }

    private void dropOrphanCarriers() {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null) return;
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            ShipVariantAPI variant = member.getVariant();
            if (variant == null || !variant.getPermaMods().contains(OmegaSubcoreHullmod.ID)) continue;
            if (exotica && OmegaExotics.hasOmegaSubcore(member)) continue;
            variant.removePermaMod(OmegaSubcoreHullmod.ID);
        }
    }
}
