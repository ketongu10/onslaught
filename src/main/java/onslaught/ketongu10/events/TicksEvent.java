package onslaught.ketongu10.events;

import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
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

    @SubscribeEvent
    public void ChunkLoadEvent(ChunkWatchEvent.Watch event) {
        World w = event.getChunkInstance().getWorld();
        if ( !w.isRemote) {
            WarData cap = w.getCapability(ModCapabilities.WAR_DATA, null);
            if (cap != null && cap instanceof WarsManager) {
               ((WarsManager) cap).deployForces(event.getChunkInstance(), true);
            }
        }

    }

    @SubscribeEvent
    public void ChunkUnLoadEvent(ChunkWatchEvent.UnWatch event) {
        World w = event.getChunkInstance().getWorld();
        if ( !w.isRemote) {
            WarData cap = w.getCapability(ModCapabilities.WAR_DATA, null);
            if (cap != null && cap instanceof WarsManager) {
               ((WarsManager) cap).deployForces(event.getChunkInstance(), false);
            }
        }

    }
}
