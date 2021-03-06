package genesis.entity.fixed;

import genesis.common.GenesisBlocks;
import genesis.common.GenesisItems;
import genesis.common.sounds.GenesisSoundEvents;
import genesis.entity.living.flying.EntityMeganeura;
import genesis.util.Constants;
import genesis.util.random.i.IntRange;
import genesis.util.render.EntityPart;
import genesis.util.render.RenderHelpers;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityMeganeuraEgg extends EntityEgg
{
	private static final IntRange AGE_RANGE = IntRange.create(1200, 1600);
	
	public EntityMeganeuraEgg(World world)
	{
		super(world);
	}
	
	public EntityMeganeuraEgg(World world, Vec3d position)
	{
		super(world, position);
	}
	
	@Override
	protected void setMaxAge()
	{
		maxAge = AGE_RANGE.get(rand);
	}
	
	@Override
	public void spawnBaby()
	{
		EntityMeganeura meganeura = new EntityMeganeura(worldObj);
		
		Vec3d center = new Vec3d(fixedTo.getX() + 0.5, posY, fixedTo.getZ() + 0.5);
		Vec3d normal = getPositionVector().subtract(center).normalize();
		float w = meganeura.width;	// Offset the meganeura out of the calamites.
		Vec3d spawnLoc = getPositionVector().addVector(normal.xCoord * w, normal.yCoord * w, normal.zCoord * w);
		
		float yaw = (float) (Math.toDegrees(Math.atan2(-normal.zCoord, -normal.xCoord)));
		
		meganeura.setPositionAndRotation(spawnLoc.xCoord, spawnLoc.yCoord, spawnLoc.zCoord, yaw, -90);
		meganeura.setState(EntityMeganeura.State.IDLE_SIDE);
		meganeura.setTargetLocation(center);
		
		worldObj.spawnEntityInWorld(meganeura);
	}
	
	@Override
	protected boolean isValid()
	{
		return worldObj.getBlockState(fixedTo).getBlock() == GenesisBlocks.CALAMITES;
	}
	
	@Override
	public ItemStack getDroppedItem()
	{
		return new ItemStack(GenesisItems.MEGANEURA_EGG);
	}
	
	@Override
	public SoundEvent getPlaceSound()
	{
		return GenesisSoundEvents.BLOCK_EGG_MEGANEURA_PLACE;
	}
	
	@Override
	public SoundEvent getBreakSound()
	{
		return GenesisSoundEvents.BLOCK_EGG_MEGANEURA_BREAK;
	}
	
	@SideOnly(Side.CLIENT)
	public static class EggRender extends Render<EntityMeganeuraEgg>
	{
		public static class Model extends ModelBase
		{
			public final EntityPart egg;
			
			public Model()
			{
				textureWidth = 8;
				textureHeight = 8;
				
				// ~~~~~~~~~~~~~~~~~~~~~~
				// ~~~~==== Body ====~~~~
				egg = new EntityPart(this);
				egg.addBox(-0.5F, 0, -0.5F, 1, 1, 1);
				
				egg.setDefaultState(true);
			}
			
			@Override
			public void render(Entity entity, float p1, float p2, float p3, float p4, float p5, float p6)
			{
				//EntityMeganeuraEgg meganeura = (EntityMeganeuraEgg) entity;
				
				egg.render(p6);
			}
			
			@Override
			public void setRotationAngles(float p1, float p2, float p3, float p4, float p5, float p6, Entity entity)
			{
				//EntityMeganeuraEgg meganeura = (EntityMeganeuraEgg) entity;
				
				egg.resetState(true);
			}
		}
		
		public static final ResourceLocation TEXTURE = new ResourceLocation(Constants.ASSETS_PREFIX + "textures/entity/meganeura/egg.png");
		protected Model model = new Model();
		
		public EggRender(RenderManager manager)
		{
			super(manager);
			
			shadowSize = 0;
		}
		
		@Override
		public void doRender(EntityMeganeuraEgg entity, double x, double y, double z, float yaw, float partialTick)
		{
			//Minecraft.getMinecraft().getTextureManager().loadTexture(texture, new net.minecraft.client.renderer.texture.SimpleTexture(texture));
			//model = new Model();
			
			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y, z);
			
			RenderHelpers.renderEntityBounds(entity, partialTick);
			
			bindEntityTexture(entity);
			model.render(entity, 0, 0, 0, 0, 0, 0.0625F);
			
			GlStateManager.popMatrix();
			
			super.doRender(entity, x, y, z, yaw, partialTick);
		}
		
		@Override
		protected ResourceLocation getEntityTexture(EntityMeganeuraEgg entity)
		{
			return TEXTURE;
		}
	}
}
