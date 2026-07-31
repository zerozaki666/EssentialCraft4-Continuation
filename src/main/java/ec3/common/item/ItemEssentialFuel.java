package ec3.common.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class ItemEssentialFuel extends Item {

	public IIcon[] icon = new IIcon[5];
	public String[] name = new String[] {"Fiery", "Watery", "Earthen", "Windy", "Unknown"};
	public ItemEssentialFuel() {
		super();
		setMaxDamage(0);
		maxStackSize = 16;
        bFull3D = false;
        setHasSubtypes(true);
	}
	
    public IIcon getIconFromDamage(int par1) {
    	return icon[Math.min(par1, icon.length-1)];
    }
    
    @Override
    public void registerIcons(IIconRegister par1IconRegister) {
    	for(int i = 0; i < 4; ++i) {
    		icon[i] = par1IconRegister.registerIcon("essentialcraft:elemental/eFuel_"+name[i]);
    	}
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List) {
        for(int var4 = 0; var4 < 4; ++var4) {
        	ItemStack min = new ItemStack(par1, 1, var4);
            par3List.add(min);
        }
    }
    
    public String getUnlocalizedName(ItemStack p_77667_1_) {
        return getUnlocalizedName()+name[Math.min(p_77667_1_.getItemDamage(), name.length-1)];
    }
    
    public EnumRarity getRarity(ItemStack par1ItemStack) {
    	return EnumRarity.rare;
    }
    
    public boolean hasEffect(ItemStack par1ItemStack) {
    	return false;
    }
}
