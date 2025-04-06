package onslaught.ketongu10.war.LongMarch;

import net.minecraft.util.math.Vec3d;
import onslaught.ketongu10.war.Battle;

import com.google.common.collect.Maps;
import onslaught.ketongu10.capabilities.ModCapabilities;
import onslaught.ketongu10.capabilities.units.UnitCapability;
import onslaught.ketongu10.util.BattlefieldHelper;
import onslaught.ketongu10.util.BlockUtils;
import onslaught.ketongu10.util.NBTHelpers;
import funwayguy.epicsiegemod.config.props.CfgProps;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import onslaught.ketongu10.war.FactionUnits;
import onslaught.ketongu10.war.War;
import onslaught.ketongu10.war.units.*;

import java.util.*;
import java.util.function.Consumer;

import static onslaught.ketongu10.util.handlers.ConfigHandler.*;


public class BattleMarch extends Battle{
    private Vec3d direction;


    public BattleMarch(War war, onslaught.ketongu10.war.Battle.BattleType type, Vec3d direct) {
        super(war, type);
        this.direction = direct;
    }

    public BattleMarch(War war) {
        super(war);
    }

    @Override
    protected Consumer<Integer> getPlot() {

        this.wavesLeft = 1;
        this.wavesTotal = 1;
        return this::march;

    }


    protected void march(int a) {
        if (timer==100) {
            prepareWave(0);
            return;
        }
        if (timer==200) {
            setUpWave();
            return;
        }
        if (timer==300) {
            startWave();
            return;
        }
    }


    public void update() {
        timer++;
        if (timer==1) {
            BattlefieldHelper.prepareBattlefield(true, partOfWar.player, this);
            //BattlefieldHelper.replaceBlocks("minecraft:fence", partOfWar.player.world, this);
            BattlefieldHelper.chooseSpawnPoint(this, partOfWar.player.world);
            //this.targetBlocks.sort((a, b) -> compareB(a, b));
            print("targetBlocksSize: "+targetBlocks.size());
            //this.spawnPoint = partOfWar.player.getBedLocation();
        }
        this.plot.accept(0);
        if (shouldStopBattle()) {
            stopBattle();
        }
        if (timer%200==0) {//checks every 10 seconds
            updateBlockTargets();
            updateEntityTargets();
            checkPlayer();
            //printBattleInfo();
            checkUnits();
            printStatistics();

        }


    }

    protected void checkUnits() {
        if (timer > TIME_BETWEEN_WAVES*(wavesTotal)) {
            for (UnitBase u: UNITS) {
                if (u.entities.isEmpty()) {
                    UNITS.remove(u);
                }
            }
        }
    }
    @Override
    public void checkPlayer() {}

    @Override
    public void updateBlockTargets() {}



    public void updateEntityTargets() {
        if (battleType != onslaught.ketongu10.war.Battle.BattleType.AMBUSH) {
            World w = partOfWar.player.world;
            List<EntityLivingBase> entities = new ArrayList<>();
            entities.addAll(w.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(playerTerritoty.minX, playerTerritoty.minY, playerTerritoty.minZ, playerTerritoty.maxX + 1, playerTerritoty.maxY + 1, playerTerritoty.maxZ + 1), onslaught.ketongu10.war.Battle::isDefender));
            print("--------------targetEntitySize: " + entities);
            entities.sort((a, b) -> this.compareE(a, b));

            int i = entities.size();
            if (i == 0) {
                return;
            }

            for (UnitBase u : UNITS) {
                if (u.type == FactionUnits.UnitType.HS) {
                    i = i > 0 ? --i : 0;
                    for (EntityLiving e : u.entities) {
                        e.setAttackTarget(entities.get(i));
                    }
                } else if (u instanceof IRangeUnit) {
                    i = i > 0 ? --i : 0;
                    for (EntityLiving e : u.entities) {
                        if (e.getDistanceSq(entities.get(i)) < 1024) {
                            e.setAttackTarget(entities.get(i));
                        }
                    }
                }
            }
        } else {
            for (UnitBase u : UNITS) {
                for (EntityLiving e : u.entities) {
                    e.setAttackTarget(partOfWar.player);
                }
            }
        }


    }






    protected boolean shouldStopBattle() {
        if (timer>TIME_BETWEEN_WAVES*(wavesTotal) && UNITS.isEmpty()) {
            print("===============PLAYER KILLED ALL FOES==============");
            return true;
        }

        if (partOfWar.player.isDead) {
            print("===============PLAYER WAS KILLED==============");
            return true;
        }
        return false;
    }

    public void stopBattle() {
        this.arrivingUNITS.clear();
        for (UnitBase unit : this.UNITS) {
            unit.lastOrder();
        }
        this.finished = true;
        this.partOfWar = null;
    }

