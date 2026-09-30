package omegacoreeverywhere.effects;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;

import indevo.ids.Ids;
import indevo.industries.RestorationDocks;
import indevo.industries.Supercomputer;
import indevo.industries.academy.industry.Academy;
import indevo.industries.artillery.scripts.CampaignAttackScript;
import indevo.industries.assembler.industry.VariableAssembler;
import indevo.industries.embassy.industry.Embassy;
import indevo.utils.helper.MiscIE;

/**
 * The Omega effects that have to reach into Industrial Evolution's own classes.
 *
 * Nothing outside {@link OmegaEffects} may call this class, and OmegaEffects only does so when
 * IndEvo is enabled: without IndEvo these imports do not exist and loading this class fails.
 */
final class IndEvoEffects {

    /** Long enough that the running core never reaches the end of its life. */
    private static final float ENDLESS_RUNTIME = 10000f;
    private static final int BUREAU_CAP = 8;
    private static final float BUREAU_REACH_LY = 100f;
    private static final String DOCK_STORAGE = "IndEvo_RepairStorage";
    private static final String VARIABLE_MANUFACTORY = "IndEvo_AdManuf";

    private IndEvoEffects() {}

    /**
     * The Supercomputer burns its core: it moves the core out of the socket into a private
     * "running" slot and counts that core's days down from a table that only knows Alpha, Beta and
     * Gamma. An Omega core reaching the running slot crashes the game the next day.
     *
     * So the Omega never gets there. While it sits in the socket the running slot is kept busy,
     * by a Gamma core conjured for the purpose if it is empty, and the countdown is stretched so
     * that the running core outlives any campaign.
     *
     * The stretch is deliberately left in place across saves. A save made without it, loaded
     * with the day about to turn, would burn the running core out and put the Omega in its place.
     */
    static void keepSupercomputerRunning(Ledger ledger, Industry ind) {
        if (!(ind instanceof Supercomputer)) return;
        Supercomputer computer = (Supercomputer) ind;
        computer.setRuntimeMult(ENDLESS_RUNTIME);
        ledger.hold(computer, "oce_runtime", computer::resetRuntimeMult);
        if (computer.getLastApplied() == null) {
            computer.setAICoreId(Commodities.GAMMA_CORE);
            computer.onNewDay();
            computer.setAICoreId(Commodities.OMEGA_CORE);
        }
    }

    /** Variable Assembler, Variable Manufactory and Commodity Forge pay a monthly batch into storage. */
    static void doubleAssemblerOutput(Ledger ledger, Industry ind) {
        if (!(ind instanceof VariableAssembler)) return;
        VariableAssembler assembler = (VariableAssembler) ind;
        // IndEvo resets this to 1 every time the colony is reapplied.
        assembler.aiCoreMult = 2f;
        ledger.hold(assembler, "oce_batch", () -> {
            if (assembler.aiCoreMult == 2f) assembler.aiCoreMult = 1f;
        });
    }

    /**
     * IndEvo's own count stops at 3 and at the star system, and gives an unknown core nothing.
     * This is the same count with a cap of 8 and a reach of 100 light-years.
     */
    static void bureau(Ledger ledger, MarketAPI market, Industry bureau, String source) {
        if (bureau.getSpecialItem() != null) return;
        Set<String> counted = MiscIE.getCSVSetFromMemory(Ids.BUREAU_LIST);

        List<MarketAPI> others = new ArrayList<>();
        for (MarketAPI other : Misc.getFactionMarkets(market.getFaction())) {
            if (other == market) continue;
            float distance = Misc.getDistanceLY(other.getLocationInHyperspace(), market.getLocationInHyperspace());
            if (distance <= BUREAU_REACH_LY) others.add(other);
        }

        for (Industry local : market.getIndustries()) {
            if (!counted.contains(local.getId())) continue;
            int copies = 0;
            for (MarketAPI other : others) {
                if (hasCopy(other, local)) copies++;
            }
            int bonus = Math.min(copies, BUREAU_CAP);
            if (bonus <= 0) continue;
            for (MutableCommodityQuantity supply : local.getAllSupply()) {
                ledger.flat(supply.getQuantity(), "oce_bureau", bonus, source);
            }
            for (MutableCommodityQuantity demand : local.getAllDemand()) {
                ledger.flat(demand.getQuantity(), "oce_bureau", bonus, source);
            }
        }
    }

