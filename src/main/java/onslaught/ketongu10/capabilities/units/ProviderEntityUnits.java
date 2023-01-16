package onslaught.ketongu10.capabilities.units;

import electroblob.wizardry.entity.living.EntityEvilWizard;
import onslaught.ketongu10.capabilities.ModCapabilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.fml.common.Loader;
import net.shadowmage.ancientwarfare.npc.entity.faction.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ProviderEntityUnits implements ICapabilitySerializable<NBTTagCompound>, ICapabilityProvider {
    public static final Map<Class<? extends EntityLiving>, Supplier<UnitCapability<?>>> capabilityMap =
            new HashMap<Class<? extends EntityLiving>, Supplier<UnitCapability<?>>>();


    public static void makeMap() {
        capabilityMap.put(EntitySkeleton.class, UnitCapability::new);
        capabilityMap.put(EntityZombie.class, UnitCapability::new);
        capabilityMap.put(EntityHusk.class, UnitCapability::new);
        capabilityMap.put(EntityBlaze.class, UnitCapability::new);
        capabilityMap.put(EntityWitch.class, UnitCapability::new);
        capabilityMap.put(EntityWitherSkeleton.class, UnitCapability::new);
        capabilityMap.put(EntityEvilWizard.class, UnitCapability::new);
        if (Loader.isModLoaded("ancientwarfare")) {
            capabilityMap.put(NpcFactionSpellcasterWizardry.class, UnitCapability::new);
            capabilityMap.put(NpcFactionSoldier.class, UnitCapability::new);
            capabilityMap.put(NpcFactionSoldierElite.class, UnitCapability::new);
            capabilityMap.put(NpcFactionLeader.class, UnitCapability::new);
            capabilityMap.put(NpcFactionLeaderElite.class, UnitCapability::new);
            capabilityMap.put(NpcFactionMountedSoldier.class, UnitCapability::new);
            capabilityMap.put(NpcFactionMountedArcher.class, UnitCapability::new);
            capabilityMap.put(NpcFactionArcher.class, UnitCapability::new);
            capabilityMap.put(NpcFactionArcherElite.class, UnitCapability::new);
        }
    }
    private UnitCapability<?> capability;

    public ProviderEntityUnits(Entity entity) {
        if(capabilityMap.containsKey(entity.getClass())) {
            capability = capabilityMap.get(entity.getClass()).get();
        }
    }

    public boolean hasCapability() {
        return capability != null;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == ModCapabilities.UNIT_CAPABILITY && this.capability != null ? true : false;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == ModCapabilities.UNIT_CAPABILITY && this.capability != null) {
            return (T) this.capability;
        }
        return null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        //noinspection ConstantConditions
        return (NBTTagCompound) ModCapabilities.UNIT_CAPABILITY.writeNBT(capability, null);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        ModCapabilities.UNIT_CAPABILITY.readNBT(capability, null, nbt);
    }
}

