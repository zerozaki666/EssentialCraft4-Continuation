package ec3.common.tile;

public class TileCreativeMRUSource extends TileMRUGeneric {

	public TileCreativeMRUSource() {
		setSlotsNum(0);
		setMaxMRU(100000);
	}
	
	
	@Override
	public int[] getOutputSlots() {
		return new int[0];
	}

	public void updateEntity()  {
		setMRU(getMaxMRU());
		super.updateEntity();
	}
}
