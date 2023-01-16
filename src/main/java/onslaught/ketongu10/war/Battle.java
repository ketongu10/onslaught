package onslaught.ketongu10.war;

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
import onslaught.ketongu10.war.units.*;

import java.util.*;
import java.util.function.Consumer;

import static onslaught.ketongu10.util.handlers.ConfigHandler.*;


public class Battle implements INBTSerializable<NBTTagCompound>{
    public War partOfWar;
    protected Long timer;
    public List<UnitBase> UNITS = new ArrayList<>();
    public List<UnitBase> arrivingUNITS = new ArrayList<>();
    public List<BlockPos> targetBlocks = new ArrayList<>();
    public StructureBoundingBox playerTerritoty;
    public BlockPos spawnPoint;
    public boolean finished = false;
    protected Consumer plot;
    protected int wavesLeft = 0;
    protected int wavesTotal = 0;
    public BattleType battleType;


    public Battle(War war, BattleType type) {
        this.timer = 0L;
        this.partOfWar = war;
        this.battleType = type;
        this.plot = this.getPlot();
    }

    public Battle(War war) {
        this.partOfWar = war;
    }

    protected Consumer<Integer> getPlot() {
        switch (battleType) {
            case PATROL:
                this.wavesLeft = 1;
                this.wavesTotal = 1;
                return this::patrol;
            case AMBUSH:
                this.wavesLeft = 1;
                this.wavesTotal = 1;
                return this::ambush;
            case SIEGE:
                this.wavesLeft = 3;
                this.wavesTotal = 3;
                return this::siege;
            case APOCALIPSE:
                this.wavesLeft = 10;
                this.wavesTotal = 10;
                return this::apocalypse;
            default:return this::patrol;
        }
    }
    protected void ambush(int a) {//ambush must start immediately
        if (timer==10) {
            prepareWave(0);
            return;
        }
        if (timer==20) {
            setUpWave();
            return;
        }
        if (timer==30) {
            startWave();
            return;
        }
    }

