package onslaught.ketongu10.objects.items.staffs;

import onslaught.ketongu10.Onslaught;
import onslaught.ketongu10.init.ItemInit;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class UnitStaff extends Item
{

    public UnitStaff(String name)
    {
        setUnlocalizedName(name);
        setRegistryName(name);
        //setCreativeTab(Onslaught.onslaughtTab);
        ItemInit.ITEMS.add(this);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn)
    {
            ItemStack item = playerIn.getHeldItem(handIn);
            Vec3d look = playerIn.getLookVec();
            BlockPos position = new BlockPos(playerIn.posX + look.x * 1.5D, playerIn.posY + look.y * 1.5D, playerIn.posZ + look.z * 1.5D);
            //Unit u = new Unit(worldIn, 4, "undead", position, false, new Battle());
            //u.spawnNoAI();
            playerIn.getCooldownTracker().setCooldown(this, 1);
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, item);

    }
}
