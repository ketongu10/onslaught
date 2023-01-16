package onslaught.ketongu10.war.units;

import onslaught.ketongu10.war.AI.SiegeGriefAI;
import onslaught.ketongu10.war.Battle;
import onslaught.ketongu10.war.FactionUnits;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import onslaught.ketongu10.war.AI.SiegeDemolitionAI;
import onslaught.ketongu10.war.AI.SiegePillarUpAI;

public class UnitDemolishers extends UnitBase {
    public UnitDemolishers(World w, FactionUnits.UnitType typ, String fac, String subfac, BlockPos pos, boolean warlord, Battle bat) {
        super(w, typ, fac, subfac, pos, warlord, bat);
    }
    public UnitDemolishers(FactionUnits.UnitType typ,Battle bat) {
        super(typ, bat);
    }

    protected void createMembersByType(FactionUnits.UnitType type) {
        if (FactionUnits.FactionSoldiers.containsKey(faction)) {
            EntityLiving ent = (EntityLiving) createWarrior(FactionUnits.FactionSoldiers.get(faction).demolishers);
            if (ent != null) {entities.add(ent);}
            ent = (EntityLiving) createWarrior(FactionUnits.FactionSoldiers.get(faction).demolishers);
            if (ent != null) {entities.add(ent);}
        }
        setNewLeader(this.isWarlord);
    }

    @Override
    public void spawnNoAI() {
        if (!this.world.isRemote) {
            for(EntityLiving e: this.entities) {
                e.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Blocks.TNT));
            }
        }
        super.spawnNoAI();
    }

    @Override
    protected void modifyAI(EntityLiving entityIn) {
        super.modifyAI(entityIn);
        entityIn.tasks.addTask(3, new SiegeGriefAI(entityIn));
        entityIn.tasks.addTask(4, new SiegePillarUpAI(entityIn));
        entityIn.tasks.addTask(4, new SiegeDemolitionAI(entityIn));
    }
}
