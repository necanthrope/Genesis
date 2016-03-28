package genesis.world.gen.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import genesis.combo.TreeBlocksAndItems;
import genesis.combo.variant.EnumTree;
import genesis.common.GenesisBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockLog.EnumAxis;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

public abstract class WorldGenTreeBase extends WorldGenAbstractTree
{
	public IBlockState wood;
	public IBlockState leaves;
	public IBlockState hangingFruit;
	public Block treeSoil;
	
	protected boolean notify;
	protected int rarity = 1;
	protected int minHeight;
	protected int maxHeight;
	
	private int treeCountPerChunk = 0;
	private boolean canGrowInWater = false;
	private List<BlockStateContainer> allowedBlocks = new ArrayList<BlockStateContainer>();
	
	public WorldGenTreeBase(IBlockState wood, IBlockState leaves, boolean notify)
	{
		super(notify);
		
		this.wood = wood;
		this.leaves = leaves;
		this.notify = notify;
	}
	
	public WorldGenTreeBase setRarity(int r)
	{
		rarity = r;
		return this;
	}
	
	public WorldGenTreeBase setTreeCountPerChunk(int count)
	{
		treeCountPerChunk = count;
		return this;
	}
	
	public int getTreeCountPerChunk()
	{
		return treeCountPerChunk;
	}
	
	public WorldGenTreeBase setCanGrowInWater(boolean canGrow)
	{
		canGrowInWater = canGrow;
		return this;
	}
	
	public void addAllowedBlocks(BlockStateContainer... blocks)
	{
		for (int i = 0; i < blocks.length; ++i)
		{
			allowedBlocks.add(blocks[i]);
		}
	}
	
	@Override
	public abstract boolean generate(World world, Random rand, BlockPos pos);
	
	public BlockPos getTreePos(World world, BlockPos pos)
	{
		BlockPos treePos = pos;
		
		do
		{
			treeSoil = world.getBlockState(treePos).getBlock();
			if (
					!treeSoil.isAir(world, treePos) 
					&& !treeSoil.isLeaves(world, treePos)
					&& !(treeSoil == Blocks.water && canGrowInWater)
			)
			{
				break;
			}
			treePos = treePos.down();
		}
		while (treePos.getY() > 0);
		
		return treePos.up();
	}
	
	public boolean canTreeGrow(World world, BlockPos pos)
	{
		Block upBlock = world.getBlockState(pos.up()).getBlock();
		
		if (
				(treeSoil == null 
				|| (
						!treeSoil.canSustainPlant(world, pos, EnumFacing.UP, GenesisBlocks.trees.getBlock(TreeBlocksAndItems.SAPLING, EnumTree.LEPIDODENDRON))
						&& !allowedBlocks.contains(treeSoil.getBlockState())))
				|| (!upBlock.isAir(world, pos.up()) && !(canGrowInWater && upBlock == Blocks.water))
		)
		{
			return false;
		}
		
		return true;
	}
	
	protected void generateBranchSideup(World world, BlockPos pos, Random rand, int dirX, int dirZ, int bBaseHeight, int bGrowSize, int leavesLength)
	{
		BlockPos branchPos = pos;
		int height = bBaseHeight + rand.nextInt(bGrowSize);
		
		branchPos = branchPos.add(dirX, 0, dirZ);
		setBlockInWorld(world, branchPos, wood);
		
		branchPos = branchPos.add(dirX, 1, dirZ);
		
		for (int i = 0; i < height; ++i)
		{
			setBlockInWorld(world, branchPos, wood);
			branchPos = branchPos.up();
		}
		
		doPineTopLeaves(world, pos, branchPos.down(), height, branchPos.getY() - height + 1, rand, false, leavesLength, false, false);
	}
	
	protected void generateBranchSide(World world, BlockPos pos, Random rand, int dirX, int dirZ, int maxLength, int branchRarity)
	{
		BlockPos branchPos = pos;
		EnumAxis axis;
		int branchLength = 1 + rand.nextInt(maxLength);
		
		branchPos = branchPos.add(dirX, 0, dirZ);
		
		if (dirX != 0)
			axis = EnumAxis.X;
		else if (dirZ != 0)
			axis = EnumAxis.Z;
		else
			axis = EnumAxis.Y;
		
		setBlockInWorld(world, branchPos, wood.withProperty(BlockLog.LOG_AXIS, axis));
		generateHorizontalBranchLeaveS(world, branchPos, dirX, dirZ);
		
		for (int i = 1; i <= branchLength - 1; ++i)
		{
			branchPos = branchPos.add(dirX, (rand.nextInt(3) - 1) * rand.nextInt(2), dirZ);
			setBlockInWorld(world, branchPos, wood.withProperty(BlockLog.LOG_AXIS, axis));
			generateHorizontalBranchLeaveS(world, branchPos, dirX, dirZ);
			
			if (rand.nextInt(branchRarity) == 0)
			{
				int dSwitch = (rand.nextInt(2) == 0)? 1:-1;
				int childLength = 1 + rand.nextInt(branchLength);
				generateBranchSide(world, branchPos, rand, ((dirX == 0)? dSwitch : 0), ((dirZ == 0)? dSwitch : 0), childLength, branchRarity + 1); //((int)(maxLength / 2) < 1)? 1 : ((int)(maxLength / 2))
			}
		}
		
		branchPos = branchPos.add(dirX, 0, dirZ);
		setBlockInWorld(world, branchPos, leaves);
	}
	
