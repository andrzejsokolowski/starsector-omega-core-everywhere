package omegacoreeverywhere.effects;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI.MessageClickAction;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.OrbitalStation;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.util.Misc;

import omegacoreeverywhere.Settings;

/**
 * Gives Omega cores their effects in colony buildings.
 *
 * Nothing replaces a building. Every frame this walks the player's colonies, finds the buildings
 * holding an Omega core and asserts the effect for each through the {@link Ledger}, which takes
 * the effect away again the moment it stops being asserted. Both the script and the listener are
 * transient, and {@link #suspend()} undoes everything before a save, so a save made with this mod
 * loads without it.
 *
 * Buildings are recognised by id. The two helpers that need another mod's classes,
 * {@link IndEvoEffects} and {@link VokEffects}, are only ever touched when that mod is enabled.
 */
public final class OmegaEffects implements EveryFrameScript, EconomyTickListener {

    static final String CORE = "Omega core";

    private static final String INDEVO_ID = "IndEvo";
    private static final String VOK_ID = "aotd_vok";

    private static final String SUPPLY = "oce_supply";
    private static final String DEMAND = "oce_demand";
    private static final String UPKEEP = "oce_upkeep";
    private static final String INCOME = "oce_income";

    private static final float EXOMATTER_BATCH_DAYS = 5f;
    private static final int EXOMATTER_CELLS = 4;
    private static final String TENEBRIUM = "aotd_tenebrium";
    private static final String DATABANK = "research_databank";

    private static final Logger log = Global.getLogger(OmegaEffects.class);

    private static OmegaEffects current;

    private final Ledger ledger = new Ledger();
    /** Survives {@link #suspend()}: the Supercomputer fix, which a save is safer with than without. */
    private final Ledger lasting = new Ledger();
    private final MarketImmigrationModifier growth = new Growth();
    private final boolean indEvo = Global.getSettings().getModManager().isModEnabled(INDEVO_ID);
    private final boolean vok = Global.getSettings().getModManager().isModEnabled(VOK_ID);
    /** Effects switched off for the session after they threw, so one bad effect cannot take the game down. */
    private final Set<String> broken = new HashSet<>();
    /** How many running frames each protected building has been seen disrupted. */
    private Map<Industry, Integer> disrupted = new IdentityHashMap<>();
    private final Map<Industry, Float> exomatterDays = new IdentityHashMap<>();
    private boolean suspended;

    private OmegaEffects() {}

    /** Called on every game load. The previous game's instance is simply dropped with its sector. */
    public static void install() {
        SectorAPI sector = Global.getSector();
        sector.removeTransientScriptsOfClass(OmegaEffects.class);
        sector.getListenerManager().removeListenerOfClass(OmegaEffects.class);
        sector.getListenerManager().removeListenerOfClass(OmegaTooltip.class);
        sector.getListenerManager().removeListenerOfClass(CorePickerText.class);

        current = new OmegaEffects();
        sector.addTransientScript(current);
        sector.getListenerManager().addListener(current, true);
        sector.getListenerManager().addListener(new OmegaTooltip(), true);
        sector.getListenerManager().addListener(new CorePickerText(), true);
    }

    /** Takes every effect off, so the save about to be written holds nothing of this mod. */
    public static void suspend() {
        if (current == null) return;
        current.suspended = true;
        current.ledger.releaseAll();
    }

    public static void resume() {
        if (current != null) current.suspended = false;
    }

    static boolean hasOmega(Industry ind) {
        return Commodities.OMEGA_CORE.equals(ind.getAICoreId());
    }

    @Override
    public boolean isDone() {
        return false;
    }

