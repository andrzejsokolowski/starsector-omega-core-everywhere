package omegacoreeverywhere.ships;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import lunalib.lunaSettings.LunaSettings;
import omegacoreeverywhere.Safe;

/**
 * Armament Support System (Omega): the Omega tier of Special Hullmod Upgrades' Alpha, Beta and Gamma
 * core upgrades. Installing it uses up an Omega core, through the base game's item requirement for
 * hull mods, so it needs nothing from Special Hullmod Upgrades' code. It only shows up while that mod
 * is enabled ({@link #hideWithoutSpecialHullmodUpgrades()}) and is learned with its Alpha upgrade.
 *
 * It follows the same three Special Hullmod Upgrades settings as the Alpha upgrade: the deployment
 * cost increase, the incompatibility between core upgrades, and the core being lost with the ship.
 *
 * A ship made of several sections takes it on the main section only, and one Omega core covers the
 * whole ship, as with Special Hullmod Upgrades: every other section gets the hidden
 * {@link OmegaArmamentSupportModule} with the same weapon bonuses, and the hidden
 * {@link OmegaArmamentSupportLink} on the main section takes those off again once this hull mod is gone.
 *
 * Every method the game calls goes through {@link Safe}: on an error the hull mod does nothing, and
 * cannot be installed, instead of crashing the refit screen or a battle.
 */
public class OmegaArmamentSupport extends BaseHullMod {

    public static final String ID = "oce_omega_core_upgrades";
    /** On every other section of a ship that has this hull mod on its main section. */
    public static final String MODULE_ID = "oce_omega_core_module_extension";
    /** On the main section while the other sections carry {@link #MODULE_ID}. */
    public static final String LINK_ID = "oce_omega_core_link";

    public static final String SHU_MOD_ID = "mayu_specialupgrades";
    public static final String SHU_ALPHA = "specialsphmod_alpha_core_upgrades";
    /**
     * Special Hullmod Upgrades' own core upgrades, the only hull mods that keep this one off a ship
     * (that mod's own rule). Exotica's subcores do not: they are hidden from the hull mod list, so a
     * player cannot see what is in the way (OCE-9, 1.2.1). The Omega Subcore yields to this hull mod.
     */
    private static final String[][] SHU_CORES = {
            {SHU_ALPHA, "Armament Support System (Alpha)"},
            {"specialsphmod_beta_core_upgrades", "Armament Support System (Beta)"},
            {"specialsphmod_gamma_core_upgrades", "Armament Support System (Gamma)"}};

    public static final float AUTOFIRE_BONUS = 100f;
    public static final float TURRET_TURN_BONUS = 100f;
    public static final int REDUCTION_SMALL = 3;
    public static final int REDUCTION_MEDIUM = 5;
    public static final int REDUCTION_LARGE = 8;
    public static final float DEPLOYMENT_COST_INCREASE = 25f;

    public static final Color OMEGA_COLOR = new Color(180, 120, 255);

    /** Called once at startup: without Special Hullmod Upgrades this hull mod does not exist for the player. */
    public static void hideWithoutSpecialHullmodUpgrades() {
        if (Global.getSettings().getModManager().isModEnabled(SHU_MOD_ID)) return;
        Global.getSettings().getHullModSpec(ID).setHidden(true);
        Global.getSettings().getHullModSpec(ID).setHiddenEverywhere(true);
        Global.getSettings().getHullModSpec(ID).addTag("hide_in_codex");
    }

    /** A Special Hullmod Upgrades toggle, false when that mod or its setting is missing. */
    static boolean shuSetting(String field) {
        return Safe.get("reading a Special Hullmod Upgrades setting", () -> {
            if (!Global.getSettings().getModManager().isModEnabled(SHU_MOD_ID)) return false;
            Boolean value = LunaSettings.getBoolean(SHU_MOD_ID, field);
            return value != null && value;
        }, false);
    }

