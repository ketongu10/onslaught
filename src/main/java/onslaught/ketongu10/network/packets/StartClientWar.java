package onslaught.ketongu10.network.packets;

import onslaught.ketongu10.capabilities.ModCapabilities;
import onslaught.ketongu10.capabilities.world.WarData;
import onslaught.ketongu10.war.ClientWar;
import onslaught.ketongu10.war.War;
import onslaught.ketongu10.war.WarsManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class StartClientWar implements IMessage {
    private NBTTagCompound nbt;
    private UUID warId;

    public StartClientWar() {

    }

    public StartClientWar(NBTTagCompound tag, UUID id) {
        this.nbt = tag;
        this.warId = id;

    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.warId = UUID.fromString(NetworkManager.readString(buf));
        this.nbt = NetworkManager.readFromStream(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkManager.writeString(this.warId.toString(), buf);
        NetworkManager.writeToStream(buf, this.nbt);
    }

    private <T extends StartClientWar> void onArrive() {
        ClientWar w = new ClientWar(Minecraft.getMinecraft().player.world);
        WarData cap = Minecraft.getMinecraft().player.world.getCapability(ModCapabilities.WAR_DATA, null);
        if (cap != null) {
            w.deserializeNBT(this.nbt);
            w.setTotalTime();
            if (w.warType != War.WarType.AMBUSH) {
                ((WarsManager) cap).playersWars.put(warId, w);
            }
        }

    }

    public static class Handler implements IMessageHandler<StartClientWar, IMessage> {
        @Override
        public IMessage onMessage(StartClientWar message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                message.onArrive();
            });

            return null;
        }
    }

}