    /** Cores are swapped with the game paused, and a swap must not wait for time to move. */
    @Override
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {
        if (suspended || current != this) return;

        SectorAPI sector = Global.getSector();
        boolean paused = sector.isPaused();
        float days = paused ? 0f : sector.getClock().convertToDays(amount);
        boolean enabled = Settings.effectsEnabled();

        Map<Industry, Integer> stillDisrupted = new IdentityHashMap<>();
        boolean research = false;
        boolean academy = false;

        for (MarketAPI market : sector.getEconomy().getMarketsCopy()) {
            if (!market.isPlayerOwned()) continue;
            for (Industry ind : new ArrayList<>(market.getIndustries())) {
                if (!hasOmega(ind)) continue;
                Effect effect = Effect.of(ind);
                if (effect == Effect.OWN) continue;

                // The crash fix is not an effect: it stays on when the effects are switched off.
                if (effect == Effect.SUPERCOMPUTER && indEvo) {
                    guarded("SUPERCOMPUTER_FIX", () -> IndEvoEffects.keepSupercomputerRunning(lasting, ind));
                }
                if (!enabled || (ind.isBuilding() && !ind.isUpgrading())) continue;

                if (effect == Effect.STATION) protect(ind, paused, stillDisrupted);
                if (effect == Effect.SHIELD) {
                    for (Industry other : market.getIndustries()) protect(other, paused, stillDisrupted);
                }
                if (!ind.isFunctional()) continue;

                if (effect == Effect.RESEARCH) research = true;
                if (effect == Effect.ACADEMY) academy = true;
                guarded(effect.name(), () -> apply(effect, market, ind, amount, days, paused));
            }
        }
        disrupted = stillDisrupted;

        if (enabled) {
            if (research && vok) guarded("RESEARCH_SPEED", () -> VokEffects.quadrupleResearch(ledger));
            boolean teaching = academy;
            guarded("ACADEMY_TRAINING", () -> Training.tick(ledger, teaching));
        }
        ledger.sweep();
        lasting.sweep();
    }