    static boolean coresStack() {
        return shuSetting("shu_generalUpgradesIncompatibilityToggle");
    }

    static boolean coreSurvivesLoss() {
        return shuSetting("shu_itemdestructionToggle");
    }

    private static boolean noDeploymentPenalty() {
        return shuSetting("shu_disableIncreasedDPAICoresToggle");
    }

    /**
     * The name of the core upgrade that keeps this hull mod off the ship, or null. If the ship cannot
     * be read, nothing is in the way: refusing a ship for no visible reason is worse.
     */
    private static String blocker(ShipAPI ship) {
        return Safe.get(WHERE, () -> {
            if (ship == null || ship.getVariant() == null || coresStack()) return null;
            for (String[] core : SHU_CORES) {
                if (ship.getVariant().hasHullMod(core[0])) return core[1];
            }
            return null;
        }, null);
    }

    private static final String WHERE = "Armament Support System (Omega)";

    @Override
    public CargoStackAPI getRequiredItem() {
        return Safe.get(WHERE, () -> Global.getSettings().createCargoStack(CargoItemType.RESOURCES,
                Commodities.OMEGA_CORE, null), null);
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        Safe.run(WHERE, () -> applyStats(stats, id));
    }

    private static void applyStats(MutableShipStatsAPI stats, String id) {
        applyWeaponStats(stats, id);
        if (!noDeploymentPenalty()) {
            stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyPercent(id, DEPLOYMENT_COST_INCREASE);
        }
    }

    /** What every section gets. The deployment cost belongs to the main section alone. */
    static void applyWeaponStats(MutableShipStatsAPI stats, String id) {
        stats.getAutofireAimAccuracy().modifyFlat(id, AUTOFIRE_BONUS / 100f);
        stats.getWeaponTurnRateBonus().modifyPercent(id, TURRET_TURN_BONUS);
        stats.getBeamWeaponTurnRateBonus().modifyPercent(id, TURRET_TURN_BONUS);
        reduce(stats, id, REDUCTION_SMALL, Stats.SMALL_BALLISTIC_MOD, Stats.SMALL_ENERGY_MOD, Stats.SMALL_MISSILE_MOD);
        reduce(stats, id, REDUCTION_MEDIUM, Stats.MEDIUM_BALLISTIC_MOD, Stats.MEDIUM_ENERGY_MOD, Stats.MEDIUM_MISSILE_MOD);
        reduce(stats, id, REDUCTION_LARGE, Stats.LARGE_BALLISTIC_MOD, Stats.LARGE_ENERGY_MOD, Stats.LARGE_MISSILE_MOD);
    }

    /**
     * Gives every other section the hidden copy while the main section has this hull mod, and takes
     * the copies off once it is gone. Called from the main section in the refit screen and in combat,
     * and from the campaign every second for the player's fleet.
     */
    public static void syncSections(ShipVariantAPI main) {
        if (main == null) return;
        boolean on = main.hasHullMod(ID);
        boolean sections = false;
        for (String slot : main.getModuleSlots()) {
            ShipVariantAPI section = main.getModuleVariant(slot);
            if (section == null) continue;
            sections = true;
            if (on && !section.hasHullMod(MODULE_ID)) section.addPermaMod(MODULE_ID);
            if (!on && section.hasHullMod(MODULE_ID)) section.removePermaMod(MODULE_ID);
        }
        if (on && sections && !main.hasHullMod(LINK_ID)) main.addPermaMod(LINK_ID);
        if (!on && main.hasHullMod(LINK_ID)) main.removePermaMod(LINK_ID);
    }

