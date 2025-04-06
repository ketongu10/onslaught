package onslaught.ketongu10.war.LongMarch;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.Constants;
import onslaught.ketongu10.capabilities.ModCapabilities;
import onslaught.ketongu10.capabilities.world.WarData;
import onslaught.ketongu10.network.packets.NetworkManager;
import onslaught.ketongu10.network.packets.StartClientWar;
import onslaught.ketongu10.util.BattlefieldHelper;
import onslaught.ketongu10.util.NBTHelpers;
import onslaught.ketongu10.war.Battle;
import onslaught.ketongu10.war.FactionUnits;
import onslaught.ketongu10.war.War;
import onslaught.ketongu10.war.WarsManager;

import java.util.*;
import javax.annotation.Nullable;
import java.util.function.Consumer;

import static onslaught.ketongu10.util.handlers.ConfigHandler.TIME_TO_PATROL;
import static onslaught.ketongu10.util.handlers.ConfigHandler.print;
import static onslaught.ketongu10.war.LongMarch.LongMarchUtils.*;

public class WarLongMarch extends War {
    protected int start_x;
    protected int start_z;
    protected int end_x;
    protected int end_z;
    protected float speed = 1;
    public float progress;
    public Vec3d direction;
    public boolean forcesAreDefeated = false;
    public Chunk dislocation;

    public WarLongMarch(World w, EntityPlayer player, String faction, @Nullable String subfaction, UUID warId,
                         int delay, int x1, int z1, int x2, int z2, boolean sameDim) {
        super(w, player,faction, subfaction,  warId,  WarType.LONGMARCH, delay, player.dimension == 0);
        this.start_x = x1;
        this.start_z = z1;
        this.end_x = x2;
        this.end_z = z2;
        if (!this.finished) {
            if (sameDim) {
                NetworkManager.sendToPlayer(new StartClientWar(this.serializeNBT(), this.warUUID), (EntityPlayerMP) player);
            }
            if (!faction.equals("AW")) {
                player.sendMessage(new TextComponentTranslation(TextFormatting.RED + faction + "s " + "are transferring forces"));
            } else {
                player.sendMessage(new TextComponentTranslation(TextFormatting.RED + subfaction + "s " + "are transferring forces"));
            }
        }
        if (!sameDim) {
            this.player = null;
        }
    }

    public WarLongMarch(World w) {
        super(w);
    }

    @Override
    protected Consumer<Integer> getPlot() {
        return this::LongMarch;
    }

    protected void LongMarch(Integer arrivingTime) {
        if (timer==startAfter) {
            startMoving();
        }

        this.siegeStarted = isChunkViewedByAnyPlayer();
        if (timer>startAfter && siegeStarted) {
            if (presentBattles.isEmpty() && finishedBattles.isEmpty()) {
                deployForces();
            }
        }
        if (timer>startAfter && !siegeStarted) {
            if (presentBattles.isEmpty() && finishedBattles.isEmpty()) {
                continueMoving();
            }
        }

        if (timer>startAfter && !siegeStarted) {
            if (!presentBattles.isEmpty()) {
                removeForces();
                continueMoving();
            }
        }

        if (timer>startAfter && siegeStarted) {
            if (!finishedBattles.isEmpty()) {
                siegeStarted = false;
                stopWar();
            }
        }

//
//        if (!finishedBattles.isEmpty() && forcesAreDefeated) {
//            if (player.isDead && checkNearPlayers()) {
//                removeForces();
//                continueMoving();
//                siegeStarted = false;
//            } else {
//                stopWar();
//            }
//        }
        if (timer > startAfter && !siegeStarted && dislocation.x==end_x && dislocation.z==end_z) {
            stopWar();
        }
    }

