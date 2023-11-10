package onslaught.ketongu10.util.handlers;

import onslaught.ketongu10.Onslaught;
import onslaught.ketongu10.commands.stopWar.StartWarCommand;
import onslaught.ketongu10.commands.stopWar.StopWarCommand;
import onslaught.ketongu10.init.ItemInit;

import net.minecraft.item.Item;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber
public class RegistryHandler 
{
	@SubscribeEvent
	public static void onItemRegister(RegistryEvent.Register<Item> event)
	{
		event.getRegistry().registerAll(ItemInit.ITEMS.toArray(new Item[0]));
	}
	

	

	

	
	public static void preInitRegistries(FMLPreInitializationEvent event)
	{


		EventHandler.registerEvents();
		SoundsHandler.registerSounds();
		ConfigHandler.registerConfig(event);
	}
	
	public static void initRegistries()
	{

		Onslaught.proxy.render();
		EnumHelper.addArt("Test", "Test", 16, 16, 112, 0);
	}
	
	public static void postInitRegistries()
	{

	}
	
	public static void serverRegistries(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new StopWarCommand());
		event.registerServerCommand(new StartWarCommand());
	}

}
