# Omega Core Everywhere

[Forum topic](https://fractalsoftworks.com/forum/index.php?topic=36168.0)

Omega cores drop from destroyed Omega ships, and do something in every colony building that takes
an AI core.

## Omega cores in colony buildings

Install an Omega core the same way as any other AI core. On your own colonies:

- **Buildings that make commodities** make twice as much for half the demand, at double the upkeep.
- **Other buildings** get an effect of their own: doubled population growth, doubled patrol fleets,
  ground defenses ×10, stations that shrug off disruption, monthly deliveries into storage, and so on.
- The building's tooltip says what its Omega core does. The core picker still says "No effect"; that
  line belongs to the building and cannot be changed from outside.

Buildings from Industrial Evolution, AoTD Vaults of Knowledge and Terraforming & Station
Construction are covered too. None of those mods is required, and their effects only exist while
the mod is installed. [docs/industries.md](docs/industries.md) lists every building and its effect.

An Omega core in Industrial Evolution's Supercomputer normally crashes the game a day later. This
mod fixes that.

The effects can be switched off in the LunaLib settings. They are taken off before every save and
put back after it, so a save made with this mod still loads without it.

## Drops

A destroyed Omega ship can drop the Omega core that commanded it. Bigger ships drop it more often,
the same way vanilla AI cores work: a destroyer counts twice, a cruiser three times and a capital
four times.

Open LunaLib's settings (`Shift+F2` in the campaign) and pick **Omega Core Everywhere** to change the
drop chance, or to switch drops or building effects off. Changes apply at once.

## With Astral Ascension

Astral Ascension also makes Omega cores drop. When it is installed, it keeps control of the drops
and this mod's drop settings do nothing.

## Requirements

- [LunaLib](https://fractalsoftworks.com/forum/index.php?topic=25658.0)

## Building

Set `starsectorPath` in `gradle.properties`, then run `gradlew releaseZip`. The build runs every
check and writes `OmegaCoreEverywhere.zip` next to this file, ready for a mod manager.

Compiling needs Industrial Evolution and AoTD Vaults of Knowledge installed in the game's `mods`
folder, because two classes call into them. Playing does not.
