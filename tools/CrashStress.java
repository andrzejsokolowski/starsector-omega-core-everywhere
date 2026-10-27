import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.AICores;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Misc;

/**
 * Calls every place the game can call into Omega Core Everywhere, outside the game, with the game in
 * three broken states. Nothing may escape: an escaping throwable is what would crash the game.
 *   none:   no game at all (Global.getSector() and getSettings() are null)
 *   blank:  a game whose every call answers a blank (empty lists, zeros, more blank objects)
 *   broken: a game whose every call throws
 * Usage: CrashStress exotics.json|-   (with Exotica on the class path when a file is given)
 *
 * Run it before every release, once without Exotica and once with each Exotica version (see
 * exotica_compat_check.py for where to get them). Compile against build/classes/java/main, the
 * game's starsector-core jars and LunaLib; run on the game's own Java with the same class path
 * (plus Exotica, LazyLib and its Kotlin-Runtime.jar for the exotics). A new place the game calls
 * into the mod gets a line here.
 */
public class CrashStress {
    static int calls, escaped;
    static boolean throwing;

    public static void main(String[] args) throws Exception {
        String exotics = args.length > 0 ? args[0] : "-";
        for (String mode : new String[] {"none", "blank", "broken"}) {
            throwing = mode.equals("broken");
            if (mode.equals("none")) {
                Global.setSettings(null);
                Global.setSector(null);
            } else {
                Global.setSettings((SettingsAPI) deep(SettingsAPI.class));
                Global.setSector((SectorAPI) deep(SectorAPI.class));
            }
            int before = escaped;
            runAll(exotics);
            int now = escaped - before;
            System.out.println("== " + mode + ": " + (now == 0 ? "nothing escaped" : now + " ESCAPED"));
        }
        System.out.println(calls + " calls, " + (escaped == 0 ? "ALL SAFE" : escaped + " WOULD CRASH THE GAME"));
        System.exit(escaped == 0 ? 0 : 1);
    }

