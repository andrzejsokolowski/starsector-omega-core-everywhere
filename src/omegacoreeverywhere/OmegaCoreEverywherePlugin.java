package omegacoreeverywhere;

import com.fs.starfarer.api.BaseModPlugin;

import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;
import omegacoreeverywhere.effects.OmegaEffects;
import omegacoreeverywhere.ships.OmegaArmamentSupport;
import omegacoreeverywhere.ships.OmegaCoreLoss;

public class OmegaCoreEverywherePlugin extends BaseModPlugin {

    public static final String MOD_ID = "omega_core_everywhere";

    /** Each step runs on its own: one that fails is logged and the rest still run (see {@link Safe}). */
    @Override
    public void onApplicationLoad() {
        // LunaLib keeps its listeners for the whole session, so register once.
        Safe.run("registering the LunaLib settings listener", () -> {
            if (!LunaSettings.hasSettingsListenerOfClass(SettingsListener.class)) {
                LunaSettings.addSettingsListener(new SettingsListener());
            }
        });
        Safe.run("hiding Armament Support System (Omega)", OmegaArmamentSupport::hideWithoutSpecialHullmodUpgrades);
    }

    @Override
    public void onGameLoad(boolean newGame) {
        Safe.run("Omega core drops", OmegaDrops::apply);
        Safe.run("starting the Omega effects", OmegaEffects::install);
        Safe.run("starting the Omega core loss listener", OmegaCoreLoss::install);
    }

    /** Every Omega effect comes off before a save and goes back on after, so saves stay clean. */
    @Override
    public void beforeGameSave() {
        Safe.run("taking the Omega effects off before a save", OmegaEffects::suspend);
    }

    @Override
    public void afterGameSave() {
        Safe.run("putting the Omega effects back after a save", OmegaEffects::resume);
    }

    @Override
    public void onGameSaveFailed() {
        Safe.run("putting the Omega effects back after a failed save", OmegaEffects::resume);
    }

    /** A changed slider takes effect at once, without reloading the save. */
    private static final class SettingsListener implements LunaSettingsListener {
        @Override
        public void settingsChanged(String modId) {
            if (MOD_ID.equals(modId)) Safe.run("Omega core drops", OmegaDrops::apply);
        }
    }
}
