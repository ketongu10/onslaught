package onslaught.ketongu10.war.units;

import onslaught.ketongu10.Onslaught;
import onslaught.ketongu10.war.AI.SiegeGriefAI;
import onslaught.ketongu10.war.Battle;
import onslaught.ketongu10.war.FactionUnits;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.shadowmage.ancientwarfare.npc.init.AWNPCEntities;

public class UnitCavalery extends UnitBase {
    public UnitCavalery(World w, FactionUnits.UnitType typ, String fac, String subfac, BlockPos pos, boolean warlord, Battle bat) {
        super(w, typ, fac, subfac, pos, warlord, bat);
    }
    public UnitCavalery(FactionUnits.UnitType typ,Battle bat) {
        super(typ, bat);
    }

    protected void createMembersByType(FactionUnits.UnitType type) {
        if (FactionUnits.FactionSoldiers.containsKey(faction)) {
            EntityLiving ent = (EntityLiving) createWarrior(FactionUnits.FactionSoldiers.get(faction).cavalry);
            if (ent != null) {entities.add(ent);}
            for (int i = 0; i < 2; i++) {
                ent = (EntityLiving) createWarrior(FactionUnits.FactionSoldiers.get(faction).cavalry);
                if (ent != null) {entities.add(ent);}
            }
        }
        setNewLeader(this.isWarlord);
    }

    @Override
    protected void modifyAI(EntityLiving entityIn) {
        super.modifyAI(entityIn);
        entityIn.tasks.addTask(3, new SiegeGriefAI(entityIn));
    }

    @Override
    protected Entity createWarrior(FactionUnits.Warrior warrior) {
        String regname = warrior.name;
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        if (warrior.tags != null) {
            try {
                nbttagcompound = JsonToNBT.getTagFromJson(warrior.tags);
            } catch (NBTException e)
            {
                Onslaught.LOGGER.warn("Failed to read NBT tags for " + regname);
                System.err.println(e);
            }
        }
        nbttagcompound.setString("id", regname);
        Entity entity;
        if (faction.equals("AW")) {
            //nbttagcompound.setString("factionName", subfaction);
            //nbttagcompound.setBoolean("horseLives", true);
            entity = AWNPCEntities.createNpc(world, "leader", "elite", subfaction);
        } else {
            entity = AnvilChunkLoader.readWorldEntity(nbttagcompound, world,  false);
        }
        if (entity != null) {
            this.alive++;
        }

        return entity;
    }
}
