package ec3.common.tile;

import ec3.common.inventory.InventoryMagicFilter;
import ec3.utils.common.ECUtils;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

@Deprecated
public class TileMINInjector extends TileMRUGeneric {
	
	public ForgeDirection getRotation() {
		int metadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		if(metadata > 5)
			metadata -= 6;
		return ForgeDirection.getOrientation(metadata);
	}
	
	public boolean injectItem(IInventory inv, int slotNum) {
		ItemStack injected = inv.getStackInSlot(slotNum);
		if(getStackInSlot(1) != null) {
			if(injected != null) {
				if(injected.stackSize + 1 <= injected.getMaxStackSize()) {
					injected.stackSize += 1;
					decrStackSize(1, 1);
					return true;
				}
			}
			else {
				ItemStack inserted = getStackInSlot(1).copy();inserted.stackSize = 1;
				inv.setInventorySlotContents(slotNum, inserted);
				decrStackSize(1, 1);
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
	
	public int findItemToInject(IInventory inv) {
		if(getAccessibleSlotsFromSide(getRotation()) != null && getAccessibleSlotsFromSide(getRotation()).length > 0) {
			ItemStack filter = getStackInSlot(0);
			int[] slots = getAccessibleSlotsFromSide(getRotation());
			for(int i = 0; i < slots.length; ++i) {
				ItemStack stk = getStackInSlot(1);
				ItemStack in = inv.getStackInSlot(slots[i]);
				
				if(stk == null)
					continue;
				if(filter == null)
					if(in == null || (in.isItemEqual(stk) && ItemStack.areItemStackTagsEqual(stk, in) && in.stackSize+1 <= in.getMaxStackSize()))
						return slots[i];
				else if(ECUtils.canFilterAcceptItem(new InventoryMagicFilter(filter), stk, filter) && (in == null || (in.stackSize + 1 <= in.getMaxStackSize() && in.isItemEqual(stk) && ItemStack.areItemStackTagsEqual(in, stk))))
						return slots[i];
			}
		}
		return -1;
	}
	
	public TileMINInjector() {
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
			if(getAccessibleSlotsFromSide(getRotation()) != null)
			{
				IInventory inv = (IInventory) worldObj.getTileEntity(xCoord+getRotation().offsetX, yCoord+getRotation().offsetY, zCoord+getRotation().offsetZ);
				if(findItemToInject(inv) != -1)
					injectItem(inv, findItemToInject(inv));
			}
	}
	
	@Override
	public int[] getOutputSlots() {
		return new int[0];
	}
}
