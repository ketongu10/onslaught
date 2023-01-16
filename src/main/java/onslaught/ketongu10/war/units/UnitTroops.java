package onslaught.ketongu10.war.units;

import onslaught.ketongu10.war.AI.SiegeDiggingAI;
import onslaught.ketongu10.war.AI.SiegeGriefAI;
import onslaught.ketongu10.war.AI.SiegePillarUpAI;
import onslaught.ketongu10.war.Battle;
import onslaught.ketongu10.war.FactionUnits;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class UnitTroops extends UnitBase {

    public UnitTroops(World w, FactionUnits.UnitType typ, String fac, String subfac, BlockPos pos, boolean warlord, Battle bat) {
        super(w, typ, fac, subfac, pos, warlord, bat);
    }
    public UnitTroops(FactionUnits.UnitType typ,Battle bat) {
        super(typ, bat);
    }

    protected void createMembersByType(FactionUnits.UnitType type) {
        if (FactionUnits.FactionSoldiers.containsKey(faction)) {
            EntityLiving ent = (EntityLiving) createWarrior(FactionUnits.FactionSoldiers.get(faction).elite);
            if (ent != null) {entities.add(ent);}
            for (int i = 0; i < 4; i++) {
                ent = (EntityLiving) createWarrior(FactionUnits.FactionSoldiers.get(faction).soldier);
                if (ent != null) {entities.add(ent);}
            }
        }
        setNewLeader(this.isWarlord);
    }
    @Override
    protected void modifyAI(EntityLiving entityIn) {
        super.modifyAI(entityIn);
        entityIn.tasks.addTask(3, new SiegeGriefAI(entityIn));
        entityIn.tasks.addTask(4, new SiegePillarUpAI(entityIn));
        entityIn.tasks.addTask(4, new SiegeDiggingAI(entityIn));
    }

}
