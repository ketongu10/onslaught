package onslaught.ketongu10.init;

import java.util.ArrayList;
import java.util.List;


import onslaught.ketongu10.objects.items.staffs.UnitStaff;


import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraftforge.common.util.EnumHelper;

public class ItemInit 
{
	public static final List<Item> ITEMS = new ArrayList<Item>();
	

	public static final Item UNIT_STAFF = new UnitStaff("unit_staff");

}
