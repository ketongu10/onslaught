package onslaught.ketongu10.war;

import com.google.common.collect.Maps;
import onslaught.ketongu10.capabilities.ModCapabilities;
import onslaught.ketongu10.capabilities.world.WarData;
import onslaught.ketongu10.network.packets.NetworkManager;
import onslaught.ketongu10.network.packets.StartClientWar;
import onslaught.ketongu10.network.packets.StopClientWar;
import onslaught.ketongu10.network.packets.WarTimerUpdate;
import onslaught.ketongu10.util.BlockUtils;
import onslaught.ketongu10.util.NBTHelpers;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.shadowmage.ancientwarfare.npc.entity.faction.NpcFaction;

import java.util.*;
import java.util.function.Consumer;

import static onslaught.ketongu10.util.handlers.ConfigHandler.*;


public class War implements INBTSerializable<NBTTagCompound> {
    public String bossName;
    public String icon;
    public String faction;
    public String subfaction;
    public World world;
    public EntityPlayer player; //better to use gameprofiles
    public UUID playerUUID; //better to use gameprofiles
    public UUID warUUID;
    protected List<Battle> presentBattles = new ArrayList<>();
    protected List<Battle> finishedBattles = new ArrayList<>();
    protected Long timer;
    protected Long waiting = 0L;
    public WarType warType;
    protected Consumer plot;
    public int startAfter;
    public boolean finished = false;
    protected boolean siegeStarted = false;


    public War(World world, EntityPlayer player, EntityLiving boss, UUID warId, boolean sameDim) {
        this.icon = BossList.classBossList.get(boss.getClass()).icon;
        this.faction = this.getFaction(boss);
        this.warType = this.getWarType();
        this.bossName = this.genBossName(boss);
        this.startAfter = this.getStartTime();
        this.player = player;
        this.playerUUID = player.getUniqueID();
        this.warUUID = warId;
        this.world = world;
        this.plot = getPlot();
        this.timer = 0L;
        if (!this.finished && warType!=WarType.AMBUSH) {
            if (sameDim) {NetworkManager.sendToPlayer(new StartClientWar(this.serializeNBT(), this.warUUID), (EntityPlayerMP) player);}
            player.sendMessage(new TextComponentTranslation(TextFormatting.RED + this.bossName + " " + "declared war on" + " " + player.getGameProfile().getName()));
        }
        if (!sameDim) {
            this.player = null;
        }
        //player.playSound(SoundsHandler.WAR_START, 1, 0);
    }

    public War(World w) {
        this.world = w;
    }

    protected Consumer<Integer> getPlot() {
        switch (this.warType) {
            case PATROL:
                return this::Patrol;
            case SIEGE:
                return this::LongMarsh;
            case AMBUSH:
                return this::Ambush;
            case APOCALYPSE:
                return this::Apocalypse;
        }
        return this::Patrol;
    }

    protected void Patrol(int startAfter) {
        if (timer>startAfter && presentBattles.isEmpty() && finishedBattles.isEmpty() && additionalConditions()) {
            startNewBattle(Battle.BattleType.PATROL);
            return;
        }
        if (timer > startAfter && !finishedBattles.isEmpty()) {
            stopWar();
        }


    }
    protected void Ambush(int finishAfter) {
        if (timer%AMBUSH_RELOAD==0) {
            this.siegeStarted = true;
        }
        if (this.siegeStarted && presentBattles.isEmpty() && finishedBattles.isEmpty() && additionalConditions() && canStartAmbush()) {
            startNewBattle(Battle.BattleType.AMBUSH);
            this.siegeStarted = false;
            return;
        }
        if (timer > finishAfter) {
            stopWar();
        }

    }
    protected void LongMarsh(int arrivingTime) {
        if (siegeStarted && !finishedBattles.isEmpty()) {
            for (Battle fb : finishedBattles) {
                if (fb.battleType == Battle.BattleType.SIEGE) {
                    stopWar();
                    return;
                }
            }
        }
        if (timer%TIME_TO_PATROL==0 && timer<arrivingTime && presentBattles.isEmpty() && additionalConditions()) {
            startNewBattle(Battle.BattleType.PATROL);
            return;
        }
        if (timer>arrivingTime && !siegeStarted && additionalConditions()) {
            startNewBattle(Battle.BattleType.SIEGE);
            siegeStarted = true;
            return;
        }

    }
    protected void Apocalypse(int arrivingTime) {
        if (siegeStarted && !finishedBattles.isEmpty()) {
            for (Battle fb : finishedBattles) {
                if (fb.battleType == Battle.BattleType.APOCALYPSE) {
                    stopWar();
                    return;
                }
            }
        }
        if (timer>arrivingTime && !siegeStarted && additionalConditions()) {
            startNewBattle(Battle.BattleType.APOCALYPSE);
            siegeStarted = true;
            return;
        }
    }

