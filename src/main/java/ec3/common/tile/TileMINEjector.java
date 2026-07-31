package ec3.common.tile;

import ec3.common.inventory.InventoryMagicFilter;
import ec3.common.item.ItemsCore;
import ec3.utils.common.ECUtils;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

@Deprecated
public class TileMINEjector extends TileMRUGeneric {
	
	public ForgeDirection getRotation() {
		int metadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		if(metadata > 5)
			metadata -= 6;
		return ForgeDirection.getOrientation(metadata);
	}
	
	public boolean ejectItem(IInventory inv, int slotNum) {
		ItemStack ejected = inv.getStackInSlot(slotNum);
		if(ejected != null) {
			if(getStackInSlot(1) == null) {
				ItemStack inserted = ejected.copy();inserted.stackSize = 1;
				setInventorySlotContents(1, inserted);
				inv.decrStackSize(slotNum, 1);
				return true;
			}
			else if(getStackInSlot(1).stackSize + 1 <= getStackInSlot(1).getMaxStackSize()) {
				++getStackInSlot(1).stackSize;
				inv.decrStackSize(slotNum, 1);
				return true;
			}
		}
		return false;
	}
	
	public int[] getAccessibleSlotsFromSide(ForgeDirection side) {
		if(worldObj.getTileEntity(xCoord+side.offsetX, yCoord+side.offsetY, zCoord+side.offsetZ) != null) {
			TileEntity tile = worldObj.getTileEntity(xCoord+side.offsetX, yCoord+side.offsetY, zCoord+side.offsetZ);
			if(tile instanceof ISidedInventory) {
				return ((ISidedInventory)tile).getAccessibleSlotsFromSide(side.getOpposite().ordinal());
			}
			else if(tile instanceof IInventory) {
				int[] ret = new int[((IInventory)tile).getSizeInventory()];
				for(int i = 0; i < ret.length; ++i)
					ret[i]= i;
				return ret;
			}
		}
		return null;
	}
	
	public int findItemToEject(IInventory inv) {
		if(getAccessibleSlotsFromSide(getRotation()) != null && getAccessibleSlotsFromSide(getRotation()).length > 0) {
			ItemStack filter = getStackInSlot(0);
			int[] slots = getAccessibleSlotsFromSide(getRotation());
			for(int i = 0; i < slots.length; ++i) {
				
				ItemStack stk = inv.getStackInSlot(slots[i]);
				if(stk == null)
					continue;
				if(filter == null || ECUtils.canFilterAcceptItem(new InventoryMagicFilter(filter), stk, filter))
					return slots[i];
			}
		}
		return -1;
	}
	
	public TileMINEjector() {
		super();
		setSlotsNum(2);
		setMaxMRU(0);
		slot0IsBoundGem = false;
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
		if(!worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord))
			if(getAccessibleSlotsFromSide(getRotation()) != null) {
				IInventory inv = (IInventory)worldObj.getTileEntity(xCoord+getRotation().offsetX, yCoord+getRotation().offsetY, zCoord+getRotation().offsetZ);
				if(findItemToEject(inv) != -1)
					ejectItem(inv, findItemToEject(inv));
			}
	}
	
	@Override
	public int[] getOutputSlots() {
		return new int[0];
	}
	
	@Override
	public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_) {
		return p_94041_1_ == 0 && p_94041_2_.getItem() == ItemsCore.filter;
	}
}
