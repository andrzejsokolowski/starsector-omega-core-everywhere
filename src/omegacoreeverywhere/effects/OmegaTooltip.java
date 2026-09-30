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
 * The core picker cannot be reached: every building writes its own "no effect" line for a core it
 * does not know, and changing that means replacing the building. The tooltip is the one place the
 * game lets another mod add to.
 */
final class OmegaTooltip extends BaseIndustryOptionProvider {

    @Override
    public void addToIndustryTooltip(Industry ind, IndustryTooltipMode mode, TooltipMakerAPI tooltip,
                                     float width, boolean expanded) {
        if (mode != IndustryTooltipMode.NORMAL || ind.getMarket() == null) return;
        if (!ind.getMarket().isPlayerOwned() || !OmegaEffects.hasOmega(ind) || !Settings.effectsEnabled()) return;
        Effect effect = Effect.of(ind);
        if (effect == Effect.OWN) return;

        tooltip.addSectionHeading("Omega core", Alignment.MID, 10f);
        tooltip.addPara(effect.text, 10f, Misc.getHighlightColor(), effect.highlights);
    }
}
