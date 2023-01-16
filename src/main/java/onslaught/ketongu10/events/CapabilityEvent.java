package onslaught.ketongu10.events;

import onslaught.ketongu10.capabilities.ModCapabilities;
import onslaught.ketongu10.capabilities.units.ProviderEntityUnits;
import onslaught.ketongu10.capabilities.units.UnitCapability;
import onslaught.ketongu10.capabilities.world.WarCapabilityProvider;
import onslaught.ketongu10.capabilities.world.WarData;
import onslaught.ketongu10.util.Reference;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid= Reference.MOD_ID)
public class CapabilityEvent {

    @SubscribeEvent
    public static void attachEntityCapability(AttachCapabilitiesEvent<Entity> event) {

        if(event.getObject().getCapability(ModCapabilities.UNIT_CAPABILITY, null) == null) {
            ProviderEntityUnits prov = new ProviderEntityUnits(event.getObject());
            if(prov.hasCapability()) {
                UnitCapability entityCap = prov.getCapability(ModCapabilities.UNIT_CAPABILITY, null);
                entityCap.onEntityConstructed((EntityLiving) event.getObject());
                event.addCapability(new ResourceLocation(Reference.MOD_ID, "units_cap"), prov);
            }
        }
    }

    @SubscribeEvent
    public void onWorldCapabilityAttach(AttachCapabilitiesEvent<World> event) {
        WarCapabilityProvider prov = new WarCapabilityProvider();
        if(prov.hasCapability()) {
            WarData warsManager = prov.getCapability(ModCapabilities.WAR_DATA, null);
            warsManager.onConstructed((World) event.getObject());
            event.addCapability(new ResourceLocation(Reference.MOD_ID, "war_data"), prov);
        }

    }


}
