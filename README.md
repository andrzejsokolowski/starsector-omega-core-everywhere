# Omega Core Everywhere

[Forum topic](https://fractalsoftworks.com/forum/index.php?topic=36168.0)

Omega cores drop from destroyed Omega ships, and do something in every colony building that takes
an AI core.

## Omega cores in colony buildings

Install an Omega core the same way as any other AI core. On your own colonies:

- **Buildings that make commodities** make twice as much for half the demand, at double the upkeep.
- **Other buildings** get an effect of their own: doubled population growth, doubled patrol fleets,
  ground defenses ×10, stations that shrug off disruption, monthly deliveries into storage, and so on.
- The core picker and the building's tooltip say what the Omega core does, where the game would say
  "No effect". An effect too long for that line gets a short version there, and the tooltip shows it
  in full under an "Omega core" heading.

Buildings from Industrial Evolution, AoTD Vaults of Knowledge and Terraforming & Station
Construction are covered too. None of those mods is required, and their effects only exist while
the mod is installed. [docs/industries.md](docs/industries.md) lists every building and its effect.

An Omega core in Industrial Evolution's Supercomputer normally crashes the game a day later. This
mod fixes that.

The effects can be switched off in the LunaLib settings. They are taken off before every save and
put back after it, so a save made with this mod still loads without it.

## Omega core captains

The base game already lets an Omega core captain an automated ship. Your Omega core captains also
get Omega Timeflow: time runs 25% faster for their ship. The bonus is a LunaLib setting (0 switches
it off). Enemy Omega ships do not get it. The skill comes off before every save and goes back on
after it.

## Turning in Omega cores

Factions that buy AI cores take an Omega core for a 1,000,000 credit bounty (times the faction's
rate) and 500 times the usual reputation, so one core maxes out your standing with any of them.

## With Exotica Technologies

- **Subsumed Omega Core:** an automated ship takes in an Omega core for +150 bandwidth. Does not
  count towards the exotic limit.
- **Omega Subcore:** +150 bandwidth, weapons cost 2/4/8 OP less by size, fighters 4 and bombers 8
  less.

Both are installed with an Omega core in Exotica's menu. Computer fleets never have them.

## With Special Hullmod Upgrades

**Armament Support System (Omega)** is the Omega tier of the Alpha, Beta and Gamma core upgrades.
It uses up an Omega core when installed: autofire accuracy +100%, turret turn rate +100%, every
weapon (missiles included) costs 3/5/8 OP less by size, deployment cost +25%. It is learned together
with the Alpha upgrade and follows Special Hullmod Upgrades' settings for deployment cost, stacking
core upgrades and losing the core with the ship.

Take the Omega exotics and Armament Support System (Omega) off your ships before removing this mod
from a save.

## Drops

A destroyed Omega ship can drop the Omega core that commanded it. Bigger ships drop it more often,
the same way vanilla AI cores work: a destroyer counts twice, a cruiser three times and a capital
four times.

Open LunaLib's settings (`Shift+F2` in the campaign) and pick **Omega Core Everywhere** to change the
drop chance, to switch drops or building effects off, or to change the Omega Timeflow bonus.
Changes apply at once.

## With Astral Ascension

Astral Ascension also makes Omega cores drop. When it is installed, it keeps control of the drops
and this mod's drop settings do nothing.

## Requirements

- [LunaLib](https://fractalsoftworks.com/forum/index.php?topic=25658.0)

## Building

Set `starsectorPath` in `gradle.properties`, then run `gradlew releaseZip`. The build runs every
check and writes `OmegaCoreEverywhere.zip` next to this file, ready for a mod manager.

Compiling needs Industrial Evolution, AoTD Vaults of Knowledge, Exotica Technologies and LazyLib
installed in the game's `mods` folder, because a few classes call into them. Playing does not.
