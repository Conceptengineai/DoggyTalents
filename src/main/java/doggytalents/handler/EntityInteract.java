package doggytalents.handler;

import doggytalents.ModEntities;
import doggytalents.ModItems;
import doggytalents.entity.EntityDog;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * @author ProPercivalalb
 **/
public class EntityInteract {

    @SubscribeEvent
    public void rightClickEntity(PlayerInteractEvent.EntityInteract event) {
        PlayerEntity player = event.getPlayer();
        World world = event.getWorld();
        ItemStack stack = event.getItemStack();
        Entity target = event.getTarget();

         if(!world.isRemote) {

            if(target instanceof WolfEntity && !stack.isEmpty() && stack.getItem() == ModItems.TRAINING_TREAT) {
                WolfEntity wolf = (WolfEntity)target;

                if(wolf.isAlive() && wolf.isTamed() && wolf.isOwner(player)) {

                    if(!player.abilities.isCreativeMode)
                        stack.shrink(1);

                     EntityDog dog = ModEntities.DOG.create(world);
                     dog.setTamedBy(player);
                     dog.setHealth(dog.getMaxHealth());
                     dog.getAISit().setSitting(false);
                     dog.setGrowingAge(wolf.getGrowingAge());
                     dog.setPositionAndRotation(wolf.getPosX(), wolf.getPosY(), wolf.getPosZ(), wolf.rotationYaw, wolf.rotationPitch);

                     world.addEntity(dog);

                    wolf.remove();

                 }
             }
         }
    }
}
