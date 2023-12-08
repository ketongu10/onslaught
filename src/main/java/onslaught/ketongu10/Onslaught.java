package onslaught.ketongu10;

import java.io.File;

import onslaught.ketongu10.capabilities.ModCapabilities;
import onslaught.ketongu10.capabilities.units.ChunkLoader;
import onslaught.ketongu10.capabilities.units.ProviderEntityUnits;
import onslaught.ketongu10.network.packets.NetworkManager;
import onslaught.ketongu10.proxy.CommonProxy;
import onslaught.ketongu10.tabs.OnslaughtTab;
import onslaught.ketongu10.util.Reference;
import onslaught.ketongu10.util.handlers.RegistryHandler;

import onslaught.ketongu10.war.Battle;
import onslaught.ketongu10.war.BossList;
import onslaught.ketongu10.war.FactionUnits;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import org.apache.logging.log4j.Logger;

@Mod(modid = Reference.MOD_ID, version = Reference.VERSION, name = Reference.NAME)//, dependencies="required-after:epicsiegemod")
public class Onslaught
{

	
	public static File config;
	public static Logger LOGGER;
	
	@Instance
	public static Onslaught instance;
	
	@SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.COMMON_PROXY_CLASS)
	public static CommonProxy proxy;
	
	//public static final CreativeTabs onslaughtTab = new OnslaughtTab("onslaughttab"); //uncomment to make CreativeTab work
	
	static
	{
		FluidRegistry.enableUniversalBucket();
	}
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		LOGGER = event.getModLog();
		RegistryHandler.preInitRegistries(event);
		NetworkManager.registerPackets();
		ForgeChunkManager.setForcedChunkLoadingCallback(this, ChunkLoader.INSTANCE);
		ModCapabilities.registerCapabilities();
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event)
	{

		RegistryHandler.initRegistries();
		ProviderEntityUnits.makeMap();
		/**BossList.setBossList();
		FactionUnits.fillMap();**/
		Battle.fillWAVES();
		proxy.init();
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		RegistryHandler.postInitRegistries();
		BossList.setBossList();
		FactionUnits.fillMap();
	}
	
	@EventHandler
	public void serverInit(FMLServerStartingEvent event)
	{
		RegistryHandler.serverRegistries(event);
	}
}
