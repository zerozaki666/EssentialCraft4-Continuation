package ec3.common.tile;

import ec3.common.mod.EssentialCraftCore;
import ec3.utils.common.ECUtils;
import DummyCore.Utils.MiscUtils;
import DummyCore.Utils.Notifier;
import DummyCore.Utils.TileStatTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class TileMagicalChest extends TileEntity implements IInventory, ISidedInventory {
	public float lidAngle;
	public float prevLidAngle;
	public int numUsingPlayers;
	private int ticksSinceSync;
	
	private ItemStack[] inventory;
	
	public String ownerName="no name";
	public int syncTick;
	public int rotation;
	private TileStatTracker tracker;
	public boolean requestSync = true;
	
	public TileMagicalChest(int metaData) {
		this();
		
		if(metaData == 0) {
			inventory = new ItemStack[54];
		}
		else if (metaData == 1) {
			inventory = new ItemStack[117];
		}
	}
	
	public TileMagicalChest() {
		super();
		tracker = new TileStatTracker(this);
	}
	
	@Override
	public int getSizeInventory() {
		return inventory.length;
	}
	
	@Override
	public ItemStack getStackInSlot(int slotIndex) {
		return inventory[slotIndex];
	}
	
	@Override
	public ItemStack decrStackSize(int slotIndex, int decrementAmount) {
		ItemStack itemStack = getStackInSlot(slotIndex);
		if (itemStack != null) {
			if (itemStack.stackSize <= decrementAmount) {
				setInventorySlotContents(slotIndex, null);
			}
			else {
				itemStack = itemStack.splitStack(decrementAmount);
				if (itemStack.stackSize == 0) {
					setInventorySlotContents(slotIndex, null);
				}
			}
		}
		
		return itemStack;
	}
	
	@Override
	public ItemStack getStackInSlotOnClosing(int slotIndex) {
		if (inventory[slotIndex] != null) {
			ItemStack itemStack = inventory[slotIndex];
			inventory[slotIndex] = null;
			return itemStack;
		}
		else {
			return null;
		}
	}
	
	@Override
	public void setInventorySlotContents(int slotIndex, ItemStack itemStack) {
		inventory[slotIndex] = itemStack;
		
		if(itemStack != null && itemStack.stackSize > getInventoryStackLimit()) {
			itemStack.stackSize = getInventoryStackLimit();
		}
		
		markDirty();
	}
	
	@Override
	public String getInventoryName() {
		return "magicalChest";
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
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this && entityplayer.getDistanceSq((double)xCoord+0.5D, (double)yCoord+0.5D, (double)zCoord+0.5D) <= 64D;
	}
	
	@Override
	public void openInventory() {
		++numUsingPlayers;
		worldObj.addBlockEvent(xCoord, yCoord, zCoord, worldObj.getBlock(xCoord, yCoord, zCoord), 1, numUsingPlayers);
	}
	
	@Override
	public void closeInventory() {
		--numUsingPlayers;
		worldObj.addBlockEvent(xCoord, yCoord, zCoord, worldObj.getBlock(xCoord, yCoord, zCoord), 1, numUsingPlayers);
	}
	
	@Override
	public boolean isItemValidForSlot(int slotIndex, ItemStack itemStack) {
		return true;
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		
		if(syncTick == 0) {
			if(tracker == null)
				Notifier.notifyCustomMod("EssentialCraft", "[WARNING][SEVERE]TileEntity " + this + " at pos " + xCoord + "," + yCoord + "," + zCoord + " tries to sync itself, but has no TileTracker attached to it! SEND THIS MESSAGE TO THE DEVELOPER OF THE MOD!");
			else if(!worldObj.isRemote && tracker.tileNeedsSyncing())
				MiscUtils.sendPacketToAllAround(worldObj, getDescriptionPacket(), xCoord, yCoord, zCoord, worldObj.provider.dimensionId, 32);
			syncTick = 60;
		}
		else
			--syncTick;
		
		if(requestSync  && worldObj.isRemote) {
			requestSync = false;
			ECUtils.requestScheduledTileSync(this, EssentialCraftCore.proxy.getClientPlayer());
		}
		
		if(++ticksSinceSync % 20 * 4 == 0)
			worldObj.addBlockEvent(xCoord, yCoord, zCoord, worldObj.getBlock(xCoord, yCoord, zCoord), 1, numUsingPlayers);
		
		prevLidAngle = lidAngle;
		float angleIncrement = 0.1F;
		
		if(numUsingPlayers > 0 && lidAngle == 0.0F)
			worldObj.playSoundEffect(xCoord+0.5D, yCoord+0.5D, zCoord+0.5D, "random.chestopen", 0.5F, worldObj.rand.nextFloat()*0.1F + 0.9F);
		
		if(numUsingPlayers == 0 && lidAngle > 0.0F || numUsingPlayers > 0 && lidAngle < 1.0F) {
			float var8 = lidAngle;
			
			if(numUsingPlayers > 0)
				lidAngle += angleIncrement;
			else
				lidAngle -= angleIncrement;
			
			if(lidAngle > 1.0F)
				lidAngle = 1.0F;
			
			if(lidAngle < 0.5F && var8 >= 0.5F)
				worldObj.playSoundEffect(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, "random.chestclosed", 0.5F, worldObj.rand.nextFloat() * 0.1F + 0.9F);
			
			if(lidAngle < 0.0F)
				lidAngle = 0.0F;
		}
	}
	
	/**
	 * Called when a client event is received with the event number and argument, see World.sendClientEvent
	 */
	@Override
	public boolean receiveClientEvent(int eventID, int numUsingPlayers) {
		if(eventID == 1) {
			this.numUsingPlayers = numUsingPlayers;
			return true;
		}
		else {
			return super.receiveClientEvent(eventID, numUsingPlayers);
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound i) {
		super.readFromNBT(i);
		MiscUtils.loadInventory(this, i);
		rotation = i.getInteger("0");
		ownerName = i.getString("1");
	}
	
	@Override
	public void writeToNBT(NBTTagCompound i) {
		super.writeToNBT(i);
		MiscUtils.saveInventory(this, i);
		i.setInteger("0", rotation);
		i.setString("1", ownerName);
	}
	
	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		nbttagcompound.setInteger("x", xCoord);
		nbttagcompound.setInteger("y", yCoord);
		nbttagcompound.setInteger("z", zCoord);
		nbttagcompound.setInteger("0", rotation);
		nbttagcompound.setString("1", ownerName);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, -10, nbttagcompound);
	}
	
	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		if(net.getNetHandler() instanceof INetHandlerPlayClient && pkt.func_148853_f() == -10 && pkt.func_148857_g().hasKey("0")) {
			rotation = pkt.func_148857_g().getInteger("0");
			ownerName = pkt.func_148857_g().getString("1");
		}
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		int[] ret = new int[inventory.length];
		
		for(int i = 0; i < ret.length; ++i)
			ret[i] = i;
		
		return ret;
	}
	
	@Override
	public boolean canInsertItem(int p_102007_1_, ItemStack p_102007_2_, int p_102007_3_) {
		return true;
	}
	
	@Override
	public boolean canExtractItem(int p_102008_1_, ItemStack p_102008_2_, int p_102008_3_) {
		return true;
	}
}
