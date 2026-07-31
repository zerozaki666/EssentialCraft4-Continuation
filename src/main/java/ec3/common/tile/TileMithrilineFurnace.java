package ec3.common.tile;

import DummyCore.Utils.Coord3D;
import DummyCore.Utils.DataStorage;
import DummyCore.Utils.DummyData;
import DummyCore.Utils.MathUtils;
import DummyCore.Utils.MiscUtils;
import DummyCore.Utils.Notifier;
import DummyCore.Utils.TileStatTracker;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.config.Configuration;
import ec3.api.MithrilineFurnaceRecipe;
import ec3.api.MithrilineFurnaceRecipes;
import ec3.common.block.BlockMithrilineCrystal;
import ec3.common.block.BlocksCore;
import ec3.common.mod.EssentialCraftCore;
import ec3.utils.common.ECUtils;

public class TileMithrilineFurnace extends TileEntity implements ISidedInventory {
	
	public static float maxEnergy = 10000F;
	public float energy;
	public float progress;
	public float reqProgress;
	private TileStatTracker tracker;
	public int syncTick;
	public ItemStack[] items = new ItemStack[2];
	public boolean requestSync = true;

	public TileMithrilineFurnace() {
		super();
		tracker = new TileStatTracker(this);
	}
	
	@Override
    public void readFromNBT(NBTTagCompound i) {
		super.readFromNBT(i);
		MiscUtils.loadInventory(this, i);
		energy = i.getFloat("energy");
		progress = i.getFloat("progress");
    }
	
	@Override
    public void writeToNBT(NBTTagCompound i) {
    	super.writeToNBT(i);
    	MiscUtils.saveInventory(this, i);
    	i.setFloat("energy", energy);
    	i.setFloat("progress", progress);
    }
	
	@Override
	public void updateEntity() {
		boolean correct = isStructureCorrect();
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
		
		if(correct) {
			Coord3D[] possiblePowerSources = new Coord3D[] {
					new Coord3D(0, 1, 0),
					new Coord3D(1, 0, 1), new Coord3D(-1, 0, 1), new Coord3D(1, 0, -1), new Coord3D(-1, 0, -1),
					new Coord3D(2, 1, 0), new Coord3D(-2, 1, 0), new Coord3D(0, 1, 2), new Coord3D(0, 1, -2),
					new Coord3D(2, 2, 2), new Coord3D(-2, 2, 2), new Coord3D(2, 2, -2), new Coord3D(-2, 2, -2)
					
			};
			if(energy < maxEnergy)
				for(int i = 0; i < possiblePowerSources.length; ++i) {
					Coord3D c = possiblePowerSources[i];
					int dX = MathHelper.floor_float(c.x);
					int dY = MathHelper.floor_float(c.y);
					int dZ = MathHelper.floor_float(c.z);
					Block b = worldObj.getBlock(xCoord+dX, yCoord+dY, zCoord+dZ);
					if(b instanceof BlockMithrilineCrystal) {
						TileEntity c_tile = worldObj.getTileEntity(xCoord+dX, yCoord+dY, zCoord+dZ);
						if(c_tile != null && c_tile instanceof TileMithrilineCrystal) {
							TileMithrilineCrystal crystal = (TileMithrilineCrystal) c_tile;
							float c_energy = crystal.energy;
							float movement = worldObj.getWorldTime() % 60;
							if(movement > 30)
								movement = 60F - movement;
							
							if(energy + c_energy <= maxEnergy) {
								energy += c_energy;
								crystal.energy = 0;
								worldObj.spawnParticle("reddust", xCoord+dX+0.5F, yCoord+dY+movement/30, zCoord+dZ+0.5F, -1, 1, 0);
								worldObj.spawnParticle("reddust", xCoord+dX+0.5F, yCoord+dY+2+movement/30, zCoord+dZ+0.5F, -1, 1, 0);
							}
							else {
								float energyReq = maxEnergy - energy;
								if(c_energy >= energyReq) {
									energy = maxEnergy;
									crystal.energy -= energyReq;
									worldObj.spawnParticle("reddust", xCoord+dX+0.5F, yCoord+dY+movement/30, zCoord+dZ+0.5F, -1, 1, 0);
									worldObj.spawnParticle("reddust", xCoord+dX+0.5F, yCoord+dY+2+movement/30, zCoord+dZ+0.5F, -1, 1, 0);
								}
							}
						}
					}
				}
			
			if(getStackInSlot(0) != null) {
				MithrilineFurnaceRecipe rec = MithrilineFurnaceRecipes.findRecipeByComponent(getStackInSlot(0));
				if(rec != null && getStackInSlot(0).stackSize >= rec.requiredRecipeSize) {
					reqProgress = rec.enderStarPulsesRequired;
					if(getStackInSlot(1) == null || (getStackInSlot(1).isItemEqual(rec.result) && getStackInSlot(1).stackSize+rec.result.stackSize <= getStackInSlot(1).getMaxStackSize())) {
						if(energy >= reqProgress) {
							progress = reqProgress;
							energy -= reqProgress;
						}
						else {
							progress += energy;
							energy = 0;
						}
						if(progress >= reqProgress) {
							decrStackSize(0, rec.requiredRecipeSize);
							if(getStackInSlot(1) == null)
								setInventorySlotContents(1, rec.result.copy());
							else
								getStackInSlot(1).stackSize += rec.result.stackSize;
							
							progress = 0;
						}
					}
				}
				else {
					progress = 0;
					reqProgress = 0;
				}
			}
			else {
				progress = 0;
				reqProgress = 0;
			}
			
			for(int i = 0; i < 10; ++i)
				EssentialCraftCore.proxy.FlameFX(xCoord+0.5F + MathUtils.randomFloat(worldObj.rand)*0.4F, yCoord+0.2F + MathUtils.randomFloat(worldObj.rand)*0.6F, zCoord+0.5F + MathUtils.randomFloat(worldObj.rand)*0.4F, 0, 0.01F, 0, 0D, 1D, 0F, 1F);
		}
	}
	
