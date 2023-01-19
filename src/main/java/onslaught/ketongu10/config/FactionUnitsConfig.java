package onslaught.ketongu10.config;

import com.google.common.collect.Lists;
import net.minecraftforge.fml.common.Loader;
import onslaught.ketongu10.util.Reference;
import onslaught.ketongu10.war.War;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.IConfigElement;

import javax.annotation.Nullable;
import java.util.List;

@Config(modid = Reference.MOD_ID, name = Reference.NAME+" Units", category="custom")
public class FactionUnitsConfig {

    @Config.Name("Boss List")
    public static CustomBossConfig bossConfig = new CustomBossConfig();
    @Config.Name("Unit List")
    public static CustomUnitConfig unitConfig = new CustomUnitConfig();
    @Config.Name("Player Entity List")
    public static CustomAlliesConfig playerConfig = new CustomAlliesConfig();

    public static class CustomUnitConfig {
        @Config.Name("custom_faction")
        public FactionConfig sampleFaction = new FactionConfig();
    }

    public static class CustomAlliesConfig {
        @Config.Name("friendly_soldier")
        public SoldierConfig allies = new SoldierConfig();
    }

    public static class CustomBossConfig {
        @Config.Name("custom_boss")
        public BossConfig sampleBoss = new BossConfig();
    }



    public static class FactionConfig {
        @Config.Name("faction")
        public String factionName = "Angry guys";
        @Config.Name("soldier")
        public SoldierConfig soldier = new SoldierConfig();
        @Config.Name("elite")
        public SoldierConfig elite = new SoldierConfig();
        @Config.Name("demolisher")
        public SoldierConfig demolisher = new SoldierConfig();
        @Config.Name("archer")
        public SoldierConfig archer = new SoldierConfig();
        @Config.Name("cavalry")
        public SoldierConfig cavalry = new SoldierConfig();
        @Config.Name("leader")
        public SoldierConfig hq = new SoldierConfig();
        @Config.Name("sentry")
        public SoldierConfig sentry = new SoldierConfig();
        @Config.Name("wizard")
        public SoldierConfig wizard = new SoldierConfig();
        @Config.Name("warlord")
        public SoldierConfig warlord = new SoldierConfig();
        @Config.Name("heavy support")
        public SoldierConfig hs = new SoldierConfig();
        @Config.Name("bossNames")
        public String[] names = {"The boss"};
        @Config.Name("nightFight")
        public boolean nightFignt = false;
        @Config.Name("war_level")
        public War.WarType warType = War.WarType.PATROL;


    }

    public static class SoldierConfig {
        @Config.Name("registry_name")
        public String registryName = "modid:registryname";
        @Config.Name("nbt_tags")
        public String tags = "{CustomName:\"Super Guy\"}";
    }

    public static class BossConfig {
        @Config.Name("registry_name")
        public String registryName = "modid:registryname";
        @Config.Name("faction")
        public String factionName = "Angry guys";
        @Config.Name("icon")
        public String icon = "minecraft:textures/items/bone.png";
        @Config.Name("custom_tag")
        public String tag = "CustomName";
        @Config.Name("tag_value")
        public String tagValue = "Wild Bob";
        @Config.Name("custom_faction")
        public String customFaction = "Bob's brothers";
    }




    public static List<BossConfig> getBossConfigs() {
        List<BossConfig> list = Lists.<BossConfig>newArrayList();
        IConfigElement root = ConfigElement.from(FactionUnitsConfig.class);
        IConfigElement bosses = getElementByName(root.getChildElements(), "boss list");

        for (IConfigElement configElement : bosses.getChildElements()) {
            List<IConfigElement> childElements = configElement.getChildElements();
            BossConfig config = new BossConfig();
            config.registryName = (String) getElementByName(childElements, "registry_name").get();
            config.factionName = ((String) getElementByName(childElements, "faction").get());
            IConfigElement icon = getElementByName(childElements, "icon");
            if (icon != null) {
                config.icon = (String) icon.get();
            }
            IConfigElement tag = ( getElementByName(childElements, "custom_tag"));
            IConfigElement tagValue = ( getElementByName(childElements, "tag_value"));
            IConfigElement customFaction = ( getElementByName(childElements, "custom_faction"));
            if (tag != null && tagValue != null && customFaction != null) {
                config.tag = (String) tag.get();
                config.tagValue = (String) tagValue.get();
                config.customFaction = (String) customFaction.get();

            }
            if (config.factionName != null && config.registryName != null) {
                for (int i=0;i<5;i++) {
                    System.out.println();
                }
                System.out.println("=================FOUND BOSS "+config.factionName+" "+config.registryName);
                for (int i=0;i<5;i++) {
                    System.out.println();
                }
                list.add(config);
            }
        }

        return list;
    }

    public static List<SoldierConfig> getAlliesConfigs() {
        List<SoldierConfig> list = Lists.<SoldierConfig>newArrayList();
        IConfigElement root = ConfigElement.from(FactionUnitsConfig.class);
        IConfigElement allies = getElementByName(root.getChildElements(), "player entity list");
        for (IConfigElement configElement : allies.getChildElements()) {
            SoldierConfig s = new SoldierConfig();
            List<IConfigElement> childElements = configElement.getChildElements();
            s.registryName = (String) getElementByName(childElements, "registry_name").get();
            if (s.registryName != null) {
                list.add(s);
            }
        }
        return list;
    }

