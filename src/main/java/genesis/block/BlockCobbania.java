package genesis.block;

import java.util.List;

import genesis.common.GenesisCreativeTabs;
import genesis.util.Constants;
import net.minecraft.block.BlockLilyPad;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class BlockCobbania extends BlockLilyPad
{
	public BlockCobbania()
	{
		setHardness(0.0F);
		setStepSound(soundTypeGrass);
		setUnlocalizedName(Constants.PREFIX + "cobbania");
		setCreativeTab(GenesisCreativeTabs.DECORATIONS);
	}
	
	@Override
	public void addCollisionBoxesToList(World worldIn, BlockPos pos, IBlockState state, AxisAlignedBB mask, List list, Entity collidingEntity) {}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state)
	{
		return null;
	}
}