package genesis.world.biome;

import java.util.Random;

import genesis.combo.variant.EnumPlant;
import genesis.combo.variant.EnumTree;
import genesis.common.GenesisBlocks;
import genesis.util.random.f.FloatRange;
import genesis.world.biome.decorate.WorldGenBoulders;
import genesis.world.biome.decorate.WorldGenDebris;
import genesis.world.biome.decorate.WorldGenGrowingPlant;
import genesis.world.biome.decorate.WorldGenMossStages;
import genesis.world.biome.decorate.WorldGenPlant;
import genesis.world.biome.decorate.WorldGenRoots;
import genesis.world.biome.decorate.WorldGenSplash;
import genesis.world.gen.feature.WorldGenDeadLog;
import genesis.world.gen.feature.WorldGenTreeLepidodendron;
import genesis.world.gen.feature.WorldGenTreePsaronius;
import genesis.world.gen.feature.WorldGenTreeSigillaria;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;

public class BiomeRainforest extends BiomeGenesis
{
	public BiomeRainforest(Biome.BiomeProperties properties)
	{
		super(properties);
		
		addDecorations();
		addTrees();
	}
	
	protected void addDecorations()
	{
		getDecorator().setGrassCount(9);
		addGrass(WorldGenPlant.create(EnumPlant.ZYGOPTERIS).setPatchCount(14), 1);
		
		addDecoration(WorldGenSplash.createHumusSplash(), 3.75F);
		addDecoration(new WorldGenBoulders(0.166F, 0.333F, 1).setRadius(FloatRange.create(0.75F, 1.5F), FloatRange.create(0.25F, 0.75F)), 0.75F);
		addDecoration(new WorldGenGrowingPlant(GenesisBlocks.ODONTOPTERIS).setNextToWater(false).setPatchCount(3), 2);
		addDecoration(new WorldGenGrowingPlant(GenesisBlocks.SPHENOPHYLLUM).setPatchCount(4), 2);
		addDecoration(new WorldGenGrowingPlant(GenesisBlocks.CALAMITES).setPatchCount(3), 3);
		addDecoration(new WorldGenMossStages(), 30);
		addDecoration(new WorldGenRoots(), 13);
		
		addPostDecoration(new WorldGenDebris(), 30);
	}
	
	protected void addTrees()
	{
		getDecorator().setTreeCount(20.6F);
		
		addTree(new WorldGenTreeSigillaria(10, 15, true).generateVine(11), 17);
		addTree(new WorldGenTreePsaronius(5, 8, true).generateVine(11), 14);
		addTree(new WorldGenTreeLepidodendron(14, 20, true).generateVine(11), 20);
		
		addTree(new WorldGenDeadLog(5, 8, EnumTree.LEPIDODENDRON, true).setCanGrowInWater(true), 2);
		addTree(new WorldGenDeadLog(4, 7, EnumTree.SIGILLARIA, true).setCanGrowInWater(true), 1);
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
