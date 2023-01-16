package onslaught.ketongu10.util;

import onslaught.ketongu10.Onslaught;
import onslaught.ketongu10.war.Battle;
import onslaught.ketongu10.war.FactionUnits;
import onslaught.ketongu10.war.War;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.util.Constants;
import net.shadowmage.ancientwarfare.core.util.NBTBuilder;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class NBTHelpers {

    public static War.WarType warTypeFromNBT(NBTTagCompound nbt) {
        War.WarType warType = War.WarType.findByName(nbt.getString("warType"));
        return warType;
    }

    public static Battle.BattleType battleTypeFromNBT(NBTTagCompound nbt) {
        Battle.BattleType battleType = Battle.BattleType.findByName(nbt.getString("battleType"));
        return battleType;
    }
    public static FactionUnits.UnitType unitTypeFromNBT(NBTTagCompound nbt) {
        FactionUnits.UnitType unitType = FactionUnits.UnitType.getTypeById(nbt.getInteger("unitType"));
        return unitType;
    }

    public static NBTTagList getNBTUniqueIdList(Collection<UUID> uuids) {
        NBTTagList ret = new NBTTagList();
        uuids.forEach(uuid -> ret.appendTag(new NBTBuilder().setUniqueId("uuid", uuid).build()));
        return ret;
    }
    public static Set<UUID> getUniqueIdSet(NBTBase tag) {
        return getSet(getTagList(tag, Constants.NBT.TAG_COMPOUND), element -> ((NBTTagCompound) element).getUniqueId("uuid"));
    }

    private static NBTTagList getTagList(NBTBase tag, int type) {
        try {
            if (tag.getId() == 9) {
                NBTTagList nbttaglist = (NBTTagList) tag;

                if (!nbttaglist.hasNoTags() && nbttaglist.getTagType() != type) {
                    return new NBTTagList();
                }

                return nbttaglist;
            }
        }
        catch (ClassCastException classcastexception) {
            Onslaught.LOGGER.error("Error casting tag to taglist: {}", tag);
        }

        return new NBTTagList();
    }

    public static NBTTagCompound writeBlockPosToNBT(NBTTagCompound tag, BlockPos pos) {
        tag.setInteger("x", pos.getX());
        tag.setInteger("y", pos.getY());
        tag.setInteger("z", pos.getZ());
        return tag;
    }



    public static NBTTagCompound writeStructureBoxToNBT(NBTTagCompound tag, StructureBoundingBox box) {
        tag.setInteger("x1", box.minX);
        tag.setInteger("y1", box.minY);
        tag.setInteger("z1", box.minZ);
        tag.setInteger("x2", box.maxX);
        tag.setInteger("y2", box.maxY);
        tag.setInteger("z2", box.maxZ);
        return tag;
    }

    public static BlockPos readBlockPosFromNBT(NBTTagCompound tag) {
        return new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"));
    }
    public static StructureBoundingBox readStructureBoxFromNBT(NBTTagCompound tag) {
        return new StructureBoundingBox(tag.getInteger("x1"), tag.getInteger("y1"), tag.getInteger("z1"), tag.getInteger("x2"), tag.getInteger("y2"), tag.getInteger("z2"));
    }


    public static <T> Set<T> getSet(NBTTagList tagList, Function<NBTBase, T> getElement) {
        Set<T> ret = new HashSet();
        Iterator var3 = tagList.iterator();

        while(var3.hasNext()) {
            NBTBase tag = (NBTBase)var3.next();
            ret.add(getElement.apply(tag));
        }

        return ret;
    }

    public static <T> NBTTagList getTagList(Collection<T> collection, Function<T, NBTBase> serializeElement) {
        NBTTagList ret = new NBTTagList();
        collection.forEach(element -> ret.appendTag(serializeElement.apply(element)));
        return ret;
    }

    public static <T> NBTTagList getTagList(Collection<T> collection, BiFunction<NBTTagCompound, T, NBTBase> serializeElement) {
        NBTTagList ret = new NBTTagList();
        collection.forEach(element -> ret.appendTag(serializeElement.apply(new NBTTagCompound(), element)));
        return ret;
    }
}