    protected boolean isChunkViewedByAnyPlayer() {

        PlayerChunkMap chunkMap = ((WorldServer) world).getPlayerChunkMap();
        if (chunkMap != null && dislocation != null) {
            PlayerChunkMapEntry chunkEntry = chunkMap.getEntry(dislocation.x, dislocation.z);

            if (chunkEntry == null) {
                return false; // Чанк не загружен
            }

            // Получаем всех игроков, наблюдающих за чанком
            List<EntityPlayerMP> players = chunkEntry.getWatchingPlayers();
            return !players.isEmpty();
        }
        return false;

    }
    protected void startMoving() {
        this.progress = 0;
        this.direction = new Vec3d(end_x - start_x, 0, end_z - start_z);
        this.dislocation = this.world.getChunkFromChunkCoords(start_x, start_z);

        WarData cap = world.getCapability(ModCapabilities.WAR_DATA, null);
        if (cap != null && cap instanceof WarsManager) {
            if (((WarsManager) cap).dislocations.containsKey(this.dislocation)) {
                ((WarsManager) cap).dislocations.get(this.dislocation).add(this);
            } else {
                List<War> list = new ArrayList<>();
                list.add(this);
                ((WarsManager) cap).dislocations.put(this.dislocation, list);
            }

        }


    }
    protected void continueMoving() {
        if (timer % 40 == 0) {
            double distance = Math.sqrt((end_x-start_x)*(end_x-start_x) + (end_z-start_z)*(end_z-start_z));
            progress += this.speed/distance;
            progress = progress > 1 ? 1 : progress;
            int x =(int) (start_x + direction.x*progress);
            int z =(int) (start_z + direction.z*progress);
            Chunk prev_loc = dislocation;
            if ((x != prev_loc.x || z != prev_loc.z) && (x != end_x || z != end_z)) {
                this.spawnCorpses();
            }
            this.dislocation = world.getChunkFromChunkCoords(x, z);



            WarData cap = world.getCapability(ModCapabilities.WAR_DATA, null);
            if (cap != null && cap instanceof WarsManager) {
                if (((WarsManager) cap).dislocations.containsKey(this.dislocation)) {
                    ((WarsManager) cap).dislocations.get(this.dislocation).add(this);
                } else {
                    List<War> list = new ArrayList<>();
                    list.add(this);
                    ((WarsManager) cap).dislocations.put(this.dislocation, list);
                }
                ((WarsManager) cap).dislocations.get(prev_loc).remove(this);
                if (((WarsManager) cap).dislocations.get(prev_loc).isEmpty()) {
                    ((WarsManager) cap).dislocations.remove(prev_loc);
                }

            }


            System.out.println("!=!=!=!=!=!=!=!=!NOW"+dislocation.x+"  "+dislocation.z+"NOW!=!=!=!=!=!=!=!=!");

            System.out.println("!=!=!=!=!=!=!=!=!HAS"+((WarsManager)cap).dislocations.containsKey(this.dislocation)+"HAS!=!=!=!=!=!=!=!=!");
        }
    }

    protected void deployForces() {
        //dislocation.getHeightValue(dislocation.x*16+8, dislocation.z*16+8)+8

        BlockPos mellon = new BlockPos(dislocation.x*16+8, 120, dislocation.z*16+8);
        world.setBlockState(mellon, Blocks.MELON_BLOCK.getDefaultState());
        System.out.println("!=!=!=!=!=!=!=!=!DEPLOYED AT"+dislocation.x+"  "+dislocation.z+"AT!=!=!=!=!=!=!=!=!");

        startNewBattle(Battle.BattleType.AMBUSH);


    }

    protected void removeForces() {
//        BlockPos mellon = new BlockPos(dislocation.x*16+8, dislocation.getHeightValue(dislocation.x*16+8, dislocation.z*16+8)+8, dislocation.z*16+8);
//        if (world.getBlockState(mellon) == Blocks.MELON_BLOCK.getDefaultState()) {
//            world.setBlockState(mellon, Blocks.AIR.getDefaultState());
//            ;
//        }
        System.out.println("!=!=!=!=!=!=!=!=!REMOVED AT"+dislocation.x+"  "+dislocation.z+"AT!=!=!=!=!=!=!=!=!");
        for (Battle bat : this.presentBattles) {
            bat.stopBattle();
        }

    }

