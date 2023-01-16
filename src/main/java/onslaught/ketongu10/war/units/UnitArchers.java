package onslaught.ketongu10.war.units;

import onslaught.ketongu10.war.Battle;
import onslaught.ketongu10.war.FactionUnits;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class UnitArchers extends UnitTroops implements IRangeUnit {
    public UnitArchers(World w, FactionUnits.UnitType typ, String fac, String subfac, BlockPos pos, boolean warlord, Battle bat) {
        super(w, typ, fac, subfac, pos, warlord, bat);
    }
    public UnitArchers(FactionUnits.UnitType typ,Battle bat) {
        super(typ, bat);
    }

    @Override
    protected void createMembersByType(FactionUnits.UnitType type) {
        if (FactionUnits.FactionSoldiers.containsKey(faction)) {
            EntityLiving ent = (EntityLiving) createWarrior(FactionUnits.FactionSoldiers.get(faction).archer);
            if (ent != null) {entities.add(ent);}
            for (int i = 0; i < 2; i++) {
                ent = (EntityLiving) createWarrior(FactionUnits.FactionSoldiers.get(faction).archer);
                if (ent != null) {entities.add(ent);}
            }
        }
        setNewLeader(this.isWarlord);
    }
}