    private static boolean hasCopy(MarketAPI other, Industry local) {
        String id = local.getId();
        if (Industries.HEAVYINDUSTRY.equals(id) || Industries.ORBITALWORKS.equals(id)) {
            return other.hasIndustry(Industries.HEAVYINDUSTRY) || other.hasIndustry(Industries.ORBITALWORKS);
        }
        Industry copy = other.getIndustry(id);
        if (copy == null) return false;
        if (VARIABLE_MANUFACTORY.equals(id)) {
            // Two manufactories only count as copies when they run the same chip.
            SpecialItemData mine = local.getSpecialItem();
            SpecialItemData theirs = copy.getSpecialItem();
            return mine != null && theirs != null && mine.getId().equals(theirs.getId());
        }
        return true;
    }

    static void embassy(Industry ind) {
        if (!(ind instanceof Embassy)) return;
        FactionAPI faction = ((Embassy) ind).alignedFaction;
        if (faction == null || faction.isPlayerFaction()) return;
        FactionAPI player = Global.getSector().getPlayerFaction();
        if (player.getRelationship(faction.getId()) < 1f) player.setRelationship(faction.getId(), 1f);
    }

    /** Strips every d-mod from the ships in the docks, the way the docks do it themselves, at no charge. */
    static void restoreDockedShips(MarketAPI market) {
        SubmarketAPI docks = market.getSubmarket(DOCK_STORAGE);
        if (docks == null || docks.getCargoNullOk() == null) return;
        FleetDataAPI ships = docks.getCargoNullOk().getMothballedShips();
        if (ships == null) return;

        int restored = 0;
        for (FleetMemberAPI ship : ships.getMembersListCopy()) {
            List<HullModSpecAPI> dmods = RestorationDocks.getListNonBuiltInDMods(ship.getVariant());
            if (dmods.isEmpty()) continue;
            ShipVariantAPI variant = ship.getVariant();
            if (variant.isStockVariant()) {
                // A stock variant is shared by every ship built from it.
                variant = variant.clone();
                variant.setSource(VariantSource.REFIT);
                ship.setVariant(variant, false, false);
            }
            for (HullModSpecAPI dmod : dmods) {
                variant.removePermaMod(dmod.getId());
                variant.removeMod(dmod.getId());
            }
            ShipHullSpecAPI pristine = RestorationDocks.getBaseShipHullSpec(variant, false);
            if (pristine != null) variant.setHullSpecAPI(pristine);
            // Only a ship that really came out clean is announced; anything else is simply
            // tried again, quietly, instead of repeating the message every frame.
            if (RestorationDocks.getListNonBuiltInDMods(variant).isEmpty()) restored++;
        }
        if (restored > 0) {
            Global.getSector().getCampaignUI().addMessage(
                    "The Omega core at the " + market.getName() + " Restoration Docks restored "
                            + restored + (restored == 1 ? " ship." : " ships."),
                    Misc.getPositiveHighlightColor());
        }
    }

    /** Officers and administrators sitting in the Academy's own storage. */
    static void academyStorage(Ledger ledger, Industry ind) {
        if (!(ind instanceof Academy)) return;
        Academy academy = (Academy) ind;
        for (PersonAPI officer : academy.getOfficerStorage()) {
            Training.raiseStored(officer);
            Training.keepSkill(ledger, officer);
        }
        for (PersonAPI admin : academy.getAdminStorage()) {
            Training.teachAdmin(admin);
        }
    }

    /**
     * The artillery's firing script counts its own delays down once a frame. Running it a second
     * time each frame makes every delay pass twice as fast, for this one artillery only: the
     * delays themselves are a single IndEvo setting shared by every artillery in the Sector.
     */
    static void artillery(MarketAPI market, float amount) {
        if (amount <= 0f) return;
        for (SectorEntityToken entity : market.getConnectedEntities()) {
            if (!entity.hasTag(Ids.TAG_ARTILLERY_STATION) || entity.getScripts() == null) continue;
            for (EveryFrameScript script : new ArrayList<>(entity.getScripts())) {
                if (script instanceof CampaignAttackScript) script.advance(amount);
            }
        }
    }
}
