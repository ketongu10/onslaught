package onslaught.ketongu10.events;

import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import onslaught.ketongu10.capabilities.ModCapabilities;
import net.minecraft.entity.EntityLiving;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EntityEvents {

    @SubscribeEvent
    public void entityEvent(LivingSpawnEvent event)
    {

        if (event.getEntity().getCapability(ModCapabilities.UNIT_CAPABILITY, null) != null) {
            event.getEntity().getCapability(ModCapabilities.UNIT_CAPABILITY, null).onEntityJoinWorld(event.getWorld());
        }


    }



    @SubscribeEvent
    public void deathEvent(LivingDeathEvent event) {
        //if (!event.getEntity().world.isRemote) {
            if (event.getEntity().getCapability(ModCapabilities.UNIT_CAPABILITY, null) != null) {
                event.getEntity().getCapability(ModCapabilities.UNIT_CAPABILITY, null).onEntityDeath((EntityLiving) event.getEntity(), event.getSource());
            }

        //}
    }


}