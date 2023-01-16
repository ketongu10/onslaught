package onslaught.ketongu10.events;

import onslaught.ketongu10.capabilities.ModCapabilities;
import onslaught.ketongu10.capabilities.world.WarData;
import onslaught.ketongu10.war.WarsManager;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class TicksEvent {



    @SuppressWarnings("unused")
    @SubscribeEvent
    public void WarsUpdate(TickEvent.WorldTickEvent event) {
        //WarsManager.INSTANCE.update();
        World w = event.world;
        if ( !w.isRemote) {
            WarData cap = w.getCapability(ModCapabilities.WAR_DATA, null);
            if (cap != null && cap instanceof WarsManager) {
                ((WarsManager) cap).update();
            }
        }
    }

}
