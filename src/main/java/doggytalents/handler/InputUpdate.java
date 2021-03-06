package doggytalents.handler;

import doggytalents.entity.EntityDog;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.InputUpdateEvent;

public class InputUpdate {

    public static void on(final InputUpdateEvent event) {
        if(event.getMovementInput().jump) {
            Entity entity = event.getPlayer().getRidingEntity();
            if(event.getPlayer().isPassenger() && entity instanceof EntityDog) {
                EntityDog dog = (EntityDog)entity;

                if(dog.canJump()) {
                    dog.setJumpPower(100);
                }
            }
        }
    }
}
