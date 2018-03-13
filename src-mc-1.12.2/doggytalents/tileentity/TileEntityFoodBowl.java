package doggytalents.tileentity;

import java.util.List;

import doggytalents.entity.EntityDog;
import doggytalents.helper.DogUtil;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;

/**
 * @author ProPercivalalb
 */
public class TileEntityFoodBowl extends TileEntity implements ITickable {
   
	public InventoryBasic inventory;

    public int timeoutCounter;
    
    
    public TileEntityFoodBowl() {
    	this.inventory = new InventoryBasic("container.foodBowl", false, 5);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        NBTTagList nbttaglist = tag.getTagList("Items", 10);

        for (int i = 0; i < nbttaglist.tagCount(); i++) {
            NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound1.getByte("Slot") & 0xff;

            if(j >= 0 && j < this.inventory.getSizeInventory()) {
            	this.inventory.setInventorySlotContents(j, new ItemStack(nbttagcompound1));
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < this.inventory.getSizeInventory(); i++) {
            if(!this.inventory.getStackInSlot(i).isEmpty()) {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte)i);
                this.inventory.getStackInSlot(i).writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }

        tag.setTag("Items", nbttaglist);
		return tag;
    }

    @Override
    public void update() {
    	
    	//Only run update code every 5 ticks (0.25s)
    	if(++this.timeoutCounter < 5) { return; }
    	
    	List<EntityDog> dogList = this.world.getEntitiesWithinAABB(EntityDog.class, new AxisAlignedBB(this.pos).grow(5, 5, 5));

    	for(EntityDog dog : dogList) {
    		dog.coords.setBowlPos(this.pos.getX(), this.pos.getY(), this.pos.getZ());
            	
    		int slotIndex = DogUtil.getFirstSlotWithFood(dog, this.inventory);
         	if(dog.getDogHunger() < 60 && slotIndex >= 0)
         		DogUtil.feedDog(dog, this.inventory, slotIndex);
        }
    	
    	this.timeoutCounter = 0;
    }
}
