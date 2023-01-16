package onslaught.ketongu10.war.AI;

import onslaught.ketongu10.capabilities.ModCapabilities;
import funwayguy.epicsiegemod.config.props.CfgProps;
import funwayguy.epicsiegemod.core.ESM;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class SiegePillarUpAI extends EntityAIBase {
    public static ResourceLocation blockName = new ResourceLocation("minecraft:cobblestone");
    public static int blockMeta = -1;
    public static boolean updateBlock = false;
    private static IBlockState pillarBlock;
    private static final EnumFacing[] placeSurface;
    private int placeDelay = 15;
    private EntityLiving builder;
    public BlockPos target;
    private BlockPos blockPos;

    public SiegePillarUpAI(EntityLiving entity) {
        this.builder = entity;
    }
    @Override
    public boolean shouldExecute() {
        this.target = this.builder.getCapability(ModCapabilities.UNIT_CAPABILITY, null).getTarget();
        if (this.target != null) {
            if (!this.builder.getNavigator().noPath() || this.builder.isRiding() || this.builder.isAirBorne || (!(this.builder.getDistance(this.target.getX(), this.builder.posY, this.target.getZ()) < 4.0D))) {// || !this.builder.isAirBorne)) {//&& !this.builder.isInWater() && !this.builder.isRiding()) {
                return false;
            } else {
                BlockPos tmpPos = this.builder.getPosition();
                int xOff = (int)Math.signum((float)(MathHelper.floor(this.target.getX()) - tmpPos.getX()));
                int zOff = (int)Math.signum((float)(MathHelper.floor(this.target.getZ()) - tmpPos.getZ()));
                boolean canPlace = false;
                EnumFacing[] var6 = placeSurface;
                int var7 = var6.length;

                for(int var8 = 0; var8 < var7; ++var8) {
                    EnumFacing dir = var6[var8];
                    if (this.builder.world.getBlockState(tmpPos.offset(dir)).isNormalCube()) {
                        canPlace = true;
                        break;
                    }
                }

                if (this.target.getY() - this.builder.posY < 16.0D && this.builder.world.getBlockState(tmpPos.add(0, -2, 0)).isNormalCube() && this.builder.world.getBlockState(tmpPos.add(0, -1, 0)).isNormalCube()) {
                    if (this.builder.world.getBlockState(tmpPos.add(xOff, -1, 0)).getMaterial().isReplaceable()) {
                        tmpPos = tmpPos.add(xOff, -1, 0);
                    } else if (this.builder.world.getBlockState(tmpPos.add(0, -1, zOff)).getMaterial().isReplaceable()) {
                        tmpPos = tmpPos.add(0, -1, zOff);
                    } else if (this.target.getY() <= this.builder.posY) {
                        return false;
                    }
                } else if (this.target.getY() <= this.builder.posY) {
                    return false;
                }

                if (canPlace && !this.builder.world.getBlockState(tmpPos.add(0, 2, 0)).getMaterial().blocksMovement() && !this.builder.world.getBlockState(tmpPos.add(0, 2, 0)).getMaterial().blocksMovement()) {//???
                    this.blockPos = tmpPos;
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }
    @Override
    public void startExecuting() {
        this.placeDelay = 15;
        if (updateBlock) {
            this.updatePillarBlock();
            updateBlock = false;
        }

    }
    @Override
    public boolean shouldContinueExecuting() {
        return this.shouldExecute();
    }

    @Override
    public void updateTask() {
        if (this.placeDelay <= 0 && this.target != null) {
            if (this.blockPos != null) {
                this.placeDelay = 15;
                this.builder.setPosition((double)this.blockPos.getX() + 0.5D, (double)this.blockPos.getY() + 1.0D, (double)this.blockPos.getZ() + 0.5D);
                if (this.builder.world.getBlockState(this.blockPos).getMaterial().isReplaceable()) {
                    this.builder.world.setBlockState(this.blockPos, pillarBlock);
                }

                this.builder.getNavigator().setPath(this.builder.getNavigator().getPathToPos(this.target), this.builder.getAIMoveSpeed());
            }
        } else {
            --this.placeDelay;
        }

    }
    @Override
    public boolean isInterruptible() {
        return false;
    }

    private void updatePillarBlock() {
        String cfgBlockName = (String) CfgProps.PILLAR_BLOCK.get(this.builder);
        String[] cfgSplit = cfgBlockName.split(":");
        if (cfgSplit.length != 2 && cfgSplit.length != 3) {
            ESM.logger.error("Incorrectly formatted pillar block config: " + cfgBlockName);
            blockName = new ResourceLocation("minecraft:cobblestone");
            blockMeta = -1;
        } else {
            blockName = new ResourceLocation(cfgSplit[0], cfgSplit[1]);
            if (cfgSplit.length == 3) {
                try {
                    blockMeta = Integer.parseInt(cfgSplit[2]);
                } catch (Exception var5) {
                    ESM.logger.error("Unable to parse pillar block metadata from: " + cfgBlockName, var5);
                    blockMeta = -1;
                }
            } else {
                blockMeta = -1;
            }
        }

        try {
            Block b = (Block)Block.REGISTRY.getObject(blockName);
            if (b == Blocks.AIR) {
                pillarBlock = Blocks.COBBLESTONE.getDefaultState();
            } else {
                pillarBlock = blockMeta < 0 ? b.getDefaultState() : b.getStateFromMeta(blockMeta);
            }
        } catch (Exception var4) {
            ESM.logger.error("Unable to read pillaring block from config", var4);
            pillarBlock = Blocks.COBBLESTONE.getDefaultState();
        }

    }

    static {
        pillarBlock = Blocks.COBBLESTONE.getDefaultState();
        placeSurface = new EnumFacing[]{EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST};
    }
}