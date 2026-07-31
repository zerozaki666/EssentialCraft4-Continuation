package ec3.common.tile;

import ec3.common.inventory.InventoryMagicFilter;
import ec3.utils.common.ECUtils;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

@Deprecated
public class TileAMINInjector extends TileMINInjector {
	public int slotnum = 0;
	
	@Override
	public ForgeDirection getRotation() {
		int metadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		if(metadata > 5)
			metadata -= 6;
		return ForgeDirection.getOrientation(metadata);
	}
	
	@Override
	public boolean injectItem(IInventory inv, int slotNum) {
		if(inv == null)
			return false;
		
		ItemStack injected = inv.getStackInSlot(slotNum);
		if(getStackInSlot(slotNum + 64) != null) {
			if(injected != null) {
				if(injected.stackSize+1 <= injected.getMaxStackSize()) {
					injected.stackSize += 1;
					decrStackSize(slotNum + 64, 1);
					return true;
				}
			}
			else {
				ItemStack inserted = getStackInSlot(slotNum + 64).copy();
				inserted.stackSize = 1;
				inv.setInventorySlotContents(slotNum, inserted);
				decrStackSize(slotNum + 64, 1);
				return true;
			}
		}
		return false;
	}
	
	public int getAvaiableSlotsNum() {
		IInventory inv = (IInventory) worldObj.getTileEntity(xCoord+getRotation().offsetX, yCoord+getRotation().offsetY, zCoord+getRotation().offsetZ);
		if(inv != null)
			return inv.getSizeInventory();
		return 0;
	}
	
	@Override
	public int[] getAccessibleSlotsFromSide(ForgeDirection side) {
		return null;
	}
	
	public int findItemToInject(IInventory inv, int prevSlotnum) {
		if(inv == null)
			return -1;
		for(int i = prevSlotnum; i < inv.getSizeInventory() && i < 64; ++i) {
			ItemStack filter = getStackInSlot(i); {
				ItemStack stk = getStackInSlot(i+64);
				ItemStack in = inv.getStackInSlot(i);
				if(filter == null || stk == null)
					continue;
				if(ECUtils.canFilterAcceptItem(new InventoryMagicFilter(filter), stk, filter) && (in == null || (in.stackSize+1 <= in.getMaxStackSize() && in.isItemEqual(stk) && ItemStack.areItemStackTagsEqual(in, stk)))) {
					return i;
				}
			}
		}
		return -1;
	}

	 public TileAMINInjector() {
		 super();
		 setSlotsNum(128);
		 setMaxMRU(0);
	 }
	 
	@Override
    public void readFromNBT(NBTTagCompound par1NBTTagCompound) {
    	super.readFromNBT(par1NBTTagCompound);
    }
    
    @Override
    public void writeToNBT(NBTTagCompound par1NBTTagCompound) {
    	super.writeToNBT(par1NBTTagCompound);
    }
    
    @Override
    public void updateEntity() {
    	super.updateEntity();
		if(!worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)) {
			IInventory inv = (IInventory)worldObj.getTileEntity(xCoord+getRotation().offsetX, yCoord+getRotation().offsetY, zCoord+getRotation().offsetZ);
			if(inv != null)
				for(int i = 0; i < inv.getSizeInventory(); ++i)
					if(findItemToInject(inv, i) != -1)
						injectItem(inv, findItemToInject(inv, i));
		}
    }
}
