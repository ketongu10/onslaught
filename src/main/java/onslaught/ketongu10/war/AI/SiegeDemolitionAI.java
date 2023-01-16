package onslaught.ketongu10.war.AI;

import onslaught.ketongu10.capabilities.ModCapabilities;
import funwayguy.epicsiegemod.ai.utils.AiUtils;
import funwayguy.epicsiegemod.config.props.CfgProps;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.List;

public class SiegeDemolitionAI extends EntityAIBase {
    private int delay = 0;
    private BlockPos target;
    private EntityLiving digger;
    private BlockPos curBlock;
    private int scanTick = 0;
    private int digTick = 0;
    private BlockPos obsPos = null;
    private int obsTick = 0;

    public SiegeDemolitionAI(EntityLiving digger) {
        this.digger = digger;
    }


    public boolean canUseTNT() {
        if (--this.delay > 0) {
            return false;
        } else {
            this.delay = 0;
            boolean hasTnt = this.digger.getHeldItemMainhand().getItem() == Item.getItemFromBlock(Blocks.TNT) || this.digger.getHeldItemOffhand().getItem() == Item.getItemFromBlock(Blocks.TNT);
            return hasTnt;
        }
    }

    public boolean shouldExecute() {
        this.target = this.digger.getCapability(ModCapabilities.UNIT_CAPABILITY, null).getTarget();
        if (this.target != null && this.digger.getNavigator().noPath() && this.digger.getAttackTarget()==null) {
            double dist = this.digger.getDistance(this.target.getX(), this.target.getY(), this.target.getZ());
            double navDist = (double)this.digger.getNavigator().getPathSearchRange();
            if (!(dist < 1.0D) && !(dist > navDist * navDist)) {
                if (this.obsPos == null) {
                    this.obsPos = this.digger.getPosition();
                }

                if (!this.obsPos.equals(this.digger.getPosition())) {
                    this.obsTick = 0;
                    this.obsPos = null;
                    return false;
                } else if (++this.obsTick < 20) {
                    return false;
                } else {
                    this.curBlock = this.curBlock != null && this.digger.getDistanceSq(this.curBlock) <= 16.0D && this.canHarvest(this.digger, this.curBlock) ? this.curBlock : this.getNextBlock(this.digger, this.target, 2.0D);
                    return this.curBlock != null;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void startExecuting() {
        super.startExecuting();
        if (canUseTNT()) {
            this.delay = 200;
            EntityTNTPrimed tnt = new EntityTNTPrimed(this.digger.world, this.digger.posX, this.digger.posY, this.digger.posZ, this.digger);
            this.digger.world.spawnEntity(tnt);
            this.digger.world.playSound((EntityPlayer)null, tnt.posX, tnt.posY, tnt.posZ, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
        this.digger.getNavigator().clearPath();
        this.obsTick = 0;
        this.obsPos = null;
    }
    @Override
    public void resetTask() {
        this.curBlock = null;
        this.delay = 0;
        this.digTick = 0;
        this.obsTick = 0;
        this.obsPos = null;
    }
    @Override
    public boolean shouldContinueExecuting() {
        return this.target != null && this.digger.getAttackTarget()==null && this.curBlock != null && this.digger.getDistanceSq(this.curBlock) <= 16.0D && this.canHarvest(this.digger, this.curBlock);
    }

    @Override
    public void updateTask() {
        this.digger.getLookHelper().setLookPosition(this.target.getX(), this.target.getY() + 0.5D, this.target.getZ(), (float)this.digger.getHorizontalFaceSpeed(), (float)this.digger.getVerticalFaceSpeed());
        this.digger.getNavigator().clearPath();
        ++this.digTick;
        float str = AiUtils.getBlockStrength(this.digger, this.digger.world, this.curBlock) * ((float)this.digTick + 1.0F);
        ItemStack heldItem = this.digger.getHeldItem(EnumHand.MAIN_HAND);
        IBlockState state = this.digger.world.getBlockState(this.curBlock);
        if (this.digger.world.isAirBlock(this.curBlock)) {
            this.resetTask();
        } else if (str >= 1.0F) {
            boolean canHarvest = state.getMaterial().isToolNotRequired() || !heldItem.isEmpty() && heldItem.canHarvestBlock(state);
            this.digger.world.destroyBlock(this.curBlock, false);
            if (canHarvest && this.digger.world instanceof WorldServer) {
                FakePlayer player = FakePlayerFactory.getMinecraft((WorldServer)this.digger.world);
                player.setHeldItem(EnumHand.MAIN_HAND, heldItem);
                player.setHeldItem(EnumHand.OFF_HAND, this.digger.getHeldItem(EnumHand.OFF_HAND));
                player.setPosition((double)this.digger.getPosition().getX(), (double)this.digger.getPosition().getY(), (double)this.digger.getPosition().getZ());
                TileEntity tile = this.digger.world.getTileEntity(this.curBlock);
                state.getBlock().harvestBlock(this.digger.world, player, this.curBlock, state, tile, heldItem);
            }

            this.digger.getNavigator().setPath(this.digger.getNavigator().getPathToPos(this.target), this.digger.getMoveHelper().getSpeed()); //?
            this.resetTask();
        } else if (this.digTick % 5 == 0) {
            this.digger.world.playSound((EntityPlayer)null, this.curBlock, state.getBlock().getSoundType(state, this.digger.world, this.curBlock, this.digger).getPlaceSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
            this.digger.swingArm(EnumHand.MAIN_HAND);
            this.digger.world.sendBlockBreakProgress(this.digger.getEntityId(), this.curBlock, (int)(str * 10.0F));
        }

    }

    private BlockPos getNextBlock(EntityLiving entityLiving, BlockPos target, double dist) {
        int digWidth = MathHelper.ceil(entityLiving.width);
        int digHeight = MathHelper.ceil(entityLiving.height);
        int passMax = digWidth * digWidth * digHeight;
        if (passMax <= 0) {
            return null;
        } else {
            int y = this.scanTick % digHeight;
            int x = this.scanTick % (digWidth * digHeight) / digHeight;
            int z = this.scanTick / (digWidth * digHeight);
            double rayX = (double)x + Math.floor(entityLiving.posX) + 0.5D - (double)digWidth / 2.0D;
            double rayY = (double)y + Math.floor(entityLiving.posY) + 0.5D;
            double rayZ = (double)z + Math.floor(entityLiving.posZ) + 0.5D - (double)digWidth / 2.0D;
            Vec3d rayOrigin = new Vec3d(rayX, rayY, rayZ);
            Vec3d rayOffset = new Vec3d(Math.floor(target.getX()) + 0.5D, Math.floor(target.getY()) + 0.5D, Math.floor(target.getZ()) + 0.5D);
            rayOffset.addVector((double)x - (double)digWidth / 2.0D, (double)y, (double)z - (double)digWidth / 2.0D);
            Vec3d norm = rayOffset.subtract(rayOrigin).normalize();
            if (Math.abs(norm.x) == Math.abs(norm.z) && norm.x != 0.0D) {
                norm = (new Vec3d(norm.x, norm.y, 0.0D)).normalize();
            }

            rayOffset = rayOrigin.add(norm.scale(dist));
            BlockPos p1 = entityLiving.getPosition();
            BlockPos p2 = target;
            if (p1.getDistance(p2.getX(), p1.getY(), p2.getZ()) < 4.0D) {
                if ((double)(p2.getY() - p1.getY()) > 2.0D) {
                    rayOffset = rayOrigin.addVector(0.0D, dist, 0.0D);
                } else if ((double)(p2.getY() - p1.getY()) < -2.0D) {
                    rayOffset = rayOrigin.addVector(0.0D, -dist, 0.0D);
                }
            }

            RayTraceResult ray = entityLiving.world.rayTraceBlocks(rayOrigin, rayOffset, false, true, false);
            this.scanTick = (this.scanTick + 1) % passMax;
            if (ray != null && ray.typeOfHit == RayTraceResult.Type.BLOCK) {
                BlockPos pos = ray.getBlockPos();
                IBlockState state = entityLiving.world.getBlockState(pos);
                if (this.canHarvest(entityLiving, pos) && ((List) CfgProps.DIG_BL.get(entityLiving)).contains(state.getBlock().getRegistryName().toString()) == (Boolean)CfgProps.DIG_BL_INV.get(entityLiving)) {
                    return pos;
                }
            }

            return null;
        }
    }

    private boolean canHarvest(EntityLiving entity, BlockPos pos) {
        IBlockState state = entity.world.getBlockState(pos);
        return true;
        /**if (state.getMaterial().isToolNotRequired() && !(state.getBlockHardness(entity.world, pos) < 0.0F)) {
         if (!state.getMaterial().isToolNotRequired() && (Boolean)CfgProps.DIG_TOOLS.get(entity)) {
         ItemStack held = entity.getHeldItem(EnumHand.MAIN_HAND);
         return !held.isEmpty() && held.getItem().canHarvestBlock(state, held);
         } else {
         return true;
         }
         } else {
         return false;
         }**/
    }
}
