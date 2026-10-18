package omegacoreeverywhere.effects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.listeners.CampaignUIRenderingListener;
import com.fs.starfarer.api.combat.ViewportAPI;
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
 * The game builds the picker when it opens and gives no mod a say in it. So on every frame a screen
 * that can show it is drawn, this walks what is on screen, finds the picker's "No effect" lines and
 * has {@link NoEffectText} rewrite each one for its building, which the line's row, the picker's
 * header and the picker itself all keep.
 *
 * It hangs off the campaign screen's drawing, not its clock: with a dialog open the game stops
 * running scripts on frames where no time passes, but it still draws them. The layer this is called
 * from is drawn before any dialog on top of it, so a rewritten line shows on the same frame.
 *
 * If anything here fails, it logs once and stops until the game is loaded again; the picker then
 * shows the game's own text, nothing worse.
 */
final class CorePickerText implements CampaignUIRenderingListener {

    private static final int MAX_DEPTH = 40;
    private static final Logger log = Global.getLogger(CorePickerText.class);

    private boolean failed;
    /** One line each in starsector.log, so a picker that keeps its old text can be traced. */
    private boolean loggedWatching, loggedRewrite, loggedOrphan;

    @Override
    public void renderInUICoordsBelowUI(ViewportAPI viewport) {
    }

    @Override
    public void renderInUICoordsAboveUIBelowTooltips(ViewportAPI viewport) {
        if (failed) return;
        try {
            run();
        } catch (RuntimeException | LinkageError e) {
            failed = true;
            log.error("Omega Core Everywhere could not read the core picker; it keeps the game's own text "
                    + "until the game is loaded again", e);
        }
    }

    @Override
    public void renderInUICoordsAboveUIAndTooltips(ViewportAPI viewport) {
    }

    private void run() {
        SectorAPI sector = Global.getSector();
        if (sector == null || !Settings.effectsEnabled()) return;
        if (!(AppDriver.getInstance().getCurrentState() instanceof CampaignState)) return;
        CampaignState state = (CampaignState) AppDriver.getInstance().getCurrentState();

        // A colony screen is either inside the dialog of a market you are docked at, or a tab.
        Object dialog = Reflect.call(state, "getEncounterDialog");
        if (dialog == null && sector.getCampaignUI().getCurrentCoreTab() == null) return;
        if (!omegaAround(sector)) return;
        if (!loggedWatching) {
            loggedWatching = true;
            log.info("Omega Core Everywhere: looking for the core picker on the colony screen");
        }

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
                Industry ind = owner(path);
                NoEffectText.Fit fit = NoEffectText.rewrite(label, ind, "Effect shown in the building's tooltip.");
                if (ind == null && !loggedOrphan) {
                    loggedOrphan = true;
                    log.warn("Omega Core Everywhere: found \"" + label.getText() + "\" with no building above it");
                } else if (ind != null && !loggedRewrite) {
                    loggedRewrite = true;
                    log.info("Omega Core Everywhere: core picker line for " + ind.getId() + ": " + fit);
                }
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
