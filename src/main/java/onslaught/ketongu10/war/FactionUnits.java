package onslaught.ketongu10.war;

import electroblob.wizardry.entity.living.EntityWizard;
import onslaught.ketongu10.Onslaught;
import onslaught.ketongu10.capabilities.units.ProviderEntityUnits;
import onslaught.ketongu10.capabilities.units.UnitCapability;
import onslaught.ketongu10.config.FactionUnitsConfig;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.shadowmage.ancientwarfare.npc.entity.NpcPlayerOwned;
import net.shadowmage.ancientwarfare.structure.entity.EntityGate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static onslaught.ketongu10.util.handlers.ConfigHandler.*;

public class FactionUnits {

    public enum UnitType {
        TROOPS(0), ELITE(1), CAVALERY(2), ARCHERS(3), SENTRIES(4), HQ(5), HS(6) ,DEMOLISHERS(7);
        final int id;

        UnitType(int id)
        {
            this.id = id;
        }

        public int getId()
        {
            return id;
        }
        public static UnitType getTypeById(int i) {
            switch (i) {
                case 0: return TROOPS;
                case 1: return ELITE;
                case 2: return CAVALERY;
                case 3: return ARCHERS;
                case 4: return SENTRIES;
                case 5: return HQ;
                case 6: return HS;
                case 7: return DEMOLISHERS;
                default: return TROOPS;
            }
        }
    }

    public static List<Class <? extends Entity>> PlayerSoldiers = new ArrayList<>();
    public static Map<String, WarProperties> FactionSoldiers = new HashMap<>();
    public static Map<String, Vehicles> Civilised = new HashMap<>();
    public static void fillMap() {

        FactionSoldiers.put("Evil", new WarProperties(
                "minecraft:zombie",
                "minecraft:wither_skeleton",
                "minecraft:zombie",
                "minecraft:stray",
                "minecraft:skeleton",
                "minecraft:skeleton",
                "minecraft:blaze",
                "minecraft:husk",
                "minecraft:husk",
                "minecraft:zombie"));
        FactionSoldiers.put("Witches", new WarProperties(false, War.WarType.AMBUSH, TOTAL_AMBUSH_TIME,new Warrior("minecraft:witch")));

        if (EBWIZARDRY) {
            FactionSoldiers.put("Wizards", new WarProperties("ebwizardry:evil_wizard", "ebwizardry:evil_wizard", "ebwizardry:evil_wizard"));
            PlayerSoldiers.add(EntityWizard.class);

            if (ANCIENT_SPELLCRAFT) {
                FactionSoldiers.put("Necromancer", new WarProperties(true,War.WarType.SIEGE, TIME_TO_SIEGE,
                        new Warrior("ebwizardry:zombie_minion"),
                        new Warrior("ebwizardry:zombie_minion"),
                        new Warrior("ebwizardry:zombie_minion"),
                        new Warrior("ancientspellcraft:skeleton_mage_minion"),
                        new Warrior("ancientspellcraft:skeleton_mage_minion"),
                        new Warrior("ebwizardry:skeleton_minion"),
                        new Warrior("ancientspellcraft:skeleton_mage_minion"),
                        new Warrior("ancientspellcraft:skeleton_mage_minion"),
                        new Warrior("ebwizardry:evil_wizard", "{element:4, CustomName:\"Necromancer\"}"),
                        new Warrior("ancientspellcraft:skeleton_mage_minion")));
            }
        }

        if (/**Loader.isModLoaded("ancientwarfare")**/ ANCIENT_WARFARE) {
            FactionSoldiers.put("AW", new WarProperties(false, War.WarType.SIEGE, TIME_TO_SIEGE,
                    new Warrior("ancientwarfarenpc:faction.soldier"),
                    new Warrior("ancientwarfarenpc:faction.soldier.elite"),
                    new Warrior("ancientwarfarenpc:faction.soldier.elite"),
                    new Warrior("ancientwarfarenpc:faction.mounted.cavalry"),
                    new Warrior("ancientwarfarenpc:faction.archer"),
                    new Warrior("ancientwarfarenpc:faction.archer.elite"),
                    new Warrior("ancientwarfarenpc:faction.spellcaster"),
                    new Warrior("ancientwarfarenpc:faction.leader"),
                    new Warrior("ancientwarfarenpc:faction.leader.elite"),
                    new Warrior("ancientwarfarenpc:faction.siege_engineer")));

            PlayerSoldiers.add(NpcPlayerOwned.class);
            PlayerSoldiers.add(EntityGate.class);

            Civilised.put("empire", new Vehicles(12, 16, 4));
            Civilised.put("nogg", new Vehicles(2, 16, 3));
            Civilised.put("dwarf", new Vehicles(11, 11, 4));
            Civilised.put("pirate", new Vehicles(11, 11, 2));
            Civilised.put("xoltec", new Vehicles(2, 12, 2));
            Civilised.put("sarconid", new Vehicles(2, 12, 2));
            Civilised.put("norska", new Vehicles(2, 2, 2));

        }

        addCustomFactions();
        addCustomPlayerSoldiers();


        PlayerSoldiers.add(EntityPlayer.class);
        PlayerSoldiers.add(EntityAgeable.class);
        PlayerSoldiers.add(EntityIronGolem.class);
        PlayerSoldiers.add(EntityVillager.class);
    }