    protected void spawnCorpses() {
        System.out.println(dislocation.x+"_"+dislocation.z);
        int h = world.getHeight( (dislocation.x<<4)+8,  (dislocation.z<<4)+8);
        BlockPos mellon = new BlockPos((dislocation.x<<4)+8, h-1, (dislocation.z<<4)+8);
        System.out.println("!=!=!=!=!=!=!=!=!SPAWNED AT"+mellon.getX()+"  "+mellon.getY()+"  "+mellon.getZ()+"AT!=!=!=!=!=!=!=!=!");
        BattlefieldHelper.replaceBlocksAlongDirection(world, mellon, direction, Blocks.GRASS_PATH, TROPINKA_WIDTH);
        if (TROPINKA_CORPSES != null && TROPINKA_CORPSES.size() > 0) {
            if (world.rand.nextFloat() < 0.2) {
                int size = LongMarchUtils.TROPINKA_CORPSES.size();
                Map.Entry<Block, List<NBTTagCompound>> special = LongMarchUtils.TROPINKA_CORPSES.get(world.rand.nextInt(size));
                Block block = special.getKey();
                NBTTagCompound tag = special.getValue().get(world.rand.nextInt(special.getValue().size()));
                int howmany = world.rand.nextInt(TROPINKA_CORPSES_MAX_NUM) + 1;
                for (int i = 0; i < howmany; i++) {
                    int x_sh = world.rand.nextInt(2 * TROPINKA_CORPSES_WIDTH + 1) - TROPINKA_CORPSES_WIDTH;
                    int z_sh = world.rand.nextInt(2 * TROPINKA_CORPSES_WIDTH + 1) - TROPINKA_CORPSES_WIDTH;
                    int _x = (dislocation.x << 4) + 8 + x_sh;
                    int _z = (dislocation.z << 4) + 8 + z_sh;
                    h = world.getHeight(_x, _z);
                    BlockPos spe_pos = new BlockPos(_x,  h, _z);
                    BlockPos spe_pos_down = new BlockPos(_x,h-1, _z);
                    if (world.isAirBlock(spe_pos) && !LongMarchUtils.TROPINKA_BLACKLIST.contains(world.getBlockState(spe_pos_down).getBlock())) {
                        Rotation[] rotations = Rotation.values(); // 4 варианта: NONE, CLOCKWISE_90, etc.
                        Rotation randomRotation = rotations[world.rand.nextInt(rotations.length)];
                        IBlockState blockstate = block.getDefaultState();
                        blockstate = blockstate.withRotation(randomRotation);
                        world.setBlockState(spe_pos,blockstate);

                        TileEntity tile = world.getTileEntity(spe_pos);
                        if (tile != null) {
                            print("CHANGING TAGS!!!");
                            // Применяем NBT-теги
                            tile.readFromNBT(tag);
                            tile.markDirty();
                        }
                    }
                }
            }
        }


    }

    protected boolean checkNearPlayers() {
        return true;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        System.out.println("PRESENT BATTLES LEN = "+this.presentBattles.size());
        NBTTagCompound tag = super.serializeNBT();

        tag.setInteger("start_x", this.start_x);
        tag.setInteger("start_z", this.start_z);
        tag.setInteger("end_x", this.end_x);
        tag.setInteger("end_z", this.end_z);
        tag.setFloat("speed", this.speed);
        tag.setFloat("progress", this.progress);
        tag.setBoolean("forcesAreDefeated", this.forcesAreDefeated);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        this.presentBattles.clear();
        this.finishedBattles.clear();
        this.bossName = tag.getString("bossNameWar");
        this.icon = tag.getString("icon");
        this.faction = tag.getString("factionWar");
        this.subfaction = tag.getString("subfactionWar") == "" ? null : tag.getString("subfactionWar");
        this.timer = tag.getLong("timerWar");
        this.startAfter = tag.getInteger("startAfterWar");
        this.warType = NBTHelpers.warTypeFromNBT(tag);
        this.playerUUID = tag.getUniqueId("playerUUID");
        this.warUUID = tag.getUniqueId("warUUID");
        this.finished = tag.getBoolean("finished");
        this.player = (EntityPlayerMP) world.getPlayerEntityByUUID(this.playerUUID);
        this.plot = this.getPlot();

        this.start_x = tag.getInteger("start_x");
        this.start_z = tag.getInteger("start_z");
        this.end_x = tag.getInteger("end_x");
        this.end_z = tag.getInteger("end_z");
        this.speed = tag.getFloat("speed");
        this.progress = tag.getFloat("progress");
        this.forcesAreDefeated = tag.getBoolean("forcesAreDefeated");

        this.direction = new Vec3d(end_x - start_x, 0, end_z - start_z);
        progress = progress > 1 ? 1 : progress;
        int x =(int) (start_x + direction.x*progress);
        int z =(int) (start_z + direction.z*progress);
        this.dislocation = world.getChunkFromChunkCoords(x, z);

        WarData cap = world.getCapability(ModCapabilities.WAR_DATA, null);
        if (cap != null && cap instanceof WarsManager) {
            if (((WarsManager) cap).dislocations.containsKey(this.dislocation)) {
                ((WarsManager) cap).dislocations.get(this.dislocation).add(this);
            } else {
                List<War> list = new ArrayList<>();
                list.add(this);
                ((WarsManager) cap).dislocations.put(this.dislocation, list);
            }

        }


        Set<Battle> playerBattleSet = NBTHelpers.getSet(tag.getTagList("battles", Constants.NBT.TAG_COMPOUND), n -> {
            Battle battle = new Battle(this);
            battle.deserializeNBT((NBTTagCompound) n);
            return battle;
        });
        for (Battle b : playerBattleSet) {
            print("Found battle");
            this.presentBattles.add(b);
        }


    }
}
