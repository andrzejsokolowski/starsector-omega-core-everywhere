package omegacoreeverywhere.effects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Misc;

import omegacoreeverywhere.Settings;

/**
 * Puts the Omega effect where the game says "No effect".
 *
 * Every building writes "Omega Core. No effect." (or "Omega Core currently assigned. No effect.")
 * for a core it does not know, in its tooltip and in the core picker. That line is the game's own
 * label, so this swaps its ending for the effect, as long as the effect fits the room the game gave
 * the line. Nothing else on the screen is touched.
 */
final class NoEffectText {

    /** How a line came out. Anything short of FULL means the tooltip still needs its own section. */
    enum Fit { FULL, PARTIAL, UNTOUCHED }

    private static final String NO_EFFECT = "No effect.";
    /** The game sized the line for one line of text, and the core's icon beside it leaves room for two. */
    private static final int MAX_LINES = 2;
    private static final int MAX_DEPTH = 40;
    /**
     * Where the game keeps parts of a panel off its child list: a tooltip's paragraphs sit on its
     * inner panel, a dialog's body on its inner panel, and a table's rows behind the buttons that
     * draw them.
     */
    private static final String[] HIDDEN_PARTS = {"getPanel", "getInnerPanel", "getRows"};

    private static final Logger log = Global.getLogger(NoEffectText.class);
    private static boolean failed;

    private NoEffectText() {}

    /** The effect a building's line should name, or null where the game's own text is right. */
    static Effect shown(Industry ind) {
        MarketAPI market = ind == null ? null : ind.getMarket();
        if (market == null || !market.isPlayerOwned() || !Settings.effectsEnabled()) return null;
        Effect effect = Effect.of(ind);
        return effect == Effect.OWN || effect == Effect.NONE ? null : effect;
    }

    static boolean isOmegaNoEffect(String text) {
        if (text == null || !text.endsWith(NO_EFFECT)) return false;
        CommoditySpecAPI core = Global.getSettings().getCommoditySpec(Commodities.OMEGA_CORE);
        return core != null && text.startsWith(core.getName());
    }

    /**
     * Rewrites one of the game's "No effect" lines for this building: the whole effect if it fits,
     * else its first sentence, else the fallback (null leaves the line alone).
     */
    static Fit rewrite(LabelAPI label, Industry ind, String fallback) {
        String text = label.getText();
        if (!isOmegaNoEffect(text)) return Fit.UNTOUCHED;
        Effect effect = shown(ind);
        if (effect == null) return Fit.UNTOUCHED;

        String prefix = text.substring(0, text.length() - NO_EFFECT.length());
        String body = String.format(effect.text, (Object[]) effect.highlights);
        if (fits(label, prefix + body)) {
            set(label, prefix + body, effect.highlights);
            return Fit.FULL;
        }
        int end = body.indexOf(". ");
        if (end > 0 && fits(label, prefix + body.substring(0, end + 1))) {
            set(label, prefix + body.substring(0, end + 1), effect.highlights);
            return Fit.PARTIAL;
        }
        if (fallback == null) return Fit.UNTOUCHED;
        set(label, prefix + fallback);
        return Fit.PARTIAL;
    }

    /**
     * Rewrites the building's own tooltip while it is being made. FULL means the line now holds the
     * whole effect; anything else and the caller adds the effect as a section of its own.
     */
    static Fit rewriteTooltip(UIPanelAPI tooltip, Industry ind) {
        if (failed) return Fit.UNTOUCHED;
        try {
            List<LabelAPI> lines = new ArrayList<>();
            visit(tooltip, new ArrayList<>(), Collections.newSetFromMap(new IdentityHashMap<>()),
                    (line, path) -> lines.add(line));
            Fit result = Fit.UNTOUCHED;
            for (LabelAPI line : lines) {
                Fit fit = rewrite(line, ind, "See Omega core below.");
                if (fit == Fit.PARTIAL) result = Fit.PARTIAL;
                else if (fit == Fit.FULL && result == Fit.UNTOUCHED) result = Fit.FULL;
            }
            return result;
        } catch (RuntimeException | LinkageError e) {
            failed = true;
            log.error("Omega Core Everywhere could not read a building tooltip; the effect is shown in its own "
                    + "section from now on", e);
            return Fit.UNTOUCHED;
        }
    }

    /**
     * Hands every one of the game's "No effect" lines for an Omega core under a panel to the visitor,
     * with the panels above it (the closest last).
     */
    static void visit(UIComponentAPI node, List<UIComponentAPI> path, Set<Object> seen,
                      BiConsumer<LabelAPI, List<UIComponentAPI>> visitor) {
        if (path.size() > MAX_DEPTH || !seen.add(node)) return;
        if (node instanceof LabelAPI) {
            LabelAPI label = (LabelAPI) node;
            if (isOmegaNoEffect(label.getText())) visitor.accept(label, path);
            return;
        }
        if (!(node instanceof UIPanelAPI)) return;
        path.add(node);
        for (UIComponentAPI child : Reflect.children((UIPanelAPI) node)) visit(child, path, seen, visitor);
        for (String part : HIDDEN_PARTS) {
            Object hidden = Reflect.call(node, part);
            if (hidden instanceof UIComponentAPI) {
                visit((UIComponentAPI) hidden, path, seen, visitor);
            } else if (hidden instanceof List) {
                for (Object item : (List<?>) hidden) {
                    if (item instanceof UIComponentAPI) visit((UIComponentAPI) item, path, seen, visitor);
                }
            }
        }
        path.remove(path.size() - 1);
    }

    private static boolean fits(LabelAPI label, String text) {
        float width = label.getPosition().getWidth();
        float line = label.computeTextHeight("Omega");
        if (width <= 0f || line <= 0f) return false;
        // Height says how the label wraps it; width is a second opinion in case it does not wrap at all.
        float byHeight = label.computeTextHeight(text) / line;
        float byWidth = label.computeTextWidth(text) / (width * 0.95f);
        return Math.max(byHeight, byWidth) <= MAX_LINES + 0.01f;
    }

    private static void set(LabelAPI label, String text, String... highlights) {
        // setText keeps the label's old height, which would cut a second line off; resizing to its own
        // width lets it grow downwards into the room beside the core's icon.
        float width = label.getPosition().getWidth();
        label.setText(text);
        label.autoSizeToWidth(width);
        List<String> present = new ArrayList<>();
        for (String h : highlights) {
            if (text.contains(h)) present.add(h);
        }
        if (present.isEmpty()) return;
        label.setHighlightColor(Misc.getHighlightColor());
        label.setHighlight(present.toArray(new String[0]));
    }
}
