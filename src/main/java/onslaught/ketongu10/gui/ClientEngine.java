package onslaught.ketongu10.gui;

import onslaught.ketongu10.capabilities.ModCapabilities;
import onslaught.ketongu10.capabilities.world.WarData;
import onslaught.ketongu10.util.Reference;
import onslaught.ketongu10.war.WarsManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientEngine {
    public static ClientEngine INSTANCE;
    public WarProgressGui gui;


    private EntityPlayer player;

    public ClientEngine() {
        INSTANCE = this;
        gui = new WarProgressGui();
    }





    public void setPlayer(EntityPlayer playerdata) {
        this.player = playerdata;
    }

    public EntityPlayer getPlayer() {
        return this.player;
    }

    @Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Side.CLIENT)
    public static class Events {
        @SubscribeEvent
        public static void renderGameOverlay(RenderGameOverlayEvent.Pre event) {
            if (event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR) {
                EntityPlayer player = Minecraft.getMinecraft().player;
                if(player.isSpectator()) return;
                if (player != null) {
                    WarData cap = player.world.getCapability(ModCapabilities.WAR_DATA, null);
                    if (cap != null) {
                        if (Minecraft.isGuiEnabled()) {
                            ClientEngine.INSTANCE.gui.renderGui(player, (WarsManager) cap, event.getPartialTicks());
                        }
                    }
                }
            }
        }
    }


}