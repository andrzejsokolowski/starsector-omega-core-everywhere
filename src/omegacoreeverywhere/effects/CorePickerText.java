package omegacoreeverywhere.effects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.listeners.CampaignUIRenderingListener;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.campaign.CampaignState;
import com.fs.state.AppDriver;

import omegacoreeverywhere.Settings;

/**
 * Puts the Omega effect into the core picker, the dialog a building opens to choose its AI core.
 *
 * The game builds the picker when it opens and gives no mod a say in it. The colony screen opens a
 * building's dialog on the top level of the campaign screen, and that dialog opens the picker beside
 * it. So on every drawn frame of a colony screen this glances over that top level, a short list, and
 * only searches what keeps a building: the picker and the building's dialog, while they are open.
 * There it finds the "No effect" lines and has {@link NoEffectText} rewrite each one for its
 * building, which the line's row, the picker's header and the picker itself all keep.
 *
 * It hangs off the campaign screen's drawing, not its clock: with a dialog open the game stops
 * running scripts on frames where no time passes, but it still draws them. The layer this is called
 * from is drawn before any dialog on top of it, so a rewritten line shows on the same frame.
 *
 * If anything here fails, it logs once and stops until the game is loaded again; the picker then
 * shows the game's own text, nothing worse.
 */
final class CorePickerText implements CampaignUIRenderingListener {

    private static final Logger log = Global.getLogger(CorePickerText.class);

    private boolean failed;
    /** One line each in starsector.log, so a picker that keeps its old text can be traced. */
    private boolean loggedFound, loggedRewrite, loggedOrphan;

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
        if (sector == null || !(AppDriver.getInstance().getCurrentState() instanceof CampaignState)) return;
        CampaignState state = (CampaignState) AppDriver.getInstance().getCurrentState();

        // A colony screen is either inside the dialog of a market you are docked at, or a tab. In
        // flight neither is open and nothing more is looked at.
        if (Reflect.call(state, "getEncounterDialog") == null && sector.getCampaignUI().getCurrentCoreTab() == null) {
            return;
        }
        Object screen = Reflect.call(state, "getScreenPanel");
        if (!(screen instanceof UIPanelAPI)) return;

        Set<Object> seen = null;
        for (UIComponentAPI child : Reflect.children((UIPanelAPI) screen)) {
            if (!(child instanceof UIPanelAPI) || Reflect.industryOf(child) == null) continue;
            if (!Settings.effectsEnabled()) return;
            if (!loggedFound) {
                loggedFound = true;
                log.info("Omega Core Everywhere: a building's dialog is open, looking for the core picker in it");
            }
            if (seen == null) seen = Collections.newSetFromMap(new IdentityHashMap<>());
            NoEffectText.visit(child, new ArrayList<>(), seen, this::rewrite);
        }
    }

    private void rewrite(LabelAPI label, List<UIComponentAPI> path) {
        String before = label.getText();
        Industry ind = owner(path);
        NoEffectText.Fit fit = NoEffectText.rewrite(label, ind, "Effect shown in the building's tooltip.");
        if (ind == null && !loggedOrphan) {
            loggedOrphan = true;
            log.warn("Omega Core Everywhere: found \"" + before + "\" with no building above it");
        } else if (ind != null && !loggedRewrite) {
            loggedRewrite = true;
            log.info("Omega Core Everywhere: core picker line for " + ind.getId() + ": " + fit);
        }
    }

    /** The building of the closest panel above the line that keeps one. */
    private static Industry owner(List<UIComponentAPI> path) {
        for (int i = path.size() - 1; i >= 0; i--) {
            Industry ind = Reflect.industryOf(path.get(i));
            if (ind != null) return ind;
        }
        return null;
    }
}
