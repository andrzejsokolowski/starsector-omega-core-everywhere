# Omega core effects: industry worksheet

Every colony building that can hold an AI core, grouped by where it comes from, with what it does
and what an Alpha core adds there today. Write the Omega effect you want after **Omega:**, or
answer in chat.

How AI cores work now, for comparison:

- **Alpha core, everywhere:** upkeep −25%, demand −1. Production buildings (and Population,
  Lion's Guard and Cryosanctum) also get +1 to everything they make. The "Alpha today" lines below
  list only what Alpha adds on top of that.
- **Omega core in vanilla, today:** it can already be installed in any building that takes AI
  cores. It gives upkeep −25% and nothing else, and the tooltip says "No effect". It also adds no
  Luddic Path interest (Alpha adds the most of the three normal cores).

## Default for production buildings

Buildings whose job is making commodities get one shared Omega effect instead of a unique one.

Proposal: **double the Alpha effect.** Production +2, demand −2, upkeep −50%.

**Omega (production default):**

---

## Vanilla

### Production buildings (use the default)

| Building | ID | Makes |
| --- | --- | --- |
| Farming | `farming` | Food, amount set by the farmland |
| Aquaculture | `aquaculture` | Farming for water worlds: food, plus lobster where they live |
| Mining | `mining` | Ore, transplutonic ore, organics and volatiles, from the planet's deposits |
| Refining | `refining` | Metals from ore, transplutonics from transplutonic ore |
| Light Industry | `lightindustry` | Domestic goods and luxury goods; recreational drugs on a free port |
| Heavy Industry | `heavyindustry` | Heavy machinery, supplies, weapons and ship hulls; runs your custom production |
| Orbital Works | `orbitalworks` | Heavy Industry upgrade: more ship hulls and fewer d-mods |
| Fuel Production | `fuelprod` | Fuel, from volatiles |

### Buildings with a side output (unique effect, or the default?)

These make a commodity, but that is not their main job.

**Population & Infrastructure** (`population`)
The colony itself. Demands food and goods; makes crew, drugs and organs once the colony is big enough.
Alpha today: +1 crew, drugs and organs.
Omega:

**Spaceport / Megaport** (`spaceport`, `megaport`)
Accessibility, which drives trade income and how well the colony imports. Also makes crew.
Alpha today: accessibility +20%.
Omega:

**Patrol HQ / Military Base / High Command** (`patrolhq`, `militarybase`, `highcommand`)
Spawn patrol fleets that defend the system (Military Base and High Command also send fleets to
other systems). Make crew and marines.
Alpha today: fleet size +25%.
Omega:

**Lion's Guard HQ** (`lionsguard`)
Sindrian Diktat only. Elite Lion's Guard patrols. Makes crew and marines.
Alpha today: +1 crew and marines.
Omega:

**Ground Defenses / Heavy Batteries** (`grounddefenses`, `heavybatteries`)
Ground defense strength against raids and invasions. Makes marines.
Alpha today: ground defenses ×1.5.
Omega:

**Cryosanctum** (`cryosanctum`)
Rare Domain-era relic, cannot be built. Makes organs.
Alpha today: +1 organs.
Omega:

### Buildings with no output (unique effect)

**Tech-Mining** (`techmining`)
Digs through Domain ruins each month for weapons, blueprints and colony items.
Alpha today: finds +25%.
Omega:

**Commerce** (`commerce`)
Colony income +25%, stability −3.
Alpha today: another +25% colony income.
Omega:

**Orbital Station / Battlestation / Star Fortress** (`orbitalstation`, `battlestation`,
`starfortress`, each also `_mid` and `_high`; low-tech, midline and high-tech)
The station that defends the colony in battle and must be beaten before a raid or bombardment.
Alpha today: stronger station in combat (an Alpha-level commander).
Omega:

**Planetary Shield** (`planetaryshield`)
Triples ground defenses.
Alpha today: ground defenses ×1.5 on top.
Omega:

**Waystation** (`waystation`)
Accessibility, plus a stockpile of fuel, supplies and crew you can buy.
Alpha today: much larger stockpiles.
Omega:

**Cryorevival Facility** (`cryorevival`)
Large population growth, powered by a Domain cryosleeper in the system.
Alpha today: growth bonus doubled.
Omega:

---

## Industrial Evolution (optional)

IndEvo gives most of its buildings their own Alpha, Beta and Gamma effects already.

### Production buildings (use the default?)

| Building | ID | Makes |
| --- | --- | --- |
| Specialized Mining | `IndEvo_SwitchableMining` | Mining focused on one resource, for more of it |
| Specialized Refining | `IndEvo_SwitchableRefining` | Refining focused on one product, for more of it |
| Salvage Yards | `IndEvo_ScrapYard` | Ship parts, metals and hulls, from wrecks destroyed in the system |
| Variable Manufactory / Assembler | `IndEvo_AdManuf`, `IndEvo_AdAssem` | Whatever commodity its installed chip is set to (Alpha: output up) |
| Commodity Forge | `IndEvo_ComForge` | Burns a chip for three big batches of a commodity into storage (Alpha: faster) |

### Buildings with no real output (unique effect)

**Specialized Government** (`IndEvo_SwitchablePopulation`)
IndEvo's Population & Infrastructure, with a chosen government focus.
Omega:

**Centralization Bureau** (`IndEvo_AdInfra`)
Raises output of every identical building in the star system. Takes a core only when no item is installed.
Alpha today: highest cap on that bonus.
Omega:

**Supercomputer** (`IndEvo_SupCom`)
Raises income of every colony in the system; burns AI cores from its own storage as fuel.
Alpha today: stronger income boost.
Omega:

**Embassy** (`IndEvo_embassy`)
Improves relations with one faction each month; makes every reputation loss 50% worse.
Alpha today: smaller reputation-loss penalty.
Omega:

**Senate** (`IndEvo_senate`)
One per star system. Lets you enact colony policies more directly.
Alpha today: stability bonus.
Omega:

**Restoration Docks** (`IndEvo_dryDock`)
Slowly removes d-mods from ships in its storage, much cheaper than instant restoration.
Alpha today: faster repairs, lower upkeep while idle.
Omega:

**Academy** (`IndEvo_Academy`)
Trains and stores officers and administrators; better officers for the system.
Alpha today: can retrain an officer's personality a second time.
Omega:

**Requisitions Centre** (`IndEvo_ReqCenter`)
A shop that sells weapons from factions that like you.
Alpha today: more weapons for sale, skips ones you already have plenty of.
Omega:

**Engineering Hub** (`IndEvo_EngHub`)
Reverse-engineers ships you put in its storage into blueprints.
Alpha today: faster reverse engineering.
Omega:

**Courier Port** (`IndEvo_PrivatePort`)
Recurring shipping contracts that move cargo between your colonies.
Alpha today: cheaper shipping.
Omega:

**Privateer Base** (`IndEvo_pirateHaven`)
Pirates living on your colony raid hostile systems and bring back spoils.
Alpha today: stronger raids, more spoils.
Omega:

**Animal Center** (`IndEvo_PetStore`)
Sells pets. Makes pet food.
Alpha today: higher chance of rare pets.
Omega:

**Museum** (`IndEvo_Museum`)
Displays ships and stages parades for income.
Alpha today: more parade fleets at once.
Omega:

**Orbital Artillery** (`IndEvo_Artillery_mortar`, `_missile`, `_railgun`)
Restored Domain defence platform that shells hostile fleets in the system.
Alpha today: stronger station in combat.
Omega:

**Hull Deconstructor** (`IndEvo_HullDecon`)
Restored ruin. Breaks ships down into forge templates.
Alpha today: gives the template a permanent logistics hull mod.
Omega:

**Hull Forge** (`IndEvo_HullForge`)
Restored ruin. Builds ships from forge templates.
Alpha today: built ships get two permanent hull mods.
Omega:

**Ancient Laboratory** (`IndEvo_ResLab`)
Restored ruin. Restores broken forge templates.
Alpha today: unlocks a third commodity, smaller bonus.
Omega:

**Rift Generator** (`IndEvo_RiftGen`)
Restored ruin. Opens an artificial rift that can move planets between star systems.
Alpha today: shorter recharge.
Omega:

**Commerce (IndEvo version)** (`commerce`)
IndEvo replaces vanilla Commerce with its own. The vanilla Commerce effect would apply to it.

---

## AoTD Vaults of Knowledge (optional)

VoK replaces most vanilla production buildings with its own upgrade tree.

### Production buildings (use the default?)

| Building | ID | Makes |
| --- | --- | --- |
| Monoculture Plots | `aotd_monoculture_plots` | Starter farm: a little food |
| Subsidised Farming | `aotd_subsidised_farming` | Farming upgrade: much more food, expensive |
| Artisanal Farming | `aotd_artisanal_farming` | Farming upgrade: less food, some luxury goods, flat income |
| Fishing Harbour | `aotd_fishing_harbour` | Aquaculture upgrade: food and lobster |
| Extractive Operation | `aotd_extractive_operation` | Starter mine: ore, organics, transplutonic ore |
| Plasma Harvester | `aotd_plasma_harvester` | Mining upgrade for gas giants: volatiles |
| Mining Megaplex | `aotd_mining_megaplex` | Mining upgrade: much more of every deposit |
| Smelting | `aotd_smelting` | Starter refinery: metals |
| Crystalizator | `aotd_crystalizator` | Refining upgrade: metals, transplutonics and refined metal |
| Enrichment Facility | `aotd_enrichment_facility` | Refining upgrade: transplutonics and purified transplutonics |
| Light Production | `aotd_light_production` | Starter light industry: domestic goods |
| High-tech Industry | `aotd_hightech_industry` | Light Industry upgrade: luxury goods and advanced components |
| Neurochemical Laboratory | `aotd_druglight` | Light Industry upgrade: drugs in bulk |
| Commercial Manufactory | `aotd_commercial_manu` | Light Industry upgrade: lots of domestic goods, some luxury goods |
| Fuel Refinery | `aotd_fuel_refinery` | Fuel Production upgrade: more fuel, plus compound |
| Civilian Heavy Production | `aotd_civilian_heavy_prod` | Heavy Industry upgrade: machinery and supplies, fewer weapons and ships |
| Orbital Skunkworks Facility | `triheavy` | Orbital Works upgrade (Tri-Tachyon style): weapons, ships, S-modded patrols |
| Orbital Fleetwork Facility | `hegeheavy` | Orbital Works upgrade (Hegemony style): more ships, bigger fleets |
| Macro-Industrial Complex | `aotd_macro_industrial_complex` | Late Heavy Industry: Domain-grade machinery and supplies |
| Garden of Elysium | `aotd_garden_of_elysium` | Resort Center upgrade: food and luxury goods, plus tourism income |

### Buildings with no real output (unique effect)

**Resort Center** (`aotd_resort_center`)
Tourism income on a beautiful, stable, accessible world.
Alpha today: income +%.
Omega:

**Terminus** (`aotd_terminus`)
Waystation upgrade: more accessibility and larger stockpiles.
Alpha today: VoK's own bonus.
Omega:

**Trade Outpost / Underworld** (`aotd_trade_outpost`, `aotd_underworld`)
VoK's Commerce: a small trade post, upgradable to an Underworld that trades in illegal goods.
Alpha today: VoK's own bonus.
Omega:

**Research Facility / Black Site** (`aotd_research_facility`, `aotd_blacksite`)
Generates research databanks for VoK's research tree; the Black Site does forbidden research.
Alpha today: more databanks per month.
Omega:

**Persean Technology Institute** (`aotd_persean_university`)
Big research speed bonus; administrators can take classes for special skills.
Alpha today: nothing extra.
Omega:

**Exomatter Processing** (`aotd_exomatter_processing`)
Turns Shrouded Substrate into Tenebrium Cells.
Alpha today: faster conversion, bigger yield.
Omega:

**Star Citadel (Low Tech / High Tech)** (`aotd_citadel`, `aotd_citadel_hightech`)
Fourth station tier, above the Star Fortress.
Alpha today: stronger station in combat.
Omega:

**Daedalus Array** (`aotd_daedalus_array`)
Restored megastructure spaceport. Huge accessibility, makes crew.
Alpha today: nothing extra.
Omega:

**Hypershunt Control** (`aotd_coronal_control`)
Colony built into a restored Coronal Hypershunt.
Alpha today: nothing extra.
Omega:

---

## Terraforming & Station Construction (optional)

### Production buildings (use the default?)

| Building | ID | Makes |
| --- | --- | --- |
| Hydroponics | `BOGGLED_HYDROPONICS` | Food without farmland |
| Cloning | `BOGGLED_CLONING` | Organs (TASC's building, not your Cloning mod) |
| Cryosanctum | `BOGGLED_CRYOSANCTUM` | A buildable Cryosanctum: organs |
| Domain Archaeology | `BOGGLED_DOMAIN_ARCHAEOLOGY` | Domain-era artifacts, from ruins |

### Buildings with no real output (unique effect)

**Mesozoic Park** (`BOGGLED_MESOZOIC_PARK`)
Tourist income from engineered dinosaurs. Needs a stable, accessible colony.
Alpha today: TASC's own bonus.
Omega:

**Genelab** (`BOGGLED_GENELAB`)
Genetic engineering: cleans pollution, adds lobsters, stocks Mesozoic Park.
Alpha today: Mesozoic Park income up.
Omega:

**Limelight Network** (`BOGGLED_LIMELIGHT_NETWORK`)
Big income from an addictive VR network; lowers production across the colony.
Alpha today: TASC's own bonus.
Omega:

**CHAMELEON** (`BOGGLED_CHAMELEON`)
Code-breaking supercomputer that hunts Pather cells and rogue AI cores.
Alpha today: wipes out Pather cells on the colony.
Omega:

**Domed Cities** (`BOGGLED_DOMED_CITIES`)
Removes bad planet conditions; the colony becomes almost defenceless.
Alpha today: stability bonus.
Omega:

**Kletka Simulator** (`BOGGLED_KLETKA_SIMULATOR`)
Tries to make one AI core each month.
Alpha today: better odds of higher cores.
Omega:

**AI Mining Drones** (`BOGGLED_AI_MINING_DRONES`)
Stations only. Boosts the station's Mining, and needs an AI core to do anything.
Alpha today: mining +N units.
Omega:

**Harmonic Damper** (`BOGGLED_HARMONIC_DAMPER`)
Cancels tectonic activity and raises ground defenses.
Alpha today: bigger ground-defense bonus.
Omega:

**Autonomous AI Battlestation** (`BOGGLED_REMNANT_STATION`)
A Remnant battlestation as your colony's station.
Alpha today: stronger station in combat.
Omega:

---

## Your own mods (already have Omega effects)

- **Cloning Industry / Clone Vat Experiments / Cloning Megafactory** (Cloning, and Coalesced Little
  Mods): upkeep −50%, population growth scales with colony size at double the Alpha rate, makes crew
  and marines.
- **Reverse Engineering Hub I–III** (Rev. Eng. Private Arsenal, and Coalesced Little Mods): 2 days
  per item, and the Private Arsenal becomes free.

## Cannot hold an AI core (skip)

These buildings refuse every AI core, so an Omega core cannot be installed either.

- IndEvo: Military Relays, Interstellar Relay, Warehouse Complex, Wormhole Complex, World Symbols
  and every wonder (palaces, Memorial, Leisure Orbitals, Cathedral, Ice Palace, Lava Fortress,
  Tree of Life, Cloud Painter), Old Industrial Ruins, Ruined Infrastructure.
- VoK: Maglev Central Hub, Bifrost Control Center, Nidavelir Complex, Pluto Mining Station,
  Coronal Wormhole, Coronal Collector.
- TASC: Stellar Reflector Array, Ismara's Sling, Galatian Particle Accelerator, Perihelion Project,
  Expand Station, Planetary Agrav Field, Atmosphere Processor, Planet Cracker, Ouyang Optimizer.