    private void apply(Effect effect, MarketAPI market, Industry ind, float amount, float days, boolean paused) {
        String source = CORE + " (" + ind.getCurrentName() + ")";
        switch (effect) {
            case PRODUCTION:
                for (MutableCommodityQuantity supply : ind.getAllSupply()) {
                    ledger.mult(supply.getQuantity(), SUPPLY, 2f, CORE);
                }
                strain(ind);
                if (indEvo) IndEvoEffects.doubleAssemblerOutput(ledger, ind);
                break;
            case FORGE:
                strain(ind);
                if (indEvo) IndEvoEffects.doubleAssemblerOutput(ledger, ind);
                break;

            case GROWTH:
                // Re-added every frame to stay behind the buildings, which re-add themselves
                // whenever the colony is reapplied: growth is doubled after everything else.
                market.removeTransientImmigrationModifier(growth);
                market.addTransientImmigrationModifier(growth);
                ledger.hold(market, "oce_growth", () -> market.removeTransientImmigrationModifier(growth));
                break;
            case FLEET_SIZE:
                ledger.mult(dynamicMod(market, Stats.COMBAT_FLEET_SIZE_MULT), "oce_fleet_size", 2f, source);
                break;
            case GROUND_DEFENSES:
                ledger.mult(dynamicMod(market, Stats.GROUND_DEFENSES_MOD), "oce_ground_defenses", 10f, source);
                break;
            case TECH_MINING:
                ledger.mult(market.getStats().getDynamic().getStat(Stats.TECH_MINING_MULT),
                        "oce_tech_mining", 2f, source);
                break;
            case COMMERCE:
                ledger.mult(market.getIncomeMult(), "oce_commerce", 2f, source, market);
                break;
            case STATION:
                fullReadiness(((OrbitalStation) ind).getStationFleet());
                break;

            case BUREAU:
                if (indEvo) IndEvoEffects.bureau(ledger, market, ind, source);
                break;
            case SUPERCOMPUTER:
                for (MarketAPI other : Global.getSector().getEconomy().getMarkets(market.getContainingLocation())) {
                    if (!other.isPlayerOwned()) continue;
                    ledger.mult(other.getIncomeMult(), "oce_supercomputer", 1.5f, source, other);
                }
                break;
            case EMBASSY:
                if (indEvo) IndEvoEffects.embassy(ind);
                break;
            case DOCKS:
                if (indEvo) IndEvoEffects.restoreDockedShips(market);
                break;
            case ACADEMY:
                // Fleet officers and colony administrators are taught in Training.tick().
                if (indEvo) IndEvoEffects.academyStorage(ledger, ind);
                break;
            case MUSEUM:
                ledger.mult(ind.getIncome(), INCOME, 2f, CORE);
                break;
            case ARTILLERY:
                if (indEvo) IndEvoEffects.artillery(market, paused ? 0f : amount);
                for (CampaignFleetAPI fleet : market.getContainingLocation().getFleets()) {
                    if (!fleet.hasTag("IndEvo_ArtilleryFleet")) continue;
                    if (fleet.getMemoryWithoutUpdate().get("$stationMarket") == market) fullReadiness(fleet);
                }
                break;

            case RESORT:
                ledger.mult(market.getIncomeMult(), "oce_resort", 2f, source, market);
                ledger.flat(market.getHazard(), "oce_resort", -0.25f, source, market);
                break;
            case TERMINUS:
                terminus(market, source);
                break;
            case TRADE:
                ledger.mult(market.getIncomeMult(), "oce_trade", 2f, source, market);
                StatMod penalty = market.getStability().getFlatStatMod("ind_" + ind.getId());
                if (penalty != null && penalty.getValue() < 0f) {
                    ledger.flat(market.getStability(), "oce_trade", -penalty.getValue(), source, market);
                }
                break;
            case EXOMATTER:
                if (!paused) exomatter(market, ind, days);
                break;

            case PARK:
                ledger.mult(ind.getIncome(), INCOME, 2f, CORE);
                break;
            case GENELAB:
                Industry park = market.getIndustry("BOGGLED_MESOZOIC_PARK");
                if (park != null) ledger.mult(park.getIncome(), "oce_genelab", 2f, source);
                break;
            case LIMELIGHT:
                ledger.mult(ind.getIncome(), INCOME, 2f, CORE);
                limelight(market, ind, source);
                break;
            case DOMED:
                ledger.flat(market.getStability(), "oce_domed_cities", 7f, source, market);
                break;
            case DRONES:
                // TASC only runs the drones on stations, and finds Mining by its name.
                if (market.getPrimaryEntity() == null || !market.getPrimaryEntity().hasTag(Tags.STATION)) break;
                for (Industry other : market.getIndustries()) {
                    if (!"Mining".equals(other.getCurrentName())) continue;
                    for (MutableCommodityQuantity supply : other.getAllSupply()) {
                        ledger.flat(supply.getQuantity(), "oce_mining_drones", 5f, source);
                    }
                }
                break;
            case DAMPER:
                ledger.mult(dynamicMod(market, Stats.GROUND_DEFENSES_MOD), "oce_harmonic_damper", 10f, source);
                break;

            // Monthly deliveries are made in reportEconomyMonthEnd(); the rest is handled in advance().
            default:
                break;
        }
    }

    private static StatBonus dynamicMod(MarketAPI market, String stat) {
        return market.getStats().getDynamic().getMod(stat);
    }

    /** Half the demand and double the upkeep: the facility cannot keep up with the core. */
    private void strain(Industry ind) {
        for (MutableCommodityQuantity demand : ind.getAllDemand()) {
            ledger.mult(demand.getQuantity(), DEMAND, 0.5f, CORE);
        }
        // Every non-Gamma core, Omega included, already cuts upkeep under this id. The doubling
        // is measured from the uncut upkeep, so that cut is divided back out.
        StatMod cut = ind.getUpkeep().getMultStatMod("ind_core");
        float cutBy = cut != null && cut.getValue() > 0f ? cut.getValue() : 1f;
        ledger.mult(ind.getUpkeep(), UPKEEP, 2f / cutBy, CORE);
    }

