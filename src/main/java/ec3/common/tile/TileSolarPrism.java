package ec3.common.tile;

import DummyCore.Utils.DataStorage;
import DummyCore.Utils.DummyData;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.config.Configuration;
import ec3.common.entity.EntitySolarBeam;

public class TileSolarPrism extends TileEntity {
	
	public static float solarBeamChance = 0.025F;
	public static boolean requiresUnabstructedSky = true;
	public static boolean requiresMidday = true;
	public static boolean ignoreRain = false;
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		if(!worldObj.isRemote) {
			if(worldObj.rand.nextFloat() <= solarBeamChance && (worldObj.canBlockSeeTheSky(xCoord, yCoord, zCoord) || !requiresUnabstructedSky) && ((worldObj.getWorldTime() % 24000 >= 5000 && worldObj.getWorldTime() % 24000 <= 7000) || !requiresMidday) && (!worldObj.isRaining() || ignoreRain)) {
				int y = yCoord-1;
				while(y > 0 && worldObj.isAirBlock(xCoord, y, zCoord)) {
					--y;
					if(!worldObj.isAirBlock(xCoord, y, zCoord)) {
						EntitySolarBeam beam = new EntitySolarBeam(worldObj,xCoord,y,zCoord);
						worldObj.spawnEntityInWorld(beam);
					}
				}
			}
			if(worldObj.isAirBlock(xCoord+2, yCoord, zCoord) || worldObj.isAirBlock(xCoord-2, yCoord, zCoord) || worldObj.isAirBlock(xCoord, yCoord, zCoord-2) || worldObj.isAirBlock(xCoord, yCoord, zCoord+2)) {
				worldObj.getBlock(xCoord, yCoord, zCoord).dropBlockAsItem(worldObj, xCoord, yCoord, zCoord, getBlockMetadata(), 0);
				worldObj.setBlock(xCoord, yCoord, zCoord, Blocks.air, 0, 3);
			}
			if(!worldObj.isAirBlock(xCoord+1, yCoord, zCoord) || !worldObj.isAirBlock(xCoord-1, yCoord, zCoord) || !worldObj.isAirBlock(xCoord, yCoord, zCoord-1) || !worldObj.isAirBlock(xCoord, yCoord, zCoord+1)) {
				worldObj.getBlock(xCoord, yCoord, zCoord).dropBlockAsItem(worldObj, xCoord, yCoord, zCoord, getBlockMetadata(), 0);
				worldObj.setBlock(xCoord, yCoord, zCoord, Blocks.air, 0, 3);
			}
			if(!worldObj.isAirBlock(xCoord+1, yCoord, zCoord+1) || !worldObj.isAirBlock(xCoord+1, yCoord, zCoord-1) || !worldObj.isAirBlock(xCoord-1, yCoord, zCoord-1) || !worldObj.isAirBlock(xCoord-1, yCoord, zCoord+1)) {
				worldObj.getBlock(xCoord, yCoord, zCoord).dropBlockAsItem(worldObj, xCoord, yCoord, zCoord, getBlockMetadata(), 0);
				worldObj.setBlock(xCoord, yCoord, zCoord, Blocks.air, 0, 3);
			}
		}
	}
	
	public static void setupConfig(Configuration cfg) {
		try {
			cfg.load();
			String[] cfgArrayString = cfg.getStringList("SolarPrismSettings", "tileentities", new String[] {
					"Chance each tick to create a solar beam:0.025",
					"Requires unobstructed sky:true",
					"Requires midday:true",
					"Is rain ignored:false"
			}, "");
			String dataString = "";
			
			for(int i = 0; i < cfgArrayString.length; ++i)
				dataString += "||" + cfgArrayString[i];
			
			DummyData[] data = DataStorage.parseData(dataString);
			
			solarBeamChance = Float.parseFloat(data[0].fieldValue);
			requiresUnabstructedSky = Boolean.parseBoolean(data[1].fieldValue);
			requiresMidday = Boolean.parseBoolean(data[2].fieldValue);
			ignoreRain = Boolean.parseBoolean(data[3].fieldValue);
			
			cfg.save();
		}
		catch(Exception e) {
			return;
		}
	}
}