//    protected void prepareWave(int waveNum) {
//        World world = partOfWar.player.getEntityWorld();
//        //if (!world.isRemote) {
//        BlockPos curpos = null;
//        int n=0;
//        for (int i = 0; i <= 7; i++) {
//            FactionUnits.UnitType unitType = FactionUnits.UnitType.getTypeById(i);
//            int unitNum = WAVES.get(waveNum).map.get(unitType);
//            for (int num = 0; num < unitNum; num++) {
//                curpos = new BlockPos(
//                        this.spawnPoint.getX() + this.direction.x * Math.pow(-1, n)*(int)((n+1)/2)*8,
//                        0,
//                        this.direction.x * Math.pow(-1, n)*(int)((n+1)/2)*8);
//                n++;
//                BlockPos spawnpoint = BlockUtils.findNearbyFloorSpace(world, curpos, 8, 4, false);
//                //this.arrivingUNITS.add(new Unit(world, unitType, partOfWar.faction, partOfWar.subfaction, spawnpoint, unitType == FactionUnits.UnitType.HQ, this));
//                this.arrivingUNITS.add(getUnit(world, unitType, partOfWar.faction, partOfWar.subfaction, spawnpoint, unitType == FactionUnits.UnitType.HQ || partOfWar.warType == War.WarType.PATROL, this));
//                print("--------------------NEW " + partOfWar.faction + " UNIT CREATED------------");
//            }
//        }
//        //}
//
//    }




    protected void setUpWave() {
        for(UnitBase u:arrivingUNITS) {
            print("----------------UNIT SPAWNED-------------");
            u.spawnNoAI();
        }
    }

    protected void startWave() {
        print("---------------SIEGE STARTS----------------");
        for (UnitBase unit : this.arrivingUNITS) {
            unit.startSiege();
        }
        this.UNITS.addAll(arrivingUNITS);
        this.arrivingUNITS.clear();
    }







    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setLong("timerBattle", this.timer);
        tag.setInteger("wavesLeft", this.wavesLeft);
        tag.setInteger("wavesTotal", this.wavesTotal);
        tag.setString("battleType", this.battleType.toString());
        tag.setBoolean("finished", this.finished);
        if (this.spawnPoint != null) {
            tag.setTag("spawnPoint", NBTHelpers.writeBlockPosToNBT(new NBTTagCompound(), this.spawnPoint));
        }
        if (this.playerTerritoty != null) {
            tag.setTag("playerTerritory", NBTHelpers.writeStructureBoxToNBT(new NBTTagCompound(), this.playerTerritoty));
        }
        tag.setTag("targetBlocks", NBTHelpers.getTagList(this.targetBlocks, NBTHelpers::writeBlockPosToNBT));
        tag.setTag("units", NBTHelpers.getTagList(this.UNITS, UnitBase::serializeNBT));
        tag.setTag("arrivingUnits", NBTHelpers.getTagList(this.arrivingUNITS, UnitBase::serializeNBT));
        System.out.println("+++++++++++++++++++++++targetBlocks len: "+this.targetBlocks.size()+" +++++++++++++++++++++++");
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        this.timer = tag.getLong("timerBattle");
        this.battleType = NBTHelpers.battleTypeFromNBT(tag);
        this.plot = this.getPlot();
        this.wavesLeft = tag.getInteger("wavesLeft");
        this.wavesTotal = tag.getInteger("wavesTotal");
        this.finished = tag.getBoolean("finished");
        this.spawnPoint = NBTHelpers.readBlockPosFromNBT(tag.getCompoundTag("spawnPoint"));
        this.playerTerritoty = NBTHelpers.readStructureBoxFromNBT(tag.getCompoundTag("playerTerritory"));
        Set<BlockPos> targetBlocksSet = NBTHelpers.getSet(tag.getTagList("targetBlocks", Constants.NBT.TAG_COMPOUND), n -> {
            return NBTHelpers.readBlockPosFromNBT((NBTTagCompound) n);
        });
        for (BlockPos blockPos : targetBlocksSet) {
            this.targetBlocks.add(blockPos);
        }





        Set<UnitBase> unitsSet = NBTHelpers.getSet(tag.getTagList("units", Constants.NBT.TAG_COMPOUND), n -> {
            FactionUnits.UnitType type = NBTHelpers.unitTypeFromNBT((NBTTagCompound) n);
            UnitBase u = getUnit(type,this, this.partOfWar.faction);
            u.deserializeNBT((NBTTagCompound) n);
            return u;
        });

        Set<UnitBase> arrivingUnitsSet = NBTHelpers.getSet(tag.getTagList("arrivingUnits", Constants.NBT.TAG_COMPOUND), n -> {
            FactionUnits.UnitType type = NBTHelpers.unitTypeFromNBT((NBTTagCompound) n);
            UnitBase u = getUnit(type,this, this.partOfWar.faction);
            u.deserializeNBT((NBTTagCompound) n);
            return u;
        });

        for (UnitBase u : unitsSet) {
            this.UNITS.add(u);
        }
        for (UnitBase u : arrivingUnitsSet) {
            this.arrivingUNITS.add(u);
        }


    }


}

