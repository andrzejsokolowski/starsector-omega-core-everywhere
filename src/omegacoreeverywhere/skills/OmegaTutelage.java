package omegacoreeverywhere.skills;

import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

/** The skill an officer picks up from an Academy run by an Omega core. */
public class OmegaTutelage {

    public static final float TIMEFLOW_BONUS = 20f;

    public static class Level1 implements ShipSkillEffect {

        @Override
        public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
            stats.getTimeMult().modifyMult(id, 1f + TIMEFLOW_BONUS / 100f);
        }

        @Override
        public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
            stats.getTimeMult().unmodify(id);
        }

        @Override
        public String getEffectDescription(float level) {
            return "Time passes " + (int) TIMEFLOW_BONUS + "% faster for the piloted ship";
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
