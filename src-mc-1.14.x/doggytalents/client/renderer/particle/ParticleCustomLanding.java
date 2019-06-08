package doggytalents.client.renderer.particle;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ParticleCustomLanding extends ParticleCustomDigging {
   
	public ParticleCustomLanding(World worldIn, double xIn, double yIn, double zIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, BlockState state, BlockPos pos, TextureAtlasSprite sprite) {
        super(worldIn, xIn, yIn, zIn, xSpeedIn, ySpeedIn, zSpeedIn, state, pos, sprite);
        this.motionX = xSpeedIn;
        this.motionY = ySpeedIn;
        this.motionZ = zSpeedIn;
    }
}