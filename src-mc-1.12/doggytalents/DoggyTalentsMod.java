package doggytalents;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import doggytalents.addon.AddonManager;
import doggytalents.api.DoggyTalentsAPI;
import doggytalents.api.registry.DogBedRegistry;
import doggytalents.api.registry.TalentRegistry;
import doggytalents.creativetab.CreativeTabDoggyTalents;
import doggytalents.handler.ConfigurationHandler;
import doggytalents.handler.ConnectionHandler;
import doggytalents.handler.EntityInteractHandler;
import doggytalents.lib.Reference;
import doggytalents.network.PacketDispatcher;
import doggytalents.proxy.CommonProxy;
import doggytalents.talent.BedFinder;
import doggytalents.talent.BlackPelt;
import doggytalents.talent.CreeperSweeper;
import doggytalents.talent.DoggyDash;
import doggytalents.talent.FisherDog;
import doggytalents.talent.GuardDog;
import doggytalents.talent.HappyEater;
import doggytalents.talent.HellHound;
import doggytalents.talent.HunterDog;
import doggytalents.talent.PackPuppy;
import doggytalents.talent.PestFighter;
import doggytalents.talent.PillowPaw;
import doggytalents.talent.PoisonFang;
import doggytalents.talent.PuppyEyes;
import doggytalents.talent.QuickHealer;
import doggytalents.talent.RescueDog;
import doggytalents.talent.ShepherdDog;
import doggytalents.talent.SwimmerDog;
import doggytalents.talent.WolfMount;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

/**
 * @author ProPercivalalb
 */
@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION, updateJSON = Reference.UPDATE_URL, guiFactory = Reference.GUI_FACTORY)
public class DoggyTalentsMod {

	@Instance(value = Reference.MOD_ID)
	public static DoggyTalentsMod instance;
	
	@SidedProxy(clientSide = Reference.CLIENT_PROXY, serverSide = Reference.SERVER_PROXY)
    public static CommonProxy proxy;
	
