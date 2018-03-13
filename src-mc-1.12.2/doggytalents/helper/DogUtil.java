package doggytalents.helper;

import doggytalents.ModItems;
import doggytalents.entity.EntityDog;
import doggytalents.item.ItemChewStick;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class DogUtil {

	public static void teleportDogToOwner(Entity owner, Entity entity, World world, PathNavigate pathfinder) {
    	int i = MathHelper.floor(owner.posX) - 2;
        int j = MathHelper.floor(owner.posZ) - 2;
        int k = MathHelper.floor(owner.getEntityBoundingBox().minY);

        for(int l = 0; l <= 4; ++l) {
            for(int i1 = 0; i1 <= 4; ++i1) {
                if((l < 1 || i1 < 1 || l > 3 || i1 > 3) && isTeleportFriendlyBlock(entity, world, i, j, k, l, i1)) {
                	entity.setLocationAndAngles((double)((float)(i + l) + 0.5F), (double)k, (double)((float)(j + i1) + 0.5F), entity.rotationYaw, entity.rotationPitch);
                    pathfinder.clearPath();
                    return;
                }
            }
        }
    }
	
	public static boolean isTeleportFriendlyBlock(Entity entity, World world, int xBase, int zBase, int y, int xAdd, int zAdd) {
		BlockPos blockpos = new BlockPos(xBase + xAdd, y - 1, zBase + zAdd);
		IBlockState iblockstate = world.getBlockState(blockpos);
		return iblockstate.getBlockFaceShape(world, blockpos, EnumFacing.DOWN) == BlockFaceShape.SOLID && iblockstate.canEntitySpawn(entity) && world.isAirBlock(blockpos.up()) && world.isAirBlock(blockpos.up(2));
	}
    
    public static ItemStack feedDog(EntityDog dog, IInventory inventory, int slotIndex) {
        if(!inventory.getStackInSlot(slotIndex).isEmpty()) {
            ItemStack itemstack = inventory.getStackInSlot(slotIndex);
            dog.setDogHunger(dog.getDogHunger() + dog.foodValue(itemstack));
            
            if(itemstack.getItem() == ModItems.CHEW_STICK) { //TODO add player paramater
            	((ItemChewStick)ModItems.CHEW_STICK).addChewStickEffects(null, dog);
            }

            if(inventory.getStackInSlot(slotIndex).getCount() <= 1) {
                ItemStack itemstack1 = inventory.getStackInSlot(slotIndex);
                inventory.setInventorySlotContents(slotIndex, ItemStack.EMPTY);
                return itemstack1;
            }

            ItemStack itemstack2 = inventory.getStackInSlot(slotIndex).splitStack(1);

            if(inventory.getStackInSlot(slotIndex).isEmpty())
            	inventory.setInventorySlotContents(slotIndex, ItemStack.EMPTY);
            else
            	inventory.markDirty();

            return itemstack2;
        }
        else
            return ItemStack.EMPTY;
    }
    
    public static boolean doesInventoryContainFood(EntityDog dog, IInventory inventory) {
        for(int i = 0; i < inventory.getSizeInventory(); i++) {
            if(dog.foodValue(inventory.getStackInSlot(i)) > 0)
            	return true;
        }

        return false;
    }
    
    public static int getFirstSlotWithFood(EntityDog dog, IInventory inventory) {
    	 for(int i = 0; i < inventory.getSizeInventory(); i++) {
             if(dog.foodValue(inventory.getStackInSlot(i)) > 0)
             	return i;
         }

        return -1;
    }
    
    public static ItemStack addItem(IInventory inventory, ItemStack stack) {
    	if(stack.isEmpty()) return ItemStack.EMPTY;
    	
        ItemStack itemstack = stack.copy();

        for(int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack itemstack1 = inventory.getStackInSlot(i);

            if(itemstack1.isEmpty()) {
            	inventory.setInventorySlotContents(i, itemstack);
            	inventory.markDirty();
                return ItemStack.EMPTY;
            }

            if(ItemStack.areItemsEqual(itemstack1, itemstack)) {
                int j = Math.min(inventory.getInventoryStackLimit(), itemstack1.getMaxStackSize());
                int k = Math.min(itemstack.getCount(), j - itemstack1.getCount());

                if(k > 0) {
                	itemstack1.grow(k);
                	itemstack.shrink(k);

                    if(itemstack.isEmpty()) {
                    	inventory.markDirty();
                        return ItemStack.EMPTY;
                    }
                }
            }
        }

        if(itemstack.getCount() != stack.getCount())
        	inventory.markDirty();

        return itemstack;
    }
}
