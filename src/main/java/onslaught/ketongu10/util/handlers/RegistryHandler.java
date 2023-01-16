package onslaught.ketongu10.util.handlers;

import onslaught.ketongu10.Onslaught;
import onslaught.ketongu10.commands.stopWar.StopWarCommand;
import onslaught.ketongu10.init.ItemInit;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
	}

}