    public static void addCustomFactions() {
        for (FactionUnitsConfig.FactionConfig config: FactionUnitsConfig.getFactionConfigs()) {
            try {
                String faction = config.factionName;
                if (!FactionSoldiers.containsKey(faction)) {
                    WarProperties members = new WarProperties(config.nightFignt, config.warType, WarProperties.getDelay(config.warType),
                            new Warrior(config.soldier.registryName, config.soldier.tags),
                            new Warrior(config.elite.registryName, config.elite.tags),
                            new Warrior(config.demolisher.registryName, config.demolisher.tags),
                            new Warrior(config.cavalry.registryName, config.cavalry.tags),
                            new Warrior(config.archer.registryName, config.archer.tags),
                            new Warrior(config.sentry.registryName, config.sentry.tags),
                            new Warrior(config.wizard.registryName, config.wizard.tags),
                            new Warrior(config.hq.registryName, config.hq.tags),
                            new Warrior(config.warlord.registryName, config.warlord.registryName),
                            new Warrior(config.hs.registryName, config.hs.tags));
                    FactionSoldiers.put(faction, members);
                }
            } catch (Exception e) {
                Onslaught.LOGGER.warn("Failed to load custom faction " + config.factionName);
                System.err.println(e);
            }
        }
    }

    public static void addCustomPlayerSoldiers() {
        for (FactionUnitsConfig.SoldierConfig soldier: FactionUnitsConfig.getAlliesConfigs()) {
            try {
                String name = soldier.registryName;
                Class clazz = EntityList.getClassFromName(name);

                if (clazz != null && !PlayerSoldiers.contains(clazz)) {
                    PlayerSoldiers.add(clazz);
                }
            } catch (Exception e) {
                Onslaught.LOGGER.warn("Failed to load custom boss " + soldier.registryName);
                System.err.println(e);
            }
        }
    }


    public static class WarProperties {
        public boolean night = false;
        public War.WarType type = War.WarType.PATROL;
        public int delay = TIME_TO_PATROL;
        public Warrior soldier;
        public Warrior elite;
        public Warrior demolishers;
        public Warrior cavalry;
        public Warrior archer;
        public Warrior sentry;
        public Warrior wizard;
        public Warrior hq;
        public Warrior hs;
        public Warrior warlord;

        private static int getDelay(War.WarType typ) {
            if (typ == War.WarType.SIEGE) {
                return TIME_TO_SIEGE;
            }
            if (typ == War.WarType.AMBUSH) {
                return TOTAL_AMBUSH_TIME;
            }
            return TIME_TO_PATROL;
        }

