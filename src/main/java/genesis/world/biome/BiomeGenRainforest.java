package genesis.world.biome;

import java.util.Random;

import genesis.block.BlockMoss;
import genesis.block.BlockMoss.EnumSoil;
import genesis.combo.variant.EnumPlant;
import genesis.combo.variant.EnumTree;
import genesis.common.GenesisBlocks;
import genesis.util.random.f.FloatRange;
import genesis.world.biome.decorate.WorldGenDebris;
import genesis.world.biome.decorate.WorldGenGrowingPlant;
import genesis.world.biome.decorate.WorldGenMossStages;
import genesis.world.biome.decorate.WorldGenPlant;
import genesis.world.biome.decorate.WorldGenRockBoulders;
import genesis.world.biome.decorate.WorldGenRoots;
import genesis.world.biome.decorate.WorldGenSplash;
import genesis.world.gen.feature.WorldGenDeadLog;
import genesis.world.gen.feature.WorldGenTreeLepidodendron;
import genesis.world.gen.feature.WorldGenTreePsaronius;
import genesis.world.gen.feature.WorldGenTreeSigillaria;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.ChunkPrimer;

public class BiomeGenRainforest extends BiomeGenBaseGenesis
{
	public BiomeGenRainforest(BiomeGenBase.BiomeProperties properties)
	{
		super(properties);
		
		addDecorations();
		addTrees();
	}
	
	protected void addDecorations()
	{
		getDecorator().setGrassCount(9);
		addGrass(WorldGenPlant.create(EnumPlant.ZYGOPTERIS).setPatchCount(14), 1);
		
		addDecoration(new WorldGenSplash((s, w, p) -> s.getBlock() == GenesisBlocks.moss, GenesisBlocks.moss.getDefaultState().withProperty(BlockMoss.DIRT, EnumSoil.HUMUS)).setPatchRadius(6), 2.1F);
		addDecoration(new WorldGenGrowingPlant(GenesisBlocks.odontopteris).setNextToWater(false).setPatchCount(3), 2);
		addDecoration(new WorldGenGrowingPlant(GenesisBlocks.sphenophyllum).setPatchCount(4), 2);
		addDecoration(new WorldGenGrowingPlant(GenesisBlocks.calamites).setPatchCount(3), 3);
		addDecoration(new WorldGenRockBoulders().setRadius(FloatRange.create(0.75F, 1.5F), FloatRange.create(0.5F, 1)), 6);
		addDecoration(new WorldGenRockBoulders().setWaterRequired(false).setRadius(FloatRange.create(0.75F, 1.5F), FloatRange.create(0.5F, 1)), 0.333F);
		addDecoration(new WorldGenMossStages(), 30);
		addDecoration(new WorldGenDebris(), 28);
		addDecoration(new WorldGenRoots(), 13);
	}
	
	protected void addTrees()
	{
		getDecorator().setTreeCount(25.4F);
		
		addTree(new WorldGenTreeSigillaria(10, 15, true).generateVine(8), 17);
		addTree(new WorldGenTreePsaronius(5, 8, true).generateVine(8), 14);
		addTree(new WorldGenTreeLepidodendron(14, 20, true).generateVine(8), 20);
		
		addTree(new WorldGenDeadLog(5, 8, EnumTree.LEPIDODENDRON, true), 2);
		addTree(new WorldGenDeadLog(4, 7, EnumTree.SIGILLARIA, true), 1);
	}
	
	@Override
	public float getNightFogModifier()
	{
		return 0.65F;
	}
	
	@Override
	public void genTerrainBlocks(World world, Random rand, ChunkPrimer primer, int blockX, int blockZ, double d)
	{
		mossStages = new int[2];
		mossStages[0] = 1;
		mossStages[1] = 2;
		super.genTerrainBlocks(world, rand, primer, blockX, blockZ, d);
	}
}
