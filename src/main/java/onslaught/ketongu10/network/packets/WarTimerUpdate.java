package onslaught.ketongu10.network.packets;

import net.minecraft.world.World;
import onslaught.ketongu10.capabilities.ModCapabilities;
import onslaught.ketongu10.capabilities.world.WarData;
import onslaught.ketongu10.war.ClientWar;
import onslaught.ketongu10.war.War;
import onslaught.ketongu10.war.WarsManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

import static onslaught.ketongu10.util.handlers.ConfigHandler.print;

public class WarTimerUpdate implements IMessage {
    //private int worldId;
    private UUID warId;
    private Long time;

    public WarTimerUpdate() {

    }

    public WarTimerUpdate(World w, UUID id, Long t) {
        //this.worldId = w.d
        this.warId = id;
        this.time = t;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.warId = UUID.fromString(NetworkManager.readString(buf));
        this.time = buf.readLong();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkManager.writeString(this.warId.toString(), buf);
        buf.writeLong(this.time);

    }

    private <T extends StartClientWar> void onArrive() {
        WarData cap = Minecraft.getMinecraft().player.world.getCapability(ModCapabilities.WAR_DATA, null);
        print("===============war_cap "+cap);
        if (cap != null) {
            War w = ((WarsManager)cap).playersWars.get(this.warId);
            if (w == null) {
                System.out.println("UUIDs " + ((WarsManager)cap).playersWars.keySet().toString());
            }
            ((ClientWar)w).setTime((Long) this.time);
        }

    }

    public static class Handler implements IMessageHandler<WarTimerUpdate, IMessage> {
        @Override
        public IMessage onMessage(WarTimerUpdate message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                message.onArrive();
            });

            return null;
        }
    }
}
