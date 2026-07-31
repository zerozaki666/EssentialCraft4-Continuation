package ec3.common.tile;

import net.minecraftforge.common.util.ForgeDirection;

public class TileFluidEjector extends TileMRUGeneric {

	public ForgeDirection getRotation() {
		int metadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		if(metadata > 5)
			metadata -= 6;
		return ForgeDirection.getOrientation(metadata);
	}
	
	public TileFluidEjector() {
		super();
		setSlotsNum(0);
		setMaxMRU(0);
	}

	@Override
	public int[] getOutputSlots() {
		return new int[0];
	}
}
