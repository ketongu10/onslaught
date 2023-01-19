package onslaught.ketongu10.util;

import onslaught.ketongu10.war.Battle;
import funwayguy.epicsiegemod.config.props.CfgProps;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.fml.common.Loader;

import java.util.ArrayList;
import java.util.List;

import static onslaught.ketongu10.util.handlers.ConfigHandler.AMBUSH_RADIUS;
import static onslaught.ketongu10.util.handlers.ConfigHandler.HOME_CHUNK_RADIUS;

public final class BattlefieldHelper {
    /**
     * BY BLOCK CAPABILITY
     * BY FENCE
     * BY PRIVATE
     * DEFAULT
     * **/
    public static void prepareBattlefield(boolean ambush, EntityPlayer player, Battle battle) {
        List<BlockPos> boundBlocks = new ArrayList<>();
        int max = 100000;
        World w = player.world;
        if (Loader.isModLoaded("epicsiegemod")) {
            if (!ambush) {
                BlockPos bed = player.getBedLocation() != null ? player.getBedLocation() : player.getPosition();
                StructureBoundingBox battlefield = new StructureBoundingBox(bed.add(-HOME_CHUNK_RADIUS * 16, -16, -HOME_CHUNK_RADIUS * 16),
                        bed.add(HOME_CHUNK_RADIUS * 16, 16, HOME_CHUNK_RADIUS * 16));
                StructureBoundingBox reducedfield = new StructureBoundingBox(max, 256, max, -max, 0, -max);
                boolean isReduced = false;
                for (int x = battlefield.minX; x <= battlefield.maxX; x++) {
                    for (int y = battlefield.minY; y <= battlefield.maxY; y++) {
                        for (int z = battlefield.minZ; z <= battlefield.maxZ; z++) {
                            BlockPos blockPos = new BlockPos(x, y, z);
                            IBlockState state = w.getBlockState(blockPos);
                            ResourceLocation regName = (ResourceLocation) Block.REGISTRY.getNameForObject(state.getBlock());
                            if (CfgProps.GRIEF_BLOCKS.get(null, null).contains(regName.toString())) {
                                boundBlocks.add(blockPos);
                                isReduced = true;
                                resetMinMax(x, y, z, reducedfield);
                            }

                        }
                    }
                }
                /**IF THERE ARE TARGET BLOCKS, TERRITORY REDUCED, ELSE SET DEFAULT 4x4 CHUNKS**/
                battle.playerTerritoty = isReduced ? reducedfield : battlefield;
                battle.targetBlocks = boundBlocks;

            } else {
                BlockPos pos = player.getPosition();
                StructureBoundingBox battlefield = new StructureBoundingBox(pos.add(-AMBUSH_RADIUS * 16, -16, -AMBUSH_RADIUS * 16),
                        pos.add(AMBUSH_RADIUS * 16, 16, AMBUSH_RADIUS * 16));
                for (int x = battlefield.minX; x <= battlefield.maxX; x++) {
                    for (int y = battlefield.minY; y <= battlefield.maxY; y++) {
                        for (int z = battlefield.minZ; z <= battlefield.maxZ; z++) {
                            BlockPos blockPos = new BlockPos(x, y, z);
                            IBlockState state = w.getBlockState(blockPos);
                            ResourceLocation regName = (ResourceLocation) Block.REGISTRY.getNameForObject(state.getBlock());
                            if (CfgProps.GRIEF_BLOCKS.get(null, null).contains(regName.toString())) {
                                boundBlocks.add(blockPos);

                            }
                        }
                    }
                }
                battle.playerTerritoty = battlefield;
                battle.targetBlocks = boundBlocks;
            }
        } else {
            if (!ambush) {
                BlockPos bed = player.getBedLocation();
                boundBlocks.add(bed);
                StructureBoundingBox battlefield = new StructureBoundingBox(bed.add(-HOME_CHUNK_RADIUS * 16, -16, -HOME_CHUNK_RADIUS * 16),
                        bed.add(HOME_CHUNK_RADIUS * 16, 16, HOME_CHUNK_RADIUS * 16));
                battle.playerTerritoty = battlefield;
                battle.targetBlocks = boundBlocks;
            } else {
                BlockPos pos = player.getPosition();
                boundBlocks.add(pos);
                StructureBoundingBox battlefield = new StructureBoundingBox(pos.add(-HOME_CHUNK_RADIUS * 16, -16, -HOME_CHUNK_RADIUS * 16),
                        pos.add(HOME_CHUNK_RADIUS * 16, 16, HOME_CHUNK_RADIUS * 16));
                battle.playerTerritoty = battlefield;
                battle.targetBlocks = boundBlocks;
            }
        }
    }

    public static void chooseSpawnPoint(Battle battle, World world) {
        if (!(battle.battleType == Battle.BattleType.AMBUSH)) {
            int RANGE = 32;
            double rx = Math.random();
            int signX = Math.random() - 0.5D > 0 ? 1 : -1;
            double x0 = ((battle.playerTerritoty.maxX + battle.playerTerritoty.minX) / 2);
            double sizeX = ((battle.playerTerritoty.maxX - battle.playerTerritoty.minX) / 2);
            double rz = Math.random();
            int signZ = Math.random() - 0.5D > 0 ? 1 : -1;
            double z0 = ((battle.playerTerritoty.maxZ + battle.playerTerritoty.minZ) / 2);
            double sizeZ = ((battle.playerTerritoty.maxZ - battle.playerTerritoty.minZ) / 2);
            boolean xfull = Math.random() - 0.5D > 0 ? true : false;
            int x;
            int z;
            if (xfull) {
                x = (int) (x0 + (rx * 16 + sizeX + RANGE) * signX);
                z = (int) (z0 + (rz * (sizeZ + RANGE)) * signZ);
            } else {
                z = (int) (z0 + (rz * 16 + sizeZ + RANGE) * signZ);
                x = (int) (x0 + (rx * (sizeX + RANGE)) * signX);
            }
            int y = ((battle.playerTerritoty.maxY + battle.playerTerritoty.minY) / 2);
            BlockPos pos = new BlockPos(x, y, z);
            BlockPos newpos = BlockUtils.findNearbyFloorSpace(world, pos, 16, 32, false);
            battle.spawnPoint = newpos != null ? newpos : pos;
            //world.setBlockState(battle.spawnPoint, Blocks.BEACON.getDefaultState());
        } else {
            BlockPos pos = battle.partOfWar.player.getPosition();
            BlockPos newpos = BlockUtils.findNearbyFloorSpace(world, pos, 32, 8, true);
            battle.spawnPoint = newpos != null ? newpos : pos;
        }

    }

    public static void resetMinMax(int x, int y, int z, StructureBoundingBox reducedfield) {
        if (x > reducedfield.maxX) {
            reducedfield.maxX = x;
        }
        if (x < reducedfield.minX) {
            reducedfield.minX = x;
        }
        if (y > reducedfield.maxY) {
            reducedfield.maxY = y;
        }
        if (y < reducedfield.minY) {
            reducedfield.minY = y;
        }
        if (z > reducedfield.maxZ) {
            reducedfield.maxZ = z;
        }
        if (z < reducedfield.minZ) {
            reducedfield.minZ = z;
        }
    }

    public static void replaceBlocks(String name, World world, Battle battle) {
        for(BlockPos b : battle.targetBlocks) {
            if (Block.REGISTRY.getNameForObject(world.getBlockState(b).getBlock()).toString().equals(name)) {
                world.setBlockState(b, Blocks.NETHER_BRICK_FENCE.getDefaultState());
            }
        }
    }
}