	public boolean isStructureCorrect() {
		int x = xCoord;
		int y = yCoord;
		int z = zCoord;
		boolean hasPlatformBelow = 
				worldObj.getBlock(x-1, y-1, z) == BlocksCore.invertedBlock &&
				worldObj.getBlock(x+1, y-1, z) == BlocksCore.invertedBlock &&
				worldObj.getBlock(x, y-1, z-1) == BlocksCore.invertedBlock &&
				worldObj.getBlock(x, y-1, z+1) == BlocksCore.invertedBlock &&
				worldObj.getBlock(x+1, y-1, z+1) == BlocksCore.invertedBlock && 
				worldObj.getBlock(x+1, y-1, z-1) == BlocksCore.invertedBlock &&
				worldObj.getBlock(x-1, y-1, z+1) == BlocksCore.invertedBlock &&
				worldObj.getBlock(x-1, y-1, z-1) == BlocksCore.invertedBlock &&
				worldObj.getBlock(x-2, y-1, z) == BlocksCore.invertedBlock &&
				worldObj.getBlock(x+2, y-1, z) == BlocksCore.invertedBlock &&
				worldObj.getBlock(x, y-1, z-2) == BlocksCore.invertedBlock &&
				worldObj.getBlock(x, y-1, z+2) == BlocksCore.invertedBlock;
		
		boolean hasGenericOutline = 
				worldObj.getBlock(x-2, y, z) == BlocksCore.invertedBlock &&
				worldObj.getBlock(x+2, y, z) == BlocksCore.invertedBlock &&
				worldObj.getBlock(x, y, z-2) == BlocksCore.invertedBlock &&
				worldObj.getBlock(x, y, z+2) == BlocksCore.invertedBlock &&
				worldObj.getBlock(x-2, y, z+2) == BlocksCore.invertedBlock &&
				worldObj.getBlock(x+2, y, z-2) == BlocksCore.invertedBlock &&
				worldObj.getBlock(x-2, y, z-2) == BlocksCore.invertedBlock &&
				worldObj.getBlock(x+2, y, z+2) == BlocksCore.invertedBlock &&
				worldObj.getBlock(x-2, y+1, z+2) == BlocksCore.invertedBlock &&
				worldObj.getBlock(x+2, y+1, z-2) == BlocksCore.invertedBlock &&
				worldObj.getBlock(x-2, y+1, z-2) == BlocksCore.invertedBlock &&
				worldObj.getBlock(x+2, y+1, z+2) == BlocksCore.invertedBlock; 
		
		return hasPlatformBelow && hasGenericOutline;
	}
	
	@Override
    public Packet getDescriptionPacket() {
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		writeToNBT(nbttagcompound);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, -10, nbttagcompound);
	}
	
	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		if(net.getNetHandler() instanceof INetHandlerPlayClient)
			if(pkt.func_148853_f() == -10)
				readFromNBT(pkt.func_148857_g());
	}
	
	public static void setupConfig(Configuration cfg) {
		try {
			cfg.load();
			String[] cfgArrayString = cfg.getStringList("MithrilineFurnaceSettings", "tileentities", new String[] {
					"Maximum enderstar pulse stored:10000"
			}, "");
			String dataString = "";
			
			for(int i = 0; i < cfgArrayString.length; ++i)
				dataString += "||" + cfgArrayString[i];
			
			DummyData[] data = DataStorage.parseData(dataString);
			
			maxEnergy = Float.parseFloat(data[0].fieldValue);
			
			cfg.save();
		}
		catch(Exception e) {
			return;
		}
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
				
				if(items[par1].stackSize == 0)
					items[par1] = null;
				
				return itemstack;
			}
		}
		else
			return null;
	}
	
	@Override
	public ItemStack getStackInSlotOnClosing(int par1) {
		if(items[par1] != null) {
			ItemStack itemstack = items[par1];
			items[par1] = null;
			return itemstack;
		}
		else
			return null;
	}
	
	@Override
	public void setInventorySlotContents(int par1, ItemStack par2ItemStack) {
		items[par1] = par2ItemStack;
		
		if(par2ItemStack != null && par2ItemStack.stackSize > getInventoryStackLimit()) {
			par2ItemStack.stackSize = getInventoryStackLimit();
		}
	}
	
	@Override
	public String getInventoryName() {
		return "ec3.container.mithrilineFurnace";
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
		return p_94041_1_ == 0;
	}
	
	@Override
	public int[] getAccessibleSlotsFromSide(int p_94128_1_) {
		return p_94128_1_ == 0 ? new int[] {1} : new int[] {0};
	}
	
	@Override
	public boolean canInsertItem(int p_102007_1_, ItemStack p_102007_2_, int p_102007_3_) {
		return isItemValidForSlot(p_102007_1_, p_102007_2_);
	}
	
	@Override
	public boolean canExtractItem(int p_102008_1_, ItemStack p_102008_2_, int p_102008_3_) {
		return p_102008_1_ == 1;
	}
}
