package genesis.world.biome;


public class BiomeGenRainforestEdge extends BiomeGenRainforest
{
	public int totalTreesPerChunk = 500;
	
	public BiomeGenRainforestEdge(int id)
	{
		super(id);
		this.theBiomeDecorator.treesPerChunk = 0;
		this.biomeName = "Rainforest Edge";
	}

}