	protected void generateHorizontalBranchLeaveS(World world, BlockPos pos, int dirX, int dirZ)
	{
		setBlockInWorld(world, pos.add(0, 1, 0), leaves);
		setBlockInWorld(world, pos.add(0, -1, 0), leaves);
		setBlockInWorld(world, pos.add(((dirX == 0) ? 1 : 0), 0, ((dirZ == 0) ? 1 : 0)), leaves);
		setBlockInWorld(world, pos.add(((dirX == 0) ? -1 : 0), 0, ((dirZ == 0) ? -1 : 0)), leaves);
	}
	
	protected void generateLeafLayerCircle(World world, Random random, double radius, BlockPos pos)
	{
		for (int x = (int) Math.ceil(pos.getX() - radius); x <= (int) Math.ceil(pos.getX() + radius); x++)
		{
			for (int z = (int) Math.ceil(pos.getZ() - radius); z <= (int) Math.ceil(pos.getZ() + radius); z++)
			{
				double xfr = z - pos.getZ();
				double zfr = x - pos.getX();
				
				if (xfr * xfr + zfr * zfr <= radius * radius)
				{
					setBlockInWorld(world, new BlockPos(x, pos.getY(), z), leaves);
				}
			}
		}
	}
	
	protected void setBlockInWorld(World world, BlockPos pos, IBlockState state)
	{
		setBlockInWorld(world, pos, state, false);
	}
	
	protected void setBlockInWorld(World world, BlockPos pos, IBlockState state, boolean force)
	{
		boolean place = true;
		
		if (!world.isBlockLoaded(pos))
			return;
		
		if (
				state == wood 
				&& !(world.getBlockState(pos).getBlock().isAir(world, pos) 
						|| world.getBlockState(pos).getBlock().getMaterial().isReplaceable()
						|| world.getBlockState(pos).getBlock().isLeaves(world, pos))
				&& !force)
		{
			place = false;
		}
		
		if (
				state == leaves 
				&& !world.getBlockState(pos).getBlock().isAir(world, pos)
				&& !force
				&& !(world.getBlockState(pos) == this.hangingFruit))
		{
			place = false;
		}
		
		if (place)
		{
			if (state == leaves && world.rand.nextInt(6) == 0 && GenesisBlocks.trees.getVariant(leaves).getFruitType() == EnumTree.FruitType.LEAVES)
			{
				state = GenesisBlocks.trees.getBlockState(TreeBlocksAndItems.LEAVES_FRUIT, GenesisBlocks.trees.getVariant(leaves));
			}
			
			if (world.getBlockState(pos.down()) == this.hangingFruit)
			{
				world.setBlockState(pos.down(), Blocks.air.getDefaultState());
			}
			
			if (notify)
			{
				world.setBlockState(pos, state, 3);
			}
			else
			{
				world.setBlockState(pos, state, 2);
			}
			
			if (this.hangingFruit != null && state == leaves)
			{
				if (world.getBlockState(pos.down()).getBlock().isAir(world, pos.down()) && world.rand.nextInt(10) == 0)
				{
					world.setBlockState(pos.down(), this.hangingFruit);
				}
			}
		}
	}
	
	protected boolean isCubeClear(World world, BlockPos pos, int radius, int height)
	{
		Iterable<BlockPos> posList = BlockPos.getAllInBox(pos.add(-radius, 0, -radius), pos.add(radius, height, radius));
		
		for (BlockPos checkPos : posList)
		{
			if (!world.isAirBlock(checkPos) && !world.getBlockState(checkPos).getBlock().isLeaves(world, pos))
			{
				return false;
			}
		}
		return true;
	}
	
	public void doBranchLeaves(World world, BlockPos pos, Random random, boolean cap, int length)
	{
		doBranchLeaves(world, pos, random, cap, length, false);
	}
	
