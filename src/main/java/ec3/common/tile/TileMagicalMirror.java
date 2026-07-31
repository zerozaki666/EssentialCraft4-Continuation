package ec3.common.tile;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import ec3.api.ApiCore;
import ec3.utils.common.ECUtils;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.config.Configuration;
import DummyCore.Utils.Coord3D;
import DummyCore.Utils.DataStorage;
import DummyCore.Utils.DummyData;
import DummyCore.Utils.MathUtils;
import DummyCore.Utils.UnformedItemStack;

public class TileMagicalMirror extends TileMRUGeneric {
	
	public Coord3D inventoryPos;
	public int transferTime = 0;
	
	public static float cfgMaxMRU = ApiCore.DEVICE_MAX_MRU_GENERIC;
	public static float cfgMaxDistance = 8;
	public ItemStack transferingStack;
	
	public boolean pulsing;
	
	public TileMagicalMirror() {
		super();
		setMaxMRU(cfgMaxMRU);
	}
	
	public void begin(TileMagicalAssembler assembler) {
		pulsing = true;
	}
	
	public void end(TileMagicalAssembler assembler) {
		pulsing = false;
		transferingStack = null;
		transferTime = 0;
	}
	
	public void pulse(TileMagicalAssembler assembler) {
		pulsing = true;
		if(pulsing) {
			TileEntity tile = worldObj.getTileEntity(xCoord, yCoord-1, zCoord);
			if(tile != null && tile instanceof ISidedInventory && tile instanceof IInventory) {
				IInventory inv = (IInventory) tile;
				int[] slots = ((ISidedInventory) tile).getAccessibleSlotsFromSide(1);
				f:
					for(int i : slots) {
					ItemStack is = inv.getStackInSlot(i);
					if(is != null) {
						int slot = findAssemblerSlotForIS(assembler, is);
						
						if(slot != -1) {
							if(transferingStack == null)
								transferingStack = is;
							if(transferTime < 60)
								++transferTime;
							double sX = xCoord+0.5D + MathUtils.randomDouble(worldObj.rand)/6;
							double sY = yCoord+0.5D + MathUtils.randomDouble(worldObj.rand)/6;
							double sZ = zCoord+0.5D + MathUtils.randomDouble(worldObj.rand)/6;
							double dX = assembler.xCoord+0.5D + MathUtils.randomDouble(worldObj.rand)/6;
							double dY = assembler.yCoord+0.5D + MathUtils.randomDouble(worldObj.rand)/6;
							double dZ = assembler.zCoord+0.5D + MathUtils.randomDouble(worldObj.rand)/6;
							if(transferTime < 20) {
								dX = sX;
								dY = sY;
								dZ = sZ;
								sY -= 1;
							}
							if(worldObj.getWorldTime()%5==0)
							ECUtils.spawnItemFX(sX, sY, sZ, dX, dY, dZ);
							if(transferTime >= 60) {
								ItemStack set = is.copy();
								set.stackSize = 1;
								assembler.setInventorySlotContents(slot, set);
								inv.decrStackSize(i, 1);
								transferingStack = null;
								transferTime = 0;
								//pulsing = false;
							}
							break f;
						}
					}
				}
			}
			else if(tile != null && tile instanceof IInventory) {
				IInventory inv = (IInventory) tile;
				int slots = inv.getSizeInventory();
				f:
					for(int i = 0; i < slots; ++i) {
						ItemStack is = inv.getStackInSlot(i);
						if(is != null) {
							int slot = findAssemblerSlotForIS(assembler, is);
							if(slot != -1) {
								if(transferingStack == null)
									transferingStack = is;
								if(transferTime < 60)
									++transferTime;
								double sX = xCoord+0.5D + MathUtils.randomDouble(worldObj.rand)/6;
								double sY = yCoord+0.5D + MathUtils.randomDouble(worldObj.rand)/6;
								double sZ = zCoord+0.5D + MathUtils.randomDouble(worldObj.rand)/6;
								double dX = assembler.xCoord+0.5D + MathUtils.randomDouble(worldObj.rand)/6;
								double dY = assembler.yCoord+0.5D + MathUtils.randomDouble(worldObj.rand)/6;
								double dZ = assembler.zCoord+0.5D + MathUtils.randomDouble(worldObj.rand)/6;
								if(transferTime < 20) {
									dX = sX;
									dY = sY;
									dZ = sZ;
									sY -= 1;
								}
								if(worldObj.getWorldTime()%5 == 0)
								ECUtils.spawnItemFX(sX, sY, sZ, dX, dY, dZ);
								if(transferTime >= 60) {
									ItemStack set = is.copy();
									set.stackSize = 1;
									assembler.setInventorySlotContents(slot, set);
									inv.decrStackSize(i, 1);
									transferingStack = null;
									transferTime = 0;
									//pulsing = false;
								}
								break f;
							}
						}
					}
			}
		}
	}
	
