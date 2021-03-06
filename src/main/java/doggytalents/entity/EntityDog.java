package doggytalents.entity;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import doggytalents.DoggyTalentsMod;
import doggytalents.ModBlocks;
import doggytalents.ModItems;
import doggytalents.ModSerializers;
import doggytalents.ModTags;
import doggytalents.ModTalents;
import doggytalents.api.feature.EnumGender;
import doggytalents.api.feature.EnumMode;
import doggytalents.api.feature.ICoordFeature;
import doggytalents.api.feature.IDog;
import doggytalents.api.feature.IGenderFeature;
import doggytalents.api.feature.IHungerFeature;
import doggytalents.api.feature.ILevelFeature;
import doggytalents.api.feature.IModeFeature;
import doggytalents.api.feature.IStatsFeature;
import doggytalents.api.feature.ITalentFeature;
import doggytalents.api.inferface.IDogEntity;
import doggytalents.api.inferface.IDogFoodItem;
import doggytalents.api.inferface.IDogItem;
import doggytalents.api.inferface.IThrowableItem;
import doggytalents.api.inferface.Talent;
import doggytalents.client.gui.GuiDogInfo;
import doggytalents.entity.ai.DogLocationManager;
import doggytalents.entity.ai.EntityAIBegDog;
import doggytalents.entity.ai.EntityAIBerserkerMode;
import doggytalents.entity.ai.EntityAIDogFeed;
import doggytalents.entity.ai.EntityAIDogWander;
import doggytalents.entity.ai.EntityAIExtinguishFire;
import doggytalents.entity.ai.EntityAIFetch;
import doggytalents.entity.ai.EntityAIFetchReturn;
import doggytalents.entity.ai.EntityAIFollowOwnerDog;
import doggytalents.entity.ai.EntityAIHurtByTargetDog;
import doggytalents.entity.ai.EntityAIIncapacitatedTargetDog;
import doggytalents.entity.ai.EntityAIOwnerHurtByTargetDog;
import doggytalents.entity.ai.EntityAIOwnerHurtTargetDog;
import doggytalents.entity.ai.EntityAIShepherdDog;
import doggytalents.entity.features.CoordFeature;
import doggytalents.entity.features.DogFeature;
import doggytalents.entity.features.GenderFeature;
import doggytalents.entity.features.HungerFeature;
import doggytalents.entity.features.LevelFeature;
import doggytalents.entity.features.ModeFeature;
import doggytalents.entity.features.StatsFeature;
import doggytalents.entity.features.TalentFeature;
import doggytalents.helper.CapabilityHelper;
import doggytalents.helper.DogUtil;
import doggytalents.helper.TalentHelper;
import doggytalents.item.ItemFancyCollar;
import doggytalents.lib.ConfigValues;
import net.minecraft.block.BlockState;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.BreedGoal;
import net.minecraft.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.SitGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.GhastEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class EntityDog extends IDogEntity implements IDog {

    private static final DataParameter<Float>                    DATA_HEALTH_ID  = EntityDataManager.createKey(EntityDog.class, DataSerializers.FLOAT);
    private static final DataParameter<Byte>                     DOG_TEXTURE     = EntityDataManager.createKey(EntityDog.class, DataSerializers.BYTE);
    private static final DataParameter<Integer>                  COLLAR_COLOUR   = EntityDataManager.createKey(EntityDog.class, DataSerializers.VARINT);
    private static final DataParameter<Byte>                     LEVEL           = EntityDataManager.createKey(EntityDog.class, DataSerializers.BYTE);
    private static final DataParameter<Byte>                     LEVEL_DIRE      = EntityDataManager.createKey(EntityDog.class, DataSerializers.BYTE);
    private static final DataParameter<Byte>                     DOG_FLAGS       = EntityDataManager.createKey(EntityDog.class, DataSerializers.BYTE);
    private static final DataParameter<Map<Talent, Integer>>     TALENTS_PARAM   = EntityDataManager.createKey(EntityDog.class, ModSerializers.TALENT_LEVEL_SERIALIZER);
    private static final DataParameter<Integer>                  HUNGER_INT      = EntityDataManager.createKey(EntityDog.class, DataSerializers.VARINT);
    private static final DataParameter<ItemStack>                BONE_VARIANT    = EntityDataManager.createKey(EntityDog.class, DataSerializers.ITEMSTACK);
    private static final DataParameter<Integer>                  CAPE            = EntityDataManager.createKey(EntityDog.class, DataSerializers.VARINT);
    private static final DataParameter<Optional<BlockPos>>       BOWL_POS        = EntityDataManager.createKey(EntityDog.class, DataSerializers.OPTIONAL_BLOCK_POS);
    private static final DataParameter<Optional<BlockPos>>       BED_POS         = EntityDataManager.createKey(EntityDog.class, DataSerializers.OPTIONAL_BLOCK_POS);
    private static final DataParameter<Byte>                     SIZE            = EntityDataManager.createKey(EntityDog.class, DataSerializers.BYTE);
    private static final DataParameter<Byte>                     GENDER_PARAM    = EntityDataManager.createKey(EntityDog.class, DataSerializers.BYTE);
    private static final DataParameter<Byte>                     MODE_PARAM      = EntityDataManager.createKey(EntityDog.class, DataSerializers.BYTE);
    private static final DataParameter<Optional<ITextComponent>> LAST_KNOWN_NAME = EntityDataManager.createKey(EntityDog.class, DataSerializers.OPTIONAL_TEXT_COMPONENT);

    @Nullable
    public DogLocationManager locationManager;

    public TalentFeature TALENTS;
    public LevelFeature LEVELS;
    public ModeFeature MODE;
    public CoordFeature COORDS;
    public GenderFeature GENDER;
    public StatsFeature STATS;
    public HungerFeature HUNGER;
    private List<DogFeature> FEATURES;

    public Map<String, Object> objects;

    private float headRotationCourse;
    private float headRotationCourseOld;
    public boolean isWet;
    public boolean gotWetInWater;
    public boolean isShaking;
    private float timeWolfIsShaking;
    private float prevTimeWolfIsShaking;

    //Timers
    private float timeWolfIsHappy;
    private float prevTimeWolfIsHappy;
    private boolean isWolfHappy;
    public boolean hiyaMaster;
    private int hungerTick;
    private int prevHungerTick;
    private int healingTick;
    private int prevHealingTick;
    private int regenerationTick;
    private int prevRegenerationTick;
    private int reversionTime;

    public EntityDog(EntityType<EntityDog> type, World worldIn) {
        super(type, worldIn);
        this.TALENTS = new TalentFeature(this);
        this.LEVELS = new LevelFeature(this);
        this.MODE = new ModeFeature(this);
        this.COORDS = new CoordFeature(this);
        this.GENDER = new GenderFeature(this);
        this.HUNGER = new HungerFeature(this);
        this.STATS = new StatsFeature(this);

        this.FEATURES = Arrays.asList(TALENTS, LEVELS, MODE, COORDS, GENDER, HUNGER, STATS);
        if(worldIn instanceof ServerWorld)
            this.locationManager = DogLocationManager.getHandler((ServerWorld)this.getEntityWorld());
        this.objects = new HashMap<String, Object>();
        this.setTamed(false);
        this.setGender(this.getRNG().nextBoolean() ? EnumGender.MALE : EnumGender.FEMALE);

        TalentHelper.onClassCreation(this);
    }

    @Override
    protected void registerGoals() {
        this.sitGoal = new SitGoal(this);
        this.goalSelector.addGoal(1, new SwimGoal(this));
        this.goalSelector.addGoal(1, new EntityAIExtinguishFire(this, 1.15D, 16));
        this.goalSelector.addGoal(2, this.sitGoal);
        this.goalSelector.addGoal(3, new EntityAIFetchReturn(this, 1.0D));
        this.goalSelector.addGoal(4, new EntityAIDogWander(this, 1.0D));
         //TODO this.tasks.addGoal(4, new EntityAIPatrolArea(this));
        this.goalSelector.addGoal(5, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(6, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(7, new EntityAIShepherdDog(this, 1.0D, 8F, entity -> !(entity instanceof EntityDog)));
        this.goalSelector.addGoal(8, new EntityAIFetch(this, 1.0D, 32));
        this.goalSelector.addGoal(10, new EntityAIFollowOwnerDog(this, 1.0D, 10.0F, 2.0F));
        //this.goalSelector.addGoal(11, new EntityAISitOnBed(this, 0.8D));
        this.goalSelector.addGoal(12, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(13, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
        this.goalSelector.addGoal(14, new EntityAIBegDog(this, 8.0F));
        this.goalSelector.addGoal(15, new EntityAIDogFeed(this, 1.0D, 20.0F));

        this.goalSelector.addGoal(25, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.addGoal(25, new LookRandomlyGoal(this));

        this.targetSelector.addGoal(0, new EntityAIIncapacitatedTargetDog(this));
        this.targetSelector.addGoal(1, new EntityAIOwnerHurtByTargetDog(this));
        this.targetSelector.addGoal(2, new EntityAIOwnerHurtTargetDog(this));
        this.targetSelector.addGoal(3, new EntityAIHurtByTargetDog(this).setCallsForHelp());
        this.targetSelector.addGoal(4, new EntityAIBerserkerMode<>(this, MonsterEntity.class, false));
    }

    @Override
    protected void registerData() {
        super.registerData();
        this.dataManager.register(DATA_HEALTH_ID, this.getHealth());
        this.dataManager.register(DOG_FLAGS, (byte)0);
        this.dataManager.register(DOG_TEXTURE, (byte)0);
        this.dataManager.register(COLLAR_COLOUR, -2);
        this.dataManager.register(TALENTS_PARAM, Collections.emptyMap());
        this.dataManager.register(HUNGER_INT, 60);
        this.dataManager.register(BONE_VARIANT, ItemStack.EMPTY);
        this.dataManager.register(MODE_PARAM, (byte)EnumMode.DOCILE.getIndex());
        this.dataManager.register(LEVEL, (byte)0);
        this.dataManager.register(LEVEL_DIRE, (byte)0);
        this.dataManager.register(BOWL_POS, Optional.empty());
        this.dataManager.register(BED_POS, Optional.empty());
        this.dataManager.register(CAPE, -2);
        this.dataManager.register(SIZE, (byte)3);
        this.dataManager.register(GENDER_PARAM, (byte)EnumGender.UNISEX.getIndex());
        this.dataManager.register(LAST_KNOWN_NAME, Optional.empty());
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        super.notifyDataManagerChange(key);
        if(SIZE.equals(key)) {
            this.recalculateSize();
        }
    }

    @Override
    public float getRenderScale() {
        if(this.isChild()) {
            return 0.5F;
        } else {
            return this.getDogSize() * 0.3F + 0.1F;
        }
    }

    @Override
    protected void updateAITasks() {
        super.updateAITasks();
        this.dataManager.set(DATA_HEALTH_ID, this.getHealth());
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3F);
        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(this.isTamed() ? 20.0D : 8.0D);
        this.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if(this.getDogHunger() <= ConfigValues.LOW_HUNGER && ConfigValues.DOG_WHINE_WHEN_HUNGER_LOW) {
            return SoundEvents.ENTITY_WOLF_WHINE;
        } else if(this.rand.nextInt(3) == 0) {
            return this.isTamed() && this.dataManager.get(DATA_HEALTH_ID) < this.getMaxHealth() / 2 ? SoundEvents.ENTITY_WOLF_WHINE : SoundEvents.ENTITY_WOLF_PANT;
        } else {
            return SoundEvents.ENTITY_WOLF_AMBIENT;
        }
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockIn) {
        this.playSound(SoundEvents.ENTITY_WOLF_STEP, 0.15F, 1.0F);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.ENTITY_WOLF_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_WOLF_DEATH;
    }

    @Override
    public float getSoundVolume() {
        return 0.4F;
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        this.FEATURES.forEach(f -> f.writeAdditional(compound));

        compound.putInt("doggyTex", this.getTameSkin());
        compound.putInt("collarColour", this.getCollarData());
        compound.putInt("dogHunger", this.getDogHunger());
        compound.putBoolean("willObey", this.willObeyOthers());
        compound.putBoolean("friendlyFire", this.canPlayersAttack());
        compound.putBoolean("radioCollar", this.hasRadarCollar());
        compound.putBoolean("sunglasses", this.hasSunglasses());
        compound.putInt("capeData", this.getCapeData());
        compound.putInt("dogSize", this.getDogSize());
        compound.putBoolean("hasBone", this.hasBone());
        if(this.hasBone()) {
            compound.put("fetchItem", this.getBoneVariant().write(new CompoundNBT()));
        }

        if(this.dataManager.get(LAST_KNOWN_NAME).isPresent()) compound.putString("lastKnownOwnerName", ITextComponent.Serializer.toJson(this.dataManager.get(LAST_KNOWN_NAME).get()));

        TalentHelper.writeAdditional(this, compound);
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        this.FEATURES.forEach(f -> f.readAdditional(compound));

        this.setTameSkin(compound.getInt("doggyTex"));
        if (compound.contains("collarColour", 99)) this.setCollarData(compound.getInt("collarColour"));
        this.setDogHunger(compound.getInt("dogHunger"));
        this.setWillObeyOthers(compound.getBoolean("willObey"));
        this.setCanPlayersAttack(compound.getBoolean("friendlyFire"));
        this.hasRadarCollar(compound.getBoolean("radioCollar"));
        this.setHasSunglasses(compound.getBoolean("sunglasses"));
        if(compound.contains("capeData", 99)) this.setCapeData(compound.getInt("capeData"));
        if(compound.contains("dogSize", 99)) this.setDogSize(compound.getInt("dogSize"));
        if(compound.contains("fetchItem", Constants.NBT.TAG_COMPOUND)) this.setBoneVariant(ItemStack.read(compound.getCompound("fetchItem")));

        if(compound.contains("lastKnownOwnerName", 8)) this.dataManager.set(LAST_KNOWN_NAME, Optional.of(ITextComponent.Serializer.fromJson(compound.getString("lastKnownOwnerName"))));

        TalentHelper.readAdditional(this, compound);

        //Backwards Compatibility
        if (compound.contains("dogName"))
            this.setCustomName(new StringTextComponent(compound.getString("dogName")));

        if(compound.getBoolean("hasBone")) {
            int variant = compound.getInt("boneVariant");
            if(variant == 0) {
                this.setBoneVariant(new ItemStack(ModItems.THROW_BONE));
            } else if(variant == 1) {
                this.setBoneVariant(new ItemStack(ModItems.THROW_STICK));
            }
        }
    }

    @Override
    public void livingTick() {
        super.livingTick();

        if(!this.world.isRemote && this.isWet && !this.isShaking && !this.hasPath() && this.onGround) {
            this.isShaking = true;
            this.timeWolfIsShaking = 0.0F;
            this.prevTimeWolfIsShaking = 0.0F;
            this.world.setEntityState(this, (byte)8);
        }

        if(!ConfigValues.DISABLE_HUNGER) {
            this.prevHungerTick = this.hungerTick;

            if (!this.isBeingRidden() && !this.isSitting() /** && !this.mode.isMode(EnumMode.WANDERING) && !this.level.isDireDog() || worldObj.getWorldInfo().getWorldTime() % 2L == 0L **/)
                this.hungerTick += 1;

            this.hungerTick += TalentHelper.hungerTick(this, this.hungerTick - this.prevHungerTick);

            if (this.hungerTick > 400) {
                this.setDogHunger(this.getDogHunger() - 1);
                this.hungerTick -= 400;
            }
        }

        if (ConfigValues.DOGS_IMMORTAL) {
            this.prevRegenerationTick = this.regenerationTick;

            if (this.isSitting()) {
                this.regenerationTick += 1;
                this.regenerationTick += TalentHelper.regenerationTick(this, this.regenerationTick - this.prevRegenerationTick);
            } else if (!this.isSitting())
                this.regenerationTick = 0;

            if (this.regenerationTick >= 2400 && this.isIncapacicated()) {
                this.setHealth(2);
                this.setDogHunger(1);
            } else if (this.regenerationTick >= 2400 && !this.isIncapacicated()) {
                if (this.regenerationTick >= 4400 && this.getDogHunger() < 60) {
                    this.setDogHunger(this.getDogHunger() + 1);
                    this.world.setEntityState(this, (byte) 7);
                    this.regenerationTick = 2400;
                }
            }
        }

        if (this.getHealth() != ConfigValues.LOW_HEATH_LEVEL) {
            this.prevHealingTick = this.healingTick;
            this.healingTick += this.nourishment();

            if (this.healingTick >= 6000) {
                if (this.getHealth() < this.getMaxHealth())
                    this.setHealth(this.getHealth() + 1);

                this.healingTick = 0;
            }
        }

        if(this.getHealth() <= 0 && this.isImmortal()) {
            this.deathTime = 0;
            this.setHealth(1);
        }

        if(this.world.isRemote && this.LEVELS.isDireDog() && ConfigValues.DIRE_PARTICLES) {
            for (int i = 0; i < 2; i++) {
                double width = this.getSize(this.getPose()).width;
                double height = this.getSize(this.getPose()).height;
                this.world.addParticle(ParticleTypes.PORTAL, this.getPosX() + (this.rand.nextDouble() - 0.5D) * width, (this.getPosY() + rand.nextDouble() * height) - 0.25D, this.getPosZ() + (rand.nextDouble() - 0.5D) * width, (this.rand.nextDouble() - 0.5D) * 2D, -this.rand.nextDouble(), (this.rand.nextDouble() - 0.5D) * 2D);
            }
        }

        if (this.reversionTime > 0)
            this.reversionTime -= 1;

        //Remove dog from players head if sneaking
        Entity entityRidden = this.getRidingEntity();

        if(entityRidden instanceof PlayerEntity)
            if(entityRidden.isShiftKeyDown())
                this.stopRiding();

        //Check if dog bowl still exists every 50t/2.5s, if not remove
        if(this.ticksExisted % 50 == 0) {
            if(this.COORDS.hasBowlPos() && this.world.isBlockLoaded(this.COORDS.getBowlPos()) && this.world.getBlockState(this.COORDS.getBowlPos()).getBlock() != ModBlocks.FOOD_BOWL) {
                this.COORDS.setBowlPos(null);
            }
        }

        TalentHelper.livingTick(this);
    }

    @Override
    public void tick() {
        super.tick();

        this.headRotationCourseOld = this.headRotationCourse;
        if (this.isBegging()) {
            this.headRotationCourse += (1.0F - this.headRotationCourse) * 0.4F;
        }
        else {
            this.headRotationCourse += (0.0F - this.headRotationCourse) * 0.4F;
        }

        if(this.isInWaterRainOrBubbleColumn()) {
            this.isWet = true;
            this.isShaking = false;
            this.timeWolfIsShaking = 0.0F;
            this.prevTimeWolfIsShaking = 0.0F;
            this.gotWetInWater = this.isInWater();
        } else if((this.isWet || this.isShaking) && this.isShaking) {
            if(this.timeWolfIsShaking == 0.0F) {
                this.playSound(SoundEvents.ENTITY_WOLF_SHAKE, this.getSoundVolume(), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
            }

            this.prevTimeWolfIsShaking = this.timeWolfIsShaking;
            this.timeWolfIsShaking += 0.05F;
            if(this.prevTimeWolfIsShaking >= 2.0F) {
                this.isWet = false;
                this.isShaking = false;
                this.prevTimeWolfIsShaking = 0.0F;
                this.timeWolfIsShaking = 0.0F;

                TalentHelper.onFinishShaking(this, this.gotWetInWater);
            }

            if(this.timeWolfIsShaking > 0.4F) {
                float f = (float)this.getBoundingBox().minY;
                int i = (int)(MathHelper.sin((this.timeWolfIsShaking - 0.4F) * (float)Math.PI) * 7.0F);
                Vec3d vec3d = this.getMotion();

                for(int j = 0; j < i; ++j) {
                    float f1 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.getWidth() * 0.5F;
                    float f2 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.getWidth() * 0.5F;
                    this.world.addParticle(ParticleTypes.SPLASH, this.getPosX() + f1, f + 0.8F, this.getPosZ() + f2, vec3d.x, vec3d.y, vec3d.z);
                }
            }
        }

        if(this.rand.nextInt(200) == 0)
            this.hiyaMaster = true;

        if(((this.isBegging()) || (this.hiyaMaster)) && (!this.isWolfHappy)) {
            this.isWolfHappy = true;
            this.timeWolfIsHappy = 0.0F;
            this.prevTimeWolfIsHappy = 0.0F;
        } else
            this.hiyaMaster = false;

        if(this.isWolfHappy) {
            if(this.timeWolfIsHappy % 1.0F == 0.0F)
                this.playSound(SoundEvents.ENTITY_WOLF_PANT, this.getSoundVolume(), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
            this.prevTimeWolfIsHappy = this.timeWolfIsHappy;
            this.timeWolfIsHappy += 0.05F;
            if (this.prevTimeWolfIsHappy >= 8.0F) {
                this.isWolfHappy = false;
                this.prevTimeWolfIsHappy = 0.0F;
                this.timeWolfIsHappy = 0.0F;
            }
        }

        if(this.isTamed()) {
            PlayerEntity player = (PlayerEntity) this.getOwner();

            if(player != null) {
                float distanceToOwner = player.getDistance(this);

                if(distanceToOwner <= 2F && this.hasBone()) {
                    if(!this.world.isRemote) {
                        IThrowableItem throwableItem = this.getThrowableItem();
                        ItemStack fetchItem = throwableItem != null ? throwableItem.getReturnStack(this.getBoneVariant()) : this.getBoneVariant();

                        this.entityDropItem(fetchItem, 0.0F);
                        this.setBoneVariant(ItemStack.EMPTY);
                    }
                }
            }
        }

        if(this.ticksExisted % 40 == 0) {
            if(!this.world.isRemote) {
                if(this.isAlive()) { //Prevent the data from being added when the entity dies
                    this.locationManager.update(this);
                } else {
                    this.locationManager.remove(this);
                }

                if(this.getOwner() != null) {
                    this.dataManager.set(LAST_KNOWN_NAME, Optional.ofNullable(this.getOwner().getDisplayName()));
                }
            }
        }

        TalentHelper.tick(this);
        this.FEATURES.forEach(DogFeature::tick);
    }

    @Override
    public boolean processInteract(PlayerEntity player, Hand hand) {

        ActionResultType result = TalentHelper.interactWithPlayer(this, player, hand);
        switch(result) {
            case SUCCESS: return true;
            case FAIL: return false;
            case PASS: break;
        }

        ItemStack stack = player.getHeldItem(hand);

        if(stack.getItem() == ModItems.OWNER_CHANGE && player.abilities.isCreativeMode && !this.isOwner(player)) {
            if(!this.world.isRemote) {
                this.setTamedBy(player);
                this.navigator.clearPath();
                this.setAttackTarget((LivingEntity) null);
                this.sitGoal.setSitting(true);
                this.world.setEntityState(this, (byte) 7);
            }
            return true;
        }

        if(this.isTamed()) {
            if(!stack.isEmpty()) {
                int foodValue = this.foodValue(stack, player);

                if(foodValue != 0 && this.getDogHunger() < this.HUNGER.getMaxHunger() && this.canInteract(player) && !this.isIncapacicated()) {
                    if(this.isIncapacicated()) {
                        if(!this.world.isRemote)
                            player.sendMessage(new TranslationTextComponent("dog.mode.incapacitated.help", this.getDisplayName(), this.GENDER.getGenderPronoun()));
                    } else {
                        this.consumeItemFromStack(player, stack);

                        if(!this.world.isRemote) {
                            this.setDogHunger(this.getDogHunger() + foodValue);

                            if (stack.getItem() instanceof IDogFoodItem) {
                                IDogFoodItem dogFood = (IDogFoodItem)stack.getItem();
                                dogFood.onItemConsumed(this, stack, player);
                            }
                        }
                        this.world.setEntityState(this, (byte)7);
                    }
                    return true;
                } else if(stack.getItem() == ModItems.DOGGY_CHARM && player.abilities.isCreativeMode) {
                    if(!this.world.isRemote) {
                        EntityDog babySpawn = this.createChild(this);
                        if(babySpawn != null) {
                           babySpawn.setGrowingAge(-ConfigValues.TIME_TO_MATURE);
                           babySpawn.setTamed(true);
                           if(ConfigValues.PUPS_GET_PARENT_LEVELS) {
                               babySpawn.LEVELS.setLevel(Math.min(this.LEVELS.getLevel(), 20));
                           }

                           babySpawn.setLocationAndAngles(this.getPosX(), this.getPosY(), this.getPosZ(), 0.0F, 0.0F);
                           this.world.addEntity(babySpawn);

                           this.consumeItemFromStack(player, stack);
                        }
                     }

                    return true;
                } else if(stack.getItem() == Items.STICK && this.canInteract(player)) {

                    if(this.isIncapacicated()) {
                        if(!this.world.isRemote)
                            player.sendMessage(new TranslationTextComponent("dog.mode.incapacitated.help", this.getDisplayName(), this.GENDER.getGenderPronoun()));
                    } else {
                        if(this.world.isRemote) {
                            DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> GuiDogInfo.open(this));
                        }
                    }

                    return true;
                } else if(stack.getItem() == ModItems.RADIO_COLLAR && this.canInteract(player) && !this.hasRadarCollar() && !this.isIncapacicated()) {
                    this.hasRadarCollar(true);

                    this.consumeItemFromStack(player, stack);
                    return true;
                } else if(stack.getItem() == ModItems.WOOL_COLLAR && this.canInteract(player) && !this.hasCollar() && !this.isIncapacicated()) {
                    int colour = -1;

                    if(stack.hasTag() && stack.getTag().contains("collar_colour", 99))
                        colour = stack.getTag().getInt("collar_colour");

                    this.setCollarData(colour);

                    this.consumeItemFromStack(player, stack);
                    return true;
                } else if(stack.getItem() instanceof ItemFancyCollar && this.canInteract(player) && !this.hasCollar() && !this.isIncapacicated()) {
                    this.setCollarData(-3 - ((ItemFancyCollar)stack.getItem()).type.ordinal());

                    this.consumeItemFromStack(player, stack);
                    return true;
                } else if(stack.getItem() == ModItems.CAPE && this.canInteract(player) && !this.hasCape() && !this.isIncapacicated()) {
                    this.setFancyCape();
                    if(!player.abilities.isCreativeMode)
                        stack.shrink(1);
                    return true;
                } else if(stack.getItem() == ModItems.LEATHER_JACKET && this.canInteract(player) && !this.hasCape() && !this.isIncapacicated()) {
                    this.setLeatherJacket();
                    if(!player.abilities.isCreativeMode)
                        stack.shrink(1);
                    return true;
                } else if(stack.getItem() == ModItems.CAPE_COLOURED && this.canInteract(player) && !this.hasCape() && !this.isIncapacicated()) {
                    int colour = -1;

                    if(stack.hasTag() && stack.getTag().contains("cape_colour", 99))
                        colour = stack.getTag().getInt("cape_colour");

                    this.setCapeData(colour);

                    this.consumeItemFromStack(player, stack);
                    return true;
                } else if(stack.getItem() == ModItems.SUNGLASSES && this.canInteract(player) && !this.hasSunglasses() && !this.isIncapacicated()) {
                    this.setHasSunglasses(true);
                    this.consumeItemFromStack(player, stack);
                    return true;
                } else if(stack.getItem() instanceof IDogItem && this.canInteract(player) && !this.isIncapacicated()) {
                    IDogItem treat = (IDogItem) stack.getItem();
                    ActionResultType treatResult = treat.onInteractWithDog(this, this.world, player, hand);

                    switch(treatResult) {
                        case SUCCESS: return true;
                        case FAIL: return false;
                        case PASS: break;
                    }

                } else if(stack.getItem() == ModItems.COLLAR_SHEARS && this.canInteract(player)) {
                    if(!this.world.isRemote) {
                        if(this.hasCollar() || this.hasSunglasses() || this.hasCape()) {
                            this.reversionTime = 40;
                            if(this.hasCollarColoured()) {
                                ItemStack collarDrop = new ItemStack(ModItems.WOOL_COLLAR, 1);
                                if(this.isCollarColoured()) {
                                    collarDrop.setTag(new CompoundNBT());
                                    collarDrop.getTag().putInt("collar_colour", this.getCollarData());
                                }
                                this.entityDropItem(collarDrop, 1);
                                this.setNoCollar();
                            }

                            if(this.hasFancyCollar()) {
                                Item drop = ModItems.MULTICOLOURED_COLLAR;
                                if(this.getCollarData() == -3)
                                    drop = ModItems.CREATIVE_COLLAR;
                                else if(this.getCollarData() == -4)
                                    drop = ModItems.SPOTTED_COLLAR;

                                this.entityDropItem(drop, 1);
                                this.setNoCollar();
                            }

                            if(this.hasFancyCape()) {
                                this.entityDropItem(new ItemStack(ModItems.CAPE, 1), 1);
                                this.setNoCape();
                            }

                            if(this.hasCapeColoured()) {
                                ItemStack capeDrop = new ItemStack(ModItems.CAPE_COLOURED, 1);
                                if (this.isCapeColoured()) {
                                    capeDrop.setTag(new CompoundNBT());
                                    capeDrop.getTag().putInt("cape_colour", this.getCapeData());
                                }
                                this.entityDropItem(capeDrop, 1);
                                this.setNoCape();
                            }

                            if(this.hasLeatherJacket()) {
                                this.entityDropItem(new ItemStack(ModItems.LEATHER_JACKET, 1), 1);
                                this.setNoCape();
                            }

                            if(this.hasSunglasses()) {
                                this.entityDropItem(new ItemStack(ModItems.SUNGLASSES, 1), 1);
                                this.setHasSunglasses(false);
                            }
                        } else if(this.reversionTime < 1) {
                            this.setTamed(false);
                            this.navigator.clearPath();
                            this.sitGoal.setSitting(false);
                            this.setHealth(8);
                            this.TALENTS.resetTalents();
                            this.setOwnerId(null);
                            this.dataManager.set(LAST_KNOWN_NAME, Optional.empty());
                            this.setWillObeyOthers(false);
                            this.MODE.setMode(EnumMode.DOCILE);
                            if(this.hasRadarCollar())
                                this.entityDropItem(ModItems.RADIO_COLLAR);
                            this.hasRadarCollar(false);
                            this.reversionTime = 40;
                        }
                    }

                    return true;
                } else if(stack.getItem() == ConfigValues.REVIVE_ITEM && this.canInteract(player) && this.isIncapacicated()) {
                    this.consumeItemFromStack(player, stack);

                    if(!this.world.isRemote) {
                        this.sitGoal.setSitting(true);
                        this.setHealth(this.getMaxHealth());
                        this.setDogHunger(ConfigValues.HUNGER_POINTS);
                        this.regenerationTick = 0;
                        this.setAttackTarget((LivingEntity) null);
                        this.world.setEntityState(this, (byte) 7);
                    }

                    return true;
                } else if(stack.getItem().isIn(net.minecraftforge.common.Tags.Items.DYES) && this.canInteract(player) && this.hasCollarColoured()) { //TODO Add Plants compatibility

                    if(!this.world.isRemote) {
                        int[] aint = new int[3];
                        int maxCompSum = 0;
                        int count = 1; //The number of different sources of colour

                        DyeColor colour = DyeColor.getColor(stack);
                        if(colour == null) {
                            return false;
                        }

                        float[] afloat = colour.getColorComponentValues();
                        int l1 = (int)(afloat[0] * 255.0F);
                        int i2 = (int)(afloat[1] * 255.0F);
                        int j2 = (int)(afloat[2] * 255.0F);
                        maxCompSum += Math.max(l1, Math.max(i2, j2));
                        aint[0] += l1;
                        aint[1] += i2;
                        aint[2] += j2;

                        if(this.isCollarColoured()) {
                            int l = this.getCollarData();
                            float f = (l >> 16 & 255) / 255.0F;
                            float f1 = (l >> 8 & 255) / 255.0F;
                            float f2 = (l & 255) / 255.0F;
                            maxCompSum = (int)(maxCompSum + Math.max(f, Math.max(f1, f2)) * 255.0F);
                            aint[0] = (int) (aint[0] + f * 255.0F);
                            aint[1] = (int) (aint[1] + f1 * 255.0F);
                            aint[2] = (int) (aint[2] + f2 * 255.0F);
                            count++;
                        }


                        int i1 = aint[0] / count;
                        int j1 = aint[1] / count;
                        int k1 = aint[2] / count;
                        float f3 = (float) maxCompSum / (float) count;
                        float f4 = Math.max(i1, Math.max(j1, k1));
                        i1 = (int)(i1 * f3 / f4);
                        j1 = (int)(j1 * f3 / f4);
                        k1 = (int)(k1 * f3 / f4);
                        int k2 = (i1 << 8) + j1;
                        k2 = (k2 << 8) + k1;
                        this.setCollarData(k2);
                    }

                    return true;
                } else if(stack.getItem() == ModItems.TREAT_BAG && this.getDogHunger() < ConfigValues.HUNGER_POINTS && this.canInteract(player) && !this.isIncapacicated()) {
                    IItemHandler treatBag = CapabilityHelper.getOrThrow(stack, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
                    DogUtil.feedDogFrom(this, treatBag);
                    return true;
                }
            }

            if(!this.isBreedingItem(stack) && this.canInteract(player)) {
                if(!this.world.isRemote) {
                    this.sitGoal.setSitting(!this.isSitting());
                    this.isJumping = false;
                    this.navigator.clearPath();
                    this.setAttackTarget((LivingEntity) null);
                }
                return true;
            }
        } else if(stack.getItem() == ModItems.COLLAR_SHEARS && this.reversionTime < 1) {
            if(!this.world.isRemote) {
                this.locationManager.remove(this);
                this.remove();
                WolfEntity wolf = EntityType.WOLF.create(this.world);
                wolf.setLocationAndAngles(this.getPosX(), this.getPosY(), this.getPosZ(), this.rotationYaw, this.rotationPitch);
                wolf.setHealth(this.getHealth());
                wolf.setGrowingAge(this.getGrowingAge());
                this.world.addEntity(wolf);
            }
            return true;
        } else if(stack.getItem() == Items.BONE || stack.getItem() == ModItems.TRAINING_TREAT) {
            this.consumeItemFromStack(player, stack);

            if(!this.world.isRemote) {
                if(stack.getItem() == ModItems.TRAINING_TREAT || this.rand.nextInt(3) == 0) {
                    this.setTamedBy(player);
                    this.navigator.clearPath();
                    this.setAttackTarget((LivingEntity) null);
                    this.sitGoal.setSitting(true);
                    this.setHealth(20.0F);
                    this.world.setEntityState(this, (byte) 7);
                } else {
                    this.world.setEntityState(this, (byte) 6);
                }
            }

            return true;
        }

        return super.processInteract(player, hand);
    }

    @Override
    public EntityDog createChild(AgeableEntity entityAgeable) {
        EntityDog entitydog = (EntityDog)this.getType().create(this.world);
        UUID uuid = this.getOwnerId();

        if(uuid != null) {
            entitydog.setOwnerId(uuid);
            entitydog.setTamed(true);
        }

        entitydog.setGrowingAge(-ConfigValues.TIME_TO_MATURE);

        if(ConfigValues.PUPS_GET_PARENT_LEVELS && entityAgeable instanceof EntityDog) {
            int combinedLevel = this.LEVELS.getLevel() + ((EntityDog)entityAgeable).LEVELS.getLevel();
            combinedLevel /= 2;
            combinedLevel = Math.min(combinedLevel, 20);
            entitydog.LEVELS.setLevel(combinedLevel);
        }

        return entitydog;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.getItem().isIn(ModTags.BREEDING_ITEMS);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean getAlwaysRenderNameTagForRender() {
        return this.hasCustomName();
    }

    // Changes visibility to public
    @Override
    public void playTameEffect(boolean successful) {
        super.playTameEffect(successful);
    }

    // Talent Hooks
    @Override
    public boolean onLivingFall(float distance, float damageMultiplier) {
        if(!TalentHelper.isImmuneToFalls(this))
            return super.onLivingFall(distance - TalentHelper.fallProtection(this), damageMultiplier);
        return false;
    }

    @Override
    public boolean attackEntityFrom(DamageSource damageSource, float damage) {
        if (this.isInvulnerableTo(damageSource))
            return false;
        else {
            Entity entity = damageSource.getTrueSource();
            //Friendly fire
            if (!this.canPlayersAttack() && entity instanceof PlayerEntity)
                return false;

            if (!TalentHelper.attackEntityFrom(this, damageSource, damage))
                return false;

            if (this.sitGoal != null)
                this.sitGoal.setSitting(false);

            if (entity != null && !(entity instanceof PlayerEntity) && !(entity instanceof ArrowEntity))
                damage = (damage + 1.0F) / 2.0F;

            return super.attackEntityFrom(damageSource, damage);
        }
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        if (!TalentHelper.shouldDamageMob(this, entityIn))
            return false;

        int damage = 4 + (MathHelper.floor(this.effectiveLevel()) + 1) / 2;
        damage = TalentHelper.attackEntityAsMob(this, entityIn, damage);

        if (entityIn instanceof ZombieEntity)
            ((ZombieEntity)entityIn).setAttackTarget(this);

        //TODO  (float)((int)this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getValue()
        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), damage);//(float)((int)this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getValue()));
        if(flag) {
            this.STATS.increaseDamageDealt(damage);
            this.applyEnchantments(this, entityIn);
        }

        return flag;
    }

    @Override
    public void setTamed(boolean tamed) {
        super.setTamed(tamed);
        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(tamed ? 20.0D : 8.0D);
        this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleStatusUpdate(byte id) {
        if(id == 8) {
            this.isShaking = true;
            this.timeWolfIsShaking = 0.0F;
            this.prevTimeWolfIsShaking = 0.0F;
        } else {
            super.handleStatusUpdate(id);
        }
    }

    @Override
    public void onDeath(DamageSource cause) {
        if(!this.isImmortal()) {
            this.isWet = false;
            this.isShaking = false;
            this.prevTimeWolfIsShaking = 0.0F;
            this.timeWolfIsShaking = 0.0F;

            if(!this.world.isRemote) {
                this.locationManager.remove(this);

                if(this.world.getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES) && this.getOwner() instanceof ServerPlayerEntity) {
                    this.getOwner().sendMessage(this.getCombatTracker().getDeathMessage());
                }
            }
        }
    }

    @Override
    public boolean isPotionApplicable(EffectInstance potioneffectIn) {
        if(this.isIncapacicated())
            return false;

        if(!TalentHelper.isPostionApplicable(this, potioneffectIn))
            return false;

        return super.isPotionApplicable(potioneffectIn);
    }

    @Override
    public void setFire(int amount) {
        if(TalentHelper.setFire(this, amount))
            super.setFire(amount);
    }

    @Override
    public boolean isSleeping() {
        return false;
    }

    @Override
    protected int decreaseAirSupply(int air) {
        return TalentHelper.shouldDecreaseAir(this, air) ? super.decreaseAirSupply(air) : air;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return TalentHelper.canBreatheUnderwater(this);
    }

    @Override
    public boolean canTrample(BlockState state, BlockPos pos, float fallDistance) { //TODO replacement for 1.14 canTriggerWalking
        return TalentHelper.canTriggerWalking(this);
    }

    @Override
    public boolean shouldAttackEntity(LivingEntity target, LivingEntity owner) {
        if(TalentHelper.canAttackEntity(this, target))
            return true;

        if (!(target instanceof CreeperEntity) && !(target instanceof GhastEntity)) {
            if (target instanceof EntityDog) {
                EntityDog entitydog = (EntityDog) target;

                return !entitydog.isTamed() || entitydog.getOwner() != owner;
            } else if (target instanceof WolfEntity) {
                WolfEntity entitywolf = (WolfEntity) target;

                return !entitywolf.isTamed() || entitywolf.getOwner() != owner;
            }

            if (target instanceof PlayerEntity && owner instanceof PlayerEntity && !((PlayerEntity)owner).canAttackPlayer((PlayerEntity)target)) {
                return false;
            } else if (target instanceof AbstractHorseEntity && ((AbstractHorseEntity)target).isTame()) {
                return false;
            } else {
                return !(target instanceof CatEntity) || !((CatEntity)target).isTamed();
            }
        }

        return false;
    }

    @Override
    public boolean canAttack(EntityType<?> cls) {
        if(TalentHelper.canAttack(this, cls))
            return true;

        return super.canAttack(cls);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return target instanceof PlayerEntity && this.canInteract(target) ? false : super.canAttack(target);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        LazyOptional<T> capOut = TalentHelper.getCapability(this, cap, side);
        if (capOut != null) {
            return capOut.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public Entity changeDimension(DimensionType dimType) {
        Entity entity = super.changeDimension(dimType);
        if(entity instanceof EntityDog) {
            EntityDog dog = (EntityDog)entity;

            if(!this.world.isRemote) {
                dog.locationManager.update(dog);
                this.locationManager.remove(this);
            }
        } else if(entity != null) {
            DoggyTalentsMod.LOGGER.warn("Dog tried to change dimension but now isn't a dog?");
        }

        return entity;
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if(!this.world.isRemote)
            this.locationManager.update(this);
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        if(!this.world.isRemote && !this.isAlive())
            this.locationManager.remove(this);
    }

    @Override
    public void remove(boolean keepData) {
        super.remove(keepData);
        if(!this.world.isRemote)
            this.locationManager.remove(this);

        if (!keepData) {
            TalentHelper.invalidateCapabilities(this);
        }
    }

    @Override
    protected float getJumpUpwardsMotion() {
        return 0.42F;
    }

    @Override
    public boolean canDespawn(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected float getWaterSlowDown() {
        return 0.8F;
    }

    @Override
    public boolean canMateWith(AnimalEntity otherAnimal) {
        if(otherAnimal == this) {
            return false;
        } else if(!this.isTamed()) {
            return false;
        } else if(!(otherAnimal instanceof EntityDog)) {
            return false;
        } else {
            EntityDog entitydog = (EntityDog)otherAnimal;
            if(!entitydog.isTamed()) {
                return false;
            } else if(entitydog.isSitting()) {
                return false;
            } else if(!this.GENDER.canMateWith(entitydog)) {
                return false;
            } else {
                return this.isInLove() && entitydog.isInLove();
            }
        }
    }

    @Override
    public ItemStack getPickedResult(RayTraceResult target) {
        return new ItemStack(ModItems.DOGGY_CHARM);
    }

    @Override
    public void onKillEntity(LivingEntity entityLivingIn) {
        super.onKillEntity(entityLivingIn);
        this.STATS.incrementKillCount(entityLivingIn);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isDogWet() {
        return this.isWet;
    }

    @OnlyIn(Dist.CLIENT)
    public float getShadingWhileWet(float partialTick) {
        return 0.75F + (this.prevTimeWolfIsShaking + (this.timeWolfIsShaking - this.prevTimeWolfIsShaking) * partialTick) / 2.0F * 0.25F;
    }

    @OnlyIn(Dist.CLIENT)
    public float getShakeAngle(float partialTick, float offset) {
        float f = (this.prevTimeWolfIsShaking + (this.timeWolfIsShaking - this.prevTimeWolfIsShaking) * partialTick + offset) / 1.8F;
        if(f < 0.0F) {
            f = 0.0F;
        } else if (f > 1.0F) {
            f = 1.0F;
        }

        return MathHelper.sin(f * (float)Math.PI) * MathHelper.sin(f * (float)Math.PI * 11.0F) * 0.15F * (float)Math.PI;
    }

    @OnlyIn(Dist.CLIENT)
    public float getInterestedAngle(float partialTick) {
        return (this.headRotationCourseOld + (this.headRotationCourse - this.headRotationCourseOld) * partialTick) * 0.15F * (float)Math.PI;
    }

    public float getWagAngle(float partialTick, float offset) {
        float f = (this.prevTimeWolfIsHappy + (this.timeWolfIsHappy - this.prevTimeWolfIsHappy) * partialTick + offset) / 2.0F;
        if (f < 0.0F) f = 0.0F;
        else if (f > 2.0F) f %= 2.0F;
        return MathHelper.sin(f * (float) Math.PI * 11.0F) * 0.3F * (float) Math.PI;
    }

    @OnlyIn(Dist.CLIENT)
    public float getTailRotation() {
        return this.isTamed() ? (0.55F - (this.getMaxHealth() - this.dataManager.get(DATA_HEALTH_ID)) / this.getMaxHealth() * 20.0F * 0.02F) * (float)Math.PI : ((float)Math.PI / 5F);
    }

    //TODO
    /**
    @Override
    public float getEyeHeight() {
         return this.height * 0.8F;
    }**/

    @Override
    public int getVerticalFaceSpeed() {
        return this.isSitting() ? 20 : super.getVerticalFaceSpeed();
    }

    public boolean isImmortal() {
        return this.isTamed() && ConfigValues.DOGS_IMMORTAL || this.LEVELS.isDireDog();
    }

    public boolean isIncapacicated() {
        return this.isImmortal() && this.getHealth() <= ConfigValues.LOW_HEATH_LEVEL;
    }

    public double effectiveLevel() {
        return (this.LEVELS.getLevel() + this.LEVELS.getDireLevel()) / 10.0D;
    }

    public double getHealthRelative() {
        return getHealth() / (double) getMaxHealth();
    }

    public boolean canWander() {
        return this.isTamed() && this.MODE.isMode(EnumMode.WANDERING) && this.COORDS.hasBowlPos() && this.COORDS.getBowlPos().distanceSq(this.getPosition()) < 400.0D;
    }

    @Override
    public boolean canInteract(LivingEntity player) {
        return this.isOwner(player) || this.willObeyOthers();
    }

    public int foodValue(ItemStack stack, @Nullable Entity entityIn) {
        if (stack.isEmpty())
            return 0;

        int foodValue = 0;

        Item item = stack.getItem();

        if (stack.getItem() != Items.ROTTEN_FLESH && item.isFood()) {
            if (item.getFood().isMeat())
                foodValue = 40;
        } else if (stack.getItem() instanceof IDogFoodItem) {
            IDogFoodItem dogFood = (IDogFoodItem)stack.getItem();
            int temp = dogFood.getFoodValue(this, stack, entityIn);
            if (temp > 0)
                foodValue = temp;
        }

        foodValue = TalentHelper.changeFoodValue(this, stack, foodValue);

        return foodValue;
    }

    public int nourishment() {
        int amount = 0;

        if (this.getDogHunger() > 0) {
            amount = 40 + 4 * (MathHelper.floor(this.effectiveLevel()) + 1);

            if (isSitting() && this.TALENTS.getLevel(ModTalents.QUICK_HEALER) == 5) {
                amount += 20 + 2 * (MathHelper.floor(this.effectiveLevel()) + 1);
            }

            if (!this.isSitting()) {
                amount *= 5 + this.TALENTS.getLevel(ModTalents.QUICK_HEALER);
                amount /= 10;
            }
        }

        return amount;
    }

    public int points() {
        return this.isCreativeCollar() ? 1000 : this.LEVELS.getLevel() + this.LEVELS.getDireLevel() + (this.LEVELS.isDireDog() ? 15 : 0) + (this.getGrowingAge() < 0 ? 0 : 15);
    }

    public int spendablePoints() {
        return this.points() - this.usedPoints();
    }

    public int usedPoints() {
        return TalentHelper.getUsedPoints(this);
    }

    public int deductive(int id) {
        if(id >= 1 && id <= 5)
            return new int[] {1,3,6,10,15}[id - 1];

        return 0;
    }

    public ITextComponent getOwnersName() {
        LivingEntity owner = this.getOwner();

        // If the player is logged in fetch there name directly
        if (owner != null) {
            return owner.getDisplayName();
        }

        return this.dataManager.get(LAST_KNOWN_NAME).orElseGet(this::getNameUnknown);
    }

    private ITextComponent getNameUnknown() {
        return new TranslationTextComponent(this.getOwnerId() != null ? "entity.doggytalents.dog.unknown_owner" : "entity.doggytalents.dog.untamed");
    }

    private boolean getDogFlag(int bit) {
        return (this.dataManager.get(DOG_FLAGS) & bit) != 0;
    }

    private void setDogFlag(int bit, boolean flag) {
        byte b0 = this.dataManager.get(DOG_FLAGS);
        if(flag) {
            this.dataManager.set(DOG_FLAGS, (byte)(b0 | bit));
        } else {
            this.dataManager.set(DOG_FLAGS, (byte)(b0 & ~bit));
        }
    }

    public void setBegging(boolean begging) {
        this.setDogFlag(1, begging);
    }

    public boolean isBegging() {
        return this.getDogFlag(1);
    }

    public void setWillObeyOthers(boolean obeyOthers) {
        this.setDogFlag(2, obeyOthers);
    }

    public boolean willObeyOthers() {
        return this.getDogFlag(2);
    }

    public void setCanPlayersAttack(boolean flag) {
        this.setDogFlag(4, flag);
    }

    public boolean canPlayersAttack() {
        return this.getDogFlag(4);
    }

    public void hasRadarCollar(boolean collar) {
        this.setDogFlag(8, collar);
    }

    public boolean hasRadarCollar() {
        return this.getDogFlag(8);
    }

    public void setHasSunglasses(boolean sunglasses) {
        this.setDogFlag(16, sunglasses);
    }

    public boolean hasSunglasses() {
        return this.getDogFlag(16);
    }

    public void setLyingDown(boolean lying) {
        this.setDogFlag(32, lying);
    }

    public boolean isLyingDown() {
        return this.getDogFlag(32);
    }

    public void set64Flag(boolean lying) {
        this.setDogFlag(64, lying);
    }

    public boolean get64Flag() {
        return this.getDogFlag(64);
    }

    public int getTameSkin() {
        return this.dataManager.get(DOG_TEXTURE);
    }

    public void setTameSkin(int index) {
        this.dataManager.set(DOG_TEXTURE, (byte)index);
    }

    public int getDogHunger() {
        return this.dataManager.get(HUNGER_INT);
    }

    public void setDogHunger(int par1) {
        this.dataManager.set(HUNGER_INT, Math.min(this.HUNGER.getMaxHunger(), Math.max(0, par1)));
    }

    public void setBoneVariant(ItemStack stack) {
        this.dataManager.set(BONE_VARIANT, stack);
    }

    public ItemStack getBoneVariant() {
        return this.dataManager.get(BONE_VARIANT);
    }

    @Nullable
    public IThrowableItem getThrowableItem() {
        Item item = this.dataManager.get(BONE_VARIANT).getItem();
        if(item instanceof IThrowableItem) {
            return (IThrowableItem)item;
        } else {
            return null;
        }
    }

    public boolean hasBone() {
        return !this.getBoneVariant().isEmpty();
    }

    public int getCollarData() {
        return this.dataManager.get(COLLAR_COLOUR);
    }

    public void setCollarData(int value) {
        this.dataManager.set(COLLAR_COLOUR, value);
    }

    public int getCapeData() {
        return this.dataManager.get(CAPE);
    }

    public void setCapeData(int value) {
        this.dataManager.set(CAPE, value);
    }

    @Override
    public void setDogSize(int value) {
        this.dataManager.set(SIZE, (byte)Math.min(5, Math.max(1, value)));
    }

    @Override
    public int getDogSize() {
        return this.dataManager.get(SIZE);
    }

    public void setGender(EnumGender gender) {
        this.dataManager.set(GENDER_PARAM, (byte)gender.getIndex());
    }

    public EnumGender getGender() {
        return EnumGender.byIndex(this.dataManager.get(GENDER_PARAM));
    }

    public void setLevel(int level) {
        this.dataManager.set(LEVEL,  (byte)level);
    }

    public int getLevel() {
        return this.dataManager.get(LEVEL);
    }

    public void setDireLevel(int level) {
        this.dataManager.set(LEVEL_DIRE,  (byte)level);
    }

    public int getDireLevel() {
        return this.dataManager.get(LEVEL_DIRE);
    }

    public void setMode(EnumMode mode) {
        EnumMode prevMode = this.getMode();
        this.dataManager.set(MODE_PARAM, (byte)mode.getIndex());
        mode.onModeSet(this, prevMode);
    }

    public EnumMode getMode() {
        return EnumMode.byIndex(this.dataManager.get(MODE_PARAM));
    }

    public void setTalentMap(Map<Talent, Integer> data) {
        this.dataManager.set(TALENTS_PARAM, data);
    }

    public Map<Talent, Integer> getTalentMap() {
        return this.dataManager.get(TALENTS_PARAM);
    }

    public boolean hasBedPos() {
        return this.dataManager.get(BED_POS).isPresent();
    }

    public boolean hasBowlPos() {
        return this.dataManager.get(BOWL_POS).isPresent();
    }

    public BlockPos getBedPos() {
        return this.dataManager.get(BED_POS).orElse(this.world.getSpawnPoint());
    }

    public BlockPos getBowlPos() {
        return this.dataManager.get(BOWL_POS).orElse(this.getPosition());
    }

    public void resetBedPosition() {
        this.dataManager.set(BED_POS, Optional.empty());
    }

    public void resetBowlPosition() {
        this.dataManager.set(BOWL_POS, Optional.empty());
    }

    public void setBedPos(BlockPos pos) {
        this.dataManager.set(BED_POS, Optional.ofNullable(pos));
    }

    public void setBowlPos(BlockPos pos) {
        this.dataManager.set(BOWL_POS, Optional.ofNullable(pos));
    }


    public void setNoCollar() {
        this.setCollarData(-2);
    }

    public boolean hasCollar() {
        return this.getCollarData() != -2;
    }

    public boolean hasCollarColoured() {
        return this.getCollarData() >= -1;
    }

    public boolean isCollarColoured() {
        return this.getCollarData() > -1;
    }

    public void setHasCollar() {
        this.setCollarData(-1);
    }

    public boolean hasFancyCollar() {
        return this.getCollarData() < -2;
    }

    public int getFancyCollarIndex() {
        return -3 - this.getCollarData();
    }

    public boolean isCreativeCollar() {
        return this.getCollarData() == -3;
    }

    public float[] getCollar() {
        return DogUtil.rgbIntToFloatArray(this.getCollarData());
    }

    public boolean hasCape() {
        return this.getCapeData() != -2;
    }

    public boolean hasCapeColoured() {
        return this.getCapeData() >= -1;
    }

    public boolean hasFancyCape() {
        return this.getCapeData() == -3;
    }

    public boolean hasLeatherJacket() {
        return this.getCapeData() == -4;
    }

    public boolean isCapeColoured() {
        return this.getCapeData() > -1;
    }

    public void setFancyCape() {
        this.setCapeData(-3);
    }

    public void setLeatherJacket() {
        this.setCapeData(-4);
    }

    public void setCapeColoured() {
        this.setCapeData(-1);
    }

    public void setNoCape() {
        this.setCapeData(-2);
    }

    public float[] getCapeColour() {
        return DogUtil.rgbIntToFloatArray(this.getCapeData());
    }

    protected boolean dogJumping;
    protected float jumpPower;

    public boolean isDogJumping() {
        return this.dogJumping;
    }

    public void setDogJumping(boolean jumping) {
        this.dogJumping = jumping;
    }

    public double getDogJumpStrength() {
        float verticalVelocity = 0.42F + 0.06F * this.TALENTS.getLevel(ModTalents.WOLF_MOUNT);
        if(this.TALENTS.getLevel(ModTalents.WOLF_MOUNT) == 5) verticalVelocity += 0.04F;
        return verticalVelocity;
    }

    @Override
    public Entity getControllingPassenger() {
        return this.getPassengers().isEmpty() ? null : (Entity) this.getPassengers().get(0);
    }

    @Override
    public boolean canBeSteered() {
        return this.getControllingPassenger() instanceof LivingEntity;
    }

    @Override
    public boolean canBePushed() {
        return !this.isBeingRidden();
    }

    @Override
    public void updatePassenger(Entity passenger) {
        super.updatePassenger(passenger);
        if(passenger instanceof LivingEntity) {
            LivingEntity entityliving = (LivingEntity)passenger;
            this.renderYawOffset = entityliving.renderYawOffset;
        }
    }

    @Override
    public double getYOffset() {
        return this.getRidingEntity() instanceof PlayerEntity ? 0.5D : 0.0D;
    }

    @Override
    public boolean canBeRiddenInWater(Entity rider) {
        switch (TalentHelper.canBeRiddenInWater(this, rider)) {
            case SUCCESS: return true;
            case FAIL: return false;
            default: return false;
        }
    }

    @Override
    public boolean canRiderInteract() {
        return true;
    }

    // 0 - 100 input
    public void setJumpPower(int jumpPowerIn) {
        if(this.TALENTS.getLevel(ModTalents.WOLF_MOUNT) > 0) {
            this.jumpPower = 1.0F;
        }
    }

    public boolean canJump() {
        return this.TALENTS.getLevel(ModTalents.WOLF_MOUNT) > 0;
    }

    @Override
    public void travel(Vec3d travelVec) {
        double prevX = this.getPosX();
        double prevY = this.getPosY();
        double prevZ = this.getPosZ();

        if(this.isAlive()) {
            if(this.isBeingRidden() && this.canBeSteered() && this.TALENTS.getLevel(ModTalents.WOLF_MOUNT) > 0) {
                LivingEntity livingentity = (LivingEntity)this.getControllingPassenger();
                this.rotationYaw = livingentity.rotationYaw;
                this.prevRotationYaw = this.rotationYaw;
                this.rotationPitch = livingentity.rotationPitch * 0.5F;
                this.setRotation(this.rotationYaw, this.rotationPitch);
                this.renderYawOffset = this.rotationYaw;
                this.rotationYawHead = this.renderYawOffset;
                float f = livingentity.moveStrafing * 0.7F;
                float f1 = livingentity.moveForward;
                if (f1 <= 0.0F) {
                   f1 *= 0.5F;
                }

                if (this.jumpPower > 0.0F && !this.isDogJumping() && this.onGround) {
                    double d0 = this.getDogJumpStrength() * this.jumpPower;
                    double d1;
                    if (this.isPotionActive(Effects.JUMP_BOOST)) {
                        d1 = d0 + (this.getActivePotionEffect(Effects.JUMP_BOOST).getAmplifier() + 1) * 0.1F;
                    } else {
                        d1 = d0;
                    }

                    Vec3d vec3d = this.getMotion();
                    this.setMotion(vec3d.x, d1, vec3d.z);
                    this.setDogJumping(true);
                    this.isAirBorne = true;
                    if (f1 > 0.0F) {
                        float f2 = MathHelper.sin(this.rotationYaw * ((float)Math.PI / 180F));
                        float f3 = MathHelper.cos(this.rotationYaw * ((float)Math.PI / 180F));
                        this.setMotion(this.getMotion().add(-0.4F * f2 * this.jumpPower, 0.0D, 0.4F * f3 * this.jumpPower));
                        //this.playJumpSound();
                    }

                    this.jumpPower = 0.0F;
                }

                this.jumpMovementFactor = this.getAIMoveSpeed() * 0.1F;
                if (this.canPassengerSteer()) {
                    this.setAIMoveSpeed((float)this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue() * 0.5F);
                    super.travel(new Vec3d(f, travelVec.y, f1));
                } else if (livingentity instanceof PlayerEntity) {
                    this.setMotion(Vec3d.ZERO);
                }

                if(this.onGround) {
                    this.jumpPower = 0.0F;
                    this.setDogJumping(false);
                }

                this.prevLimbSwingAmount = this.limbSwingAmount;
                double d2 = this.getPosX() - this.prevPosX;
                double d3 = this.getPosZ() - this.prevPosZ;
                float f4 = MathHelper.sqrt(d2 * d2 + d3 * d3) * 4.0F;
                if (f4 > 1.0F) {
                   f4 = 1.0F;
                }

                this.limbSwingAmount += (f4 - this.limbSwingAmount) * 0.4F;
                this.limbSwing += this.limbSwingAmount;
             } else {
                 this.jumpMovementFactor = 0.02F;
                 super.travel(travelVec);
             }

            this.addMovementStat(this.getPosX() - prevX, this.getPosY() - prevY, this.getPosZ() - prevZ);
        }
    }

    public void addMovementStat(double xD, double yD, double zD) {
        if(this.isBeingRidden()) {
            int j = Math.round(MathHelper.sqrt(xD * xD + zD * zD) * 100.0F);
            this.STATS.increaseDistanceRidden(j);
        }
        if(!this.isPassenger()) {
            if(this.areEyesInFluid(FluidTags.WATER, true)) {
                int j = Math.round(MathHelper.sqrt(xD * xD + yD * yD + zD * zD) * 100.0F);
                if (j > 0) {
                    this.STATS.increaseDistanceOnWater(j);
                }
            } else if(this.isInWater()) {
                int k = Math.round(MathHelper.sqrt(xD * xD + zD * zD) * 100.0F);
                if (k > 0) {
                    this.STATS.increaseDistanceInWater(k);
                }
            } else if(this.onGround) {
                int l = Math.round(MathHelper.sqrt(xD * xD + zD * zD) * 100.0F);
                if(l > 0) {
                    if (this.isSprinting()) {
                        this.STATS.increaseDistanceSprint(l);
                    } else if(this.isShiftKeyDown()) {
                        this.STATS.increaseDistanceSneaking(l);
                    } else {
                        this.STATS.increaseDistanceWalk(l);
                    }
                }
            } else { // Time in air
                int j1 = Math.round(MathHelper.sqrt(xD * xD + zD * zD) * 100.0F);
                //this.STATS.increaseDistanceInWater(k);
            }


        }
    }

    // IDog

    @Override
    public ICoordFeature getCoordFeature() {
        return this.COORDS;
    }

    @Override
    public IGenderFeature getGenderFeature() {
        return this.GENDER;
    }

    @Override
    public ILevelFeature getLevelFeature() {
        return this.LEVELS;
    }

    @Override
    public IModeFeature getModeFeature() {
        return this.MODE;
    }

    @Override
    public IStatsFeature getStatsFeature() {
        return this.STATS;
    }

    @Override
    public ITalentFeature getTalentFeature() {
        return this.TALENTS;
    }

    @Override
    public IHungerFeature getHungerFeature() {
        return this.HUNGER;
    }

    @Override
    public <T> void putObject(String key, T value) {
        this.objects.put(key, value);
    }

    @Override
    public <T> T getObject(String key, Class<T> type) {
        return (T) this.objects.get(key);
    }
}
