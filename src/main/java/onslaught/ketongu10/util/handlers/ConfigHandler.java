package onslaught.ketongu10.util.handlers;

import java.io.File;

import onslaught.ketongu10.Onslaught;
import onslaught.ketongu10.util.Reference;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ConfigHandler 
{
	public static Configuration config;



	//BattleConst
	public static int TIME_TO_PATROL = 1200; //20 min
	public static int TIME_TO_SIEGE = 3600; //60 min
	public static int TIME_BETWEEN_WAVES = 120; //2 minutes
	public static int AMBUSH_RELOAD = 1200; //20 min
	public static int TOTAL_AMBUSH_TIME = 7200; //120 min

	public static int BREAK_TIME = 7200; //120 min



	public static int HOME_CHUNK_RADIUS = 2;
	public static int AMBUSH_RADIUS = 2;

	public static boolean USE_CHUNKLOADING = true;


	public static final boolean EBWIZARDRY = Loader.isModLoaded("ebwizardry");
	public static final boolean ANCIENT_WARFARE = Loader.isModLoaded("ancientwarfare");
	public static final boolean ANCIENT_SPELLCRAFT = Loader.isModLoaded("ancientspellcraft");
	public static final int IN_TICKS = 40;

	public static void print(String s) {
		if (SHOW_BI) {
			System.out.println(s);
		}
	}

	//For developers
	public static boolean SHOW_BI = false;

	
	public static void init(File file)
	{
		config = new Configuration(file);

		String category;

		category = "Battle parameters";
		config.addCustomCategoryComment(category, "Set battle properties. One minecraft-day equals 20 minutes or 1200 seconds.");
		TIME_TO_PATROL = IN_TICKS * config.getInt("Patrol arriving delay", category, 1200, 1, 43200, "Time between slaying an entity and its vengeance. Only for PATROL & AMBUSH wartypes. In seconds.");
		TIME_TO_SIEGE = IN_TICKS * config.getInt("Siege arriving delay", category, 3600, 1, 43200, "Time between slaying an entity and its vengeance. Only for SIEGE & APOCALYPSE wartypes. In seconds.");
		TOTAL_AMBUSH_TIME = IN_TICKS * config.getInt("Period of time, when ambush can be deployed", category, 7200, 1, 43200, "In seconds.");
		TIME_BETWEEN_WAVES = IN_TICKS * config.getInt("Delay between waves", category, 120, 1, 43200, "In seconds.");
		AMBUSH_RELOAD = IN_TICKS * config.getInt("Delay between ambushes", category, 1200, 1, 43200, "In seconds.");
		BREAK_TIME = IN_TICKS * config.getInt("After that time war always ends", category, 7200, 1, 43200, "In seconds.");
		HOME_CHUNK_RADIUS = config.getInt("Home searching radius", category, 2, 1, 10, "In chunks. One chunk equals 16 blocks");
		AMBUSH_RADIUS = config.getInt("Distance for ambush spawn", category, 2, 1, 10, "In chunks. One chunk equals 16 blocks");

		category = "PERFORMANCE";
		config.addCustomCategoryComment(category, "Usually Minecraft loads only a small number of chunks near to player. This mod uses chunk forceloading so some chunks update even if there is not any players nearby. Large number of wars can overload server side of Minecraft.");
		SHOW_BI = config.getBoolean("Should use chunk forceloading?", category, true, "Set false to cancel forceloading.");

		category = "For developers";
		config.addCustomCategoryComment(category, "You can turn on showing battle info to cmd.");
		USE_CHUNKLOADING = config.getBoolean("Show battle info", category, false, "Only to check how mod works");

		config.save();
	}
	
	public static void registerConfig(FMLPreInitializationEvent event)
	{
		Onslaught.config = new File(event.getModConfigurationDirectory().getPath() /**+ "/" + Reference.MOD_ID**/);
		Onslaught.config.mkdirs();
		init(new File(Onslaught.config.getPath(), Reference.NAME +" General"+ ".cfg"));
	}
}