    static void runAll(String exotics) throws Exception {
        // Mod plugin
        Object plugin = Class.forName("omegacoreeverywhere.OmegaCoreEverywherePlugin").getConstructor().newInstance();
        for (String hook : new String[] {"onApplicationLoad", "beforeGameSave", "afterGameSave", "onGameSaveFailed"}) {
            call("plugin." + hook, () -> plugin.getClass().getMethod(hook).invoke(plugin));
        }
        call("plugin.onGameLoad", () -> plugin.getClass().getMethod("onGameLoad", boolean.class).invoke(plugin, false));
        Class<?> listener = Class.forName("omegacoreeverywhere.OmegaCoreEverywherePlugin$SettingsListener");
        Object settingsListener = make(listener);
        Method changed = open(listener.getMethod("settingsChanged", String.class));
        call("settingsChanged", () -> changed.invoke(settingsListener, "omega_core_everywhere"));

        // Settings
        Class<?> settings = Class.forName("omegacoreeverywhere.Settings");
        for (String m : new String[] {"effectsEnabled", "captainTimeflow"}) {
            call("Settings." + m, () -> settings.getMethod(m).invoke(null));
        }

        // Every-frame script, economy listener, save hooks
        Class<?> effects = Class.forName("omegacoreeverywhere.effects.OmegaEffects");
        Object script = null;
        try {
            script = make(effects);
        } catch (InvocationTargetException e) {
            System.out.println("  (the effects script cannot even be built here: " + e.getCause() + ")");
        }
        if (script != null) {
            Field current = effects.getDeclaredField("current");
            current.setAccessible(true);
            current.set(null, script);
            Object s = script;
            Method advance = effects.getMethod("advance", float.class);
            call("advance(1)", () -> advance.invoke(s, 1f));
            call("advance(0)", () -> advance.invoke(s, 0f));
            call("reportEconomyTick", () -> effects.getMethod("reportEconomyTick", int.class).invoke(s, 0));
            call("reportEconomyMonthEnd", () -> effects.getMethod("reportEconomyMonthEnd").invoke(s));
            call("advance after a failure", () -> advance.invoke(s, 1f));
        }
        call("plugin.beforeGameSave again", () -> plugin.getClass().getMethod("beforeGameSave").invoke(plugin));
        call("plugin.afterGameSave again", () -> plugin.getClass().getMethod("afterGameSave").invoke(plugin));
        Class<?> growth = Class.forName("omegacoreeverywhere.effects.OmegaEffects$Growth");
        Object g = make(growth);
        Method modifyIncoming = open(growth.getMethod("modifyIncoming", MarketAPI.class, PopulationComposition.class));
        call("growth", () -> modifyIncoming.invoke(g, deep(MarketAPI.class), null));
        call("growth with a colony", () -> modifyIncoming.invoke(g, deep(MarketAPI.class), new PopulationComposition()));

        // Building tooltip and core picker
        Class<?> tooltip = Class.forName("omegacoreeverywhere.effects.OmegaTooltip");
        Object t = make(tooltip);
        Method addTooltip = open(tooltip.getMethod("addToIndustryTooltip", Industry.class, Industry.IndustryTooltipMode.class,
                TooltipMakerAPI.class, float.class, boolean.class));
        // The game's Industry cannot be faked outside the game (its proxy class fails to load), so no building.
        for (Industry.IndustryTooltipMode mode : Industry.IndustryTooltipMode.values()) {
            call("tooltip " + mode, () -> addTooltip.invoke(t, null, mode, deep(TooltipMakerAPI.class), 300f, true));
        }
        Class<?> picker = Class.forName("omegacoreeverywhere.effects.CorePickerText");
        Object p = make(picker);
        Method render = open(picker.getMethod("renderInUICoordsAboveUIBelowTooltips", ViewportAPI.class));
        call("core picker", () -> render.invoke(p, deep(ViewportAPI.class)));

        // Skills, in combat
        for (String skill : new String[] {"omegacoreeverywhere.skills.OmegaTimeflow$Level1",
                "omegacoreeverywhere.skills.OmegaTutelage$Level1"}) {
            Object e = Class.forName(skill).getConstructor().newInstance();
            Object stats = deep(MutableShipStatsAPI.class);
            Method apply = e.getClass().getMethod("apply", MutableShipStatsAPI.class, HullSize.class, String.class, float.class);
            Method unapply = e.getClass().getMethod("unapply", MutableShipStatsAPI.class, HullSize.class, String.class);
            call(skill + ".apply", () -> apply.invoke(e, stats, HullSize.CRUISER, "id", 1f));
            call(skill + ".apply(null)", () -> apply.invoke(e, null, null, null, 1f));
            call(skill + ".unapply", () -> unapply.invoke(e, stats, HullSize.CRUISER, "id"));
            call(skill + ".describe", () -> e.getClass().getMethod("getEffectDescription", float.class).invoke(e, 1f));
        }

        // Hull mods, in the refit screen and in combat
        for (String mod : new String[] {"omegacoreeverywhere.ships.OmegaArmamentSupport",
                "omegacoreeverywhere.ships.OmegaSubcoreHullmod"}) {
            Object h = Class.forName(mod).getConstructor().newInstance();
            Class<?> c = h.getClass();
            for (Object s : new Object[] {deep(MutableShipStatsAPI.class), null}) {
                call(mod + ".applyBefore", () -> c.getMethod("applyEffectsBeforeShipCreation", HullSize.class,
                        MutableShipStatsAPI.class, String.class).invoke(h, HullSize.CRUISER, s, "id"));
            }
            for (Object sh : new Object[] {deep(ShipAPI.class), null}) {
                call(mod + ".applyAfter", () -> c.getMethod("applyEffectsAfterShipCreation", ShipAPI.class, String.class).invoke(h, sh, "id"));
                call(mod + ".isApplicable", () -> c.getMethod("isApplicableToShip", ShipAPI.class).invoke(h, sh));
                call(mod + ".unapplicableReason", () -> c.getMethod("getUnapplicableReason", ShipAPI.class).invoke(h, sh));
                call(mod + ".canAddRemove", () -> c.getMethod("canBeAddedOrRemovedNow", ShipAPI.class, MarketAPI.class,
                        CoreUITradeMode.class).invoke(h, sh, deep(MarketAPI.class), CoreUITradeMode.OPEN));
                call(mod + ".cannotReason", () -> c.getMethod("getCanNotBeInstalledNowReason", ShipAPI.class, MarketAPI.class,
                        CoreUITradeMode.class).invoke(h, sh, null, null));
                call(mod + ".postDescription", () -> c.getMethod("addPostDescriptionSection", TooltipMakerAPI.class,
                        HullSize.class, ShipAPI.class, float.class, boolean.class)
                        .invoke(h, deep(TooltipMakerAPI.class), HullSize.CRUISER, sh, 300f, false));
            }
            call(mod + ".requiredItem", () -> c.getMethod("getRequiredItem").invoke(h));
            call(mod + ".descriptionParam", () -> c.getMethod("getDescriptionParam", int.class, HullSize.class).invoke(h, 0, HullSize.CRUISER));
        }

        // Battle listener
        Class<?> loss = Class.forName("omegacoreeverywhere.ships.OmegaCoreLoss");
        Object l = make(loss);
        Method engagement = loss.getMethod("reportPlayerEngagement", EngagementResultAPI.class);
        call("battle", () -> engagement.invoke(l, deep(EngagementResultAPI.class)));
        call("battle(null)", () -> engagement.invoke(l, (Object) null));

        // Turn-in rule
        Object turnIn = Class.forName("omegacoreeverywhere.rulecmd.OCE_OmegaTurnIn").getConstructor().newInstance();
        Method exec = turnIn.getClass().getMethod("execute", String.class, InteractionDialogAPI.class, List.class, Map.class);
        call("turn-in check", () -> exec.invoke(turnIn, "r", deep(InteractionDialogAPI.class),
                List.of(new Misc.Token("playerHasOmegaCores", Misc.TokenType.LITERAL)), new HashMap<>()));
        call("turn-in check, no params", () -> exec.invoke(turnIn, "r", null, new ArrayList<>(), new HashMap<>()));
        Method rep = AICores.class.getDeclaredMethod("computeCoreReputationValue", CargoAPI.class);
        rep.setAccessible(true);
        call("turn-in reputation", () -> rep.invoke(turnIn, deep(CargoAPI.class)));

        // Exotica exotics
        if (exotics.equals("-")) return;
        StringBuilder sb = new StringBuilder();
        for (String line : java.nio.file.Files.readAllLines(java.nio.file.Path.of(exotics))) {
            if (!line.trim().startsWith("#")) sb.append(line).append('\n');
        }
        JSONObject all = new JSONObject(sb.toString());
        Iterator<?> keys = all.keys();
        Class<?> member = FleetMemberAPI.class;
        Class<?> fleet = CampaignFleetAPI.class;
        Class<?> market = MarketAPI.class;
        Class<?> mods = Class.forName("exoticatechnologies.modifications.ShipModifications");
        Class<?> data = Class.forName("exoticatechnologies.modifications.exotics.ExoticData");
        while (keys.hasNext()) {
            String key = (String) keys.next();
            JSONObject cfg = all.getJSONObject(key);
            Object ex = Class.forName(cfg.getString("exoticClass")).getConstructor(String.class, JSONObject.class).newInstance(key, cfg);
            Class<?> c = ex.getClass();
            Object m = deep(member), f = deep(fleet), mk = deep(market);
            // Only the methods this mod writes: the rest is Exotica's own code, which Exotica calls
            // with real objects.
            Set<String> own = new HashSet<>();
            for (Method dm : c.getDeclaredMethods()) own.add(dm.getName());
            if (own.contains("shouldShow")) call(key + ".shouldShow", () -> c.getMethod("shouldShow", member, mods, market).invoke(ex, m, null, mk));
            if (own.contains("canAfford")) call(key + ".canAfford", () -> c.getMethod("canAfford", fleet, market).invoke(ex, f, mk));
            if (own.contains("removeItemsFromFleet")) call(key + ".removeItems", () -> c.getMethod("removeItemsFromFleet", fleet, member, market).invoke(ex, f, m, mk));
            if (own.contains("getResourceCostMap")) call(key + ".cost", () -> c.getMethod("getResourceCostMap", member, mods, market).invoke(ex, m, null, mk));
            if (own.contains("canApply")) call(key + ".canApply", () -> c.getMethod("canApply", member, mods).invoke(ex, m, null));
            if (own.contains("getExtraBandwidth")) call(key + ".bandwidth", () -> c.getMethod("getExtraBandwidth", member, mods, data).invoke(ex, m, null, null));
            if (own.contains("modifyToolTip")) call(key + ".tooltip", () -> c.getMethod("modifyToolTip", TooltipMakerAPI.class, UIComponentAPI.class, member,
                    mods, data, boolean.class).invoke(ex, deep(TooltipMakerAPI.class), deep(UIComponentAPI.class), m, null, null, true));
            if (own.contains("onInstall")) call(key + ".onInstall", () -> c.getMethod("onInstall", member).invoke(ex, m));
            if (own.contains("onDestroy")) call(key + ".onDestroy", () -> c.getMethod("onDestroy", member).invoke(ex, m));
            if (own.contains("advanceInCampaign")) call(key + ".advanceInCampaign", () -> c.getMethod("advanceInCampaign", member, mods, float.class, data)
                    .invoke(ex, m, null, 1f, null));
        }
        call("hasOmegaSubcore", () -> Class.forName("omegacoreeverywhere.exotica.OmegaExotics")
                .getMethod("hasOmegaSubcore", member).invoke(null, deep(member)));
    }

