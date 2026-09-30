package omegacoreeverywhere.effects;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.econ.impl.OrbitalStation;
import com.fs.starfarer.api.impl.campaign.ids.Industries;

/**
 * What an Omega core does in a building, and the sentence the building's tooltip shows for it.
 *
 * Buildings are matched by id, so an effect for a building from another mod can only ever run when
 * that mod put the building there.
 */
enum Effect {

    /** Buildings from Andrzej's own mods that already have an Omega effect of their own. */
    OWN(null),
    NONE("No additional effect."),
    PRODUCTION("Production %s, demand %s, upkeep %s. The facility struggles to keep up with the core.",
            "×2", "×0.5", "×2"),

    GROWTH("Population growth %s.", "×2"),
    CREW("%s crew delivered to this colony's storage every month.", "1,000"),
    FLEET_SIZE("Fleet size %s.", "×2"),
    GROUND_DEFENSES("Ground defenses %s. %s marines delivered to this colony's storage every month.",
            "×10", "500"),
    TECH_MINING("Finds %s.", "+100%"),
    COMMERCE("Colony income %s.", "×2"),
    STATION("Recovers from disruption at once. Always fights at %s combat readiness.", "100%"),
    SHIELD("Every building on this colony recovers from disruption at once."),
    WAYSTATION("%s each of fuel, supplies, heavy machinery, metals and food delivered to this colony's "
            + "storage every month.", "1,000"),

    BUREAU("Counts matching buildings on your colonies within %s light-years, up to %s of each. "
            + "Production and demand both rise.", "100", "8"),
    SUPERCOMPUTER("Income %s for every colony you own in this star system. The core never burns out; "
            + "a Gamma core stands in as the running core.", "×1.5"),
    EMBASSY("Reputation with the ambassador's faction is held at the %s.", "maximum"),
    DOCKS("Every ship placed in the docks is fully restored at once, for %s.", "free"),
    ACADEMY("Officers in your fleet and in the Academy's storage reach their %s and learn %s. "
            + "Administrators on your colonies and in the Academy's storage learn %s.",
            "maximum level", "Omega Tutelage", "Hypercognition"),
    MUSEUM("Museum income %s.", "×2"),
    ARTILLERY("Fires %s as often. Its station always fights at %s combat readiness.", "twice", "100%"),
    FORGE("Monthly output %s, demand %s, upkeep %s. The facility struggles to keep up with the core.",
            "×2", "×0.5", "×2"),

    RESORT("Colony income %s. Hazard rating %s.", "×2", "-25%"),
    TERMINUS("Accessibility raised to %s.", "1,000%"),
    TRADE("No stability penalty. Colony income %s.", "×2"),
    RESEARCH("Research is %s as fast. %s research databanks delivered to this colony's storage every "
            + "month.", "4×", "10"),
    EXOMATTER("A batch takes %s and yields %s Tenebrium Cells.", "5 days", "4"),

    PARK("Park income %s.", "×2"),
    GENELAB("Mesozoic Park income %s.", "×2"),
    LIMELIGHT("Network income %s. The colony's production is no longer lowered.", "×2"),
    DOMED("Stability %s.", "+7"),
    KLETKA("One %s delivered to this colony's storage every month.", "Alpha core"),
    DRONES("Mining production %s.", "+5"),
    DAMPER("Ground defenses %s.", "×10");

    final String text;
    final String[] highlights;

    Effect(String text, String... highlights) {
        this.text = text;
        this.highlights = highlights;
    }

    private static final Map<String, Effect> BY_ID = new HashMap<>();

    private static void put(Effect effect, String... ids) {
        for (String id : ids) BY_ID.put(id, effect);
    }

    static {
        put(OWN, "oddisz_clone_vat_experiments", "oddisz_cloning", "oddisz_cloning_megafactory",
                "RePA_ReverseEngPhase1Ind", "RePA_ReverseEngPhase2Ind", "RePA_ReverseEngPhase3Ind");

        put(GROWTH, Industries.POPULATION);
        put(CREW, Industries.SPACEPORT, Industries.MEGAPORT);
        put(FLEET_SIZE, Industries.PATROLHQ, Industries.MILITARYBASE, Industries.HIGHCOMMAND);
        put(GROUND_DEFENSES, Industries.GROUNDDEFENSES, Industries.HEAVYBATTERIES);
        put(TECH_MINING, Industries.TECHMINING);
        put(COMMERCE, Industries.COMMERCE);
        put(SHIELD, Industries.PLANETARYSHIELD);
        put(WAYSTATION, Industries.WAYSTATION);
        put(NONE, Industries.LIONS_GUARD, Industries.CRYOREVIVAL);

        // Industrial Evolution
        put(BUREAU, "IndEvo_AdInfra");
        put(SUPERCOMPUTER, "IndEvo_SupCom");
        put(EMBASSY, "IndEvo_embassy");
        put(DOCKS, "IndEvo_dryDock");
        put(ACADEMY, "IndEvo_Academy");
        put(MUSEUM, "IndEvo_Museum");
        put(ARTILLERY, "IndEvo_Artillery_mortar", "IndEvo_Artillery_missile", "IndEvo_Artillery_railgun");
        put(FORGE, "IndEvo_ComForge");
        put(NONE, "IndEvo_senate", "IndEvo_EngHub", "IndEvo_PrivatePort", "IndEvo_pirateHaven",
                "IndEvo_PetStore", "IndEvo_RiftGen", "IndEvo_HullDecon", "IndEvo_HullForge",
                "IndEvo_ResLab", "IndEvo_ReqCenter", "IndEvo_SwitchablePopulation");

        // AoTD Vaults of Knowledge
        put(RESORT, "aotd_resort_center");
        put(TERMINUS, "aotd_terminus");
        put(TRADE, "aotd_trade_outpost", "aotd_underworld");
        put(RESEARCH, "aotd_research_facility", "aotd_blacksite");
        put(EXOMATTER, "aotd_exomatter_processing");
        put(NONE, "aotd_daedalus_array", "aotd_coronal_control", "aotd_persean_university");

        // Terraforming & Station Construction
        put(PARK, "BOGGLED_MESOZOIC_PARK");
        put(GENELAB, "BOGGLED_GENELAB");
        put(LIMELIGHT, "BOGGLED_LIMELIGHT_NETWORK");
        put(DOMED, "BOGGLED_DOMED_CITIES");
        put(KLETKA, "BOGGLED_KLETKA_SIMULATOR");
        put(DRONES, "BOGGLED_AI_MINING_DRONES");
        put(DAMPER, "BOGGLED_HARMONIC_DAMPER");
        put(NONE, "BOGGLED_CHAMELEON");
    }

    static Effect of(Industry ind) {
        Effect effect = BY_ID.get(ind.getId());
        if (effect != null) return effect;
        // Covers the vanilla stations, VoK's Star Citadels and TASC's Autonomous AI Battlestation.
        if (ind instanceof OrbitalStation) return STATION;
        // A building nobody listed: if it makes commodities it is a production building.
        return ind.getAllSupply().isEmpty() ? NONE : PRODUCTION;
    }
}
