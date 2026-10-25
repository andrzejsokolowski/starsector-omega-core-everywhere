package omegacoreeverywhere.effects;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;

import omegacoreeverywhere.Settings;

/**
 * Gives the Omega Timeflow skill to every Omega core captaining a ship in the player's fleet.
 *
 * The base game already lets an Omega core captain an automated ship. Enemy Omega ships never get
 * the skill: only the player's fleet is looked at. Like Omega Tutelage, the skill is this mod's own
 * and must not reach a save, so it is held in the {@link Ledger} and comes off before every save.
 */
final class Captains {

    static final String SKILL = "oce_omega_timeflow";

    private Captains() {}

    static void tick(Ledger ledger) {
        if (Settings.captainTimeflow() <= 0f) return;
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null) return;
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            PersonAPI captain = member.getCaptain();
            if (captain == null || !captain.isAICore()) continue;
            if (!Commodities.OMEGA_CORE.equals(captain.getAICoreId())) continue;
            MutableCharacterStatsAPI stats = captain.getStats();
            if (stats.getSkillLevel(SKILL) <= 0f) stats.setSkillLevel(SKILL, 1f);
            ledger.hold(captain, SKILL, () -> stats.setSkillLevel(SKILL, 0f));
        }
    }
}
