package onslaught.ketongu10.war.LongMarch;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import onslaught.ketongu10.network.packets.NetworkManager;
import onslaught.ketongu10.network.packets.StartClientWar;
import onslaught.ketongu10.war.Battle;
import onslaught.ketongu10.war.War;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Consumer;

import static onslaught.ketongu10.util.handlers.ConfigHandler.TIME_TO_PATROL;

public class WarLongMarch extends War {
    protected int start_x;
    protected int start_z;
    protected int end_x;
    protected int end_z;
    protected float speed;
    public float progress;
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
            return;
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
        if (!siegeStarted && dislocation.x==end_x && dislocation.z==end_z) {
            stopWar();
        }
    }
    protected void startMoving() {

    }
    protected void continueMoving() {

    }

    protected void deployForces() {}

    protected void removeForces() {}

    protected boolean checkNearPlayers() {

    }
}
