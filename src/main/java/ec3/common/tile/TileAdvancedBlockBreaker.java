package ec3.common.tile;

import ec3.common.inventory.InventoryMagicFilter;
import ec3.common.item.ItemFilter;
import ec3.utils.common.ECUtils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class TileAdvancedBlockBreaker extends TileMRUGeneric {

	public TileAdvancedBlockBreaker() {
		setMaxMRU(0);
		setSlotsNum(1);
	}
	
	public ForgeDirection getRotation() {
		int metadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		if(metadata > 5)
			metadata -= 6;
		return ForgeDirection.getOrientation(metadata);
	}
	
	@Override
	public int[] getOutputSlots() {
		return new int[] {0};
	}
	
	public void breakBlocks() {
		for(int i = 1; i < 13; ++i) {
			Block b = worldObj.getBlock(xCoord + getRotation().offsetX*i, yCoord + getRotation().offsetY*i, zCoord + getRotation().offsetZ*i);
			if(b != null && !b.isAir(worldObj, xCoord + getRotation().offsetX*i, yCoord + getRotation().offsetY*i, zCoord + getRotation().offsetZ*i)) {
				ItemStack fromBlock = new ItemStack(b,1,worldObj.getBlockMetadata(xCoord + getRotation().offsetX*i, yCoord + getRotation().offsetY*i, zCoord + getRotation().offsetZ*i));
				World w = worldObj;
				int dX = xCoord + getRotation().offsetX*i;
				int dY = yCoord + getRotation().offsetY*i;
				int dZ = zCoord + getRotation().offsetZ*i;
				if(getStackInSlot(0) == null || !(getStackInSlot(0).getItem() instanceof ItemFilter)) {
					b.breakBlock(w, dX, dY, dZ, b, w.getBlockMetadata(dX, dY, dZ));
					b.onBlockDestroyedByPlayer(w, dX, dY, dZ, w.getBlockMetadata(dX, dY, dZ));
					b.dropBlockAsItem(w, dX, dY, dZ, w.getBlockMetadata(dX, dY, dZ), 0);
					w.setBlock(dX, dY, dZ, Blocks.air, 0, 2);
				}
				else {
					if(ECUtils.canFilterAcceptItem(new InventoryMagicFilter(getStackInSlot(0)), fromBlock, getStackInSlot(0))) {
						b.breakBlock(w, dX, dY, dZ, b, w.getBlockMetadata(dX, dY, dZ));
						b.onBlockDestroyedByPlayer(w, dX, dY, dZ, w.getBlockMetadata(dX, dY, dZ));
						b.dropBlockAsItem(w, dX, dY, dZ, w.getBlockMetadata(dX, dY, dZ), 0);
						w.setBlock(dX, dY, dZ, Blocks.air, 0, 2);
					}
				}
			}
		}
	}
}
