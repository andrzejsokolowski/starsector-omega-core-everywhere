package omegacoreeverywhere.effects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.campaign.CampaignState;
import com.fs.state.AppDriver;

import omegacoreeverywhere.Settings;

/**
 * Puts the Omega effect into the core picker, the dialog a building opens to choose its AI core.
 *
 * The game builds the picker when it opens and gives no mod a say in it. So while the game is paused
 * on a screen that can show it, this walks what is on screen, finds the picker's "No effect" lines
 * and has {@link NoEffectText} rewrite each one for its building, which the line's row, the
 * picker's header and the picker itself all keep.
 *
 * If anything here fails, it logs once and stops until the game is loaded again; the picker then
 * shows the game's own text, nothing worse.
 */
final class CorePickerText implements EveryFrameScript {

    private static final int MAX_DEPTH = 40;
    private static final Logger log = Global.getLogger(CorePickerText.class);

    private boolean failed;

    @Override
    public boolean isDone() {
        return false;
    }

    /** The picker only exists while the game is paused. */
    @Override
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {
        if (failed) return;
        try {
            run();
        } catch (RuntimeException | LinkageError e) {
            failed = true;
            log.error("Omega Core Everywhere could not read the core picker; it keeps the game's own text "
                    + "until the game is loaded again", e);
        }
    }

    private void run() {
        SectorAPI sector = Global.getSector();
        if (sector == null || !sector.isPaused() || !Settings.effectsEnabled()) return;
        if (!(AppDriver.getInstance().getCurrentState() instanceof CampaignState)) return;
        CampaignState state = (CampaignState) AppDriver.getInstance().getCurrentState();

        // A colony screen is either inside the dialog of a market you are docked at, or a tab.
        Object dialog = Reflect.call(state, "getEncounterDialog");
        if (dialog == null && sector.getCampaignUI().getCurrentCoreTab() == null) return;
        if (!omegaAround(sector)) return;

        Set<Object> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        List<UIComponentAPI> path = new ArrayList<>();
        for (Object root : new Object[] {Reflect.call(state, "getScreenPanel"), Reflect.call(state, "getCore"),
                Reflect.call(dialog, "getCoreUI")}) {
            if (root instanceof UIPanelAPI) walk((UIPanelAPI) root, path, seen);
        }
    }

    private void walk(UIComponentAPI node, List<UIComponentAPI> path, Set<Object> seen) {
        if (path.size() > MAX_DEPTH || !seen.add(node)) return;
        if (node instanceof LabelAPI) {
            LabelAPI label = (LabelAPI) node;
            if (NoEffectText.isOmegaNoEffect(label.getText())) {
                NoEffectText.rewrite(label, owner(path), "Effect shown in the building's tooltip.");
            }
            return;
        }
        if (!(node instanceof UIPanelAPI)) return;
        path.add(node);
        for (UIComponentAPI child : Reflect.children((UIPanelAPI) node)) walk(child, path, seen);
        path.remove(path.size() - 1);
    }

    /** The building of the closest panel above the line that keeps one. */
    private static Industry owner(List<UIComponentAPI> path) {
        for (int i = path.size() - 1; i >= 0; i--) {
            Industry ind = Reflect.industryOf(path.get(i));
            if (ind != null) return ind;
        }
        return null;
    }

    /** Without an Omega core in reach, no picker can be showing one, and the walk is skipped. */
    private static boolean omegaAround(SectorAPI sector) {
        CampaignFleetAPI fleet = sector.getPlayerFleet();
        if (fleet != null && fleet.getCargo().getCommodityQuantity(Commodities.OMEGA_CORE) > 0f) return true;
        for (MarketAPI market : sector.getEconomy().getMarketsCopy()) {
            if (!market.isPlayerOwned()) continue;
            for (Industry ind : market.getIndustries()) {
                if (OmegaEffects.hasOmega(ind)) return true;
            }
            SubmarketAPI storage = market.getSubmarket(Submarkets.SUBMARKET_STORAGE);
            if (storage != null && storage.getCargo().getCommodityQuantity(Commodities.OMEGA_CORE) > 0f) return true;
        }
        return false;
    }
}
