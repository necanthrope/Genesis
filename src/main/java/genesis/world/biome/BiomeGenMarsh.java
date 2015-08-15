package genesis.world.biome;

import genesis.common.GenesisBlocks;
import genesis.metadata.EnumPlant;
import genesis.world.biome.decorate.WorldGenGrowingPlant;
import genesis.world.biome.decorate.WorldGenMossStages;
import genesis.world.biome.decorate.WorldGenPlant;
import genesis.world.biome.decorate.WorldGenPrototaxites;
import genesis.world.biome.decorate.WorldGenUnderWaterPatch;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

public class BiomeGenMarsh extends BiomeGenBaseGenesis
{
	public BiomeGenMarsh(int id)
	{
		super(id);
		setBiomeName("Marsh");
		this.temperature = 1.15F;
		setHeight(0.0F, -0.01F);
		
		theBiomeDecorator.grassPerChunk = 0;
		
		addDecoration(new WorldGenMossStages().setCountPerChunk(30));
		
		addDecoration(new WorldGenUnderWaterPatch(GenesisBlocks.peat.getDefaultState()).setCountPerChunk(4));
		
		addDecoration(new WorldGenPlant().setPlant(GenesisBlocks.plants.getBlockState(EnumPlant.COOKSONIA)).setPatchSize(6).setCountPerChunk(5));
		addDecoration(new WorldGenPlant().setPlant(GenesisBlocks.plants.getBlockState(EnumPlant.BARAGWANATHIA)).setPatchSize(6).setCountPerChunk(5));
		addDecoration(new WorldGenPlant().setPlant(GenesisBlocks.plants.getBlockState(EnumPlant.RHYNIA)).setPatchSize(6).setCountPerChunk(5));
		addDecoration(new WorldGenPlant().setPlant(GenesisBlocks.plants.getBlockState(EnumPlant.PSILOPHYTON)).setPatchSize(4).setCountPerChunk(2));
		addDecoration(new WorldGenPlant().setPlant(GenesisBlocks.plants.getBlockState(EnumPlant.SCIADOPHYTON)).setPatchSize(4).setCountPerChunk(2));
		addDecoration(new WorldGenPlant().setPlant(GenesisBlocks.plants.getBlockState(EnumPlant.NOTHIA)).setPatchSize(4).setCountPerChunk(2));
		
		addDecoration(new WorldGenPrototaxites().setCountPerChunk(1));
		
		//Asteroxylon
	}
	
	@Override
	public void genTerrainBlocks(World world, Random rand, ChunkPrimer p_180622_3_, int p_180622_4_, int p_180622_5_, double p_180622_6_)
	{
		double d1 = field_180281_af.func_151601_a((double)p_180622_4_ * 0.25D, (double)p_180622_5_ * 0.25D);
		
		if (d1 > -0.2D)
		{
			int k = p_180622_4_ & 15;
			int l = p_180622_5_ & 15;
			
			for (int i1 = 255; i1 >= 0; --i1)
			{
				if (p_180622_3_.getBlockState(l, i1, k).getBlock().getMaterial() != Material.air)
				{
					if (i1 == 62 && p_180622_3_.getBlockState(l, i1, k).getBlock() != Blocks.water)
					{
						p_180622_3_.setBlockState(l, i1, k, Blocks.water.getDefaultState());
					}
					
					break;
				}
			}
		}
		
		generateBiomeTerrain(world, rand, p_180622_3_, p_180622_4_, p_180622_5_, p_180622_6_);
    }
	
	@Override
	public void generateBiomeTerrain(World world, Random rand, ChunkPrimer primer, int blockX, int blockZ, double d)
	{
		mossStages = new int[1];
		mossStages[0] = 0;
		super.generateBiomeTerrain(world, rand, primer, blockX, blockZ, d);
	}
}
