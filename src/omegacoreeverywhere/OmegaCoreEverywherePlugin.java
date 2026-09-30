package omegacoreeverywhere;

import com.fs.starfarer.api.BaseModPlugin;

import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;

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
    }

    /** A changed slider takes effect at once, without reloading the save. */
    private static final class SettingsListener implements LunaSettingsListener {
        @Override
        public void settingsChanged(String modId) {
            if (MOD_ID.equals(modId)) OmegaDrops.apply();
        }
    }
}
