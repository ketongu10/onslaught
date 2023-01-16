package onslaught.ketongu10.network.packets;

import onslaught.ketongu10.capabilities.ModCapabilities;
import onslaught.ketongu10.capabilities.world.WarData;
import onslaught.ketongu10.war.WarsManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class StopClientWar implements IMessage {
    private UUID warId;

    public StopClientWar() {

    }

    public StopClientWar(UUID id) {
        this.warId = id;

    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.warId = UUID.fromString(NetworkManager.readString(buf));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkManager.writeString(this.warId.toString(), buf);
    }

    private <T extends StopClientWar> void onArrive() {
        WarData cap = Minecraft.getMinecraft().player.world.getCapability(ModCapabilities.WAR_DATA, null);
        if (cap != null) {
            if (((WarsManager)cap).playersWars.containsKey(this.warId)) {
                /**War w = ((WarsManager) cap).playersWars.get(warId);
                w.stopWar();**/
                ((WarsManager) cap).playersWars.remove(warId);
            }
        }

    }

    public static class Handler implements IMessageHandler<StopClientWar, IMessage> {
        @Override
        public IMessage onMessage(StopClientWar message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                message.onArrive();
            });

            return null;
        }
    }
}
