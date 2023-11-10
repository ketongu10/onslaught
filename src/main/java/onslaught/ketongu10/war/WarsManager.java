package onslaught.ketongu10.war;

import net.minecraft.world.chunk.Chunk;
import onslaught.ketongu10.capabilities.world.WarData;
import onslaught.ketongu10.util.NBTHelpers;
import onslaught.ketongu10.war.LongMarch.WarLongMarch;
import onslaught.ketongu10.war.units.UnitBase;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import java.util.*;

import static onslaught.ketongu10.util.handlers.ConfigHandler.SHOW_BI;
import static onslaught.ketongu10.util.handlers.ConfigHandler.print;


public class WarsManager extends WarData {
    public Map<UUID, War> playersWars = new HashMap<>();
    public List<UUID> finishedWars = new ArrayList<>();
    public List<EntityPlayerMP> players = new ArrayList<>(); //UUID!!!!!!!!!
    public Map<UUID, UnitBase> unitIDs = new HashMap();
    public Map<War, Chunk> dislocations = new HashMap<>();
    public int test = 0;
    public Long tick = 0L;

    public static void fillMap() {

    }



    private UUID getNewID() {
        UUID newID = UUID.randomUUID();
        while (this.playersWars.containsKey(newID)){
            newID = UUID.randomUUID();
        }
        return newID;
    }



    public void onPlayerJoined(EntityPlayerMP player, boolean sameDim) {
        if (!this.players.contains(player)) {
            print("===============PLAYER ADDED TO ONLINE LIST==============");
            this.players.add(player);
        }
        if (sameDim) {
            for (War war : playersWars.values()) {
                war.updatePlayerInfo(player);
            }
        }

    }

    public void onPlayerLeft(EntityPlayerMP player) {
        if (this.players.contains(player)) {
            print("===============PLAYER REMOVED FROM ONLINE LIST==============");
            this.players.remove(player);
            for (War war: playersWars.values()) {
                war.removePlayerInfo(player);
            }
        }
    }

    public void onPlayerChangeDim(EntityPlayerMP player) {
        if (this.players.contains(player)) {
            for (War war: playersWars.values()) {
                war.removePlayerInfo(player);
            }
        }
    }

    public void startLongMarch(EntityPlayerMP player, String faction, String subfaction,  int delay, int x1, int z1, int x2, int z2) {
        if (this.players.contains(player)) {
            print("===============NEW WAR STARTED==============");
            UUID id = getNewID();
            this.playersWars.put(id, new WarLongMarch(world, player, faction, subfaction,  id,  delay, x1, z1, x2, z2, player.dimension == 0));
        }
    }

    public void startWarWithParameters(EntityPlayerMP player, War.WarType type,String faction, String subfaction,  int delay) {
        if (this.players.contains(player)) {
            print("===============NEW WAR STARTED==============");
            UUID id = getNewID();
            this.playersWars.put(id, new War(world, player,faction, subfaction,  id,  type, delay, player.dimension == 0));
        }
    }

    public void tryStartNewWar(EntityPlayerMP player, EntityLiving boss) {
        if (BossList.classBossList.containsKey(boss.getClass())) {
            if (this.players.contains(player)) {
                print("===============NEW WAR STARTED==============");
                UUID id = getNewID();
                this.playersWars.put(id, new War(world, player, boss, id, player.dimension == 0));
            }
        }
    }
    public void stopAllWars() {
        for(War w: playersWars.values()) {
            w.stopWar();
        }
    }

    public WarsManager() {
        super();
    }

    public void update() {
        if (!world.isRemote) {
            this.tick++;
            if (this.tick % 100 == 0) {
                //printWorldWarInfo();
            }
            /**for (War war : this.playersWars.values()) {
                if (!war.finished) {
                    war.update();
                } else {
                    this.finishedWars.add(this.pla);
                }
            }**/
            for (UUID id: this.playersWars.keySet()) {
                War war = this.playersWars.get(id);
                if (!war.finished) {
                    war.update();
                } else {
                    this.finishedWars.add(id);
                }
            }
            for (UUID id : this.finishedWars) {
                this.playersWars.remove(id);
                print("===============WAR FINISHED==============");
            }
            this.finishedWars.clear();

        }
    }

    public void printWorldWarInfo() {
        if (SHOW_BI) {
            System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
            System.out.println("||||||||||||||||||| timer: " + this.tick + " |||||||||||||||||||");
            System.out.println("||||||||| KILLED WARRIORS: " + this.test + " |||||||||||||||||||");
            System.out.println("|||||||||||||||||||| WARS: " + this.playersWars.size() + " |||||||||||||||||||");
            System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
        }
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("testWar", this.test);
        tag.setTag("playerWars", NBTHelpers.getTagList(playersWars.values(), War::serializeNBT));
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        this.test = tag.getInteger("testWar");
        playersWars.clear();
        finishedWars.clear();
        unitIDs.clear();

        Set<War> playerWarSet = NBTHelpers.getSet(tag.getTagList("playerWars", Constants.NBT.TAG_COMPOUND), n -> {
            War war = new War(world);
            war.deserializeNBT((NBTTagCompound) n);
            return war;
        });

        for (War w : playerWarSet) {
            this.playersWars.put(w.warUUID, w);
        }

    }
}
