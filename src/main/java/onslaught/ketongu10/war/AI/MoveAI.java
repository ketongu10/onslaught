package onslaught.ketongu10.war.AI;



import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import javax.annotation.Nullable;


public abstract class MoveAI extends EntityAIBase {


    public static final int MIN_RANGE = 9;

    protected int moveRetryDelay;
    protected double moveSpeed = 1.0d;
    private final double maxPFDist;
    private final double maxPFDistSq;

    protected EntityLiving ent;

    public MoveAI(EntityLiving entityIn) {
        this.ent = entityIn;
        maxPFDist = entityIn.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getBaseValue() * 0.90d;

        maxPFDistSq = maxPFDist * maxPFDist;
    }

    protected final void moveToEntity(Entity target, double sqDist) {
        moveToPosition(target.posX, target.getEntityBoundingBox().minY, target.posZ, sqDist);
    }

    protected final void moveToPosition(int x, int y, int z, double sqDist) {
        moveToPosition(x + 0.5d, y, z + 0.5d, sqDist);
    }

    protected final void moveToPosition(BlockPos pos, double sqDist) {
        moveToPosition(pos.getX(), pos.getY(), pos.getZ(), sqDist);
    }

    protected final void forceMoveToPosition(BlockPos pos, double sqDist) {
        moveRetryDelay = 0;
        moveToPosition(pos, sqDist);
    }

    protected final void moveToPosition(double x, double y, double z, double sqDist) {
        moveRetryDelay -= 10;
        if (moveRetryDelay <= 0) {
            if (sqDist > maxPFDistSq) {
                moveLongDistance(x, y, z);
                moveRetryDelay = 30;
            } else {
                setPath(x, y, z);
                moveRetryDelay = 5;
                if (sqDist > 256) {
                    moveRetryDelay += 5;
                }
                if (sqDist > 1024) {
                    moveRetryDelay += 10;
                }
            }
        }
    }

    @Override
    public boolean shouldExecute() {
        return !ent.isAIDisabled();
    }



    protected final void moveLongDistance(double x, double y, double z) {
        Vec3d vec = new Vec3d(x - ent.posX, y - ent.posY, z - ent.posZ);

        //normalize vector to a -1 <-> 1 value range
        double w = Math.sqrt(vec.x * vec.x + vec.y * vec.y + vec.z * vec.z);
        if (w != 0) {
            vec = vec.scale(1d / w);
        }

        //then mult by PF distance to find the proper vector for our PF length
        vec = vec.scale(maxPFDist);

        //finally re-offset by npc position to get an actual target position
        vec = vec.addVector(ent.posX, ent.posY, ent.posZ);

        //move npc towards the calculated partial target
        setPath(vec.x, vec.y, vec.z);
    }
    

    protected final void setPath(double x, double y, double z) {
        Path path = trimPath(ent.getNavigator().getPathToXYZ(x, y, z));
        ent.getNavigator().setPath(path, moveSpeed);
    }

    protected final void setPath(BlockPos pos) {
        Path path = trimPath(ent.getNavigator().getPathToPos(pos));
        ent.getNavigator().setPath(path, moveSpeed);
    }

    @Nullable
    protected Path trimPath(@Nullable Path path) {
        if (path != null) {
            int index = path.getCurrentPathIndex();
            PathPoint pathpoint = path.getPathPointFromIndex(index);
            if (this.getBlockPathWeight(new BlockPos(pathpoint.x, pathpoint.y, pathpoint.z)) >= 0) {

                for (int i = index + 1; i < path.getCurrentPathLength(); i++) {
                    pathpoint = path.getPathPointFromIndex(i);
                    if (this.getBlockPathWeight(new BlockPos(pathpoint.x, pathpoint.y, pathpoint.z)) < 0) {
                        path.setCurrentPathLength(i - 1);
                        break;
                    }
                }
            } else {
                Vec3d vec = RandomPositionGenerator.findRandomTargetBlockAwayFrom((EntityCreature) ent, MIN_RANGE, MIN_RANGE, new Vec3d(ent.posX, ent.posY, ent.posZ));
                if (vec != null) {
                    return ent.getNavigator().getPathToXYZ(vec.x, vec.y, vec.z);
                }
            }
        }
        return path;
    }

    public float getBlockPathWeight(BlockPos pos) {
        IBlockState stateBelow = ent.world.getBlockState(pos.down());
        if (stateBelow.getMaterial() == Material.LAVA || stateBelow.getMaterial() == Material.CACTUS)//Avoid cacti and lava when wandering
        { return -10; } else if (stateBelow.getMaterial().isLiquid())//Don't try swimming too much
        { return 0; }
        float level = getLitBlockWeight(pos);//Prefer lit areas
        if (level < 0) { return 0; } else { return level + (stateBelow.isSideSolid(ent.world, pos.down(), EnumFacing.UP) ? 1 : 0); }
    }

    protected float getLitBlockWeight(BlockPos pos) {
        return ent.world.getLightBrightness(pos);
    }

}
