package ec3.common.tile;

import ec3.api.ApiCore;
import ec3.utils.common.ECUtils;

@Deprecated
public class TileTeleportationRune extends TileMRUGeneric {

	public TileTeleportationRune() {
		setSlotsNum(2);
		setMaxMRU(ApiCore.DEVICE_MAX_MRU_GENERIC);
	}
	
	@Override
	public int[] getOutputSlots() {
		return new int[0];
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		ECUtils.manage(this, 0);
	}
	
	public void teleport() {}
}
