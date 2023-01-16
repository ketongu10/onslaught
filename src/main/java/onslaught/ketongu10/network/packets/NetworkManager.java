package onslaught.ketongu10.network.packets;

import onslaught.ketongu10.Onslaught;
import onslaught.ketongu10.util.Reference;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class NetworkManager {
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MOD_ID);

    public static void registerPackets() {
        int id = 0;
        INSTANCE.registerMessage(StartClientWar.Handler.class, StartClientWar.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(StopClientWar.Handler.class, StopClientWar.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(WarTimerUpdate.Handler.class, WarTimerUpdate.class, id++, Side.CLIENT);
    }

    public static void sendToServer(IMessage message) {
        INSTANCE.sendToServer(message);
    }

    public static void sendToAll(IMessage message) {
        INSTANCE.sendToAll(message);
    }

    public static void sendToAllPlayerTrackingThisEntity(IMessage message, Entity entity) {
        INSTANCE.sendToAllTracking(message, entity);
    }

    public static void sendToPlayer(IMessage message, EntityPlayerMP player) {
        INSTANCE.sendTo(message, player);
    }

    public static void sendToAllPlayerTrackingThisEntityWithSelf(IMessage message, EntityPlayerMP entity) {
        sendToPlayer(message, entity);
        sendToAllPlayerTrackingThisEntity(message, entity);
    }

    private static final int MAX_LENGTH = 32767;

    public static void writeString(String string, ByteBuf buffer) {
        byte[] abyte = string.getBytes(StandardCharsets.UTF_8);

        if (abyte.length > MAX_LENGTH) {
            throw new EncoderException(
                    "String too big (was " + abyte.length + " bytes encoded, max " + MAX_LENGTH + ")");
        } else {
            buffer.writeInt(abyte.length);
            buffer.writeBytes(abyte);
        }
    }

    public static String readString(ByteBuf buffer) {
        int i = buffer.readInt();
        if (i > MAX_LENGTH * 4) {
            throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + i + " > " + MAX_LENGTH * 4 + ")");
        } else if (i < 0) {
            throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
        } else {
            String s = buffer.toString(buffer.readerIndex(), i, StandardCharsets.UTF_8);
            buffer.readerIndex(buffer.readerIndex() + i);
            if (s.length() > MAX_LENGTH) {
                throw new DecoderException("The received string length is longer than maximum allowed (" + i + " > " + MAX_LENGTH + ")");
            } else {
                return s;
            }
        }
    }
    public static void writeToStream(ByteBuf data, NBTTagCompound packetData) {
        if (packetData != null) {
            try (ByteBufOutputStream outputStream = new ByteBufOutputStream(data)) {
                CompressedStreamTools.writeCompressed(packetData, outputStream);
            }
            catch (IOException e) {
                Onslaught.LOGGER.error("Error writing nbt packet data: ", e);
            }
        }
    }

    public static NBTTagCompound readFromStream(ByteBuf data) {
        try (ByteBufInputStream inputStream = new ByteBufInputStream(data)) {
            return CompressedStreamTools.readCompressed(inputStream);
        }
        catch (IOException e) {
            Onslaught.LOGGER.error("Error reading nbt packet data: ", e);
        }
        return null;
    }
}
