package onslaught.ketongu10.gui;

import onslaught.ketongu10.util.Reference;
import onslaught.ketongu10.war.ClientWar;
import onslaught.ketongu10.war.War;
import onslaught.ketongu10.war.WarsManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec2f;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.Iterator;

import static onslaught.ketongu10.util.handlers.ConfigHandler.TIME_BETWEEN_WAVES;

@SideOnly(Side.CLIENT)
public class WarProgressGui extends ModIngameGui {

	//public static final List<EntityIndicator> ENTITY_INDICATOR_RENDERERS = Lists.newArrayList();
	public static final ResourceLocation BATTLE_ICON = new ResourceLocation(Reference.MOD_ID, "textures/gui/war_progress.png");
	public static final ResourceLocation HAT_ICON = new ResourceLocation(Reference.MOD_ID, "textures/gui/hat_icon.png");
	//private final Map<SkillSlot, Vec3f> screenPositionMap;
	private int guiSlider;
	private int tick = 0;
	private boolean guiSliderToggle;
	protected FontRenderer font;
	
	public WarProgressGui() {

		guiSlider = 1;
		guiSliderToggle = false;
		//screenPositionMap = new HashMap<SkillSlot, Vec3f> ();
		//screenPositionMap.put(SkillSlot.DODGE, new Vec3f(74F, 36F, 0.078F));
		//screenPositionMap.put(SkillSlot.WEAPON_SPECIAL_ATTACK, new Vec3f(42F, 48F, 0.117F));
		font = Minecraft.getMinecraft().fontRenderer;
	}
	
	private static final Vec2f[] vectorz = {
		new Vec2f(0.5F, 0.5F),
		new Vec2f(0.5F, 0.0F),
		new Vec2f(0.0F, 0.0F),
		new Vec2f(0.0F, 1.0F),
		new Vec2f(1.0F, 1.0F),
		new Vec2f(1.0F, 0.0F)
	};
	
	public void renderGui(EntityPlayer player, WarsManager warData, float partialTicks) {
		int shift = 0;
		this.tick+=1;
		if (warData.playersWars.size() <= 3) {
			for (War w : warData.playersWars.values()) {

				renderWarSlider(w, shift);
				shift++;


			}
		} else {
			Iterator<War> iterat = warData.playersWars.values().iterator();
			while (shift < 3 && iterat.hasNext()) {
				War w = iterat.next();

				renderWarSlider(w, shift);
				shift++;
			}
		}

	}

	private int getStart(War.WarType typ) {
		if (typ == War.WarType.SIEGE) {
			return 38;
		}
		return 16;
	}

	private double getRatio(double t, double T, War w) {
		if (t >= 0.95*T) {
			return 0.95;
		}
		if (w.warType == War.WarType.SIEGE) {
			if (t < w.startAfter) {
				return 0.5D * t/w.startAfter;
			} else {
			return 0.5+ (t- w.startAfter)/(2*TIME_BETWEEN_WAVES) * 0.5;
			}
		} else {
			return t/T;
		}
	}

	public void renderWarSlider(War war, int shift) {

		ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
		int width = sr.getScaledWidth();
		int height = sr.getScaledHeight();
		
		boolean depthTestEnabled = GL11.glGetBoolean(GL11.GL_DEPTH_TEST);
		boolean alphaTestEnabled = GL11.glGetBoolean(GL11.GL_ALPHA_TEST);
		boolean blendEnabled = GL11.glGetBoolean(GL11.GL_BLEND);
		
		if(!depthTestEnabled)
			GlStateManager.enableDepth();
		if(!alphaTestEnabled)
			GL11.glEnable(GL11.GL_ALPHA_TEST);
		if(!blendEnabled)
			GlStateManager.enableBlend();
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(WarProgressGui.BATTLE_ICON);

		
		double totalTime =(double)(((ClientWar)war).getTotalTime());//playerdata.getMaxStunArmor();
		double time = (double)((ClientWar)war).getTimer();//playerdata.getStunArmor();
		
		if(totalTime > 0.0F) {
			float ratio = (float) getRatio(time, totalTime, war);

			int v = getStart(war.warType);
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, (float)guiSlider * 0.5F, 0);
			GlStateManager.scale(0.5F, 0.5F, 1.0F);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			drawModalRectWithCustomSizedTexture((int)((width-237/2)), 0 + shift*11+4, 2.0F, v, 237, 10, 255, 255);//120, 10//237
			drawModalRectWithCustomSizedTexture((int)((width-237/2)), 0 + shift*11+4, 2.0F, v+11, (int)(237*ratio), 10, 255, 255);

			/**RED FRAME**/
			if (time > war.startAfter) {
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				Minecraft.getMinecraft().getTextureManager().bindTexture(WarProgressGui.BATTLE_ICON);
				drawModalRectWithCustomSizedTexture((int) ((width - 237 / 2)), 0 + shift * 11 + 3, 2.0F, 3, 237, 12, 255, 255);//120, 10//237
			}

			/**ICON**/
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			Minecraft.getMinecraft().getTextureManager().bindTexture(getBossTexture(war.icon, war.faction, war.subfaction));
			drawModalRectWithCustomSizedTexture((width-237/2+(int)(237*ratio)-6), 0 + shift*11, 0.0F, 0.0F, 16, 16, 16, 16);


			GlStateManager.popMatrix();

		}

		if(!depthTestEnabled)
			GlStateManager.disableDepth();
		if(!alphaTestEnabled)
			GL11.glDisable(GL11.GL_ALPHA_TEST);
		if(!blendEnabled)
			GlStateManager.disableBlend();
	}
	private ResourceLocation getBossTexture(String icon, String fac, String sub) {;
		if (icon != null && !icon.equals("default")) {
			ResourceLocation r = new ResourceLocation(icon);
			return r;
		}

		return WarProgressGui.HAT_ICON;
	}
	

	
	public void slideUp() {
		this.guiSlider = 28;
		this.guiSliderToggle = true;
	}

	public void slideDown() {
		this.guiSlider = 1;
		this.guiSliderToggle = false;
	}
}