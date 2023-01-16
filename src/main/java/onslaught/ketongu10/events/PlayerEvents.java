package onslaught.ketongu10.events;

import com.sun.org.apache.xpath.internal.operations.Mod;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.server.FMLServerHandler;
import onslaught.ketongu10.capabilities.ModCapabilities;
import onslaught.ketongu10.capabilities.world.WarData;
import onslaught.ketongu10.war.WarsManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.core.util.WatchManager;

import java.util.List;

public class PlayerEvents {

    @SubscribeEvent
    public void playerJoinedServerEvent(PlayerEvent.PlayerLoggedInEvent event) {

        for(World w: event.player.getServer().worlds) {
            WarData cap = w.getCapability(ModCapabilities.WAR_DATA, null);
            if (cap != null && cap instanceof WarsManager) {
                ((WarsManager) cap).onPlayerJoined((EntityPlayerMP) event.player, event.player.world == w);

            }
        }
    }

    @SubscribeEvent
    public void playerLeftServerEvent(PlayerEvent.PlayerLoggedOutEvent event) {
        for(World w: event.player.getServer().worlds) {
            WarData cap = w.getCapability(ModCapabilities.WAR_DATA, null);
            if (cap != null && cap instanceof WarsManager) {
                ((WarsManager) cap).onPlayerLeft((EntityPlayerMP) event.player);

            }
        }
    }

    /**@SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void playerJoinedClientEvent(PlayerEvent.PlayerLoggedInEvent event) {
        WarData cap = event.player.world.getCapability(ModCapabilities.WAR_DATA, null);
        //System.out.println("########################this event also works######################");
        if (cap != null && cap instanceof WarsManager) {

            //System.out.println("######################## DEBIL WAS ADDED TO CE ######################");
            //ClientEngine.INSTANCE.setPlayer(event.player);
        }
    }**/

    @SubscribeEvent
    public void playerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.fromDim != event.toDim ) {
            WarData cap1 = event.player.getServer().getWorld(event.fromDim).getCapability(ModCapabilities.WAR_DATA, null);
            if (cap1 != null) {
                ((WarsManager)cap1).onPlayerChangeDim((EntityPlayerMP) event.player);
            }
            WarData cap2 = event.player.getServer().getWorld(event.toDim).getCapability(ModCapabilities.WAR_DATA, null);
            if (cap2 != null) {
                ((WarsManager)cap2).onPlayerJoined((EntityPlayerMP) event.player, true);
            }
        }
    }

    @SubscribeEvent
    public void playerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.isEndConquered()) {
            WarData cap1 = event.player.getServer().getWorld(1).getCapability(ModCapabilities.WAR_DATA, null);
            if (cap1 != null) {
                ((WarsManager)cap1).onPlayerChangeDim((EntityPlayerMP) event.player);
            }
            WarData cap2 = event.player.getServer().getWorld(0).getCapability(ModCapabilities.WAR_DATA, null);
            if (cap2 != null) {
                ((WarsManager)cap2).onPlayerJoined((EntityPlayerMP) event.player, true);
            }
        }

    }






}
