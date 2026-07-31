package ec3.common.tile;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.config.Configuration;
import DummyCore.Utils.DataStorage;
import DummyCore.Utils.DummyData;
import ec3.api.ApiCore;
import ec3.common.item.ItemEssence;
import ec3.common.item.ItemsCore;
import ec3.utils.common.ECUtils;

public class TileCrystalExtractor extends TileMRUGeneric {
	
	public int progressLevel;
	public static float cfgMaxMRU = ApiCore.DEVICE_MAX_MRU_GENERIC;
	public static int mruUsage = 100;
	public static int requiredTime = 1000;
	
	public TileCrystalExtractor() {
		super();
		maxMRU = (int)cfgMaxMRU;
		setSlotsNum(13);
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		ECUtils.manage(this, 0);
		
		if(!worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord))
			doWork();
		spawnParticles();
		
	}
	
	public void doWork() {
		if(canWork()) {
			if(getMRU() > mruUsage) {
				if(!worldObj.isRemote) {
					if(setMRU(getMRU() - mruUsage))
						++progressLevel;
					if(progressLevel >= requiredTime) {
						progressLevel = 0;
						createItems();
					}	
				}
			}
		}
	}
	
	public void createItems() {
		TileElementalCrystal t = getCrystal();
		float f = t.fire;
		float w = t.water;
		float e = t.earth;
		float a = t.air;
		float s = t.size * 3000;
		float[] baseChance = new float[] {f, w, e, a};
		int[] essenceChance = new int[] {37500, 50000, 75000, 150000};
		int[] getChance = new int[16];
		for(int i = 0; i < 16; ++i) {
			getChance[i] = (int)((s*baseChance[ItemEssence.convertDamageToIntBefore4(i)])/essenceChance[i/4]);
		}
		if(!worldObj.isRemote) {
			for(int i = 1; i < 13; ++i) {
				ItemStack st = new ItemStack(ItemsCore.essence,1,worldObj.rand.nextInt(16));
				if(worldObj.rand.nextInt(100) < getChance[st.getItemDamage()]) {
					int sts = worldObj.rand.nextInt(1 + getChance[st.getItemDamage()]/4);
					st.stackSize = sts;
					if(st.stackSize <= 0) {
						st.stackSize = 1;
					}
					setInventorySlotContents(i, st);
				}
			}
		}
	}
	
	public boolean canWork() {
		for(int i = 1; i < 13; ++i) {
			if(getStackInSlot(i) != null) {
				return false;
			}
		}
		if(getCrystal() == null)
			return false;
		return true;
	}
	
	public boolean hasItemInSlots() {
		for(int i = 1; i < 13; ++i) {
			if(getStackInSlot(i) != null) {
				return true;
			}
		}
		return false;
	}
	
	public void spawnParticles() {
		if(canWork() && getMRU() > 0) {
			TileElementalCrystal t = getCrystal();
			if(t != null)
				for(int o = 0; o < 10; ++o) {
					worldObj.spawnParticle("portal", xCoord + worldObj.rand.nextDouble(), t.yCoord + worldObj.rand.nextDouble(), zCoord + worldObj.rand.nextDouble(), (t.xCoord-xCoord), 0.0D, (t.zCoord-zCoord));
				}
		}
	}
	
	public TileElementalCrystal getCrystal() {
		TileElementalCrystal t = null;
		if(hasCrystalOnFront()) {
			t = (TileElementalCrystal)worldObj.getTileEntity(xCoord+1, yCoord, zCoord);
		}
		if(hasCrystalOnBack()) {
			t = (TileElementalCrystal)worldObj.getTileEntity(xCoord-1, yCoord, zCoord);
		}
		if(hasCrystalOnLeft()) {
			t = (TileElementalCrystal)worldObj.getTileEntity(xCoord, yCoord, zCoord+1);
		}
		if(hasCrystalOnRight()) {
			t = (TileElementalCrystal)worldObj.getTileEntity(xCoord, yCoord, zCoord-1);
		}
		return t;
	}
	
	public boolean hasCrystalOnFront() {
		TileEntity t = worldObj.getTileEntity(xCoord+1, yCoord, zCoord);
		return t != null && t instanceof TileElementalCrystal;
	}
	
	public boolean hasCrystalOnBack() {
		TileEntity t = worldObj.getTileEntity(xCoord-1, yCoord, zCoord);
		return t != null && t instanceof TileElementalCrystal;
	}
	
	public boolean hasCrystalOnLeft() {
		TileEntity t = worldObj.getTileEntity(xCoord, yCoord, zCoord+1);
		return t != null && t instanceof TileElementalCrystal;
	}
	
	public boolean hasCrystalOnRight() {
		TileEntity t = worldObj.getTileEntity(xCoord, yCoord, zCoord-1);
		return t != null && t instanceof TileElementalCrystal;
	}
	
	public static void setupConfig(Configuration cfg) {
		try {
			cfg.load();
			String[] cfgArrayString = cfg.getStringList("CrystalExtractorSettings", "tileentities", new String[] {
					"Max MRU:" + ApiCore.DEVICE_MAX_MRU_GENERIC,
					"MRU Usage:100",
					"Ticks required to get an essence:1000"
			}, "");
			String dataString = "";
			
			for(int i = 0; i < cfgArrayString.length; ++i)
				dataString += "||" + cfgArrayString[i];
			
			DummyData[] data = DataStorage.parseData(dataString);
			
			mruUsage = Integer.parseInt(data[1].fieldValue);
			requiredTime = Integer.parseInt(data[2].fieldValue);
			cfgMaxMRU = Float.parseFloat(data[0].fieldValue);
			
			cfg.save();
		}
		catch(Exception e) {
			return;
		}
	}
	
	@Override
	public int[] getOutputSlots() {
		return new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
	}
	
	@Override
	public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_) {
		return p_94041_1_ == 0 && isBoundGem(p_94041_2_);
	}
}
