package omegacoreeverywhere.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.AICores;
import com.fs.starfarer.api.util.Misc.Token;

/**
 * The base game's AI core turn-in, with an Omega core worth 500 times its usual reputation.
 *
 * The base game already takes Omega cores (a 1,000,000 credit bounty and 15 reputation, each times
 * the faction's own rate). Our rule only wins over the base game's when the player carries an
 * Omega core; the bounty and every other core stay as the base game has them.
 *
 * OCE_OmegaTurnIn playerHasOmegaCores
 * OCE_OmegaTurnIn selectCores
 */
public class OCE_OmegaTurnIn extends AICores {

    public static final float OMEGA_REPUTATION_MULT = 500f;

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if ("playerHasOmegaCores".equals(params.get(0).getString(memoryMap))) {
            CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
            return fleet != null && fleet.getCargo().getCommodityQuantity(Commodities.OMEGA_CORE) >= 1f;
        }
        return super.execute(ruleId, dialog, params, memoryMap);
    }

    @Override
    protected float computeCoreReputationValue(CargoAPI cargo) {
        float rep = 0f;
        for (CargoStackAPI stack : cargo.getStacksCopy()) {
            CommoditySpecAPI spec = stack.getResourceIfResource();
            if (spec == null || !spec.getDemandClass().equals(Commodities.AI_CORES)) continue;
            float each = getBaseRepValue(spec.getId());
            if (Commodities.OMEGA_CORE.equals(spec.getId())) each *= OMEGA_REPUTATION_MULT;
            rep += each * stack.getSize();
        }
        return rep * repMult;
    }
}
