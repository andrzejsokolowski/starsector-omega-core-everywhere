package omegacoreeverywhere.effects;

import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.MutableStat.StatMod;

import data.kaysaar.aotd.vok.scripts.research.AoTDFactionResearchManager;
import data.kaysaar.aotd.vok.scripts.research.AoTDMainResearchManager;

/**
 * The one Omega effect that has to reach into AoTD Vaults of Knowledge's own classes.
 *
 * Nothing outside {@link OmegaEffects} may call this class, and OmegaEffects only does so when
 * VoK is enabled: without VoK these imports do not exist and loading this class fails.
 */
final class VokEffects {

    private static final String SPEED = "oce_research_speed";

    private VokEffects() {}

    /**
     * VoK divides research time by (1 + bonus). To make research exactly four times as fast as
     * it would otherwise be, the bonus has to grow with everything else that adds to it, so it is
     * worked out again every frame from what the others currently give.
     */
    static void quadrupleResearch(Ledger ledger) {
        AoTDMainResearchManager research = AoTDMainResearchManager.getInstance();
        if (research == null) return;
        AoTDFactionResearchManager player = research.getManagerForPlayer();
        if (player == null) return;

        MutableStat bonus = player.getResearchSpeedBonus();
        StatMod own = bonus.getFlatStatMod(SPEED);
        float ownNow = own == null ? 0f : own.getValue();
        float others = bonus.getModifiedValue() - ownNow;
        float wanted = 3f * (1f + others);
        if (own != null && Math.abs(wanted - ownNow) < 0.001f) wanted = ownNow;
        ledger.flat(bonus, SPEED, wanted, OmegaEffects.CORE + " (Research Facility)");
    }
}
