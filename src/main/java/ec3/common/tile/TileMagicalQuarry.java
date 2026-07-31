package ec3.common.tile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import DummyCore.Utils.DataStorage;
import DummyCore.Utils.DummyData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.FakePlayer;
import ec3.api.ApiCore;
import ec3.common.item.ItemGenericEC3;
import ec3.common.item.ItemsCore;
import ec3.common.mod.EssentialCraftCore;
import ec3.network.PacketNBT;
import ec3.utils.common.ECUtils;

public class TileMagicalQuarry extends TileMRUGeneric {
	
	public int progressLevel;
	public int miningX;
	public int miningY;
	public int miningZ;
	public boolean flag;
	
	public static float cfgMaxMRU = ApiCore.DEVICE_MAX_MRU_GENERIC;
	public static boolean generatesCorruption = true;
	public static int genCorruption = 2;
	public static boolean ignoreLiquids = true;
	public static float mruUsage = 8;
	public static float efficencyPerUpgrade = 0.5F;
	public static float blockHardnessModifier = 9F;
	
	public static final List<Object> voidList = new ArrayList<Object>();
	
	static {
		voidList.add(Blocks.cobblestone);
		voidList.add(Blocks.dirt);
		voidList.add(Blocks.grass);
		voidList.add(Blocks.stone);
		voidList.add(Blocks.sand);
		voidList.add(Blocks.sandstone);
		voidList.add(Blocks.tallgrass);
		voidList.add(Blocks.red_flower);
		voidList.add(Blocks.yellow_flower);
		voidList.add(Blocks.brown_mushroom);
		voidList.add(Blocks.red_mushroom);
		voidList.add(Blocks.leaves);
		voidList.add(Blocks.leaves2);
		voidList.add(Items.wheat_seeds);
	}
	 
	public TileMagicalQuarry() {
		super();
		maxMRU = (int)cfgMaxMRU;
		setSlotsNum(5);
	}
	
	public boolean canGenerateMRU() {
		return false;
	}
	
	@Override
	public void updateEntity() {
		if(syncTick == 10)
			syncTick = 0;
		super.updateEntity();
		if(!worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord))
			mine();
		if(!worldObj.isRemote)
			collectItems();
		
