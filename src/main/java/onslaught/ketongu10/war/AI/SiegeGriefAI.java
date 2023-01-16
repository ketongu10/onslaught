package onslaught.ketongu10.war.AI;

import onslaught.ketongu10.capabilities.ModCapabilities;
import onslaught.ketongu10.capabilities.units.UnitCapability;
import funwayguy.epicsiegemod.ai.utils.AiUtils;
import funwayguy.epicsiegemod.config.props.CfgProps;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class SiegeGriefAI extends MoveAI {
    //EntityLiving entityLiving;
    UnitCapability cap;
    //BlockPoscap.getTarget();
    int digTick = 0;

    public SiegeGriefAI(EntityLiving entity) {
        //this.entityLiving = entity;
        super(entity);
        this.cap = entity.getCapability(ModCapabilities.UNIT_CAPABILITY, null);
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (this.ent.getRNG().nextInt(1) != 0 || this.ent.getAttackTarget() != null) {
            return false;
        } else {
            if (cap.getTarget() == null) {
                BlockPos curPos = this.ent.getPosition();
                BlockPos candidate = null;
                ItemStack item = this.ent.getHeldItemMainhand();
                BlockPos tarPos = curPos.add(this.ent.getRNG().nextInt(6) - 3, this.ent.getRNG().nextInt(6) - 3, this.ent.getRNG().nextInt(6) - 3);
                IBlockState state = this.ent.world.getBlockState(tarPos);
                ResourceLocation regName = (ResourceLocation) Block.REGISTRY.getNameForObject(state.getBlock());
                if ((((List) CfgProps.GRIEF_BLOCKS.get(this.ent)).contains(regName.toString()) || state.getLightValue(this.ent.world, tarPos) > 0) && state.getBlockHardness(this.ent.world, tarPos) >= 0.0F && !state.getMaterial().isLiquid() && (!(Boolean) CfgProps.DIG_TOOLS.get(this.ent) ||true|| !item.isEmpty() && item.getItem().canHarvestBlock(state, item) || state.getMaterial().isToolNotRequired())) {
                    candidate = tarPos;
                }

                if (candidate == null) {
                    return false;
                } else {
                    cap.setTarget(candidate);
                    //this.ent.getNavigator().tryMoveToXYZ(cap.getTarget().getX(),cap.getTarget().getY(),cap.getTarget().getZ(), 1.0D);
                    moveToPosition(cap.getTarget(), ent.getDistanceSq(cap.getTarget()));
                    this.digTick = 0;
                    return true;
                }
            } else {
                //this.ent.getNavigator().tryMoveToXYZ(cap.getTarget().getX(),cap.getTarget().getY(),cap.getTarget().getZ(), 1.0D);
                moveToPosition(cap.getTarget(), ent.getDistanceSq(cap.getTarget()));
                this.digTick = 0;
                return true;
            }

        }
    }
    @Override
    public boolean shouldContinueExecuting() {
        if (cap.getTarget() != null && this.ent.isEntityAlive() && this.ent.getRevengeTarget() == null && this.ent.getAttackTarget() == null) {
            IBlockState state = this.ent.world.getBlockState(cap.getTarget());
            ResourceLocation regName = (ResourceLocation)Block.REGISTRY.getNameForObject(state.getBlock());
            if (state.getBlock() != Blocks.AIR && (((List)CfgProps.GRIEF_BLOCKS.get(this.ent)).contains(regName.toString()) || state.getLightValue(this.ent.world, cap.getTarget()) > 0)) {
                ItemStack item = this.ent.getHeldItemMainhand();
                return !(Boolean)CfgProps.DIG_TOOLS.get(this.ent) || !item.isEmpty() && item.getItem().canHarvestBlock(state, item) || state.getMaterial().isToolNotRequired() || true;
            } else {
                cap.setTarget(null);
                return false;
            }
        } else {
            cap.setTarget(null);
            return false;
        }
    }

    @Override
    public void updateTask() {
        if (!this.shouldContinueExecuting()) {
            this.digTick = 0;
        } else if (this.ent.getDistance(cap.getTarget().getX(), cap.getTarget().getY(), cap.getTarget().getZ()) >= 3.0D) {
            if (this.ent.getNavigator().noPath()) {
                //this.ent.getNavigator().tryMoveToXYZ(cap.getTarget().getX(), cap.getTarget().getY(), cap.getTarget().getZ(), 1.0D);
                moveToPosition(cap.getTarget(), ent.getDistanceSq(cap.getTarget()));
            }

            this.digTick = 0;
        } else {
            IBlockState state = this.ent.world.getBlockState(cap.getTarget());
            ++this.digTick;
            float str = AiUtils.getBlockStrength(this.ent, this.ent.world, cap.getTarget()) * (float)(this.digTick + 1);
            if (str >= 1.0F) {
                this.digTick = 0;
                if (cap.getTarget() != null) {
                    ItemStack item = this.ent.getHeldItemMainhand();
                    boolean canHarvest = state.getMaterial().isToolNotRequired() || !item.isEmpty() && item.getItem().canHarvestBlock(state, item) || true;
                    this.ent.world.destroyBlock(cap.getTarget(), canHarvest);//???
                    cap.setTarget(null);
                }
            } else if (this.digTick % 5 == 0) {
                SoundType sndType = state.getBlock().getSoundType(state, this.ent.world, cap.getTarget(), this.ent);
                this.ent.playSound(sndType.getBreakSound(), sndType.volume, sndType.pitch);
                this.ent.swingArm(EnumHand.MAIN_HAND);
            }

        }
    }
}