    private static void fullReadiness(CampaignFleetAPI fleet) {
        if (fleet == null) return;
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if (member.getRepairTracker().getCR() < 1f) member.getRepairTracker().setCR(1f);
        }
    }

    /**
     * Ends a disruption, but only once the building has lived through it for a running frame:
     * a building that never saw itself disrupted never runs its own recovery, and a station would
     * stay a wreck. A station still in a battle is left alone until the battle is over.
     */
    private void protect(Industry ind, boolean paused, Map<Industry, Integer> stillDisrupted) {
        if (!ind.isDisrupted() || (ind.isBuilding() && !ind.isUpgrading())) return;
        int seen = disrupted.getOrDefault(ind, 0);
        if (ind instanceof OrbitalStation) {
            CampaignFleetAPI fleet = ((OrbitalStation) ind).getStationFleet();
            if (fleet != null && fleet.getBattle() != null) seen = 0;
            else if (!paused) seen++;
        } else if (!paused) {
            seen++;
        }
        if (seen < 2) {
            stillDisrupted.put(ind, seen);
            return;
        }
        ind.setDisrupted(0f);
        ledger.touch(ind.getMarket());
    }

    /** Whatever the colony's accessibility is, top it up to 1000%. */
    private void terminus(MarketAPI market, String source) {
        StatBonus access = market.getAccessibilityMod();
        StatMod own = access.getFlatBonus("oce_terminus");
        float ownNow = own == null ? 0f : own.getValue();
        float mult = access.getMult();
        if (mult <= 0f) return;
        float wanted = Math.max(0f, 10f / mult - (access.getFlatBonus() - ownNow));
        // Rounding noise must not look like a change, or the colony would be reapplied every frame.
        if (own != null && Math.abs(wanted - ownNow) < 0.005f) wanted = ownNow;
        ledger.flat(access, "oce_terminus", wanted, source, market);
    }

    /** The network takes 1 off everything the colony makes. Give it back. */
    private void limelight(MarketAPI market, Industry network, String source) {
        for (Industry other : market.getIndustries()) {
            for (CommodityOnMarketAPI commodity : market.getCommoditiesCopy()) {
                MutableStat supply = other.getSupply(commodity.getId()).getQuantity();
                StatMod taken = supply.getFlatStatMod(network.getId());
                if (taken != null && taken.getValue() < 0f) {
                    ledger.flat(supply, "oce_limelight", -taken.getValue(), source);
                }
            }
        }
    }

    /**
     * VoK counts the batch inside the building and offers no way in, so the building is pushed
     * past the end of its own countdown every five days. If a batch came out of that, the single
     * cell VoK delivered is topped up to four.
     */
    private void exomatter(MarketAPI market, Industry ind, float days) {
        float waited = exomatterDays.getOrDefault(ind, 0f) + days;
        if (waited < EXOMATTER_BATCH_DAYS) {
            exomatterDays.put(ind, waited);
            return;
        }
        exomatterDays.put(ind, 0f);

        MarketAPI gathering = Global.getSector().getPlayerFaction().getProduction().getGatheringPoint();
        if (gathering == null) gathering = market;
        CargoAPI storage = Misc.getStorageCargo(gathering);
        if (storage == null) return;

        SpecialItemData cell = new SpecialItemData(TENEBRIUM, null);
        float before = storage.getQuantity(CargoItemType.SPECIAL, cell);
        ind.advance(Global.getSector().getClock().convertToSeconds(40f));
        float made = storage.getQuantity(CargoItemType.SPECIAL, cell) - before;
        if (made > 0f && made < EXOMATTER_CELLS) storage.addSpecial(cell, EXOMATTER_CELLS - made);
    }

    @Override
    public void reportEconomyTick(int iterIndex) {
        if (suspended || current != this || !indEvo || !Settings.effectsEnabled()) return;
        // IndEvo's forges pay out from their own listener, registered after this one, and lose
        // the doubling whenever the colony is reapplied. Set it again right before they read it.
        guarded(Effect.FORGE.name(), () -> {
            for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
                if (!market.isPlayerOwned()) continue;
                for (Industry ind : market.getIndustries()) {
                    if (!hasOmega(ind) || !ind.isFunctional()) continue;
                    Effect effect = Effect.of(ind);
                    if (effect == Effect.FORGE || effect == Effect.PRODUCTION) {
                        IndEvoEffects.doubleAssemblerOutput(ledger, ind);
                    }
                }
            }
        });
    }

    @Override
    public void reportEconomyMonthEnd() {
        if (suspended || current != this) return;
        reportEconomyTick(-1);
        if (!Settings.effectsEnabled()) return;
        guarded("DELIVERIES", this::deliver);
    }

    private void deliver() {
        Map<MarketAPI, List<String>> delivered = new LinkedHashMap<>();
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (!market.isPlayerOwned()) continue;
            CargoAPI storage = Misc.getStorageCargo(market);
            if (storage == null) continue;
            List<String> goods = new ArrayList<>();
            for (Industry ind : market.getIndustries()) {
                if (!hasOmega(ind) || !ind.isFunctional()) continue;
                switch (Effect.of(ind)) {
                    case CREW:
                        add(storage, goods, Commodities.CREW, 1000);
                        break;
                    case GROUND_DEFENSES:
                        add(storage, goods, Commodities.MARINES, 500);
                        break;
                    case WAYSTATION:
                        add(storage, goods, Commodities.FUEL, 1000);
                        add(storage, goods, Commodities.SUPPLIES, 1000);
                        add(storage, goods, Commodities.HEAVY_MACHINERY, 1000);
                        add(storage, goods, Commodities.METALS, 1000);
                        add(storage, goods, Commodities.FOOD, 1000);
                        break;
                    case RESEARCH:
                        if (vok) add(storage, goods, DATABANK, 10);
                        break;
                    case KLETKA:
                        add(storage, goods, Commodities.ALPHA_CORE, 1);
                        break;
                    default:
                        break;
                }
            }
            if (!goods.isEmpty()) delivered.put(market, goods);
        }
        if (delivered.isEmpty()) return;

        MessageIntel message = new MessageIntel("Omega cores delivered goods to colony storage",
                Misc.getBasePlayerColor());
        MarketAPI first = null;
        for (Map.Entry<MarketAPI, List<String>> entry : delivered.entrySet()) {
            if (first == null) first = entry.getKey();
            String name = entry.getKey().getName();
            message.addLine(BaseIntelPlugin.BULLET + "%s: " + String.join(", ", entry.getValue()),
                    Misc.getTextColor(), new String[] {name}, Misc.getHighlightColor());
        }
        message.setIcon(Global.getSettings().getCommoditySpec(Commodities.OMEGA_CORE).getIconName());
        message.setSound(BaseIntelPlugin.getSoundStandardUpdate());
        Global.getSector().getCampaignUI().addMessage(message, MessageClickAction.COLONY_INFO, first);
    }

    private static void add(CargoAPI storage, List<String> goods, String commodity, int amount) {
        if (Global.getSector().getEconomy().getCommoditySpec(commodity) == null) return;
        storage.addCommodity(commodity, amount);
        goods.add(Misc.getWithDGS(amount) + " "
                + Global.getSector().getEconomy().getCommoditySpec(commodity).getName().toLowerCase());
    }

    /** Runs one effect. An effect that throws is logged once and left off for the session. */
    private void guarded(String effect, Runnable body) {
        if (broken.contains(effect)) return;
        try {
            body.run();
        } catch (RuntimeException | LinkageError e) {
            broken.add(effect);
            log.error("Omega effect " + effect + " failed and is switched off until the game is loaded again", e);
            Global.getSector().getCampaignUI().addMessage(
                    "Omega Core Everywhere: one Omega core effect hit an error and was switched off. "
                            + "See starsector.log.", Misc.getNegativeHighlightColor());
        }
    }

    /** Doubles a colony's growth once every other modifier has had its say. A shrinking colony is left alone. */
    private static final class Growth implements MarketImmigrationModifier {
        @Override
        public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
            if (incoming.getWeight().getModifiedValue() > 0f) {
                incoming.getWeight().modifyMult("oce_growth", 2f, CORE + " (Population & Infrastructure)");
            }
        }
    }
}
