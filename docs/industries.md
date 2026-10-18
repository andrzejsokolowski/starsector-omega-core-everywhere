# Omega core effects

Every colony building that can hold an AI core, grouped by where it comes from: what the building
does, what the normal cores do there today, and the Omega effect. All effects were decided by
Andrzej and are built as of 0.2.0.

Effects only apply on colonies the player owns, and only while the building is working. Effects
for a mod's buildings only exist while that mod is installed; none of the three mods is required.

## How the normal cores work, for comparison

- **Gamma:** demand −1.
- **Beta:** demand −1, upkeep −25%.
- **Alpha:** demand −1, upkeep −25%, and production buildings make +1 of everything.
- **Omega in vanilla, today:** installs anywhere AI cores fit, gives upkeep −25% and nothing else.
  The core picker says "No effect". No Luddic Path interest.

Modded buildings often replace these with their own effects; those are listed per building.

## Shared rules

**Production buildings** (anything whose job is making commodities) share one Omega effect:

- Production ×2.
- Demand ×0.5.
- Upkeep ×2. This replaces the usual −25%.

**Buildings with a unique effect** keep the usual −25% upkeep from having an AI core.

**Buildings marked "no effect"** still accept the core and still get the vanilla −25% upkeep,
nothing more.

**Buildings from mods not on this sheet:** if they make commodities, they get the production
effect automatically. Otherwise nothing.