    public static List<FactionConfig> getFactionConfigs() {
        List<FactionConfig> list = Lists.<FactionConfig>newArrayList();
        IConfigElement root = ConfigElement.from(FactionUnitsConfig.class);
        IConfigElement factions = getElementByName(root.getChildElements(), "unit list");


        for (IConfigElement configElement : factions.getChildElements()) {
            List<IConfigElement> childElements = configElement.getChildElements();
            FactionConfig config = new FactionConfig();
            config.factionName = (String) getElementByName(childElements, "faction").get();
            config.nightFignt = new Boolean((String) getElementByName(childElements, "nightFight").get());
            config.warType = War.WarType.findByName((String) getElementByName(childElements, "war_level").get());
            //config.names = getElementByName(childElements,"bossNames").get();
            IConfigElement element = getElementByName(childElements, "soldier");
            if (element != null && config.factionName != null) {
                for (int i=0;i<5;i++) {
                    System.out.println();
                }
                System.out.println("=================FOUND FACTION "+config.factionName);
                for (int i=0;i<5;i++) {
                    System.out.println();
                }
                List<IConfigElement> entity = element.getChildElements();
                config.soldier.registryName = (String) getElementByName(entity, "registry_name").get();
                config.soldier.tags = (String) getElementByName(entity, "nbt_tags").get();

                element = getElementByName(childElements, "elite");
                if (element != null) {
                    entity = element.getChildElements();
                    config.elite.registryName = (String) getElementByName(entity, "registry_name").get();
                    config.elite.tags = (String) getElementByName(entity, "nbt_tags").get();
                } else {
                    config.elite.registryName = config.soldier.registryName;
                    config.soldier.tags = (String) getElementByName(entity, "nbt_tags").get();
                }
                element = getElementByName(childElements, "demolisher");
                if (element != null) {
                    entity = element.getChildElements();
                    config.demolisher.registryName = (String) getElementByName(entity, "registry_name").get();
                    config.demolisher.tags = (String) getElementByName(entity, "nbt_tags").get();
                } else {
                    config.demolisher.registryName = config.soldier.registryName;
                    config.soldier.tags = (String) getElementByName(entity, "nbt_tags").get();
                }
                element = getElementByName(childElements, "archer");
                if (element != null) {
                    entity = element.getChildElements();
                    config.archer.registryName = (String) getElementByName(entity, "registry_name").get();
                    config.archer.tags = (String) getElementByName(entity, "nbt_tags").get();
                } else {
                    config.archer.registryName = config.soldier.registryName;
                    config.soldier.tags = (String) getElementByName(entity, "nbt_tags").get();
                }
                element = getElementByName(childElements, "sentry");
                if (element != null) {
                    entity = element.getChildElements();
                    config.sentry.registryName = (String) getElementByName(entity, "registry_name").get();
                    config.sentry.tags = (String) getElementByName(entity, "nbt_tags").get();
                } else {
                    config.sentry.registryName = config.soldier.registryName;
                    config.soldier.tags = (String) getElementByName(entity, "nbt_tags").get();
                }
                element = getElementByName(childElements, "cavalry");
                if (element != null) {
                    entity = element.getChildElements();
                    config.cavalry.registryName = (String) getElementByName(entity, "registry_name").get();
                    config.cavalry.tags = (String) getElementByName(entity, "nbt_tags").get();
                } else {
                    config.cavalry.registryName = config.soldier.registryName;
                    config.soldier.tags = (String) getElementByName(entity, "nbt_tags").get();
                }
                element = getElementByName(childElements, "leader");
                if (element != null) {
                    entity = element.getChildElements();
                    config.hq.registryName = (String) getElementByName(entity, "registry_name").get();
                    config.hq.tags = (String) getElementByName(entity, "nbt_tags").get();
                } else {
                    config.hq.registryName = config.soldier.registryName;
                    config.soldier.tags = (String) getElementByName(entity, "nbt_tags").get();
                }
                element = getElementByName(childElements, "wizard");
                if (element != null) {
                    entity = element.getChildElements();
                    config.wizard.registryName = (String) getElementByName(entity, "registry_name").get();
                    config.wizard.tags = (String) getElementByName(entity, "nbt_tags").get();
                } else {
                    config.wizard.registryName = config.soldier.registryName;
                    config.soldier.tags = (String) getElementByName(entity, "nbt_tags").get();
                }
                element = getElementByName(childElements, "heavy support");
                if (element != null) {
                    entity = element.getChildElements();
                    config.hs.registryName = (String) getElementByName(entity, "registry_name").get();
                    config.hs.tags = (String) getElementByName(entity, "nbt_tags").get();
                } else {
                    config.hs.registryName = config.soldier.registryName;
                    config.soldier.tags = (String) getElementByName(entity, "nbt_tags").get();
                }
                element = getElementByName(childElements, "warlord");
                if (element != null) {
                    entity = element.getChildElements();
                    config.warlord.registryName = (String) getElementByName(entity, "registry_name").get();
                    config.warlord.tags = (String) getElementByName(entity, "nbt_tags").get();
                } else {
                    config.warlord.registryName = config.soldier.registryName;
                    config.soldier.tags = (String) getElementByName(entity, "nbt_tags").get();
                }
                list.add(config);
            }
        }

        return list;
    }

    @Nullable
    public static IConfigElement getElementByName(List<IConfigElement> configElements, String name) {
        for (IConfigElement element : configElements) {
            if (element.getName().equals(name)) {
                return element;
            }
        }

        return null;
    }
}
