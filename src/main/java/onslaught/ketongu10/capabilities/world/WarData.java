package onslaught.ketongu10.capabilities.world;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;
import net.shadowmage.ancientwarfare.core.util.NBTHelper;

public abstract class WarData implements INBTSerializable<NBTTagCompound> {
    public World world;

    public void onConstructed(World w) {
        this.world = w;
    }
}

