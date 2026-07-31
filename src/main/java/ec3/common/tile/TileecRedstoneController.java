package ec3.common.tile;

import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import ec3.api.EnumStructureType;
import ec3.api.IStructurePiece;

public class TileecRedstoneController extends TileEntity implements IStructurePiece {
	public TileecController controller;
	public UUID uuid = UUID.randomUUID();
	public int setting;
	public int tickTimer;
	
	public void readFromNBT(NBTTagCompound p_145839_1_) {
		super.readFromNBT(p_145839_1_);
		setting = p_145839_1_.getInteger("setting");
	}
	
	public void writeToNBT(NBTTagCompound p_145841_1_) {
		super.writeToNBT(p_145841_1_);
		p_145841_1_.setInteger("setting", setting);
	}
	
	@Override
	public EnumStructureType getStructure() {
		return EnumStructureType.MRUCUContaigementChamber;
	}

	@Override
	public TileEntity structureController() {
		return controller;
	}
	
	@Override
	public void setStructureController(TileEntity tile, EnumStructureType structure) {
		if(tile instanceof TileecController && structure == getStructure())
			controller = (TileecController)tile;
	}
	
	@Override
	public void updateEntity() {
		++tickTimer;
		if(tickTimer >= 20) {
			tickTimer = 0;
			worldObj.notifyBlockChange(xCoord, yCoord, zCoord, worldObj.getBlock(xCoord, yCoord, zCoord));
		}
	}
	
	public boolean outputRedstone() {
		if(controller != null && controller.getMRUCU() != null && ((float)controller.getMRU()/(float)controller.getMaxMRU())*10 >= setting)
			return true;
		
		return false;
	}
}
