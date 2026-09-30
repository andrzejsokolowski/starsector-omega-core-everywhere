package omegacoreeverywhere;

import com.fs.starfarer.api.BaseModPlugin;

import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;
import omegacoreeverywhere.effects.OmegaEffects;

public class OmegaCoreEverywherePlugin extends BaseModPlugin {

    public static final String MOD_ID = "omega_core_everywhere";

    @Override
    public void onApplicationLoad() {
        // LunaLib keeps its listeners for the whole session, so register once.
        if (!LunaSettings.hasSettingsListenerOfClass(SettingsListener.class)) {
            LunaSettings.addSettingsListener(new SettingsListener());
        }
    }

    @Override
    public void onGameLoad(boolean newGame) {
        OmegaDrops.apply();
        OmegaEffects.install();
    }

    /** Every Omega effect comes off before a save and goes back on after, so saves stay clean. */
    @Override
    public void beforeGameSave() {
        OmegaEffects.suspend();
    }

    @Override
    public void afterGameSave() {
        OmegaEffects.resume();
    }

    @Override
    public void onGameSaveFailed() {
        OmegaEffects.resume();
    }

    /** A changed slider takes effect at once, without reloading the save. */
    private static final class SettingsListener implements LunaSettingsListener {
        @Override
        public void settingsChanged(String modId) {
            if (MOD_ID.equals(modId)) OmegaDrops.apply();
        }
    }
}
