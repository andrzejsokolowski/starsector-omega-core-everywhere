package omegacoreeverywhere.skills;

import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import omegacoreeverywhere.Settings;

/** The perk of an Omega core captaining one of the player's ships. The amount is a LunaLib setting. */
public class OmegaTimeflow {

    public static class Level1 implements ShipSkillEffect {

        @Override
        public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
            stats.getTimeMult().modifyMult(id, 1f + Settings.captainTimeflow() / 100f);
        }

        @Override
        public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
            stats.getTimeMult().unmodify(id);
        }

        @Override
        public String getEffectDescription(float level) {
            return "Time passes " + Math.round(Settings.captainTimeflow()) + "% faster for the ship";
        }

        @Override
        public String getEffectPerLevelDescription() {
            return null;
        }

        @Override
        public ScopeDescription getScopeDescription() {
            return ScopeDescription.PILOTED_SHIP;
        }
    }
}