	public int findAssemblerSlotForIS(TileMagicalAssembler assembler, ItemStack is) {
		List<UnformedItemStack> lst = new ArrayList<UnformedItemStack>();
		
		lst.addAll(assembler.requiredItemsToCraft);

		for(int i = 2; i < 17; ++i) {
			ItemStack stk = assembler.getStackInSlot(i);
			if(stk != null) {
				c:
					for(int j = 0; j < lst.size(); ++j)
					{
						UnformedItemStack uis = lst.get(j);
						if(uis.itemStackMatches(stk))
						{
							lst.remove(j);
							break c;
						}
					}
			}
		}
		for(int i = 2; i < 17; ++i) {
			ItemStack stk = assembler.getStackInSlot(i);
			if(stk == null) {
				for(int j = 0; j < lst.size(); ++j) {
					UnformedItemStack uis = lst.get(j);
					if(uis != null && uis.itemStackMatches(is))
						return i;
				}
			}
		}
		return -1;
	}
	
	@Override
	public void updateEntity() {
		if(worldObj.isRemote) {
			if(pulsing && transferTime < 60)
				++transferTime;
		}
		super.updateEntity();
		if(inventoryPos != null) {
			TileEntity tile = worldObj.getTileEntity(MathHelper.floor_double(inventoryPos.x), MathHelper.floor_double(inventoryPos.y), MathHelper.floor_double(inventoryPos.z));
			if(tile != null && tile instanceof TileMagicalAssembler) {
				TileMagicalAssembler a = (TileMagicalAssembler)tile;
				if(!a.mirrorsTiles.contains(this)) {
					a.mirrors.add(new Coord3D(xCoord,yCoord,zCoord));
					a.mirrorsTiles.add(this);
				}
			}
			else {
				pulsing = false;
			}
		}
		TileEntity tile = worldObj.getTileEntity(xCoord, yCoord-1, zCoord);
		if(tile != null && tile instanceof TileMagicalAssembler && inventoryPos != null) {
			TileMagicalAssembler assembler = (TileMagicalAssembler) tile;
			TileEntity tile1 = worldObj.getTileEntity(MathHelper.floor_double(inventoryPos.x), MathHelper.floor_double(inventoryPos.y), MathHelper.floor_double(inventoryPos.z));
			if(tile1 != null && tile1 instanceof IInventory && !(tile1 instanceof TileMagicalAssembler)) {
				IInventory inv = (IInventory) tile1;
				if(assembler.getStackInSlot(17) != null) {
					ItemStack is = assembler.getStackInSlot(17);
					fori:
						for(int i = 0; i < inv.getSizeInventory(); ++i) {
							ItemStack is1 = inv.getStackInSlot(i);
							if(is1 == null || (is1.isItemEqual(is) && is1.stackSize+1 < is1.getMaxStackSize()+1)) {
								pulsing = true;
								if(!worldObj.isRemote && transferTime <= 60)
									++transferTime;
								transferingStack = assembler.getStackInSlot(17);
								double sX = xCoord+0.5D + MathUtils.randomDouble(worldObj.rand)/6;
								double sY = yCoord+0.5D + MathUtils.randomDouble(worldObj.rand)/6;
								double sZ = zCoord+0.5D + MathUtils.randomDouble(worldObj.rand)/6;
								double dX = inventoryPos.x+MathUtils.randomDouble(worldObj.rand)/6;
								double dY = inventoryPos.y+MathUtils.randomDouble(worldObj.rand)/6;
								double dZ = inventoryPos.z+MathUtils.randomDouble(worldObj.rand)/6;
								if(transferTime < 20) {
									dX = sX;
									dY = sY;
									dZ = sZ;
									sY -= 1;
								}
								if(worldObj.getWorldTime()%5 == 0)
									ECUtils.spawnItemFX(sX, sY, sZ, dX, dY, dZ);
								if(transferTime >= 60) {
									ItemStack set = is.copy();
									set.stackSize = 1;
									if(inv.getStackInSlot(i) == null)
										inv.setInventorySlotContents(i, set);
									else
										++inv.getStackInSlot(i).stackSize;
									assembler.decrStackSize(17, 1);
									transferingStack = null;
									transferTime = 0;
									pulsing = false;
								}
								break fori;
							}
						}
				}
			}
		}
		else if(tile instanceof IInventory)
		{
			IInventory assembler = (IInventory) tile;
			if(inventoryPos != null)
			{
				TileEntity tile1 = worldObj.getTileEntity(MathHelper.floor_double(inventoryPos.x), MathHelper.floor_double(inventoryPos.y), MathHelper.floor_double(inventoryPos.z));
				if(assembler != null && tile1 != null && tile1 instanceof IInventory && !(tile1 instanceof TileMagicalAssembler))
				{
					IInventory inv = (IInventory) tile1;
					CycleF:
						for(int w = 0; w < assembler.getSizeInventory(); ++w) {
							if(assembler.getStackInSlot(w) != null) {
								ItemStack is = assembler.getStackInSlot(w);
								for(int i = 0; i < inv.getSizeInventory(); ++i) {
									ItemStack is1 = inv.getStackInSlot(i);
									if(is1 == null || (is1.isItemEqual(is) && is1.stackSize + 1 < is1.getMaxStackSize() + 1)) {
										pulsing = true;
										syncTick = 0;
										if(!worldObj.isRemote && transferTime <= 60)
											++transferTime;
										transferingStack = assembler.getStackInSlot(w);
										double sX = xCoord+0.5D + MathUtils.randomDouble(worldObj.rand)/6;
										double sY = yCoord+0.5D + MathUtils.randomDouble(worldObj.rand)/6;
										double sZ = zCoord+0.5D + MathUtils.randomDouble(worldObj.rand)/6;
										double dX = inventoryPos.x + MathUtils.randomDouble(worldObj.rand)/6;
										double dY = inventoryPos.y + MathUtils.randomDouble(worldObj.rand)/6;
										double dZ = inventoryPos.z + MathUtils.randomDouble(worldObj.rand)/6;
										if(transferTime < 20) {
											dX = sX;
											dY = sY;
											dZ = sZ;
											sY -= 1;
										}
										if(worldObj.getWorldTime()%5 == 0)
										ECUtils.spawnItemFX(sX, sY, sZ, dX, dY, dZ);
										if(transferTime >= 60) {
											ItemStack set = is.copy();
											set.stackSize = 1;
											if(inv.getStackInSlot(i) == null)
												inv.setInventorySlotContents(i, set);
											else
												++inv.getStackInSlot(i).stackSize;
											assembler.decrStackSize(w, 1);
											transferingStack = null;
											transferTime = 0;
											pulsing = false;
										}
										break CycleF;
									}
								}
							}
						}
				}
			}
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound i) {
		if(i.hasKey("coord")) {
			DummyData[] coordData = DataStorage.parseData(i.getString("coord"));
			inventoryPos = new Coord3D(Double.parseDouble(coordData[0].fieldValue),Double.parseDouble(coordData[1].fieldValue),Double.parseDouble(coordData[2].fieldValue));
		}
		else
			inventoryPos = null;
		transferTime = i.getInteger("transferTime");
		pulsing = i.getBoolean("pulse");
		if(i.hasKey("transferingStack")) {
			NBTTagCompound tag = i.getCompoundTag("transferingStack");
			ItemStack is = ItemStack.loadItemStackFromNBT(tag);
			if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
				is.stackSize = 1;
			transferingStack = is;
		}
		else {
			transferingStack = null;
		}
		super.readFromNBT(i);
	}
	
	@Override
	public void writeToNBT(NBTTagCompound i) {
		if(inventoryPos != null)
			i.setString("coord", inventoryPos.toString());
		i.setInteger("transferTime", transferTime);
		i.setBoolean("pulse", pulsing);
		if(transferingStack != null) {
			NBTTagCompound tag = new NBTTagCompound();
			transferingStack.writeToNBT(tag);
			i.setTag("transferingStack", tag);
		}
		super.writeToNBT(i);
	}
	
	public static void setupConfig(Configuration cfg) {
		try {
			cfg.load();
			String[] cfgArrayString = cfg.getStringList("MagicalMirrorSettings", "tileentities", new String[] {
					"Max MRU:"+ApiCore.DEVICE_MAX_MRU_GENERIC,
					"Max range of item transfering:8.0"
			}, "");
			String dataString = "";
			
			for(int i = 0; i < cfgArrayString.length; ++i)
				dataString += "||" + cfgArrayString[i];
			
			DummyData[] data = DataStorage.parseData(dataString);
			
			cfgMaxMRU = Float.parseFloat(data[0].fieldValue);
			cfgMaxDistance = Float.parseFloat(data[1].fieldValue);
			
			cfg.save();
		}
		catch(Exception e) {
			return;
		}
	}
	
	@Override
	public int[] getOutputSlots() {
		return new int[0];
	}
}
