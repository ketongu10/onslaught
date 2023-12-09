package onslaught.ketongu10.war.LongMarch;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.Constants;
import onslaught.ketongu10.capabilities.ModCapabilities;
import onslaught.ketongu10.capabilities.world.WarData;
import onslaught.ketongu10.network.packets.NetworkManager;
import onslaught.ketongu10.network.packets.StartClientWar;
import onslaught.ketongu10.network.packets.WarTimerUpdate;
import onslaught.ketongu10.util.NBTHelpers;
import onslaught.ketongu10.war.Battle;
import onslaught.ketongu10.war.War;
import onslaught.ketongu10.war.WarsManager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static onslaught.ketongu10.util.handlers.ConfigHandler.*;

public class WarLongMarch extends War {
    protected int start_x;
    protected int start_z;
    protected int end_x;
    protected int end_z;
    protected float speed = 1;
    public float progress;
    public Chunk dislocation;
    public WarLongMarch(World w, @Nullable EntityPlayer player, String faction, @Nullable String subfaction, UUID warId,
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

    public void update() {

        this.timer++;
        if (timer % 200 == 0) {
            printWarInfo();
        }
        for (Battle battle : this.presentBattles) {
            if (!battle.finished) {
                battle.update();
            } else {
                this.finishedBattles.add(battle);
            }
        }
        for (Battle fbattle : this.finishedBattles) {
            this.presentBattles.remove(fbattle);
        }
        this.plot.accept(this.startAfter);
        finishedBattles.clear();

        if (this.timer > BREAK_TIME) {
            stopWar();
        }



    }

    @Override
    protected Consumer<Integer> getPlot() {
        return this::LongMarch;
    }

    protected void LongMarch(Integer arrivingTime) {
        if (timer==startAfter) {
            startMoving();
        }

        if (timer>startAfter && presentBattles.isEmpty() && finishedBattles.isEmpty()) {
            if (siegeStarted) {
                deployForces();
            } else {
                continueMoving();
            }
            //return;
        }

        if (!finishedBattles.isEmpty()) {
            if (player.isDead && checkNearPlayers()) {
                removeForces();
                continueMoving();
                siegeStarted = false;
            } else {
                stopWar();
            }
        }
        if (timer > startAfter && !siegeStarted && dislocation.x==end_x && dislocation.z==end_z) {
            stopWar();
        }
    }
    protected void startMoving() {
        this.progress = 0;
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
            this.progress += this.speed/distance;
            int x =(int) (start_x + (end_x - start_x)*progress);
            int z =(int) (start_z + (end_z - start_z)*progress);
            x = x < end_x ? x: end_x;
            z = z < end_z ? z: end_z;
            Chunk prev_loc = dislocation;
            this.dislocation = world.getChunkFromChunkCoords(x, z);
            if (this.dislocation.isLoaded()) {
                this.siegeStarted = true;
            }

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
        startNewBattle();
    }

    protected void removeForces() {
        BlockPos mellon = new BlockPos(dislocation.x*16+8, dislocation.getHeightValue(dislocation.x*16+8, dislocation.z*16+8)+8, dislocation.z*16+8);
        if (world.getBlockState(mellon) == Blocks.MELON_BLOCK.getDefaultState()) {
            world.setBlockState(mellon, Blocks.AIR.getDefaultState());
            System.out.println("!=!=!=!=!=!=!=!=!REMOVED AT"+dislocation.x+"  "+dislocation.z+"AT!=!=!=!=!=!=!=!=!");
        }


    }

    protected void startNewBattle() {
        print("--------TRYING TO START NEW BATTLE-------------");
        WarData cap = world.getCapability(ModCapabilities.WAR_DATA, null);
        System.out.println("------cap==null: "+cap==null);
        if (cap != null && cap instanceof WarsManager) {
            print("------cap is WarMan: "+(cap instanceof WarsManager));
            print("------player is in List: "+((WarsManager)cap).players.contains(player));
            if (((WarsManager)cap).players.contains(player)) {

                print("===============NEW BATTLE CREATED==============");
                Vec3d direct = new Vec3d(end_x-start_x, 0, end_z-start_z).normalize();
                this.presentBattles.add(new BattleMarch(this, Battle.BattleType.MARCH, direct));
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

        int x =(int) (start_x + (end_x - start_x)*progress);
        int z =(int) (start_z + (end_z - start_z)*progress);
        x = x < end_x ? x: end_x;
        z = z < end_z ? z: end_z;
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
