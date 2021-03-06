package genesis.world.gen.feature;

import java.util.Random;

import genesis.combo.variant.EnumTree;
import genesis.util.random.i.IntRange;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldGenTreeBjuvia extends WorldGenTreeBase
{
	public WorldGenTreeBjuvia(int minHeight, int maxHeight, boolean notify)
	{
		super(EnumTree.BJUVIA, IntRange.create(minHeight, maxHeight), notify);
	}
	
	@Override
	public int getRadius()
	{
		return 2;
	}
	
	@Override
	protected boolean doGenerate(World world, Random rand, BlockPos pos)
	{
		int height = heightProvider.get(rand) - 1;
		
		if (!isCubeClear(world, pos, 1, height))
			return false;
		
		for (int i = 0; i < height; i++)
		{
			setBlockInWorld(world, pos.up(i), wood);
		}
		
		BlockPos branchPos = pos.up(height);
		
		doLeavesBranch(world, branchPos, 1, 0, rand, 1 + rand.nextInt(2));
		doLeavesBranch(world, branchPos, -1, 0, rand, 1 + rand.nextInt(2));
		doLeavesBranch(world, branchPos, 0, 1, rand, 1 + rand.nextInt(2));
		doLeavesBranch(world, branchPos, 0, -1, rand, 1 + rand.nextInt(2));
		
		doLeavesBranch(world, branchPos, 1, 1, rand, 1 + rand.nextInt(2));
		doLeavesBranch(world, branchPos, -1, 1, rand, 1 + rand.nextInt(2));
		doLeavesBranch(world, branchPos, 1, -1, rand, 1 + rand.nextInt(2));
		doLeavesBranch(world, branchPos, -1, -1, rand, 1 + rand.nextInt(2));
		
		return true;
	}
	
	private void doLeavesBranch(World world, BlockPos pos, int dirX, int dirZ, Random random, int length)
	{
		for (int i = 1; i <= length; ++ i)
		{
			pos = pos.add(dirX, 0, dirZ);
			
			if (i == length)
				pos = pos.add(0, -1, 0);
			
			setBlockInWorld(world, pos, leaves);
		}
	}
}