    interface Body { void run() throws Throwable; }

    static void call(String what, Body body) {
        calls++;
        try {
            body.run();
        } catch (InvocationTargetException e) {
            escaped++;
            System.out.println("  ESCAPED from " + what + ": " + e.getCause());
        } catch (Throwable t) {
            escaped++;
            System.out.println("  ESCAPED from " + what + ": " + t);
        }
    }

    static Method open(Method m) {
        m.setAccessible(true);
        return m;
    }

    static Object make(Class<?> type) throws Exception {
        Constructor<?> c = type.getDeclaredConstructor();
        c.setAccessible(true);
        return c.newInstance();
    }

    static Object deep(Class<?> type) {
        return Proxy.newProxyInstance(CrashStress.class.getClassLoader(), new Class<?>[] {type}, (proxy, m, a) -> {
            if (m.getDeclaringClass() == Object.class) {
                switch (m.getName()) {
                    case "hashCode": return System.identityHashCode(proxy);
                    case "equals": return proxy == a[0];
                    default: return "blank " + type.getSimpleName();
                }
            }
            if (throwing) throw new IllegalStateException("broken game: " + type.getSimpleName() + "." + m.getName());
            return blank(m.getReturnType());
        });
    }

    static Object blank(Class<?> r) {
        if (r == void.class) return null;
        if (r == boolean.class) return false;
        if (r == float.class) return 0f;
        if (r == double.class) return 0d;
        if (r == int.class) return 0;
        if (r == long.class) return 0L;
        if (r == short.class) return (short) 0;
        if (r == byte.class) return (byte) 0;
        if (r == char.class) return (char) 0;
        if (r == String.class) return "";
        if (List.class.isAssignableFrom(r) || r == Collection.class || r == Iterable.class) return new ArrayList<>();
        if (Set.class.isAssignableFrom(r)) return new HashSet<>();
        if (Map.class.isAssignableFrom(r)) return new HashMap<>();
        if (r.isArray()) return Array.newInstance(r.getComponentType(), 0);
        if (r.isInterface()) return deep(r);
        return null;
    }
}
