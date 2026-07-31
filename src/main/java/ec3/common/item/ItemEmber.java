package ec3.common.item;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class ItemEmber extends Item{
	public static String[] unlocalisedName = new String[]{"acidic", "chaos", "common", "corrupted", "crystal", "divine", "magical", "flame", "unknown"};
	public static IIcon[] itemIcons = new IIcon[128];
	
	public ItemEmber() {
		setMaxDamage(0);
		setHasSubtypes(true);
	}
	
	public String getUnlocalizedName(ItemStack p_77667_1_) {
		return getUnlocalizedName()+unlocalisedName[Math.min(p_77667_1_.getItemDamage(), unlocalisedName.length-1)];
	}
	
	public void registerIcons(IIconRegister p_94581_1_) {
		super.registerIcons(p_94581_1_);
		for(int i = 0; i < unlocalisedName.length; ++i)
			itemIcons[i] = p_94581_1_.registerIcon("essentialcraft:misc/ember_"+unlocalisedName[i]);
	}
	
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int i) {
		return itemIcons[Math.min(i, itemIcons.length-1)];
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item p_150895_1_, CreativeTabs p_150895_2_, List p_150895_3_) {
		for(int i = 0; i < unlocalisedName.length-1; ++i) {
			p_150895_3_.add(new ItemStack(p_150895_1_, 1, i));
		}
	}
}
