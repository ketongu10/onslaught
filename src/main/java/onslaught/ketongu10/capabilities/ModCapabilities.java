package onslaught.ketongu10.capabilities;

import onslaught.ketongu10.capabilities.units.UnitCapability;
import onslaught.ketongu10.capabilities.world.WarData;
import onslaught.ketongu10.war.WarsManager;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ModCapabilities {


    @CapabilityInject(UnitCapability.class)
    public static final Capability<UnitCapability<?>> UNIT_CAPABILITY = null;

    @CapabilityInject(WarData.class)
    public static Capability<WarData> WAR_DATA = null;

    public static void registerCapabilities() {

        CapabilityManager.INSTANCE.register(WarData.class, new Capability.IStorage<WarData>() {
                    @Override
                    public NBTBase writeNBT(Capability<WarData> capability, WarData instance, EnumFacing side) {
                        return instance.serializeNBT();
                    }

                    @Override
                    public void readNBT(Capability<WarData> capability, WarData instance, EnumFacing side, NBTBase nbt) {
                        if (!(instance instanceof WarData))
                            throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");
                        instance.deserializeNBT((NBTTagCompound) nbt);
                    }
                },
                WarsManager::new);



        CapabilityManager.INSTANCE.register(UnitCapability.class, new Capability.IStorage<UnitCapability>() {
            @Override
            public NBTBase writeNBT(Capability<UnitCapability> capability, UnitCapability instance,
                                    EnumFacing side) {
                return instance.serializeNBT();
            }

            @Override
            public void readNBT(Capability<UnitCapability> capability, UnitCapability instance, EnumFacing side,
                                NBTBase nbt) {
                if (!(instance instanceof UnitCapability))
                    throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");
                instance.deserializeNBT((NBTTagCompound) nbt);

            }
        }, UnitCapability::new);



    }

}