	public void doBranchLeaves(World world, BlockPos pos, Random random, boolean cap, int length, boolean irregular)
	{
		for (int i = 1; i <= length - ((irregular && random.nextInt(3) == 0)? (random.nextInt(length + 1)) : 0); ++i)
		{
			setBlockInWorld(world, pos.north(i), leaves);
			if (!irregular || !(random.nextInt(6) == 0))
				setBlockInWorld(world, pos.north(i - 1).east(), leaves);
			if (!irregular || !(random.nextInt(6) == 0))
				setBlockInWorld(world, pos.north(i - 1).west(), leaves);
		}
		
		for (int i = 1; i <= length - ((irregular && random.nextInt(3) == 0)? (random.nextInt(length + 1)) : 0); ++i)
		{
			setBlockInWorld(world, pos.south(i), leaves);
			if (!irregular || !(random.nextInt(6) == 0))
				setBlockInWorld(world, pos.south(i - 1).east(), leaves);
			if (!irregular || !(random.nextInt(6) == 0))
				setBlockInWorld(world, pos.south(i - 1).west(), leaves);
		}
		
		for (int i = 1; i <= length - ((irregular && random.nextInt(3) == 0)? (random.nextInt(length + 1)) : 0); ++i)
		{
			setBlockInWorld(world, pos.east(i), leaves);
			if (!irregular || !(random.nextInt(6) == 0))
				setBlockInWorld(world, pos.east(i - 1).north(), leaves);
			if (!irregular || !(random.nextInt(6) == 0))
				setBlockInWorld(world, pos.east(i - 1).south(), leaves);
		}
		
		for (int i = 1; i <= length - ((irregular && random.nextInt(3) == 0)? (random.nextInt(length + 1)) : 0); ++i)
		{
			setBlockInWorld(world, pos.west(i), leaves);
			if (!irregular || !(random.nextInt(6) == 0))
				setBlockInWorld(world, pos.west(i - 1).north(), leaves);
			if (!irregular || !(random.nextInt(6) == 0))
				setBlockInWorld(world, pos.west(i - 1).south(), leaves);
		}
		
		if (cap)
		{
			setBlockInWorld(world, pos.up(1), leaves);
			setBlockInWorld(world, pos.up(1).north(), leaves);
			setBlockInWorld(world, pos.up(1).south(), leaves);
			setBlockInWorld(world, pos.up(1).east(), leaves);
			setBlockInWorld(world, pos.up(1).west(), leaves);
			setBlockInWorld(world, pos.up(2), leaves);
		}
	}
	
	public void doPineTopLeaves(World world, BlockPos genPos, BlockPos branchPos, int treeHeight, int leavesBase, Random rand, boolean alternate, boolean irregular, boolean inverted)
	{
		doPineTopLeaves(world, genPos, branchPos, treeHeight, leavesBase, rand, alternate, 4, irregular, inverted);
	}
	
	public void doPineTopLeaves(World world, BlockPos genPos, BlockPos branchPos, int treeHeight, int leavesBase, Random rand, boolean alternate, boolean irregular)
	{
		doPineTopLeaves(world, genPos, branchPos, treeHeight, leavesBase, rand, alternate, 4, irregular, false);
	}
	
	public void doPineTopLeaves(World world, BlockPos genPos, BlockPos branchPos, int treeHeight, int leavesBase, Random rand, boolean alternate)
	{
		doPineTopLeaves(world, genPos, branchPos, treeHeight, leavesBase, rand, alternate, 4, false, false);
	}
	
	public void doPineTopLeaves(World world, BlockPos genPos, BlockPos branchPos, int treeHeight, int leavesBase, Random rand, boolean alternate, int maxLeaveLength, boolean irregular, boolean inverted)
	{
		boolean alt = false;
		float percent;
		int leaves;
		
		if (leavesBase > branchPos.getY())
			return;
		
		doBranchLeaves(world, branchPos, rand, true, 1);
		
		while (branchPos.getY() > leavesBase)
		{
			branchPos = branchPos.add(0, -1, 0);
			
			percent = ((branchPos.getY() - leavesBase) / (float) (genPos.getY() + treeHeight - leavesBase));
			
			if (!inverted)
				percent = 1 - percent;
			
			leaves = MathHelper.ceiling_float_int(maxLeaveLength * percent);
			
			if (leaves > maxLeaveLength)
				leaves = maxLeaveLength;
			
			if (alt || !alternate || (irregular && rand.nextInt(5) == 0))
				doBranchLeaves(world, branchPos, rand, false, leaves, irregular);
			
			alt = !alt;
		}
	}
	
	@Override
	protected boolean func_150523_a(Block block)
	{
		return block == GenesisBlocks.moss || super.func_150523_a(block);
	}
}
