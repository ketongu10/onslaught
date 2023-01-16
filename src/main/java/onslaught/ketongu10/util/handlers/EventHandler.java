package onslaught.ketongu10.util.handlers;

import net.minecraftforge.common.MinecraftForge;
import onslaught.ketongu10.events.*;

public class EventHandler 
{
	public static void registerEvents()
	{
		EntityEvents entityEvents = new EntityEvents();
		PlayerEvents playerEvents = new PlayerEvents();
		TicksEvent keyDeny = new TicksEvent();
		CapabilityEvent capEvent = new CapabilityEvent();


		MinecraftForge.EVENT_BUS.register(keyDeny);
		MinecraftForge.EVENT_BUS.register(entityEvents);
		MinecraftForge.EVENT_BUS.register(playerEvents);
		MinecraftForge.EVENT_BUS.register(capEvent);
	}
}
