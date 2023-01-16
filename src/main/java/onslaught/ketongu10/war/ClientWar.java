package onslaught.ketongu10.war;


import onslaught.ketongu10.util.NBTHelpers;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Set;

import static onslaught.ketongu10.util.handlers.ConfigHandler.TIME_BETWEEN_WAVES;

@SideOnly(Side.CLIENT)
public class ClientWar extends War{

    protected int totalTime;

    public ClientWar(World w) {
        super(w);
    }

    @Override
    public void stopWar() {
        this.finished = true;
    }

    public void setTotalTime() {
        switch (warType) {
            case PATROL:
                this.totalTime = startAfter;
                return;
            case SIEGE:
                this.totalTime = startAfter+2*TIME_BETWEEN_WAVES;
                return;
            case APOCALIPSE:
                this.totalTime = startAfter+9*TIME_BETWEEN_WAVES;
                return;
        }
        this.totalTime = startAfter;
    }

    public int getTotalTime() {
        return this.totalTime;
    }
    public Long getTimer() {
        return this.timer;
    }

    public void setTime(Long t) {
        this.timer = t;
    }

    @Override
    public void update() {
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
        this.finished = tag.getBoolean("finished");
        this.player = Minecraft.getMinecraft().player;
        //this.plot = this.getPlot();

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