		ECUtils.manage(this, 0);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound i) {
		super.readFromNBT(i);
		progressLevel = i.getInteger("progressLevel");
		miningX = i.getInteger("miningX");
		miningY = i.getInteger("miningY");
		miningZ = i.getInteger("miningZ");
		flag = i.getBoolean("localFlag");
	}
	
	@Override
	public void writeToNBT(NBTTagCompound i) {
		super.writeToNBT(i);
		i.setInteger("progressLevel", progressLevel);
		i.setInteger("miningX", miningX);
		i.setInteger("miningY", miningY);
		i.setInteger("miningZ", miningZ);
		i.setBoolean("localFlag", flag);
	}
	
	public boolean hasInventoryUpgrade() {
		ItemStack s = ItemGenericEC3.getStkByName("inventoryUpgrade");
		for(int i = 0; i < getSizeInventory(); ++i) {
			if(getStackInSlot(i) != null && getStackInSlot(i).isItemEqual(s))
				return true;
		}
		return false;
	}
	
	public boolean hasSmeltingUpgrade() {
    	ItemStack s = ItemGenericEC3.getStkByName("blazingUpgrade");
    	for(int i = 0; i < getSizeInventory(); ++i) {
    		if(getStackInSlot(i) != null && getStackInSlot(i).isItemEqual(s))
    			return true;
    	}
    	return false;
	}
	
	public boolean hasVoidUpgrade() {
		ItemStack s = ItemGenericEC3.getStkByName("voidUpgrade");
		for(int i = 0; i < getSizeInventory(); ++i) {
			if(getStackInSlot(i) != null && getStackInSlot(i).isItemEqual(s))
				return true;
		}
		return false;
	}
	
	public boolean hasSilkyUpgrade() {
		if(hasSmeltingUpgrade())
			return false;
		
		ItemStack s = ItemGenericEC3.getStkByName("silkyUpgrade");
		for(int i = 0; i < getSizeInventory(); ++i) {
			if(getStackInSlot(i) != null && getStackInSlot(i).isItemEqual(s))
				return true;
		}
		return false;
	}
	
	public boolean hasFortuneUpgrade() {
		if(hasSilkyUpgrade())
			return false;
		
		ItemStack s = ItemGenericEC3.getStkByName("fortuneUpgrade");
		for(int i = 0; i < getSizeInventory(); ++i) {
			if(getStackInSlot(i) != null && getStackInSlot(i).isItemEqual(s))
				return true;
		}
		return false;
	}
    
	public boolean hasMiningUpgrade() {
		ItemStack s = ItemGenericEC3.getStkByName("diamondUpgrade");
		for(int i = 0; i < getSizeInventory(); ++i) {
			if(getStackInSlot(i) != null && getStackInSlot(i).isItemEqual(s))
				return true;
		}
		return false;
	}
    
	public boolean hasInventory() {
		TileEntity t = worldObj.getTileEntity(xCoord, yCoord+1, zCoord);
		if(t != null && t instanceof IInventory)
			return hasSpaceInInv((IInventory)t);
		return false;
	}
    
	public IInventory getInventory() {
		TileEntity t = worldObj.getTileEntity(xCoord, yCoord+1, zCoord);
		if(t != null && t instanceof IInventory)
			return (IInventory)t;
		return null;
	}
	
	public boolean hasSpaceInInv(IInventory inv) {
		for(int i = 0; i < inv.getSizeInventory(); ++i) {
			if(inv.getStackInSlot(i) == null)
				return true;
		}
		return false;
	}
	
	public float getEfficency() {
		float f = efficencyPerUpgrade + 1;
		ItemStack s = ItemGenericEC3.getStkByName("efficencyUpgrade");
		for(int i = 0; i < getSizeInventory(); ++i) {
			if(getStackInSlot(i) != null && getStackInSlot(i).isItemEqual(s))
				f += (float)getStackInSlot(i).stackSize * efficencyPerUpgrade;
		}
		return hasSmeltingUpgrade() ? f/2 : f;
	}
	
	public int getMiningRange() {
		int f = 5;
		ItemStack s = ItemGenericEC3.getStkByName("diamondUpgrade");
		for(int i = 0; i < getSizeInventory(); ++i) {
			if(getStackInSlot(i) != null && getStackInSlot(i).isItemEqual(s))
				f += getStackInSlot(i).stackSize;
		}
		return f;
	}
	
	public boolean canMineBlock(Block b) {
		if(b == Blocks.bedrock)
			return false;
		if(b == Blocks.obsidian && !hasMiningUpgrade())
			return false;
		return true;
	}
	
	public boolean shouldInstaMine(Block b) {
		return (b instanceof BlockLiquid && ignoreLiquids) || b == null || b == Blocks.air;
	}
	
	public int determineFortune() {
		int fortune = 0;
		ItemStack s = ItemGenericEC3.getStkByName("fortuneUpgrade");
		for(int i = 0; i < getSizeInventory(); ++i) {
			if(getStackInSlot(i) != null && getStackInSlot(i).isItemEqual(s))
				fortune += getStackInSlot(i).stackSize;
		}
		
		if(fortune <= 128)
			fortune = 5;
		if(fortune <= 64)
			fortune = 4;
		if(fortune <= 32)
			fortune = 3;
		if(fortune <= 16)
			fortune = 2;
		if(fortune <= 8)
			fortune = 1;
		
		return fortune;
	}
	
	public boolean mineBlock(Block b) {
		if(canMineBlock(b)) {
			NBTTagCompound currentMinedCoords = new NBTTagCompound();
			currentMinedCoords.setInteger("x", xCoord);
			currentMinedCoords.setInteger("y", yCoord);
			currentMinedCoords.setInteger("z", zCoord);
			currentMinedCoords.setInteger("mx", miningX);
			currentMinedCoords.setInteger("my", miningY);
			currentMinedCoords.setInteger("mz", miningZ);
			PacketNBT packet = new PacketNBT(currentMinedCoords).setID(4);
			EssentialCraftCore.network.sendToAllAround(packet, new TargetPoint(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, 16+getMiningRange()));
			
			if(shouldInstaMine(b)) {
				worldObj.setBlock(miningX, miningY, miningZ, Blocks.air, 0, 3);
				return true;
			}
			else {
				float required = b.getBlockHardness(worldObj, miningX, miningY, miningZ)*blockHardnessModifier;
				if(setMRU((int)(getMRU() - (mruUsage/4*getEfficency()))))
					progressLevel += getEfficency();
				if(progressLevel >= required) {
					FakePlayer quarryFakePlayer = new FakePlayer((WorldServer)worldObj, quarryFakePlayerProfile);
					
					progressLevel = 0;
					if(hasMiningUpgrade())
						quarryFakePlayer.setCurrentItemOrArmor(0, new ItemStack(ItemsCore.wind_elemental_pick));
					else
						quarryFakePlayer.setCurrentItemOrArmor(0, new ItemStack(ItemsCore.weak_elemental_pick));
					
					if(hasFortuneUpgrade())
						quarryFakePlayer.getCurrentEquippedItem().addEnchantment(Enchantment.fortune, determineFortune());
					if(hasSilkyUpgrade())
						quarryFakePlayer.getCurrentEquippedItem().addEnchantment(Enchantment.silkTouch, 1);
					
					b.harvestBlock(getWorldObj(), quarryFakePlayer, miningX, miningY, miningZ, worldObj.getBlockMetadata(miningX, miningY, miningZ));
					
					worldObj.setBlock(miningX, miningY, miningZ, Blocks.air, 0, 3);
					if(generatesCorruption)
						ECUtils.increaseCorruptionAt(worldObj, xCoord, yCoord, zCoord, worldObj.rand.nextInt(genCorruption));
					
					quarryFakePlayer = null;
				}
			}
		}
		return false;
	}
	
	public boolean isMainColomnMined() {
		int r = yCoord-2;
		while(--r >= 0) {
			Block b = worldObj.getBlock(xCoord, r, zCoord);
			if((b != null && b.getBlockHardness(worldObj,xCoord, r, zCoord) != -1 && b != Blocks.air && !(b instanceof BlockLiquid && ignoreLiquids) && canMineBlock(b)) && canMineBlock(b))
				return false;
		}
		return true;
	}
	
	public int genMiningColomnY(int current)  {
		int r = yCoord-2;
		while(--r >= 0) {
			Block b = worldObj.getBlock(xCoord, r, zCoord);
			if((b != null && b.getBlockHardness(worldObj,xCoord, r, zCoord) != -1 && b != Blocks.air && !(b instanceof BlockLiquid && ignoreLiquids) && canMineBlock(b)) && canMineBlock(b))
				return r;
		}
		return current;
	}
	
	public boolean isRowMined() {
		int rad = getMiningRange();
		for(int x = -rad; x <= rad; ++x) {
			for(int z = -rad; z <= rad; ++z) {
				Block b = worldObj.getBlock(xCoord+x, miningY, zCoord+z);
				if(b != null && b.getBlockHardness(worldObj,xCoord+x, miningY, zCoord+z) != -1 && b != Blocks.air && !(b instanceof BlockLiquid && ignoreLiquids) && canMineBlock(b))
					return false;
			}
		}
		return true;
	}
	
	public boolean canWork() {
		return getEfficency() > 0 && getMRU()>mruUsage;
	}
	
	public void mine() {
		if(canWork() && !worldObj.isRemote) {
			if(isMainColomnMined()) {
				
				if(!flag) {
					flag = true;
					miningY = yCoord-3;
				}
				flag = isMainColomnMined();
				
				if(isRowMined()) {
					--miningY;
				}
				else {
					int rad = getMiningRange();
					Fort:
						for(int x = -rad; x <= rad; ++x) {
							for(int z = -rad; z <= rad; ++z)
							{
								if(worldObj.checkChunksExist(xCoord+x-1, miningY-1, zCoord+z-1, xCoord+x+1, miningY+1, zCoord+z+1) && worldObj.blockExists(xCoord+x, miningY, zCoord+z) && worldObj.getBlock(xCoord+x, miningY, zCoord+z) != null && worldObj.getBlock(xCoord+x, miningY, zCoord+z).getBlockHardness(worldObj,xCoord+x, miningY, zCoord+z) != -1 && worldObj.getBlock(xCoord+x, miningY, zCoord+z) != Blocks.air && !(worldObj.getBlock(xCoord+x, miningY, zCoord+z) instanceof BlockLiquid))
								{
									miningX = xCoord+x;
									miningZ = zCoord+z;
									mineBlock(worldObj.getBlock(xCoord+x, miningY, zCoord+z));
									break Fort;
								}
							}
						}
				}
			}
			else {
				flag = false;
				miningY = genMiningColomnY(miningY);
				miningX = xCoord;
				miningZ = zCoord;
				if(worldObj.checkChunksExist(miningX-1, miningY-1, miningZ-1, miningX+1, miningY+1, miningZ+1) && worldObj.blockExists(miningX, miningY, miningZ)){
					if(worldObj.getBlock(miningX, miningY, miningZ) != null && worldObj.getBlock(miningX, miningY, miningZ) != Blocks.air && !(worldObj.getBlock(miningX, miningY, miningZ) instanceof BlockLiquid)){
						if(mineBlock(worldObj.getBlock(miningX, miningY, miningZ)))
							--miningY;
					}
					else
						--miningY;
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void collectItems() {
		List<EntityItem> l = worldObj.getEntitiesWithinAABB(EntityItem.class, AxisAlignedBB.getBoundingBox(miningX, miningY, miningZ, miningX+1, miningY+1, miningZ+1).expand(4D, 2D, 4D));
		if(!l.isEmpty()) {
			for(int i = 0; i < l.size(); ++i) {
				EntityItem item = l.get(i);
				ItemStack s = item.getEntityItem();
				if(hasSmeltingUpgrade() && s != null) {
					ItemStack forged = FurnaceRecipes.smelting().getSmeltingResult(s);
					if(forged != null) {
						ItemStack copy = forged.copy();
						copy.stackSize *= s.stackSize;
						if(hasFortuneUpgrade()) {
							int fortune = determineFortune();
							for(int i1 = 0; i1 < s.stackSize; ++i1) {
								copy.stackSize += worldObj.rand.nextInt(fortune);
							}
						}
						s = copy;
						item.setEntityItemStack(copy);
					}
				}
				if(hasVoidUpgrade()) {
					if(voidItemStack(s)) {
						item.setPositionAndRotation(0, 0, 0, 0, 0);
						item.setDead();
						return;
					}
				}
				item.setPositionAndRotation(0, 0, 0, 0, 0);
				item.setDead();
				if(hasInventoryUpgrade() && hasInventory())
					insertItem(s);
				else
					splitItem(s);
			}
		}
	}
	
	public boolean voidItemStack(ItemStack s) {
		return s == null || voidList.contains(s.getItem() instanceof ItemBlock ? Block.getBlockFromItem(s.getItem()) : s.getItem());
	}
	
	public void splitItem(ItemStack s) {
		EntityItem item = new EntityItem(worldObj, xCoord, yCoord, zCoord, s);
		item.setPositionAndRotation(xCoord+0.5D, yCoord+2, zCoord+0.5D, 0, 0);
		worldObj.spawnEntityInWorld(item);
	}
	
	public void insertItem(ItemStack s) {
		if(s == null)
			return;
		
		if(hasSpaceInInv(getInventory())) {
			for(int i = 0; i < getInventory().getSizeInventory(); ++i) {
				ItemStack stk = getInventory().getStackInSlot(i);
				if(stk != null && stk.isItemEqual(s) && stk.stackSize + s.stackSize <= stk.getMaxStackSize()) {
					stk.stackSize += s.stackSize;
					return;
				}
			}
			for(int i = 0; i < getInventory().getSizeInventory(); ++i) {
				ItemStack stk = getInventory().getStackInSlot(i);
				if(stk == null) {
					getInventory().setInventorySlotContents(i, s);
					return;
				}
			}
			splitItem(s);
		}
		else
			splitItem(s);
	}
	
	public static void setupConfig(Configuration cfg) {
		try {
			cfg.load();
			String[] cfgArrayString = cfg.getStringList("MagicalQuarrySettings", "tileentities", new String[] {
					"Max MRU:" + ApiCore.DEVICE_MAX_MRU_GENERIC,
					"MRU Usage:8",
					"Should not mine liquids:true",
					"Can this device actually generate corruption:true",
					"The amount of corruption generated each tick(do not set to 0!):2",
					"Efficency added to the device per upgrade:0.5",
					"Block hardness modifier:9.0"
			}, "");
			String dataString = "";
			
			for(int i = 0; i < cfgArrayString.length; ++i)
				dataString += "||" + cfgArrayString[i];
			
			DummyData[] data = DataStorage.parseData(dataString);
			
			cfgMaxMRU = Float.parseFloat(data[0].fieldValue);
			mruUsage = Integer.parseInt(data[1].fieldValue);
			ignoreLiquids = Boolean.parseBoolean(data[2].fieldValue);
			generatesCorruption = Boolean.parseBoolean(data[3].fieldValue);
			genCorruption = Integer.parseInt(data[4].fieldValue);
			efficencyPerUpgrade = Float.parseFloat(data[5].fieldValue);
			blockHardnessModifier = Float.parseFloat(data[6].fieldValue);
			
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
	
	public static GameProfile quarryFakePlayerProfile = new GameProfile(UUID.fromString("5cd89d0b-e9ba-0000-89f4-badbb05963dd"), "[EC3]Quarry");
}
