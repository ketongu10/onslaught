package onslaught.ketongu10.war;

import electroblob.wizardry.entity.living.EntityEvilWizard;
import onslaught.ketongu10.Onslaught;
import onslaught.ketongu10.capabilities.units.ProviderEntityUnits;
import onslaught.ketongu10.capabilities.units.UnitCapability;
import onslaught.ketongu10.config.FactionUnitsConfig;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityHusk;
import net.minecraft.entity.monster.EntityWitch;
import net.shadowmage.ancientwarfare.npc.entity.faction.*;

import java.util.Arrays;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static onslaught.ketongu10.util.handlers.ConfigHandler.*;

public class BossList {
    public static Map<Class<? extends EntityLiving>, BossParameters> classBossList = new HashMap<>();
    public static Map<String, List<String>> bossNames = new HashMap<>();

    public static void setBossList() {
        classBossList.put(EntityHusk.class, new BossParameters("Evil", "minecraft:textures/items/bone.png"));
        classBossList.put(EntityWitch.class, new BossParameters("Witches", "minecraft:textures/items/bone.png"));
        if (ANCIENT_WARFARE) {
            classBossList.put(NpcFactionSpellcasterWizardry.class, new BossParameters("AW"));
            classBossList.put(NpcFactionLeader.class, new BossParameters("AW"));
            classBossList.put(NpcFactionLeaderElite.class, new BossParameters("AW"));
            bossNames.put("AWorc", Arrays.asList("Da boss Arch-Killa", "Da boss Nazdreg", "Gutrak Dethhead", "Mogdos Gilt-Toof", "Da boss Drogg"));
        }

        if (EBWIZARDRY) {
            classBossList.put(EntityEvilWizard.class, new BossParameters("Wizards", "onslaught:textures/gui/hat_icon.png", "element", "4", "Necromancer"));
            bossNames.put("Necromancer", Arrays.asList("The Necromancer"));
            bossNames.put("Wizards", Arrays.asList("The great wizard Ogast", "The great wizard Uxium", "The great wizard Ilveshan", "The great wizard Qamarium", "The great wizard Cergon", "The great wizard Inorium", "The great wizard Alzahagan"));
        }
        addCustomBosses();

    }

    public static void addCustomBosses() {
        for (FactionUnitsConfig.BossConfig config: FactionUnitsConfig.getBossConfigs()) {
            try {
                String bossName = config.registryName;
                Class clazz = EntityList.getClassFromName(bossName);

                if (!classBossList.containsKey(clazz)) {
                    BossParameters par = new BossParameters(config.factionName);
                    if (config.icon != null) {par.icon = config.icon;}
                    if (config.tag != null) {par.tags.put(config.tag, new CustomBoss(config.tagValue, config.customFaction));}
                    if (!ProviderEntityUnits.capabilityMap.containsKey(clazz)) {ProviderEntityUnits.capabilityMap.put(clazz, UnitCapability::new);}
                    classBossList.put(clazz, par);
                } else {
                    if (config.tag != null ) {
                        if (!classBossList.get(clazz).tags.containsKey(config.tag)) {
                            classBossList.get(clazz).tags.put(config.tag, new CustomBoss(config.tagValue, config.customFaction));
                        }
                    }
                }
            } catch (Exception e) {
                Onslaught.LOGGER.warn("Failed to load custom boss " + config.registryName);
                System.err.println(e);
            }
        }
    }

    public static class BossParameters {
        public String faction;
        public String icon = "default";
        public Map<String, CustomBoss> tags = new HashMap<>();


        public BossParameters(String fac) {
            this.faction = fac;
        }

        public BossParameters(String fac, String str) {
            this.faction = fac;
            this.icon = str;

        }
        public BossParameters(String fac, String str, String tag, String value, String customFac) {
            this.faction = fac;
            this.icon = str;
            if (!this.tags.containsKey(tag)) {
                this.tags.put(tag, new CustomBoss(value, customFac));
            }

        }
    }
    public static class CustomBoss {
        public String value;
        public String faction;
        public CustomBoss(String v, String f) {
            value = v;
            faction = f;
        }
    }



}
