package onslaught.ketongu10.war.AI;

import onslaught.ketongu10.capabilities.ModCapabilities;
import onslaught.ketongu10.capabilities.units.UnitCapability;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;

public class SiegeAIHurtByTarget extends EntityAIHurtByTarget {

    public UnitCapability cap;

    public SiegeAIHurtByTarget(EntityCreature creatureIn, boolean entityCallsForHelpIn, Class<?>... excludedReinforcementTypes) {
        super(creatureIn, entityCallsForHelpIn, excludedReinforcementTypes);
        this.cap = creatureIn.getCapability(ModCapabilities.UNIT_CAPABILITY, null);

    }

    @Override
    public boolean shouldExecute()
    {
        EntityLivingBase target = this.taskOwner.getRevengeTarget();
        if (target != null) {
            if (cap.isTeam(target)) {
                return false;
            }
        }
        return super.shouldExecute();
    }

}
