package onslaught.ketongu10.tabs;

import onslaught.ketongu10.init.ItemInit;

import onslaught.ketongu10.util.Reference;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class OnslaughtTab extends CreativeTabs
{
	public OnslaughtTab(String label)
	{
		super("onslaughttab");
		//this.setBackgroundImageName("fakeplayer.png");
	}
	
	@Override
	public ItemStack getTabIconItem()
	{
		return new ItemStack(ItemInit.UNIT_STAFF);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public net.minecraft.util.ResourceLocation getBackgroundImage()
	{
		return new net.minecraft.util.ResourceLocation(Reference.MOD_ID + ":textures/tab/onslaught.png");
	}
}
