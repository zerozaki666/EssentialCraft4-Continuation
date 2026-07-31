package ec3.common.tile;

import java.util.Random;

import DummyCore.Utils.DataStorage;
import DummyCore.Utils.DummyData;
import DummyCore.Utils.MiscUtils;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.config.Configuration;

public class TileElementalCrystal extends TileEntity{
	public int syncTick = 10;
	public float size,fire,water,earth,air;
	
	public static float mutatuinChance = 0.001F;
	public static float growthModifier = 1.0F;
	
	@Override
    public void readFromNBT(NBTTagCompound i) {
		super.readFromNBT(i);
		size = i.getFloat("size");
		fire = i.getFloat("fire");
		water = i.getFloat("water");
		earth = i.getFloat("earth");
		air = i.getFloat("air");
    }
	
	@Override
    public void writeToNBT(NBTTagCompound i)  {
    	super.writeToNBT(i);
    	i.setFloat("size", size);
    	i.setFloat("fire", fire);
    	i.setFloat("water", water);
    	i.setFloat("earth", earth);
    	i.setFloat("air", air);
    }
    
	public float getElementByNum(int num) {
		if(num == 0)
			return fire;
		if(num == 1)
			return water;
		if(num == 2)
			return earth;
		if(num == 3)
			return air;
		return -1;
	}
	
	public void setElementByNum(int num, float amount) {
		if(num == 0)
			fire += amount;
		if(num == 1)
			water += amount;
		if(num == 2)
			earth += amount;
		if(num == 3)
			air += amount;
	}
	
	public void randomlyMutate() {
		Random r = worldObj.rand;
		if(r.nextFloat() <= mutatuinChance)
			mutate(r.nextInt(4), r.nextInt(3)-r.nextInt(3));
	}
	
	public boolean mutate(int element, int amount)  {
		if(getElementByNum(element) + amount <= 100 && getElementByNum(element) + amount >= 0)
			setElementByNum(element, amount);
		return false;
	}
	
	public int getDominant() {
		if(fire > water && fire > earth && fire > air)
			return 0;
		if(water > fire && water > earth && water > air)
			return 1;
		if(earth > water && earth > fire && earth > air)
			return 2;
		if(air > fire && air > earth && air > water)
			return 3;
		return -1;
	}
	
	public void updateEntity() {
		int metadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		
		if(metadata == 1) {
			Block b = worldObj.getBlock(xCoord, yCoord-1, zCoord);
			if(!b.isBlockSolid(worldObj, xCoord, yCoord-1, zCoord, 0)) {
				worldObj.getBlock(xCoord, yCoord, zCoord).dropBlockAsItem(getWorldObj(), xCoord, yCoord, zCoord, metadata, 0);
				worldObj.setBlockToAir(xCoord, yCoord, zCoord);
			}
		}
		
		if(metadata == 0) {
			Block b = worldObj.getBlock(xCoord, yCoord+1, zCoord);
			if(!b.isBlockSolid(worldObj, xCoord, yCoord+1, zCoord, 1)) {
				worldObj.getBlock(xCoord, yCoord, zCoord).dropBlockAsItem(getWorldObj(), xCoord, yCoord, zCoord, metadata, 0);
				worldObj.setBlockToAir(xCoord, yCoord, zCoord);
			}
		}
		
		if(metadata == 2) {
			Block b = worldObj.getBlock(xCoord, yCoord, zCoord+1);
			if(!b.isBlockSolid(worldObj, xCoord, yCoord, zCoord+1, 3)) {
				worldObj.getBlock(xCoord, yCoord, zCoord).dropBlockAsItem(getWorldObj(), xCoord, yCoord, zCoord, metadata, 0);
				worldObj.setBlockToAir(xCoord, yCoord, zCoord);
			}
		}
		
		if(metadata == 3) {
			Block b = worldObj.getBlock(xCoord, yCoord, zCoord-1);
			if(!b.isBlockSolid(worldObj, xCoord, yCoord, zCoord-1, 2)) {
				worldObj.getBlock(xCoord, yCoord, zCoord).dropBlockAsItem(getWorldObj(), xCoord, yCoord, zCoord, metadata, 0);
				worldObj.setBlockToAir(xCoord, yCoord, zCoord);
			}
		}
		
		if(metadata == 4) {
			Block b = worldObj.getBlock(xCoord+1, yCoord, zCoord);
			if(!b.isBlockSolid(worldObj, xCoord+1, yCoord, zCoord, 5)) {
				worldObj.getBlock(xCoord, yCoord, zCoord).dropBlockAsItem(getWorldObj(), xCoord, yCoord, zCoord, metadata, 0);
				worldObj.setBlockToAir(xCoord, yCoord, zCoord);
			}
		}
		
		if(metadata == 5) {
			Block b = worldObj.getBlock(xCoord-1, yCoord, zCoord);
			if(!b.isBlockSolid(worldObj, xCoord-1, yCoord, zCoord, 4)) {
				worldObj.getBlock(xCoord, yCoord, zCoord).dropBlockAsItem(getWorldObj(), xCoord, yCoord, zCoord, metadata, 0);
				worldObj.setBlockToAir(xCoord, yCoord, zCoord);
			}
		}
		
		if(size < 100) {
			worldObj.spawnParticle("enchantmenttable", xCoord+worldObj.rand.nextFloat(),yCoord+1,zCoord+worldObj.rand.nextFloat(), 0, 0, 0);
			if(!worldObj.isRemote) {
	    		size += 0.002F*growthModifier;
	    			randomlyMutate();
			}
		}
		//Sending the sync packets to the CLIENT. 
		if(syncTick == 0) {
			if(!worldObj.isRemote)
				MiscUtils.sendPacketToAllAround(worldObj, getDescriptionPacket(), xCoord, yCoord, zCoord, worldObj.provider.dimensionId, 128);
			syncTick = 10;
		}
		else
			--syncTick;
	}
	
	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		writeToNBT(nbttagcompound);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, -10, nbttagcompound);
	}
	
	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		if(net.getNetHandler() instanceof INetHandlerPlayClient && pkt.func_148853_f() == -10)
			readFromNBT(pkt.func_148857_g());
	}
	
	public static void setupConfig(Configuration cfg) {
		try {
			cfg.load();
			String[] cfgArrayString = cfg.getStringList("ElementalCrystalSettings", "tileentities", new String[] {
					"Chance to mutate per tick:0.001",
					"Growth per tick modifier(crystal grows at 0.2% per tick):1.0"
			}, "");
			String dataString="";
			
			for(int i = 0; i < cfgArrayString.length; ++i)
				dataString += "||" + cfgArrayString[i];
			
			DummyData[] data = DataStorage.parseData(dataString);
			
			mutatuinChance = Float.parseFloat(data[0].fieldValue);
			growthModifier = Float.parseFloat(data[1].fieldValue);
			
			cfg.save();
		}
		catch(Exception e) {
			return;
		}
	}
}
