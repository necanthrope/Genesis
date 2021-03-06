package genesis.block;

import java.util.List;

import genesis.combo.ObjectType;
import genesis.combo.VariantsOfTypesCombo;
import genesis.combo.VariantsOfTypesCombo.BlockProperties;
import genesis.combo.variant.EnumTree;
import genesis.combo.variant.PropertyIMetadata;
import genesis.common.GenesisCreativeTabs;
import genesis.common.GenesisDimensions;
import genesis.item.ItemBlockMulti;
import genesis.util.BlockStateToMetadata;
import net.minecraft.block.BlockLog;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockGenesisLogs extends BlockLog
{
	/**
	 * Used in BlocksAndItemsWithVariantsOfTypes.
	 */
	@BlockProperties
	public static IProperty<?>[] getProperties()
	{
		return new IProperty[]{ LOG_AXIS };
	}
	
	public final VariantsOfTypesCombo<EnumTree> owner;
	public final ObjectType<EnumTree, ? extends BlockGenesisLogs, ? extends ItemBlockMulti<EnumTree>> type;
	
	public final List<EnumTree> variants;
	public final PropertyIMetadata<EnumTree> variantProp;
	
	public BlockGenesisLogs(VariantsOfTypesCombo<EnumTree> owner,
			ObjectType<EnumTree, ? extends BlockGenesisLogs, ? extends ItemBlockMulti<EnumTree>> type,
			List<EnumTree> variants, Class<EnumTree> variantClass)
	{
		super();
		
		this.owner = owner;
		this.type = type;
		
		this.variants = variants;
		variantProp = new PropertyIMetadata<>("variant", variants, variantClass);
		
		blockState = new BlockStateContainer(this, variantProp, LOG_AXIS);
		setDefaultState(getBlockState().getBaseState().withProperty(LOG_AXIS, EnumAxis.NONE));
		
		Blocks.FIRE.setFireInfo(this, 5, 5);
		
		setCreativeTab(GenesisCreativeTabs.BLOCK);
	}
	
	@Override
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
	{
		IBlockState state = super.onBlockPlaced(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);
		
		if (placer.isSneaking())
		{
			state = state.withProperty(LOG_AXIS, EnumAxis.NONE);
		}
		
		return state;
	}
	
	@Override
	public String getHarvestTool(IBlockState state)
	{
		return "axe";
	}
	
	@Override
	public int getHarvestLevel(IBlockState state)
	{
		return 0;
	}
	
	@Override
	public int getMetaFromState(IBlockState state)
	{
		return BlockStateToMetadata.getMetaForBlockState(state, variantProp, LOG_AXIS);
	}
	
	@Override
	public IBlockState getStateFromMeta(int metadata)
	{
		return BlockStateToMetadata.getBlockStateFromMeta(getDefaultState(), metadata, variantProp, LOG_AXIS);
	}
	
	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list)
	{
		owner.fillSubItems(type, variants, list);
	}
	
	@Override
	public int damageDropped(IBlockState state)
	{
		return owner.getStack(type, state.getValue(variantProp)).getItemDamage();
	}
	
	@Override
	public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player)
	{	// Prevent logs from dropping if the player isn't using the appropriate tool type.
		if (world instanceof World && GenesisDimensions.isGenesis((World) world))
		{
			@SuppressWarnings("deprecation")
			IBlockState state = getActualState(world.getBlockState(pos), world, pos);
			ItemStack held = player.getHeldItem(EnumHand.MAIN_HAND);
			
			if (held == null || held.getItem().getHarvestLevel(held, getHarvestTool(state), player, state) < 0)
			{
				return false;
			}
		}
		
		return super.canHarvestBlock(world, pos, player);
	}
}