**Where the effect is shown:** every building writes "Omega Core. No effect." itself, in the core
picker ("choose an AI core") and in its tooltip's Items section. Replacing the buildings would break
other mods and existing colonies, so the mod rewrites that line on screen instead: it names the
effect, or a shorter wording of it when the whole effect does not fit in two lines. When the line cannot
hold the whole effect, or cannot be found, the tooltip adds it under an "Omega core" heading.
Buildings with no effect keep the game's "No effect". Buildings that draw their whole tooltip
themselves (IndEvo's Orbital Artillery) show nothing in the tooltip, only in the picker.

**Stacking:** income multipliers from different buildings multiply each other (Commerce and a
Resort Center on one colony give ×4). The Supercomputer's ×1.5 and the research ×4 count once.

**Saves:** every effect is taken off before a save and put back after it, so a save made with
this mod loads without it. Things already handed out stay: delivered goods, officer levels,
Hypercognition, restored ships, reputation.

---

## Vanilla

### Production buildings (shared effect)

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
| Cryosanctum | `cryosanctum` | Organs. Rare Domain-era relic, cannot be built |

### Unique effects

**Population & Infrastructure** (`population`)
The colony itself. Demands food and goods; makes crew, drugs and organs once the colony is big enough.
Alpha today: +1 crew, drugs and organs.
Omega: population growth ×2, applied after every other modifier. Only growth is doubled, a
shrinking colony does not shrink faster.

**Spaceport / Megaport** (`spaceport`, `megaport`)
Accessibility, which drives trade income and how well the colony imports. Also makes crew.
Alpha today: accessibility +20%.
Omega: 1000 crew into the colony's storage every month.

**Patrol HQ / Military Base / High Command** (`patrolhq`, `militarybase`, `highcommand`)
Spawn patrol fleets that defend the system. Make crew and marines.
Alpha today: fleet size +25%.
Omega: fleet size ×2, applied after every other modifier.

**Lion's Guard HQ** (`lionsguard`)
Sindrian Diktat only, cannot be built.
Omega: no effect.

**Ground Defenses / Heavy Batteries** (`grounddefenses`, `heavybatteries`)
Ground defense strength against raids and invasions. Makes marines.
Alpha today: ground defenses ×1.5.
Omega: ground defenses ×10, and 500 marines into storage every month.

**Tech-Mining** (`techmining`)
Digs through Domain ruins each month for weapons, blueprints and colony items.
Alpha today: finds +25%.
Omega: finds +100%.

**Commerce** (`commerce`)
Colony income +25%, stability −3.
Alpha today: another +25% colony income.
Omega: colony income ×2.

**Orbital Station / Battlestation / Star Fortress** (`orbitalstation`, `battlestation`,
`starfortress`, each also `_mid` and `_high`)
The station that defends the colony in battle.
Alpha today: stronger station in combat (an Alpha-level commander).
Omega: the station recovers from disruption at once (once any battle it is in has ended), and
always fights at 100% combat readiness.

**Planetary Shield** (`planetaryshield`)
Triples ground defenses.
Alpha today: ground defenses ×1.5 on top.
Omega: every building on the colony recovers from disruption at once.

**Waystation** (`waystation`)
Accessibility, plus a stockpile of fuel, supplies and crew you can buy.
Alpha today: much larger stockpiles.
Omega: every month, storage gets 1,000 each of fuel, supplies, heavy machinery, metals and food.

**Cryorevival Facility** (`cryorevival`)
Large population growth, powered by a Domain cryosleeper in the system.
Omega: no effect.

---

## Industrial Evolution (optional)

### Production buildings (shared effect)

| Building | ID | Makes |
| --- | --- | --- |
| Specialized Mining | `IndEvo_SwitchableMining` | Mining focused on one resource, for more of it |
| Specialized Refining | `IndEvo_SwitchableRefining` | Refining focused on one product, for more of it |
| Salvage Yards | `IndEvo_ScrapYard` | Ship parts, metals and hulls, from wrecks destroyed in the system |
| Variable Manufactory / Assembler | `IndEvo_AdManuf`, `IndEvo_AdAssem` | Whatever commodity its installed chip is set to |
| Commodity Forge | `IndEvo_ComForge` | Burns a chip for big batches of a commodity into storage |

The Variable Manufactory, Variable Assembler and Commodity Forge also pay a monthly batch into
storage; Omega doubles that batch too.

### Unique effects

**Centralization Bureau** (`IndEvo_AdInfra`)
For each building on this colony that also exists on your other colonies in the same star system,
adds production (and the same amount of demand) per copy, up to a cap. Takes a core only when no
colony item is installed.
Today: cap 1 (Gamma), 2 (Beta), 3 (Alpha). The Logistics Core item raises the cap to 4 and the
reach from the star system to 12 light-years.
Omega: cap 8, reach 100 light-years. Demand rises with production, as with the Logistics Core.

**Supercomputer** (`IndEvo_SupCom`)
Needs an AI core to run and burns it out after a while, then takes the next one from its storage.
Raises the income of every colony you own in the star system: +5%, +15% on a cold world, +20% on a
very cold one.
Today: Gamma upkeep −10%. Beta upkeep −20% and a monthly credit payout. Alpha makes the income
bonus 20% stronger.
Omega: income ×1.5 for every colony you own in that star system, on top of the Supercomputer's
normal bonus, and the core never burns out. Several Omega Supercomputers in one system still give
×1.5 once.
The crash fix: without this mod an Omega core in a Supercomputer crashes the game a day later (an
IndEvo bug, it happens with Astral Ascension too), because the Supercomputer moves its core into a
hidden "running" slot and looks up a lifetime that only exists for Gamma, Beta and Alpha. This mod
keeps the Omega in the socket, keeps the running slot occupied (by a Gamma core made up for the
purpose if it is empty) and stretches the running core's lifetime so it never ends. The fix works
even with the effects switched off in the settings. The Supercomputer's own tooltip shows the
stand-in core as the one running.

**Embassy** (`IndEvo_embassy`)
Hosts an ambassador from one faction. Slowly improves relations with it; every reputation loss is
50% worse.
Today: Gamma lower upkeep. Beta raises how high the reputation can climb on its own. Alpha shrinks
the reputation-loss penalty.
Omega: reputation with the ambassador's faction goes straight to the maximum and stays there.

**Restoration Docks** (`IndEvo_dryDock`)
Slowly removes d-mods from ships in its storage, much cheaper than instant restoration.
Today: Gamma spreads the work, a capped number of d-mods per ship each month. Beta cheaper repairs,
least damaged ships first. Alpha more d-mods removed per month and lower upkeep while idle.
Omega: every ship placed inside is fully restored at once, for free.

**Academy** (`IndEvo_Academy`)
Trains and stores officers and administrators; can retrain an officer's personality.
Today: Gamma lower upkeep. Beta shorter training times. Alpha unlocks a second kind of training.
Omega: officers in your fleet and in the Academy's storage get the experience for their maximum
level (you pick their skills as usual) and learn Omega Tutelage: their ship's time runs 20%
faster. Administrators you employ, on your colonies and in the Academy's storage, learn
Hypercognition. Mercenaries and AI cores are not taught. Officers keep all of it after the core is
removed.

**Requisitions Centre** (`IndEvo_ReqCenter`)
A shop that sells weapons from a faction that likes you, normally the one whose ambassador sits in
the Embassy.
Today: Gamma sells from any faction that likes you enough. Beta more weapons for sale. Alpha more
weapons, and skips ones you already have plenty of.
Omega: no effect. The shop's prices and stock are fixed inside IndEvo's shop itself, and making
it free or fuller means replacing that shop.

**Museum** (`IndEvo_Museum`)
Ships put in its storage are exhibited for income, by their value (duplicates do not count). It can also send parade fleets around your colonies.
Today: Gamma more museum income. Beta stability and growth that scale with museum income. Alpha
more parade fleets at once.
Omega: museum income ×2.

**Orbital Artillery** (`IndEvo_Artillery_mortar`, `_missile`, `_railgun`)
Restored Domain platform that shells hostile fleets in the system, with a battlestation.
Today: all cores give a stronger station in combat.
Omega: fires twice as often, and its station always fights at 100% combat readiness. Only the
artillery holding the core is sped up.

**Hull Deconstructor** (`IndEvo_HullDecon`)
Restored ruin. Destroys a ship you put in and writes it onto an empty Forge Template, with a few
charges. Takes days by ship size.
Today: Gamma lower upkeep while idle, fewer scanning errors. Beta one extra charge on the template,
more errors. Alpha the ship also gets a permanent logistics hull mod, more errors.
Omega: no effect.

**Hull Forge** (`IndEvo_HullForge`)
Restored ruin. Builds a copy of the ship stored on a Forge Template, using one charge. An emptied
template degrades and has to be repaired. Takes days by ship size.
Today: Gamma lower upkeep while idle, fewer construction errors. Beta a chance an emptied template
does not degrade, cheaper builds, more errors. Alpha built ships get two permanent hull mods, more
errors.
Omega: no effect.

**Ancient Laboratory** (`IndEvo_ResLab`)
Restored ruin. Repairs Degraded Forge Templates back into empty ones. Also boosts production of
two commodities on the colony.
Today: Gamma faster repair. Beta one boosted commodity, bigger boost. Alpha three boosted
commodities, smaller boost.
Omega: no effect.

### No effect

Senate (edicts are left alone), Engineering Hub, Courier Port, Privateer Base, Animal Center
(deprecated), Rift Generator, Requisitions Centre, Hull Deconstructor, Hull Forge, Ancient
Laboratory.

### Not supported

Specialized Government (`IndEvo_SwitchablePopulation`): buggy, and clashes with AoTD.

---

## AoTD Vaults of Knowledge (optional)

### Production buildings (shared effect)

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

### Unique effects

**Resort Center** (`aotd_resort_center`)
Tourism income that scales with accessibility, and +2 stability. Needs luxury goods, a habitable
world and no pollution.
Today: resort income +30% (Gamma), +40% (Beta), +50% (Alpha).
Omega: colony income ×2, hazard rating −25%.

**Terminus** (`aotd_terminus`)
Waystation upgrade: accessibility +30%, stockpiles of fuel, supplies and crew.
Today: Alpha lower upkeep and demand, accessibility +20%, much larger stockpiles.
Omega: accessibility raised to 1000%.

**Trade Outpost / Underworld** (`aotd_trade_outpost`, `aotd_underworld`)
VoK's Commerce. Trade Outpost: colony income +25%, stability −1, and a trade market open the first
week of each month. Underworld (needs free port): colony income +40%, stability −5, and a black
market.
Today: Alpha adds another +25% (Trade Outpost) or +40% (Underworld) colony income.
Omega: no stability penalty, colony income ×2.

**Research Facility / Black Site** (`aotd_research_facility`, `aotd_blacksite`)
Lets you research VoK technologies and makes 1 research databank a month. Expensive upkeep that
grows with what is being researched. The Black Site is a hidden version with extra cover-up costs.
Today: upkeep −10% (Gamma), −25% (Beta), −50% (Alpha). Alpha also +1 databank a month.
Omega: 10 databanks into storage every month, research 4× as fast. The 4× counts once, however
many facilities hold an Omega core.

**Exomatter Processing** (`aotd_exomatter_processing`)
Turns 4 Shrouded Substrate (2 when improved) into 1 Tenebrium Cell every 35 days. Demands marines.
Today: Beta lower upkeep and demand, 15 days per batch. Alpha the same, and 2 cells per batch.
Omega: 5 days per batch, 4 cells per batch.

**Star Citadel (Low Tech / High Tech)** (`aotd_citadel`, `aotd_citadel_hightech`)
Fourth station tier, above the Star Fortress.
Omega: same as the vanilla stations. Recovers from disruption at once, always 100% combat
readiness.

### No effect

Daedalus Array, Hypershunt Control (would be buggy).

Persean Technology Institute (`aotd_persean_university`): in VoK 5.0.5 this is an unfinished
placeholder. It cannot be built and does nothing, so there is nothing to improve yet.

---

## Terraforming & Station Construction (optional)

Built against the version of TASC that runs with AoTD.

### Production buildings (shared effect)

| Building | ID | Makes |
| --- | --- | --- |
| Hydroponics | `BOGGLED_HYDROPONICS` | Food without farmland |
| Cloning | `BOGGLED_CLONING` | Organs, and population growth that rises with colony size. Needs Domain artifacts, and organics on worlds that are not habitable. Draws Luddic Path interest |
| Cryosanctum | `BOGGLED_CRYOSANCTUM` | A buildable Cryosanctum: organs |
| Domain Archaeology | `BOGGLED_DOMAIN_ARCHAEOLOGY` | Domain-era artifacts, from ruins |

All four use the plain vanilla core effects today.

### Unique effects

**Mesozoic Park** (`BOGGLED_MESOZOIC_PARK`)
Tourist income from engineered dinosaurs. Pays more the higher the accessibility and stability.
Today: park income +10% (Gamma), +20% (Beta), +40% (Alpha).
Omega: park income ×2.

**Genelab** (`BOGGLED_GENELAB`)
Doubles Mesozoic Park income on the same colony. Also runs genetic terraforming projects: removing
pollution, seeding lobsters.
Today: adds to the park income boost: +15% (Gamma), +30% (Beta), +60% (Alpha).
Omega: Mesozoic Park income ×2 again, on top of the Genelab's normal doubling.

**Limelight Network** (`BOGGLED_LIMELIGHT_NETWORK`)
Big income from an addictive VR network. Lowers every commodity the colony produces by 1. Needs
Domain artifacts.
Today: Alpha network income +30%.
Omega: network income ×2, and the colony's production is no longer lowered by 1.

**CHAMELEON** (`BOGGLED_CHAMELEON`)
Surveillance supercomputer. The building itself draws Luddic Path interest.
Today: Alpha, while its artifact demand is met, cancels all Luddic Path interest on the colony, so
no Pather cells.
Omega: no effect.

**Domed Cities** (`BOGGLED_DOMED_CITIES`)
Removes bad planet conditions (also as Sky Cities and Seafloor Cities). Ground defenses drop to
almost nothing.
Today: stability +1 (Gamma), +2 (Beta), +3 (Alpha).
Omega: stability +7.

**Kletka Simulator** (`BOGGLED_KLETKA_SIMULATOR`)
Each month it tries to train an AI core: about 25% Beta, 25% Gamma, 50% nothing. It cannot make an
Alpha without an installed core.
Today: better odds: +5 (Gamma), +10 (Beta), +15 (Alpha) points on a 100-point roll. Alpha cores
only appear at the very top of the roll.
Omega: one Alpha core into storage every month. The simulator's own monthly roll cannot be
switched off from outside, so it still happens on top and sometimes adds a Beta or Gamma.

**AI Mining Drones** (`BOGGLED_AI_MINING_DRONES`)
Stations only. Does nothing without a core.
Today: mining +1 (Gamma), +2 (Beta), +3 (Alpha).
Omega: mining +5.

**Harmonic Damper** (`BOGGLED_HARMONIC_DAMPER`)
Cancels tectonic activity.
Today: ground defenses ×1.25 (Gamma), ×1.5 (Beta), ×2 (Alpha).
Omega: ground defenses ×10.

**Autonomous AI Battlestation** (`BOGGLED_REMNANT_STATION`)
A Remnant battlestation as the colony's station. The installed core becomes its commander.
Today: a stronger commander with each core tier. An Omega core is treated like a Gamma.
Omega: same as the vanilla stations. Recovers from disruption at once, always 100% combat
readiness.

---

## Your own mods (already have Omega effects)

- **Cloning Industry / Clone Vat Experiments / Cloning Megafactory** (Cloning, and Coalesced Little
  Mods): upkeep −50%, population growth scales with colony size at double the Alpha rate, makes crew
  and marines.
- **Reverse Engineering Hub I–III** (Rev. Eng. Private Arsenal, and Coalesced Little Mods): 2 days
  per item, and the Private Arsenal becomes free.

This mod leaves both alone, so they do not also get the production effect.

## Cannot hold an AI core (skip)

- IndEvo: Military Relays, Interstellar Relay, Warehouse Complex, Wormhole Complex, World Symbols
  and every wonder (palaces, Memorial, Leisure Orbitals, Cathedral, Ice Palace, Lava Fortress,
  Tree of Life, Cloud Painter), Old Industrial Ruins, Ruined Infrastructure.
- VoK: Maglev Central Hub, Bifrost Control Center, Nidavelir Complex, Pluto Mining Station,
  Coronal Wormhole, Coronal Collector.
- TASC: Stellar Reflector Array, Ismara's Sling, Galatian Particle Accelerator, Perihelion Project,
  Expand Station, Planetary Agrav Field, Atmosphere Processor, Planet Cracker, Ouyang Optimizer.
