package onslaught.ketongu10.capabilities.world;

import onslaught.ketongu10.capabilities.ModCapabilities;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WarCapabilityProvider implements ICapabilitySerializable<NBTTagCompound> {
    private WarData warData = ModCapabilities.WAR_DATA.getDefaultInstance();

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == ModCapabilities.WAR_DATA;
    }

    public boolean hasCapability() {
        return warData != null;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == ModCapabilities.WAR_DATA ? ModCapabilities.WAR_DATA.cast(warData) : null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        //noinspection ConstantConditions
        return (NBTTagCompound) ModCapabilities.WAR_DATA.writeNBT(warData, null);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        ModCapabilities.WAR_DATA.readNBT(warData, null, nbt);
    }
}
