package omegacoreeverywhere.effects;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.AdminData;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.plugins.OfficerLevelupPlugin;
import com.fs.starfarer.api.util.Misc;

/**
 * What an Omega core in IndEvo's Academy teaches. The Academy's own storage is handled by
 * {@link IndEvoEffects}; everything here is plain vanilla, so it needs nothing from IndEvo.
 *
 * Levels and Hypercognition are kept for good. Omega Tutelage is this mod's own skill, and a save
 * that names a skill the game does not know will not load, so the skill itself never reaches a
 * save: the officer carries a memory flag instead, and the skill is put back from that flag.
 */
final class Training {

    static final String SKILL = "oce_omega_tutelage";
    static final String TAUGHT = "$oce_omegaTutelage";

    private Training() {}

    /** @param teaching a working Academy holds an Omega core right now */
    static void tick(Ledger ledger, boolean teaching) {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet != null) {
            OfficerLevelupPlugin levels = levels();
            for (OfficerDataAPI officer : fleet.getFleetData().getOfficersCopy()) {
                PersonAPI person = officer.getPerson();
                if (person == null || person.isAICore() || Misc.isMercenary(person)) continue;
                if (teaching) {
                    raise(officer, levels);
                    person.getMemoryWithoutUpdate().set(TAUGHT, true);
                }
                keepSkill(ledger, person);
            }
        }
        if (!teaching) return;

        for (AdminData admin : Global.getSector().getCharacterData().getAdmins()) {
            teachAdmin(admin.getPerson());
        }
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (market.isPlayerOwned()) teachAdmin(market.getAdmin());
        }
    }

    static OfficerLevelupPlugin levels() {
        return (OfficerLevelupPlugin) Global.getSettings().getPlugin("officerLevelUp");
    }

    /** Hands over the experience for the top level. The player still picks every skill. */
    private static void raise(OfficerDataAPI officer, OfficerLevelupPlugin levels) {
        MutableCharacterStatsAPI stats = officer.getPerson().getStats();
        int top = levels.getMaxLevel(officer.getPerson());
        if (stats.getLevel() >= top) return;
        long missing = levels.getXPForLevel(top) - stats.getXP();
        if (missing > 0) {
            officer.addXP(missing);
        } else if (officer.canLevelUp() && !officer.isMadePicks()) {
            // Came out of the Academy's storage with the experience already in hand.
            officer.makeSkillPicks();
        }
    }

    /** For an officer in storage, who has no level-up screen until they rejoin the fleet. */
    static void raiseStored(PersonAPI person) {
        if (person.isAICore() || Misc.isMercenary(person)) return;
        OfficerLevelupPlugin levels = levels();
        long top = levels.getXPForLevel(levels.getMaxLevel(person));
        if (person.getStats().getXP() < top) person.getStats().setXP(top);
        person.getMemoryWithoutUpdate().set(TAUGHT, true);
    }

    static void keepSkill(Ledger ledger, PersonAPI person) {
        if (!person.getMemoryWithoutUpdate().getBoolean(TAUGHT)) return;
        MutableCharacterStatsAPI stats = person.getStats();
        if (stats.getSkillLevel(SKILL) <= 0f) stats.setSkillLevel(SKILL, 1f);
        ledger.hold(person, SKILL, () -> stats.setSkillLevel(SKILL, 0f));
    }

    static void teachAdmin(PersonAPI person) {
        if (person == null || person.isPlayer() || person.isAICore()) return;
        if (person.getStats().getSkillLevel(Skills.HYPERCOGNITION) <= 0f) {
            person.getStats().setSkillLevel(Skills.HYPERCOGNITION, 1f);
        }
    }
}
