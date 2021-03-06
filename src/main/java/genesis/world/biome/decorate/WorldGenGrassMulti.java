package genesis.world.biome.decorate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockBush;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldGenGrassMulti extends WorldGenGrass
{
	private final List<IBlockState> states = new ArrayList<>();
	private int volume = 128;

	public WorldGenGrassMulti(IBlockState... blockStates)
	{
		Collections.addAll(states, blockStates);
	}

	public WorldGenGrassMulti setVolume(int vol)
	{
		volume = vol;
		return this;
	}

	@Override
	public boolean generate(World world, Random random, BlockPos pos)
	{
		do
		{
			IBlockState state = world.getBlockState(pos);

			if (!state.getBlock().isAir(state, world, pos) && !state.getBlock().isLeaves(state, world, pos))
				break;

			pos = pos.down();
		}
		while (pos.getY() > 0);

		for (int i = 0; i < volume; ++i)
		{
			BlockPos placePos = pos.add(random.nextInt(8) - random.nextInt(8), random.nextInt(4) - random.nextInt(4), random.nextInt(8) - random.nextInt(8));
			IBlockState state = states.get(random.nextInt(states.size()));

			if (world.isAirBlock(placePos) && ((BlockBush) state.getBlock()).canBlockStay(world, placePos, state))
			{
				world.setBlockState(placePos, state, 2);
			}
		}

		return true;
	}

	@Override
	public IBlockState getSpawnablePlant(Random rand)
	{
		return states.get(rand.nextInt(states.size()));
	}
}