    protected void patrol(int a) {
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
    protected void siege(int a) {
        int d = wavesTotal-wavesLeft < 3 ? wavesTotal-wavesLeft : 0;
        int wave = d+1;
        if (timer==100+TIME_BETWEEN_WAVES*d) {
            prepareWave(wave);
            print("===============PREPAIRING "+wave+" WAVE=============");
            return;
        }
        if (timer==200+TIME_BETWEEN_WAVES*d) {
            setUpWave();
            print("===============SETTING UP "+wave+" WAVE=============");
            return;
        }
        if (timer==300+TIME_BETWEEN_WAVES*d) {
            wavesLeft--;
            startWave();
            print("===============STARTING "+wave+" WAVE=============");
            return;
        }
    }
    protected void apocalypse(int a) {
        int d = wavesTotal-wavesLeft;
        int wave = d+1 < 4 ? d+1 : 2; //all waves after 3rd are like 2nd
        if (timer==100+TIME_BETWEEN_WAVES*d) {
            prepareWave(wave);
            return;
        }
        if (timer==200+TIME_BETWEEN_WAVES*d) {
            setUpWave();
            return;
        }
        if (timer==300+TIME_BETWEEN_WAVES*d) {
            startWave();
            wavesLeft--;
            return;
        }
    }

    public void update() {
        timer++;
        if (timer==1) {
            BattlefieldHelper.prepareBattlefield(false, partOfWar.player, this);
            //BattlefieldHelper.replaceBlocks("minecraft:fence", partOfWar.player.world, this);
            BattlefieldHelper.chooseSpawnPoint(this, partOfWar.player.world);
            this.targetBlocks.sort((a, b) -> compareB(a, b));
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
            printBattleInfo();
            checkUnits();

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

    public void checkPlayer() {
        BlockPos p = partOfWar.player.getPosition();
        if (!playerTerritoty.intersectsWith(p.getX()-16, p.getZ()-16, p.getX()+16, p.getZ()+16)) {
            partOfWar.player.sendMessage(new TextComponentTranslation(TextFormatting.RED+"Your home is under attack!!!"));
        }
    }


    public void updateBlockTargets() {
        if (battleType!=BattleType.AMBUSH) {
            World w = partOfWar.player.world;
            List<BlockPos> toRemove = new ArrayList<>();
            for (BlockPos b : targetBlocks) {
                String name = Block.REGISTRY.getNameForObject(w.getBlockState(b).getBlock()).toString();
                if (!CfgProps.GRIEF_BLOCKS.get(null, null).contains(name)) {
                    toRemove.add(b);
                }
            }
            for (BlockPos b : toRemove) {
                targetBlocks.remove(b);
            }
            int len = targetBlocks.size();
            if (len == 0) {
                return;
            }

            int i = len;
            for (UnitBase u : UNITS) {
                i = i > 0 ? --i : len - 1;

                for (EntityLiving e : u.entities) {
                    UnitCapability cap = e.getCapability(ModCapabilities.UNIT_CAPABILITY, null);
                    if (cap != null) {
                        if (shouldResetTarget(cap)) {
                            //w.setBlockState(targetBlocks.get(i), Blocks.CHEST.getDefaultState());
                            cap.setTarget(targetBlocks.get(i));
                        }
                    }
                }
            }
        }
        /**for (Unit u : UNITS) {
            for (EntityLiving e : u.entities) {
                UnitCapability cap = e.getCapability(ModCapabilities.UNIT_CAPABILITY, null);
                if (cap != null) {
                    if (cap.getTarget() == null) {
                        cap.setTarget(targetBlocks.get(i-1));
                    }
                }
            }
        }**/
    }

    private boolean shouldResetTarget(UnitCapability cap) {
        if (cap.getTarget() == null) {
            return true;
        } else {
            World w = partOfWar.player.world;
            IBlockState state = w.getBlockState(cap.getTarget());
            if (state.getBlock() == Blocks.AIR) {
                return true;
            }
        }
        return false;
    }

    public void updateEntityTargets() {
        if (battleType != BattleType.AMBUSH) {
            World w = partOfWar.player.world;
            List<EntityLivingBase> entities = new ArrayList<>();
            entities.addAll(w.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(playerTerritoty.minX, playerTerritoty.minY, playerTerritoty.minZ, playerTerritoty.maxX + 1, playerTerritoty.maxY + 1, playerTerritoty.maxZ + 1), Battle::isDefender));
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

    public static boolean isDefender(EntityLivingBase entityIn) {
        if (FactionUnits.PlayerSoldiers.contains(entityIn.getClass())) {
            return true;
        }
        for (Class clazz:FactionUnits.PlayerSoldiers) {
            if (clazz.isAssignableFrom(entityIn.getClass())) {
                return true;
            }
        }
        return false;
    }

    public int compareE(EntityLivingBase a, EntityLivingBase b) {
        double d1 = a.getDistanceSq(this.spawnPoint);
        double d2 = b.getDistanceSq(this.spawnPoint);
        return d1>d2 ? -1 : 1;
    }
    public int compareB(BlockPos a, BlockPos b) {
        double d1 = a.distanceSq(spawnPoint);
        double d2 = b.distanceSq(spawnPoint);
        World w = this.partOfWar.player.world;
        if (Block.REGISTRY.getNameForObject(w.getBlockState(a).getBlock()).toString().equals("minecraft:bed") && !Block.REGISTRY.getNameForObject(w.getBlockState(b).getBlock()).toString().equals("minecraft:bed")) {
            return 1;
        } else if (Block.REGISTRY.getNameForObject(w.getBlockState(b).getBlock()).toString().equals("minecraft:bed") && !Block.REGISTRY.getNameForObject(w.getBlockState(a).getBlock()).toString().equals("minecraft:bed")) {
            return -1;
        } else {
            return d1 > d2 ? -1 : (d1 < d2 ? 1 : 0);
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
    }

    protected void prepareWave(int waveNum) {
        World world = partOfWar.player.getEntityWorld();
        //if (!world.isRemote) {
            for (int i = 0; i <= 7; i++) {
                FactionUnits.UnitType unitType = FactionUnits.UnitType.getTypeById(i);
                int unitNum = WAVES.get(waveNum).map.get(unitType);
                for (int num = 0; num < unitNum; num++) {
                    BlockPos spawnpoint = BlockUtils.findNearbyFloorSpace(world, this.spawnPoint, 8, 4, false);
                    //this.arrivingUNITS.add(new Unit(world, unitType, partOfWar.faction, partOfWar.subfaction, spawnpoint, unitType == FactionUnits.UnitType.HQ, this));
                    this.arrivingUNITS.add(getUnit(world, unitType, partOfWar.faction, partOfWar.subfaction, spawnpoint, unitType == FactionUnits.UnitType.HQ || partOfWar.warType == War.WarType.PATROL, this));
                    print("--------------------NEW " + partOfWar.faction + " UNIT CREATED------------");
                }
            }
        //}

    }
    public void printBattleInfo() {
        if (SHOW_BI) {
            System.out.println(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
            System.out.println(":::::::::::::::::timer: " + this.timer + ":::::::::::::::::::::::::::");
            System.out.println(":::::::::::::wavesLeft: " + this.wavesLeft + ":::::::::::::::::::::::::::");
            System.out.println("::::::::::::wavesTotal: " + this.wavesTotal + ":::::::::::::::::::::::::::");
            System.out.println("::::::::::::battleType: " + this.battleType.toString().toUpperCase() + ":::::::::::::::::::::::::::");
            System.out.println("::::::::::targetBlocks: " + this.targetBlocks.size() + ":::::::::::::::::::::::::::");
            System.out.println(":::::::::::::::::UNITs: " + this.UNITS.size() + ":::::::::::::::::::::::::::");
            System.out.println("::::::::::::spawnpoint: " + this.spawnPoint + ":::::::::::::::::::::::::::");
            System.out.println(":::::::playerTerritory: " + this.playerTerritoty.toString() + ":::::::::::::::::::::::::::");
            System.out.println(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
        }
    }

    public static UnitBase getUnit(World world, FactionUnits.UnitType type, String faction, String subfaction, BlockPos pos, boolean warlord, Battle battle) {
        switch (type) {
            case TROOPS:
                return new UnitTroops(world, type, faction, subfaction, pos, warlord, battle);
            case ELITE:
                return new UnitElite(world, type, faction, subfaction, pos, warlord, battle);
            case ARCHERS:
                return new UnitArchers(world, type, faction, subfaction, pos, warlord, battle);
            case SENTRIES:
                return new UnitSentries(world, type, faction, subfaction, pos, warlord, battle);
            case CAVALERY:
                return new UnitCavalery(world, type, faction, subfaction, pos, warlord, battle);
            case HQ:
                return new UnitHQ(world, type, faction, subfaction, pos, warlord, battle);
            case HS:
                if (faction == "AW") {//TO BE FIXED!!!!!
                    return new UnitHS(world, type, faction, subfaction, pos, warlord, battle);
                }
                return new UnitDemolishers(world, type, faction, subfaction, pos, warlord, battle);
            case DEMOLISHERS:
                return new UnitDemolishers(world, type, faction, subfaction, pos, warlord, battle);
        }
        return null;
    }
    public static UnitBase getUnit(FactionUnits.UnitType type, Battle battle, String faction) {
        switch (type) {
            case TROOPS:
                return new UnitTroops(type, battle);
            case ELITE:
                return new UnitElite(type, battle);
            case ARCHERS:
                return new UnitArchers(type, battle);
            case SENTRIES:
                return new UnitSentries(type, battle);
            case CAVALERY:
                return new UnitCavalery(type, battle);
            case HQ:
                return new UnitHQ(type, battle);
            case HS:
                if (faction == "AW") {//TO BE FIXED!!!!!
                    return new UnitHS(type, battle);
                }
                return new UnitTroops(type, battle);
            case DEMOLISHERS:
                return new UnitDemolishers(type, battle);
        }
        return null;
    }

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




    public static final List<Wave> WAVES = new ArrayList<Wave>();
    public static void fillWAVES() {
        WAVES.add(new Wave(1, 0, 0));
        WAVES.add(new Wave(2, 1, 1, 0, 1,0,0,1));
        WAVES.add(new Wave(2, 1, 1, 0, 1,0,0,0));
        WAVES.add(new Wave(2, 1, 0, 0, 2, 0, 1, 0));
        //WAVES.add(new Wave(4, 2, 0, 4, 0, 0, 0));
        //WAVES.add(new Wave(8, 4, 0, 8, 0, 1, 0));
    }

    private static class Wave {
        public Map<FactionUnits.UnitType, Integer> map = new HashMap<>();
        Wave(int troops, int elite, int demolishers, int cavalry, int archers, int sentries, int hq, int hs) {
            map.put(FactionUnits.UnitType.TROOPS, troops);
            map.put(FactionUnits.UnitType.ELITE, elite);
            map.put(FactionUnits.UnitType.DEMOLISHERS, demolishers);
            map.put(FactionUnits.UnitType.CAVALERY, cavalry);
            map.put(FactionUnits.UnitType.ARCHERS, archers);
            map.put(FactionUnits.UnitType.SENTRIES, sentries);
            map.put(FactionUnits.UnitType.HQ, hq);
            map.put(FactionUnits.UnitType.HS, hs);
        }
        Wave(int troops, int elite, int archers) {
            this(troops, elite, 0, 0, archers, 0, 0, 0);
        }
        Wave(int troops, int elite, int demolishers, int archers) {
            this(troops, elite, demolishers, 0, archers, 0, 0, 0);
        }
    }

    public enum BattleType {
        PATROL(0), AMBUSH(1), SIEGE(2), APOCALIPSE(3);
        final int id;

        BattleType(int id)
        {
            this.id = id;
        }

        public int getId()
        {
            return id;
        }

        static Map<String, BattleType> searchByName = Maps.<String, BattleType>newHashMap();

        static {
            searchByName.put("PATROL", BattleType.PATROL);
            searchByName.put("AMBUSH", BattleType.AMBUSH);
            searchByName.put("SIEGE", BattleType.SIEGE);
            searchByName.put("APOCALYPSE", BattleType.APOCALIPSE);
        }

        public static BattleType findByName(String name) {
            return searchByName.get(name);
        }
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