        public WarProperties(String soldier, String elite, String demolishers,String cavalery,String archer, String sentry, String wizard, String hq, String warlord, String hs) {

            this.soldier = new Warrior(soldier);
            this.elite = new Warrior(elite);
            this.demolishers = new Warrior(demolishers);
            this.cavalry = new Warrior(cavalery);
            this.archer = new Warrior(archer);
            this.sentry = new Warrior(sentry);
            this.wizard = new Warrior(wizard);
            this.hq = new Warrior(hq);
            this.hs = new Warrior(hs);
            this.warlord = new Warrior(warlord);

            if (soldier != null && !ProviderEntityUnits.capabilityMap.containsKey(EntityList.getClassFromName(soldier))) {
                ProviderEntityUnits.capabilityMap.put((Class<? extends EntityLiving>) EntityList.getClassFromName(soldier), UnitCapability::new);
            }
            if (elite != null && !ProviderEntityUnits.capabilityMap.containsKey(EntityList.getClassFromName(elite))) {
                ProviderEntityUnits.capabilityMap.put((Class<? extends EntityLiving>) EntityList.getClassFromName(elite), UnitCapability::new);
            }
            if (demolishers != null && !ProviderEntityUnits.capabilityMap.containsKey(EntityList.getClassFromName(demolishers))) {
                ProviderEntityUnits.capabilityMap.put((Class<? extends EntityLiving>) EntityList.getClassFromName(demolishers), UnitCapability::new);
            }
            /*if (cavalery != null && !ProviderEntityUnits.capabilityMap.containsKey(EntityList.getClassFromName(cavalery))) {
                ProviderEntityUnits.capabilityMap.put((Class<? extends EntityLiving>) EntityList.getClassFromName(cavalery), UnitCapability::new);
            }**/
            if (archer != null && !ProviderEntityUnits.capabilityMap.containsKey(EntityList.getClassFromName(archer))) {
                ProviderEntityUnits.capabilityMap.put((Class<? extends EntityLiving>) EntityList.getClassFromName(archer), UnitCapability::new);
            }
            if (sentry != null && !ProviderEntityUnits.capabilityMap.containsKey(EntityList.getClassFromName(sentry))) {
                ProviderEntityUnits.capabilityMap.put((Class<? extends EntityLiving>) EntityList.getClassFromName(sentry), UnitCapability::new);
            }
            if (wizard != null && !ProviderEntityUnits.capabilityMap.containsKey(EntityList.getClassFromName(wizard))) {
                ProviderEntityUnits.capabilityMap.put((Class<? extends EntityLiving>) EntityList.getClassFromName(wizard), UnitCapability::new);
            }
            if (hq != null && !ProviderEntityUnits.capabilityMap.containsKey(EntityList.getClassFromName(hq))) {
                ProviderEntityUnits.capabilityMap.put((Class<? extends EntityLiving>) EntityList.getClassFromName(hq), UnitCapability::new);
            }
            if (warlord != null && !ProviderEntityUnits.capabilityMap.containsKey(EntityList.getClassFromName(warlord))) {
                ProviderEntityUnits.capabilityMap.put((Class<? extends EntityLiving>) EntityList.getClassFromName(warlord), UnitCapability::new);
            }
            if (hs != null && !ProviderEntityUnits.capabilityMap.containsKey(EntityList.getClassFromName(hs))) {
                ProviderEntityUnits.capabilityMap.put((Class<? extends EntityLiving>) EntityList.getClassFromName(hs), UnitCapability::new);
            }
        }

        public WarProperties(String soldier, String elite,  String warlord) {
            this(soldier, elite, null, null, null, null, null, null, warlord, null);
        }

        public WarProperties(boolean nightFight, War.WarType typ, int delay, Warrior soldier, Warrior elite, Warrior demolishers, Warrior cavalery, Warrior archer, Warrior sentry, Warrior wizard, Warrior hq, Warrior warlord, Warrior hs) {
            this(soldier.name, elite.name, demolishers.name, cavalery.name, archer.name, sentry.name, wizard.name, hq.name, warlord.name, hs.name);
            this.soldier = soldier;
            this.elite = elite;
            this.demolishers = demolishers;
            this.archer = archer;
            this.cavalry = cavalery;
            this.sentry = sentry;
            this.hq = hq;
            this.hs = hs;
            this.wizard = wizard;
            this.warlord = warlord;
            this.night = nightFight;
            this.delay = delay;
            this.type = typ;
        }
        public WarProperties(boolean nightFight, War.WarType typ, int delay, Warrior soldier) {
            this(soldier.name, soldier.name, null, null, null, null,null,null,null,null);
            this.night = nightFight;
            this.delay = delay;
            this.type = typ;
        }
    }
    public static class Warrior {
        public String name;
        public String tags;
        public Warrior(String n, String t) {
            this.name = n;
            this.tags = t;
        }
        public Warrior(String n) {
            this.name = n;
            this.tags = null;
        }
    }
    public static class Vehicles {
        /**
         * light & heavy:
         * CATAPULT_STAND_FIXED = 0
         * CATAPULT_MOBILE_FIXED = 2
         * CANNON_MOBILE_FIXED = 11
         * HWACHA = 12
         * TREBUCHET_LARGE = 16

         * level: 4 - iron, 2 - wood
         */
        public int light;
        public int heavy;
        public int level;
        public Vehicles(int light, int heavy, int level) {
            this.light = light;
            this.heavy = heavy;
            this.level = level;
        }
    }

}
