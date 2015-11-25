package genesis.world.biome.decorate;

import java.util.Random;

import genesis.common.GenesisBlocks;
import genesis.metadata.EnumDebrisOther;
import genesis.metadata.EnumTree;
import genesis.metadata.TreeBlocksAndItems;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class WorldGenDebris extends WorldGenDecorationBase
{
	@Override
	public boolean generate(World world, Random random, BlockPos pos)
	{
		Block block;
		
		do
		{
			block = world.getBlockState(pos).getBlock();
			if (!block.isAir(world, pos) && !block.isLeaves(world, pos))
			{
				break;
			}
			pos = pos.down();
		}
		while (pos.getY() > 0);
		
		boolean willGenerate = false;
		
		int debrisCount = this.getPatchSize();
		
		if (debrisCount <= 1)
			debrisCount = 25;
		
		for (int i = 0; i < debrisCount; ++i)
		{
			if (generateDebris(world, pos.add(3 - random.nextInt(7), 0, 3 - random.nextInt(7)), 5, 3, 5))
			{
				willGenerate = true;
			}
		}
		
		return willGenerate;
	}
	
	private boolean generateDebris(World world, BlockPos pos, int distanceX, int distanceY, int distanceZ)
	{
		boolean willGenerate = false;
		
		if (!(world.getBlockState(pos).getBlock() == GenesisBlocks.moss || world.getBlockState(pos).getBlock() == Blocks.dirt))
			return false;
		
		if (!world.getBlockState(pos.up()).getBlock().isAir(world, pos))
			return false;
		
		IBlockState wood;
		EnumTree variant = null;
		IBlockState debris = null;
		
		BlockPos debrisPos = pos.up();
		
		found:
			for (int x = -distanceX; x <= distanceX; ++x)
			{
				for (int z = -distanceZ; z <= distanceZ; ++z)
				{
					for (int y = -distanceY; y <= distanceY; ++y)
					{
						wood = world.getBlockState(debrisPos.add(x, y, z));
						
						if (wood == GenesisBlocks.calamites.getDefaultState())
						{
							debris = GenesisBlocks.debris.getBlockState(EnumDebrisOther.CALAMITES);
							willGenerate = true;
							break found;
						}
						else if (GenesisBlocks.trees.isStateOf(wood, TreeBlocksAndItems.LOG))
						{
							variant = GenesisBlocks.trees.getVariant(wood);
							
							if (
									variant == EnumTree.ARCHAEOPTERIS
									|| variant == EnumTree.SIGILLARIA
									|| variant == EnumTree.LEPIDODENDRON
									|| variant == EnumTree.ARAUCARIOXYLON)
							{
								debris = GenesisBlocks.debris.getBlockState(variant);
								willGenerate = true;
								break found;
							}
						}
					}
				}
			}
		
		if (willGenerate && debris != null)
		{
			setBlockInWorld(world, debrisPos, debris);
		}
		
		return willGenerate;
	}
}