# Omega Core Everywhere

Omega cores drop from destroyed Omega ships, so the mods that use them in colony buildings keep
working without Astral Ascension.

## Drops

A destroyed Omega ship can drop the Omega core that commanded it. Bigger ships drop it more often,
the same way vanilla AI cores work: a destroyer counts twice, a cruiser three times and a capital
four times.

Open LunaLib's settings (`Shift+F2` in the campaign) and pick **Omega Core Everywhere** to change the
drop chance or switch drops off. Changes apply at once.

## With Astral Ascension

Astral Ascension also makes Omega cores drop. When it is installed, it keeps control of the drops
and this mod's drop settings do nothing.

## Requirements

- [LunaLib](https://fractalsoftworks.com/forum/index.php?topic=25658.0)

## Building

Set `starsectorPath` in `gradle.properties`, then run `gradlew releaseZip`. The build runs every
check and writes `OmegaCoreEverywhere.zip` next to this file, ready for a mod manager.
