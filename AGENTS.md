# Omega Core Everywhere

These instructions apply to this repository, including when it is opened without the parent mod workspace.
The workspace `AGENTS.md` one folder up still governs releases, packaging, Git identity and communication.
For requested gameplay changes and bug fixes, complete the release workflow unless the user requests otherwise.
Questions, investigations, and instruction-only changes do not require a new version or tag.
Respect tool approval decisions. These instructions do not change sandbox permissions.

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
- `docs/industries.md` - the Omega effect worksheet for every building that takes an AI core.

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
