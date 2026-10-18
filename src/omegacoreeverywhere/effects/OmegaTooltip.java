package omegacoreeverywhere.effects;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.Industry.IndustryTooltipMode;
import com.fs.starfarer.api.campaign.listeners.BaseIndustryOptionProvider;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import omegacoreeverywhere.Settings;

/**
 * Says in a building's tooltip what its Omega core does.
 *
 * The game writes "Omega Core. No effect." in the tooltip's Items section before any mod is asked.
 * That line is rewritten to name the effect ({@link NoEffectText}). Only when the line cannot hold
 * the whole effect, or cannot be found, does the tooltip get an "Omega core" section of its own.
 */
final class OmegaTooltip extends BaseIndustryOptionProvider {

    @Override
    public void addToIndustryTooltip(Industry ind, IndustryTooltipMode mode, TooltipMakerAPI tooltip,
                                     float width, boolean expanded) {
        if (mode != IndustryTooltipMode.NORMAL || ind.getMarket() == null) return;
        if (!ind.getMarket().isPlayerOwned() || !OmegaEffects.hasOmega(ind) || !Settings.effectsEnabled()) return;
        Effect effect = Effect.of(ind);
        // The game's "No effect" is the truth for these.
        if (effect == Effect.OWN || effect == Effect.NONE) return;
        if (NoEffectText.rewriteTooltip(tooltip, ind) == NoEffectText.Fit.FULL) return;

        tooltip.addSectionHeading("Omega core", Alignment.MID, 10f);
        tooltip.addPara(effect.text, 10f, Misc.getHighlightColor(), effect.highlights);
    }
}