    /**
     * True for a section other than the main one. In the refit screen the game does not always mark
     * them, so this also asks the hull and, as Special Hullmod Upgrades does, the player's fleet: only
     * a main section is a fleet member.
     */
    static boolean isSection(ShipAPI ship) {
        if (ship.isStationModule() || ship.getParentStation() != null) return true;
        if (ship.getHullSpec() != null && ship.getHullSpec().getHints().contains(ShipTypeHints.MODULE)) return true;
        String member = ship.getFleetMemberId();
        CampaignFleetAPI fleet = Global.getSector() == null ? null : Global.getSector().getPlayerFleet();
        if (member == null || fleet == null) return false;
        for (FleetMemberAPI m : fleet.getFleetData().getMembersListCopy()) {
            if (member.equals(m.getId())) return false;
        }
        return true;
    }

    private static void reduce(MutableShipStatsAPI stats, String id, int amount, String... mods) {
        for (String mod : mods) stats.getDynamic().getMod(mod).modifyFlat(id, -amount);
    }

    /** One core runs the fire control: a Special Hullmod Upgrades core added later is taken off again. */
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        Safe.run(WHERE, () -> {
            syncSections(ship.getVariant());
            if (coresStack()) return;
            for (String[] core : SHU_CORES) {
                if (ship.getVariant().hasHullMod(core[0])) ship.getVariant().removeMod(core[0]);
            }
        });
    }

    /** Without its Omega core requirement the hull mod would be free, so then it is not offered at all. */
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if (ship == null || getRequiredItem() == null) return false;
        return blocker(ship) == null;
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        String core = blocker(ship);
        if (core != null) return "Incompatible with " + core;
        if (getRequiredItem() == null) return "The Omega core it needs could not be found";
        return null;
    }

    /** Not added to another section, but one already there (put on before 1.2.2) can still come off. */
    private static boolean blockedSection(ShipAPI ship) {
        return ship != null && ship.getVariant() != null && !ship.getVariant().hasHullMod(ID) && isSection(ship);
    }

    @Override
    public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
        return Safe.get(WHERE, () -> {
            if (blockedSection(ship)) return false;
            return super.canBeAddedOrRemovedNow(ship, marketOrNull, mode);
        }, false);
    }

    @Override
    public String getCanNotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
        return Safe.get(WHERE, () -> {
            if (blockedSection(ship)) return "Can only be installed on the main section";
            return super.getCanNotBeInstalledNowReason(ship, marketOrNull, mode);
        }, null);
    }

    @Override
    public boolean affectsOPCosts() {
        return true;
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "Omega core";
        return null;
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width,
                                          boolean isForModSpec) {
        Safe.run(WHERE, () -> describe(tooltip));
    }

    private static void describe(TooltipMakerAPI tooltip) {
        float pad = 3f;
        float opad = 10f;
        Color good = Misc.getPositiveHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        tooltip.addSectionHeading("Technical details", Alignment.MID, opad);
        tooltip.addPara("Improves autofire accuracy by %s", opad, good, Math.round(AUTOFIRE_BONUS) + "%");
        tooltip.addPara("Increases turret turn rate by %s", pad, good, Math.round(TURRET_TURN_BONUS) + "%");
        tooltip.addPara("Reduces the OP cost of every weapon, missiles included, by %s/%s/%s", pad, good,
                "" + REDUCTION_SMALL, "" + REDUCTION_MEDIUM, "" + REDUCTION_LARGE);
        if (!noDeploymentPenalty()) {
            tooltip.addPara("Increases deployment cost by %s", pad, bad, Math.round(DEPLOYMENT_COST_INCREASE) + "%");
        }
        if (!coresStack()) {
            tooltip.addPara("Not compatible with %s, %s, %s", opad, bad,
                    "Armament Support System (Alpha)", "(Beta)", "(Gamma)");
        }
        if (!coreSurvivesLoss()) {
            tooltip.addPara("The Omega core is lost if the ship is disabled or destroyed in combat.",
                    Misc.getGrayColor(), opad);
        }
    }

    @Override
    public Color getNameColor() {
        return OMEGA_COLOR;
    }

    @Override
    public Color getBorderColor() {
        return OMEGA_COLOR;
    }
}
