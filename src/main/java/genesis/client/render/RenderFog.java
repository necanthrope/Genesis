package genesis.client.render;

import genesis.common.GenesisBlocks;
import genesis.common.GenesisDimensions;
import genesis.util.GenesisMath;
import genesis.world.WorldProviderGenesis;
import genesis.world.biome.IBiomeGenFog;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogColors;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.relauncher.Side;

public class RenderFog
{
	public static RenderFog INSTANCE = new RenderFog();
	
	public Vec3 color = new Vec3(0, 0, 0);
	public Vec3 prevColor = color;
	public float fogDensity = 0;
	public float prevFogDensity = 0;
	
	public Vec3 lastVanillaColor = new Vec3(0, 0, 0);
	
	private static float getDayNightFactor(long time, float partialTicks)
	{
		float timeF = (time + partialTicks) % WorldProviderGenesis.DAY_LENGTH / WorldProviderGenesis.DAY_LENGTH;
		float factor = 1;
		
		if (timeF > WorldProviderGenesis.NIGHT_END)
		{	// Sunrise
			factor = (timeF - WorldProviderGenesis.NIGHT_END) / WorldProviderGenesis.LATE_TWILIGHT_TIME;
		}
		else if (timeF > WorldProviderGenesis.NIGHT_START)
		{	// Night
			factor = 0;
		}
		else if (timeF > WorldProviderGenesis.TWILIGHT_START)
		{	// Sunset
			factor = 1 - (timeF - WorldProviderGenesis.TWILIGHT_START) / WorldProviderGenesis.EARLY_TWILIGHT_TIME;
		}
		
		return MathHelper.clamp_float(factor, 0, 1);
	}
	
	@SubscribeEvent
	public void onTick(ClientTickEvent event)
	{
		if (event.side != Side.CLIENT)
			return;
		
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.isGamePaused())
			return;
		
		Entity entity = mc.getRenderViewEntity();
		if (entity == null)
			return;
		
		World world = entity.worldObj;
		if (!GenesisDimensions.isGenesis(world))
			return;
		
		final float partialTicks = 1;
		
		float red = 0;
		float green = 0;
		float blue = 0;
		float density = 0;
		int samples = 0;
		
		Vec3 playerPos = ActiveRenderInfo.projectViewFromEntity(entity, partialTicks);
		int areaSize = 10;
		int supersamples = 8;
		float sampleStep = 1 / (float) supersamples;
		
		for (float x = -areaSize; x <= areaSize; x += sampleStep)
		{
			for (float z = -areaSize; z <= areaSize; z += sampleStep)
			{
				if (x * x + z * z < areaSize * areaSize)
				{
					BlockPos samplePos = new BlockPos(playerPos.addVector(x, 0, z));
					BiomeGenBase biome = world.getBiomeGenForCoords(samplePos);
					Vec3 color = null;
					
					if (biome instanceof IBiomeGenFog)
					{
						IBiomeGenFog fogBiome = (IBiomeGenFog) biome;
						long time = world.getWorldTime();
						
						float percent = getDayNightFactor(time, partialTicks);
						
						color = GenesisMath.lerp(fogBiome.getFogColor(), fogBiome.getFogColorNight(), 1 - percent);
						
						density += fogBiome.getFogDensity() * (1 - fogBiome.getNightFogModifier() * (1 - percent));
					}
					else
					{
						color = lastVanillaColor;
					}
					
					red += (float) color.xCoord;
					green += (float) color.yCoord;
					blue += (float) color.zCoord;
					
					samples++;
				}
			}
		}
		
		red /= samples;
		green /= samples;
		blue /= samples;
		density /= samples;
		
		/* Sunrise/set fog code, slightly different from vanilla, but it must be handled by vanilla when using WorldProviderGenesis.getFogColor.
		if (mc.gameSettings.renderDistanceChunks >= 4)
		{
			float angle = world.getCelestialAngle(partialTicks);
			Vec3 sunDirection = MathHelper.sin(angle * (float) Math.PI * 2) > 0 ? new Vec3(-1, 0, 0) : new Vec3(1, 0, 0);
			float sunsetAmount = (float) entity.getLook(partialTicks).dotProduct(sunDirection);
			final float negAmount = 0.5F;
			sunsetAmount += negAmount;
			sunsetAmount *= 1 / (1 + negAmount);
			sunsetAmount = Math.max(sunsetAmount, 0.1F);
			
			float[] sunriseColor = world.provider.calcSunriseSunsetColors(angle, partialTicks);
			
			if (sunriseColor != null)
			{
				sunsetAmount = sunsetAmount * sunriseColor[3];
				sunsetAmount = Math.max(sunsetAmount, 0);
				
				red = red * (1 - sunsetAmount) + sunriseColor[0] * sunsetAmount;
				green = green * (1 - sunsetAmount) + sunriseColor[1] * sunsetAmount;
				blue = blue * (1 - sunsetAmount) + sunriseColor[2] * sunsetAmount;
			}
		}*/
		
		prevColor = color;
		color = new Vec3(red, green, blue);
		prevFogDensity = fogDensity;
		fogDensity = Math.min(density * 0.75F, 0.75F);
	}
	
	@SubscribeEvent
	public void onGetFogColor(FogColors event)
	{
		lastVanillaColor = new Vec3(event.red, event.green, event.blue);
		
		Entity entity = event.entity;
		World world = entity.worldObj;
		
		float partialTicks = (float) event.renderPartialTicks;
		Block blockAtEyes = ActiveRenderInfo.getBlockAtEntityViewpoint(world, event.entity, partialTicks);
		
		if (blockAtEyes == GenesisBlocks.komatiitic_lava)
		{
			event.red = 0.575F;
			event.green = 0.3F;
			event.blue = 0.05F;
		}
		
		if (!GenesisDimensions.isGenesis(world))
			return;
		
		if (blockAtEyes.getMaterial() == Material.water)
		{
			BlockPos eyePos = new BlockPos(ActiveRenderInfo.projectViewFromEntity(entity, partialTicks));
			
			int waterColorMultiplier = BiomeColorHelper.getWaterColorAtPos(world, eyePos);
			event.red = (waterColorMultiplier & 0xFF0000) >> 16;
			event.green = (waterColorMultiplier & 0x00FF00) >> 8;
			event.blue = (waterColorMultiplier & 0x0000FF);
			
			event.red *= 0.160784314F;
			event.green *= 0.384313725F;
			event.blue *= 0.749019608F;
			
			event.red *= 0.0008F;
			event.green *= 0.0008F;
			event.blue *= 0.0008F;
		}
		
		// TODO: Make night vision at night in Genesis not be pure white.
	}
	
	@SubscribeEvent
	public void onRenderFog(EntityViewRenderEvent.RenderFogEvent event)
	{
		if (!GenesisDimensions.isGenesis(event.entity.worldObj))
			return;
		
		float density = GenesisMath.lerp(prevFogDensity, fogDensity, (float) event.renderPartialTicks);
		renderFog(event.fogMode,
				event.farPlaneDistance, density);
	}
	
	private static void renderFog(int fogMode, float farPlaneDistance, float farPlaneDistanceScale)
	{
		if (fogMode < 0)
		{
			GlStateManager.setFogStart(0);
			GlStateManager.setFogEnd(farPlaneDistance);
		}
		else
		{
			GlStateManager.setFogStart(farPlaneDistance * farPlaneDistanceScale);
			GlStateManager.setFogEnd(farPlaneDistance);
		}
	}
}
