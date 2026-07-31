package ec3.common.tile;

import java.util.UUID;

import DummyCore.Utils.MathUtils;
import DummyCore.Utils.MiscUtils;
import DummyCore.Utils.Notifier;
import DummyCore.Utils.TileStatTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import ec3.api.ITERequiresMRU;
import ec3.common.item.ItemBoundGem;
import ec3.common.item.ItemsCore;
import ec3.common.mod.EssentialCraftCore;
import ec3.utils.common.ECUtils;

public abstract class TileMRUGeneric extends TileEntity implements ITERequiresMRU, IInventory, ISidedInventory {

	public TileMRUGeneric() {
		super();
		tracker = new TileStatTracker(this);
	}
	
	public int syncTick;
	int mru;
	int maxMRU = 100;
	UUID uuid = UUID.randomUUID();
	float balance;
	public int innerRotation;
	private ItemStack[] items = new ItemStack[1];
	private TileStatTracker tracker;
	public boolean slot0IsBoundGem = true;
	public boolean requestSync = true;
	
	public abstract int[] getOutputSlots();
	
	public void setSlotsNum(int i) {
		items = new ItemStack[i];
	}
	
	@Override
	public void readFromNBT(NBTTagCompound i) {
		super.readFromNBT(i);
		ECUtils.loadMRUState(this, i);
		MiscUtils.loadInventory(this, i);
	}
	
	@Override
	public void writeToNBT(NBTTagCompound i) {
		super.writeToNBT(i);
		ECUtils.saveMRUState(this, i);
		MiscUtils.saveInventory(this, i);
	}
	
	public void updateEntity() {
		++innerRotation;
		//Sending the sync packets to the CLIENT. 
		if(syncTick == 0) {
			if(tracker == null)
				Notifier.notifyCustomMod("EssentialCraft", "[WARNING][SEVERE]TileEntity " + this + " at pos " + xCoord + "," + yCoord + ","  + zCoord + " tries to sync itself, but has no TileTracker attached to it! SEND THIS MESSAGE TO THE DEVELOPER OF THE MOD!");
			else
				if(!worldObj.isRemote && tracker.tileNeedsSyncing()) {
					MiscUtils.sendPacketToAllAround(worldObj, getDescriptionPacket(), xCoord, yCoord, zCoord, worldObj.provider.dimensionId, 32);
				}
			syncTick = 60;
		}
		else
			--syncTick;
		
		if(requestSync && worldObj.isRemote) {
			requestSync = false;
			ECUtils.requestScheduledTileSync(this, EssentialCraftCore.proxy.getClientPlayer());
		}
	}
	
	@Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        writeToNBT(nbttagcompound);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, -10, nbttagcompound);
    }
	
	@Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
    {
		if(net.getNetHandler() instanceof INetHandlerPlayClient)
			if(pkt.func_148853_f() == -10)
				readFromNBT(pkt.func_148857_g());
    }
	
	@Override
	public int getMRU() {
		return mru;
	}

	@Override
	public int getMaxMRU() {
		return maxMRU;
	}

	@Override
	public boolean setMRU(int i) {
		mru = i;
		return true;
	}

	@Override
	public float getBalance() {
		return balance;
	}

	@Override
	public boolean setBalance(float f) {
		balance = f;
		return true;
	}

	@Override
	public boolean setMaxMRU(float f) {
		maxMRU = (int)f;
		return true;
	}

	@Override
	public UUID getUUID() {
		return uuid;
	}
	
	@Override
	public int getSizeInventory() {
		return items.length;
	}

	@Override
	public ItemStack getStackInSlot(int par1) {
		return items[par1];
	}

	@Override
	public ItemStack decrStackSize(int par1, int par2) {
		if(items[par1] != null) {
            ItemStack itemstack;

			if(items[par1].stackSize <= par2) {
                itemstack = items[par1];
                items[par1] = null;
                return itemstack;
            }
			else {
                itemstack = items[par1].splitStack(par2);

				if(items[par1].stackSize == 0) {
					items[par1] = null;
				}

                return itemstack;
            }
        }
        else {
            return null;
        }
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int par1) {
		if(items[par1] != null) {
			ItemStack itemstack = items[par1];
			items[par1] = null;
			return itemstack;
		}
		else {
			return null;
		}
	}
	
	@Override
	public void setInventorySlotContents(int par1, ItemStack par2ItemStack) {
		items[par1] = par2ItemStack;
		
		if (par2ItemStack != null && par2ItemStack.stackSize > getInventoryStackLimit()) {
			par2ItemStack.stackSize = getInventoryStackLimit();
		}
	}

	@Override
	public String getInventoryName() {
		return "ec3.container.generic";
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this && player.dimension == worldObj.provider.dimensionId;
	}

	@Override
	public void openInventory() {}

	@Override
	public void closeInventory() {}

	@Override
	public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_) {
		return slot0IsBoundGem && p_94041_1_ == 0 ? isBoundGem(p_94041_2_) : true;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		if(getSizeInventory() > 0) {
			if(slot0IsBoundGem && side == 1)
				return new int[] {0};
			
			if(side == 0)
				return getOutputSlots();
			else if(getInputSlots().length > 0)
				return getInputSlots();
			else {
				int[] retInt;
				if(getSizeInventory() > getOutputSlots().length + (slot0IsBoundGem ? 1 : 0))
					retInt = new int[getSizeInventory() - (getOutputSlots().length + (slot0IsBoundGem ? 1 : 0))];
				else
					retInt = new int[0];
				int cnt = 0;
				if(retInt.length > 0) {
					for(int i = 0; i < getSizeInventory(); ++i) {
						if((slot0IsBoundGem && i != 0) && !MathUtils.arrayContains(getOutputSlots(), i)) {
							if(cnt < retInt.length)
								retInt[cnt] = i;
							++cnt;
						}
						else if(!slot0IsBoundGem && !MathUtils.arrayContains(getOutputSlots(), i)) {
							if(cnt < retInt.length)
								retInt[cnt] = i;
							++cnt;
						}
					}
				}
				return retInt;
			}
		}
		else
			return new int[0];
	}

	@Override
	public boolean canInsertItem(int p_102007_1_, ItemStack p_102007_2_, int p_102007_3_) {
		return isItemValidForSlot(p_102007_1_, p_102007_2_);
	}
	
	@Override
	public boolean canExtractItem(int p_102008_1_, ItemStack p_102008_2_, int p_102008_3_) {
		return MathUtils.arrayContains(getOutputSlots(), p_102008_1_);
	}
	
	public int[] getInputSlots() {
		return new int[0];
	}
	
	public boolean isBoundGem(ItemStack stack) {
		return stack.getItem() instanceof ItemBoundGem;
	}
}