	public static final Logger logger = LogManager.getLogger(Reference.MOD_NAME);
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		ConfigurationHandler.init(new Configuration(event.getSuggestedConfigurationFile()));
		DoggyTalentsAPI.CREATIVE_TAB = new CreativeTabDoggyTalents();
		ModEntities.inti();
		proxy.preInit();
		PacketDispatcher.registerPackets();
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);
		MinecraftForge.EVENT_BUS.register(new EntityInteractHandler());
		MinecraftForge.EVENT_BUS.register(new ConnectionHandler());
		proxy.init();
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		DoggyTalentsAPI.PACKPUPPY_BLACKLIST.registerItem(ModItems.THROW_BONE);
		DoggyTalentsAPI.BREED_WHITELIST.registerItem(ModItems.BREEDING_BONE);
		DoggyTalentsAPI.BEG_WHITELIST.registerItem(Items.BONE);
		DoggyTalentsAPI.BEG_WHITELIST.registerItem(ModItems.THROW_BONE);
		DoggyTalentsAPI.BEG_WHITELIST.registerItem(ModItems.TRAINING_TREAT);
		DoggyTalentsAPI.BEG_WHITELIST.registerItem(ModItems.MASTER_TREAT);
		DoggyTalentsAPI.BEG_WHITELIST.registerItem(ModItems.SUPER_TREAT);
		DoggyTalentsAPI.BEG_WHITELIST.registerItem(ModItems.DIRE_TREAT);
		
		DogBedRegistry.CASINGS.registerMaterial(Blocks.PLANKS, 0, "minecraft:blocks/planks_oak");
		DogBedRegistry.CASINGS.registerMaterial(Blocks.PLANKS, 1, "minecraft:blocks/planks_spruce");
		DogBedRegistry.CASINGS.registerMaterial(Blocks.PLANKS, 2, "minecraft:blocks/planks_birch");
		DogBedRegistry.CASINGS.registerMaterial(Blocks.PLANKS, 3, "minecraft:blocks/planks_jungle");
		DogBedRegistry.CASINGS.registerMaterial(Blocks.PLANKS, 4, "minecraft:blocks/planks_acacia");
		DogBedRegistry.CASINGS.registerMaterial(Blocks.PLANKS, 5, "minecraft:blocks/planks_big_oak");
		
		DogBedRegistry.BEDDINGS.registerMaterial(Blocks.WOOL, 0, "minecraft:blocks/wool_colored_white");
		DogBedRegistry.BEDDINGS.registerMaterial(Blocks.WOOL, 1, "minecraft:blocks/wool_colored_orange");
		DogBedRegistry.BEDDINGS.registerMaterial(Blocks.WOOL, 2, "minecraft:blocks/wool_colored_magenta");
		DogBedRegistry.BEDDINGS.registerMaterial(Blocks.WOOL, 3, "minecraft:blocks/wool_colored_light_blue");
		DogBedRegistry.BEDDINGS.registerMaterial(Blocks.WOOL, 4, "minecraft:blocks/wool_colored_yellow");
		DogBedRegistry.BEDDINGS.registerMaterial(Blocks.WOOL, 5, "minecraft:blocks/wool_colored_lime");
		DogBedRegistry.BEDDINGS.registerMaterial(Blocks.WOOL, 6, "minecraft:blocks/wool_colored_pink");
		DogBedRegistry.BEDDINGS.registerMaterial(Blocks.WOOL, 7, "minecraft:blocks/wool_colored_gray");
		DogBedRegistry.BEDDINGS.registerMaterial(Blocks.WOOL, 8, "minecraft:blocks/wool_colored_silver");
		DogBedRegistry.BEDDINGS.registerMaterial(Blocks.WOOL, 9, "minecraft:blocks/wool_colored_cyan");
		DogBedRegistry.BEDDINGS.registerMaterial(Blocks.WOOL, 10, "minecraft:blocks/wool_colored_purple");
		DogBedRegistry.BEDDINGS.registerMaterial(Blocks.WOOL, 11, "minecraft:blocks/wool_colored_blue");
		DogBedRegistry.BEDDINGS.registerMaterial(Blocks.WOOL, 12, "minecraft:blocks/wool_colored_brown");
		DogBedRegistry.BEDDINGS.registerMaterial(Blocks.WOOL, 13, "minecraft:blocks/wool_colored_green");
		DogBedRegistry.BEDDINGS.registerMaterial(Blocks.WOOL, 14, "minecraft:blocks/wool_colored_red");
		DogBedRegistry.BEDDINGS.registerMaterial(Blocks.WOOL, 15, "minecraft:blocks/wool_colored_black");
		
		TalentRegistry.registerTalent(new BedFinder());
		TalentRegistry.registerTalent(new BlackPelt());
		TalentRegistry.registerTalent(new CreeperSweeper());
		TalentRegistry.registerTalent(new DoggyDash());
		TalentRegistry.registerTalent(new FisherDog());
		TalentRegistry.registerTalent(new GuardDog());
		TalentRegistry.registerTalent(new HappyEater());
		TalentRegistry.registerTalent(new HellHound());
		TalentRegistry.registerTalent(new HunterDog());
		TalentRegistry.registerTalent(new PackPuppy());
		TalentRegistry.registerTalent(new PestFighter());
		TalentRegistry.registerTalent(new PillowPaw());
		TalentRegistry.registerTalent(new PoisonFang());
		TalentRegistry.registerTalent(new PuppyEyes());
		TalentRegistry.registerTalent(new QuickHealer());
		TalentRegistry.registerTalent(new RescueDog());
		TalentRegistry.registerTalent(new ShepherdDog());
		TalentRegistry.registerTalent(new SwimmerDog());
		TalentRegistry.registerTalent(new WolfMount());
		
		AddonManager.registerAddons();
		AddonManager.runRegisteredAddons(ConfigurationHandler.config);
		proxy.postInit();
	}
}