    public void update() {
        if (player != null) {
            this.timer++;
            if (timer % 200 == 0) {
                if (warType != WarType.AMBUSH) {
                    NetworkManager.sendToPlayer(new WarTimerUpdate(world, this.warUUID, this.timer - this.waiting), (EntityPlayerMP) this.player);
                }
                //printWarInfo();
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


    }

    public void stopWar() {
        for (Battle b: presentBattles) {
            b.stopBattle();
        }

        this.presentBattles.clear();
        this.finishedBattles.clear();

        if (player != null) {
            if (!this.finished && warType != WarType.AMBUSH) {
                NetworkManager.sendToPlayer(new StopClientWar(this.warUUID), (EntityPlayerMP) player);
                player.sendMessage(new TextComponentTranslation(TextFormatting.GREEN + "You defeated " + bossName + ". The war is finished! You won!"));
            }
        }
        this.finished = true;
    }
    public void updatePlayerInfo(EntityPlayerMP player) {
        if (this.playerUUID.equals(player.getUniqueID())) {
            this.player = player;
            NetworkManager.sendToPlayer(new StartClientWar(this.serializeNBT(), this.warUUID), player);
            print("******************* PLAYER UPDATED **********************");
        }
    }

    public void removePlayerInfo(EntityPlayerMP player) {
        if (this.playerUUID.equals(player.getUniqueID())) {
            this.player = null;
            //NetworkManager.sendToPlayer(new StartClientWar(this.serializeNBT(), this.warUUID), player);
            print("******************* PLAYER WARS PAUSED **********************");
        }
    }

    protected void startNewBattle(Battle.BattleType battleType) {
        print("--------TRYING TO START NEW BATTLE-------------");
        WarData cap = world.getCapability(ModCapabilities.WAR_DATA, null);
        System.out.println("------cap==null: "+cap==null);
        if (cap != null && cap instanceof WarsManager) {
            print("------cap is WarMan: "+(cap instanceof WarsManager));
            print("------player is in List: "+((WarsManager)cap).players.contains(player));
            if (((WarsManager)cap).players.contains(player)) {

                print("===============NEW BATTLE CREATED==============");
                this.presentBattles.add(new Battle(this, battleType));
            }
        }
    }
    protected boolean additionalConditions() {
        if (FactionUnits.FactionSoldiers.get(faction).night) {
            boolean ret = 13000 < world.getWorldTime() && world.getWorldTime() < 20000;
            if (!ret) {this.waiting++;}
            return ret;
        }
        return true;
    }
    protected boolean canStartAmbush() {
        if (player != null) {

            boolean dist = player.getBedLocation().distanceSq(player.getPosition()) > 9216; //6 chunks
            BlockPos pos = BlockUtils.findNearbyFloorSpace(world, player.getPosition(), 32, 8, true);
            boolean canPlace = pos != null && world.getBlockState(pos).getLightValue(world, pos) < 8;
            return dist && canPlace;
        }
        return false;
    }

    protected String genBossName(EntityLiving boss) {
        if (boss.hasCustomName()) {
            return boss.getCustomNameTag();
        }
        String key = subfaction == null ? faction : faction+subfaction;
        if (BossList.bossNames.containsKey(key)) {
            int size = BossList.bossNames.get(key).size();
            if (size>0) {
                int index = boss.getRNG().nextInt(size);
                return BossList.bossNames.get(key).get(index);
            }
        }
        return "Noname boss";
    }

    protected String getFaction(EntityLiving boss) {
        String str = BossList.classBossList.get(boss.getClass()).faction;
        if (str == "AW") {
            this.subfaction = ((NpcFaction) boss).getFaction();
        }
        BossList.BossParameters par = BossList.classBossList.get(boss.getClass());
        if (par.tags != null) {
            for (String t: par.tags.keySet()) {
                NBTBase tag = boss.serializeNBT().getTag(t);

                if (tag != null && tag.toString().equals(par.tags.get(t).value)) {
                    str = par.tags.get(t).faction;
                }
            }
        }
        if (!FactionUnits.FactionSoldiers.containsKey(str)) {
            this.finished = true;
        }
        return str;
    }

    protected int getStartTime() {
        if (FactionUnits.FactionSoldiers.containsKey(this.faction)) {

            int type = FactionUnits.FactionSoldiers.get(this.faction).delay;
            return type;
        }
        return -1;
    }


    protected WarType getWarType() {
        if (FactionUnits.FactionSoldiers.containsKey(this.faction)) {
            WarType type = FactionUnits.FactionSoldiers.get(this.faction).type;
            return type != null ? type : WarType.PATROL;
        }
        return WarType.PATROL;
    }
    public enum WarType {
        PATROL(0), AMBUSH(1),SIEGE(2), APOCALYPSE(3);
        final int id;

        WarType(int id)
        {
            this.id = id;
        }

        public int getId()
        {
            return id;
        }

        static Map<String, WarType> searchByName = Maps.<String, War.WarType>newHashMap();

        static {
            searchByName.put("PATROL", War.WarType.PATROL);
            searchByName.put("AMBUSH", War.WarType.AMBUSH);
            searchByName.put("SIEGE", War.WarType.SIEGE);
            searchByName.put("APOCALYPSE", War.WarType.APOCALYPSE);
        }

        public static War.WarType findByName(String name) {
            return searchByName.get(name);
        }
    }

    public void printWarInfo() {
        if (SHOW_BI) {
            System.out.println("-------------------------------------------------------");
            System.out.println("---------bossName: " + this.bossName + "---------------------------");
            System.out.println("----------faction: " + this.faction + "---------------------------");
            System.out.println("-------subFaction: " + this.subfaction + "---------------------------");
            System.out.println("----------warType: " + this.warType.toString().toUpperCase() + "---------------------------");
            System.out.println("----startWarAfter: " + this.startAfter + "---------------------------");
            System.out.println("-------playerUUID: " + this.playerUUID + "---------------------------");
            System.out.println();
            System.out.println("------------timer: " + this.timer);
            System.out.println("----------BATTLES: " + this.presentBattles.size());
            System.out.println("-------------------------------------------------------");
        }
    }


    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("bossNameWar", this.bossName);
        tag.setString("icon", this.icon);
        tag.setString("factionWar", this.faction);
        if (this.subfaction != null) {
            tag.setString("subfactionWar", this.subfaction);
        }
        tag.setLong("timerWar", this.timer);
        tag.setInteger("startAfterWar", this.startAfter);
        tag.setString("warType", this.warType.toString());
        tag.setUniqueId("playerUUID", this.playerUUID);
        tag.setUniqueId("warUUID", this.warUUID);
        tag.setBoolean("finished", this.finished);
        tag.setTag("battles", NBTHelpers.getTagList(presentBattles, Battle::serializeNBT));
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

        Set<Battle> playerBattleSet = NBTHelpers.getSet(tag.getTagList("battles", Constants.NBT.TAG_COMPOUND), n -> {
            Battle battle = new Battle(this);
            battle.deserializeNBT((NBTTagCompound) n);
            return battle;
        });
        for (Battle b : playerBattleSet) {
            this.presentBattles.add(b);
        }


    }
}
