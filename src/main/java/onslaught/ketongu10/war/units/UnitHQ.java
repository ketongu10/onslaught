package onslaught.ketongu10.war.units;

import onslaught.ketongu10.war.Battle;
import onslaught.ketongu10.war.FactionUnits;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class UnitHQ extends UnitTroops {
    public UnitHQ(World w, FactionUnits.UnitType typ, String fac, String subfac, BlockPos pos, boolean warlord, Battle bat) {
        super(w, typ, fac, subfac, pos, warlord, bat);
    }
    public UnitHQ(FactionUnits.UnitType typ,Battle bat) {
        super(typ, bat);
    }

    @Override
    protected void createMembersByType(FactionUnits.UnitType type) {
        if (FactionUnits.FactionSoldiers.containsKey(faction)) {
            EntityLiving ent = (EntityLiving) createWarrior(FactionUnits.FactionSoldiers.get(faction).warlord);
            if (ent != null) {entities.add(ent);}
            for (int i = 0; i < 2; i++) {
                ent = (EntityLiving) createWarrior(FactionUnits.FactionSoldiers.get(faction).hq);
                if (ent != null) {entities.add(ent);}
            }
        }
        setNewLeader(this.isWarlord);
    }
}
