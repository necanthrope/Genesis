package genesis.world.biome;

import java.util.Random;

import genesis.combo.PlantBlocks;
import genesis.combo.variant.EnumPlant;
import genesis.common.GenesisBlocks;
import genesis.world.biome.decorate.WorldGenCircleReplacement;
import genesis.world.biome.decorate.WorldGenGrowingPlant;
import genesis.world.biome.decorate.WorldGenPlant;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;

public class BiomeMarsh extends BiomeGenesis
{
	public BiomeMarsh(Biome.BiomeProperties properties)
	{
		super(properties);
		this.topBlock = GenesisBlocks.PROTOTAXITES_RHIZOMORPHS.getDefaultState();
		
		getDecorator().setGrassCount(14);
		addGrass(WorldGenPlant.create(PlantBlocks.PLANT, EnumPlant.ASTEROXYLON).setPatchCount(5), 1);
		
		getDecorator().setFlowerCount(13);
		addFlower(WorldGenPlant.create(GenesisBlocks.PLANTS, PlantBlocks.DOUBLE_PLANT, EnumPlant.ASTEROXYLON).setPatchCount(5), 7);
		addFlower(WorldGenPlant.create(EnumPlant.RHYNIA).setPatchCount(4), 6);
		addFlower(WorldGenPlant.create(EnumPlant.NOTHIA).setPatchCount(4), 5);
		addFlower(WorldGenPlant.create(EnumPlant.SCIADOPHYTON).setPatchCount(4), 3);
		addFlower(WorldGenPlant.create(EnumPlant.PSILOPHYTON).setPatchCount(4), 2);
		addFlower(WorldGenPlant.create(EnumPlant.BARAGWANATHIA).setPatchCount(4), 1);
		addFlower(WorldGenPlant.create(EnumPlant.COOKSONIA).setPatchCount(4), 1);
		
		addDecoration(new WorldGenGrowingPlant(GenesisBlocks.PROTOTAXITES).setPatchCount(3), 0.242F);
		addDecoration(WorldGenCircleReplacement.getPeatGen(), 6.75F);
	}
	
	@Override
	public float getFogDensity()
	{
		return 0.45F;
	}
	
	@Override
	public float getNightFogModifier()
	{
		return 0.65F;
	}
	
	@Override
	public void genTerrainBlocks(World world, Random rand, ChunkPrimer chunkPrimer, int chunkX, int chunkZ, double d)
	{
		double d1 = GRASS_COLOR_NOISE.getValue(chunkX * 0.25D, chunkZ * 0.25D);
		
		if (d1 > -0.2D)
		{
			int k = chunkX & 15;
			int l = chunkZ & 15;
			
			for (int i1 = 255; i1 >= 0; --i1)
			{
				IBlockState state = chunkPrimer.getBlockState(l, i1, k);
				
				if (state.getMaterial() != Material.AIR)
				{
					if (i1 == 62 && chunkPrimer.getBlockState(l, i1, k).getBlock() != Blocks.WATER)
					{
						chunkPrimer.setBlockState(l, i1, k, Blocks.WATER.getDefaultState());
					}
					
					break;
				}
			}
		}
		super.genTerrainBlocks(world, rand, chunkPrimer, chunkX, chunkZ, d);
	}
}
