# Omega Core Everywhere

These instructions apply to this repository, including when it is opened without the parent mod workspace.
The workspace `AGENTS.md` one folder up still governs releases, packaging, Git identity and communication.
For requested gameplay changes and bug fixes, complete the release workflow unless the user requests otherwise.
Questions, investigations, and instruction-only changes do not require a new version or tag.
Respect tool approval decisions. These instructions do not change sandbox permissions.

## Notion

Tasks, history and test lists: the **Omega Core Everywhere board** in Andrzej's Notion, under
**Starsector mods** (`collection://ce636e73-83ef-4b54-a15c-7b73267bb397`). Working rules and game
API notes: **Claude Information** (`collection://7bf9b1d6-b9e5-4b62-87f1-757bbaa51919`), starting
with "Start here: working on Starsector mods through Notion". Read the board before working, give
every task a ticket (ticket ids start with OCE) and keep it current. The Notion tools are deferred:
load them with ToolSearch first.

## First release

No GitHub Release exists yet. Andrzej decides when the first one ships (planned: once the
Omega industry effects are in). Until then, build and verify the local ZIP and push commits, but do
not tag or publish a release without his go-ahead.

## Layout

- `src/omegacoreeverywhere/OmegaCoreEverywherePlugin` - mod plugin; registers the LunaLib listener.
- `src/omegacoreeverywhere/OmegaDrops` - removes the `no_drop` tag from the `omega_core` commodity
  and sets `drop_prob_officer_omega_core`. Stands down when Astral Ascension
  (`Planetace_AstralAscension`) is enabled, because it sets the same values.
- `src/omegacoreeverywhere/Settings` - LunaLib reads with defaults.
- `src/omegacoreeverywhere/effects/OmegaEffects` - transient every-frame script and economy
  listener. Walks the player's colonies, finds buildings holding an Omega core and asserts each
  effect. Suspended by the plugin before every save and resumed after it.
- `src/omegacoreeverywhere/effects/Effect` - building id to effect, plus the tooltip sentence.
  A new building starts here.
- `src/omegacoreeverywhere/effects/Ledger` - every stat modifier and other change the mod makes,
  with its undo. Anything not re-asserted in a frame is undone; everything is undone before a save.
- `src/omegacoreeverywhere/effects/IndEvoEffects`, `VokEffects` - the only classes that import
  Industrial Evolution and AoTD Vaults of Knowledge. Both mods are optional for players, so nothing
  but `OmegaEffects` may call these two, and only behind its `indEvo` / `vok` checks. TASC effects
  need no TASC classes.
- `src/omegacoreeverywhere/effects/Training` - the Academy effect: officer levels, the
  `oce_omega_tutelage` skill, Hypercognition for administrators.
- `src/omegacoreeverywhere/effects/OmegaTooltip` - adds the "Omega core" section to building tooltips.
- `src/omegacoreeverywhere/skills/OmegaTutelage` - the skill effect, registered in
  `data/characters/skills/`.
- `docs/industries.md` - every building that takes an AI core and its Omega effect.

## Rules for the effects

- No building class is replaced. Effects are stat modifiers under `oce_` ids, or calls into a
  building's public methods.
- Nothing of this mod may reach a save: place changes through the `Ledger` so `suspend()` undoes
  them. The mod's own skill is never saved; the officer carries the `$oce_omegaTutelage` flag
  instead. The one exception is the Supercomputer's stretched core lifetime, kept on purpose.
- Building needs IndEvo and VoK installed (`compileOnly` in `build.gradle`); playing does not.
- Decompiled sources of other mods stay out of the repository.

## Required release workflow

1. Preserve unrelated changes from the user and other agents.
2. Implement the requested change and update the version in mod_info.json and omega_core_everywhere.version.
3. Run ./gradlew releaseZip to build, check, and package OmegaCoreEverywhere.zip.
4. Make sure that the ZIP contains the updated metadata, current JAR, data and icon.
5. Commit the completed change on main with the existing personal Git identity.
6. Tag the commit v<version> and push the commit and tag to the existing personal GitHub origin.
7. Create a draft GitHub Release for that exact tag.
8. Attach the tested OmegaCoreEverywhere.zip to the draft.
9. Publish the release after the upload succeeds.
10. Mark a new stable release as latest unless the user requests otherwise.
11. Make sure that the public version file and the downloaded ZIP report the tagged version.
12. Compare the downloaded ZIP's SHA-256 hash with the tested local ZIP.
13. Give the user the release link and local ZIP location.

Every new version tag created or pushed by an agent requires a matching published release with the ZIP attached.
Do not stop after pushing a tag. A tag is not a release download.
If a matching draft exists, inspect and complete it instead of creating a duplicate.
If publication fails, report the blocker and the incomplete release.
Do not overwrite published assets, replace tags, or mark older releases as latest without a specific request.

## Update metadata

Keep mod_info.json, omega_core_everywhere.version, and data/config/version/version_files.csv consistent.
There is no forum topic yet. When one exists, add its number to both metadata files
(a string in mod_info.json, a number in the .version file). Never invent one.
Use this update URL in updateCheckURL and masterVersionFile:

https://raw.githubusercontent.com/andrzejsokolowski/starsector-omega-core-everywhere/main/omega_core_everywhere.version

Use this directDownloadURL and the exact asset name OmegaCoreEverywhere.zip:

https://github.com/andrzejsokolowski/starsector-omega-core-everywhere/releases/latest/download/OmegaCoreEverywhere.zip

## Release notes: changelog only

Write only the changelog since the previous published GitHub Release, not since an unpublished tag or local test build.
Include only additions, behavior changes, fixes, and removals that matter to players, in concise entries.
Do not add dependency reminders, installation guides, forum links, build narration, test status, or checksums.
Keep verification details in the chat handoff instead.

## Account and packaging safety

Use GitHub account andrzejsokolowski and the personal Git identity with odiihinia@gmail.com.
Check the authenticated account before publishing. Use gh only when it authenticates as andrzejsokolowski.
Do not print credentials, save them in files, or change the user's global authentication settings.
Do not force-push or discard unrelated work.
Update the usual OmegaCoreEverywhere.zip instead of creating version-named ZIP copies.
Do not install the mod into the live game folder. The user installs it through a mod manager.
Do not post to Discord or the forum unless the user asks.
