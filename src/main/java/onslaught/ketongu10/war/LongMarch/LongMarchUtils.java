package onslaught.ketongu10.war.LongMarch;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static onslaught.ketongu10.util.handlers.ConfigHandler.*;

public class LongMarchUtils {
    public static List<Block> TROPINKA_BLACKLIST = new ArrayList<>();
    public static List<Map.Entry<Block, List<NBTTagCompound>>> TROPINKA_CORPSES;
    public static final int TROPINKA_WIDTH = 2;
    public static final int TROPINKA_CORPSES_WIDTH = 4;
    public static final int TROPINKA_CORPSES_MAX_NUM = 6;


    public static void fill_blacklist() {
        LongMarchUtils.TROPINKA_BLACKLIST.add(Blocks.WATER);
        LongMarchUtils.TROPINKA_BLACKLIST.add(Blocks.FLOWING_WATER);
        LongMarchUtils.TROPINKA_BLACKLIST.add(Blocks.LEAVES);
        LongMarchUtils.TROPINKA_BLACKLIST.add(Blocks.LEAVES2);

        if (ANCIENT_WARFARE) {
            HashMap<Block, List<NBTTagCompound>> tmp = new HashMap<>();
            List var = new ArrayList<>();
            NBTTagCompound tag = new NBTTagCompound();
            for (short i=0; i<8; ++i) {
                if (i != 5) {
                    tag = new NBTTagCompound();
                    tag.setInteger("variant",i);
                    var.add(tag);
                }
            }
            tmp.put(AWStructureBlocks.GRAVESTONE, var);
//            LongMarchUtils.TROPINKA_CORPSES.add(AWStructureBlocks.STATUE);


            var = new ArrayList<>();
            tag = new NBTTagCompound();
            tag.setString("variant","default");
            tag.setBoolean("lit",true);
            var.add(tag);
            tag = new NBTTagCompound();
            tag.setString("variant","default");
            tag.setBoolean("lit",false);
            var.add(tag);
            tag = new NBTTagCompound();
            tag.setString("variant","log");
            tag.setBoolean("lit",true);
            var.add(tag);
            tag = new NBTTagCompound();
            tag.setString("variant","log");
            tag.setBoolean("lit",false);
            var.add(tag);
            tag = new NBTTagCompound();
            tag.setString("variant","square");
            tag.setBoolean("lit",true);
            var.add(tag);
            tag = new NBTTagCompound();
            tag.setString("variant","square");
            tag.setBoolean("lit",false);
            var.add(tag);
            tmp.put(AWStructureBlocks.FIRE_PIT, var);


            TROPINKA_CORPSES = new ArrayList<>(tmp.entrySet());

        }
    }

}
