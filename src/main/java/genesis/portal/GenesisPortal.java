package genesis.portal;

import genesis.block.tileentity.portal.*;
import genesis.combo.variant.EnumMenhirPart;
import genesis.common.*;
import genesis.util.FacingHelpers;
import genesis.util.WorldUtils;
import genesis.util.math.PosVecIterable;
import genesis.util.math.PosVecIterable.PosVecIterator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.*;
import java.util.stream.Collectors;

import static genesis.portal.GenesisPortalCircle.genStructure;

public class GenesisPortal
{
	public static final int MENHIR_MIN_DISTANCE = 1;
	public static final int MENHIR_DEFAULT_DISTANCE = 3;
	public static final int MENHIR_MAX_DISTANCE = 5;
	public static final int PORTAL_HEIGHT = 4;

	public static final int COOLDOWN = 10;

	// Values for external things
	public static final byte PORTAL_CHECK_TIME = 5;

	public static boolean isBlockingPortal(IBlockAccess world, BlockPos pos, IBlockState state)
	{
		return GenesisBlocks.MENHIRS.containsState(state) || state.getLightOpacity(world, pos) >= 15;
	}

	public static boolean isBlockingPortal(IBlockAccess world, BlockPos pos)
	{
		return isBlockingPortal(world, pos, world.getBlockState(pos));
	}

	protected final IBlockAccess blockAccess;
	protected BlockPos center;
	protected BlockPos portal;
	protected final Map<EnumFacing, MenhirData> menhirs = new EnumMap<>(EnumFacing.class);
	protected final Map<EnumFacing, MenhirData> menhirsView = Collections.unmodifiableMap(menhirs);

	public static GenesisPortal fromPortalBlock(IBlockAccess blockAccess, BlockPos pos)
	{
		return new GenesisPortal(blockAccess, pos.down(PORTAL_HEIGHT));
	}

	public static GenesisPortal fromCenterBlock(IBlockAccess blockAccess, BlockPos pos)
	{
		return new GenesisPortal(blockAccess, pos);
	}

	public static GenesisPortal fromMenhirBlock(IBlockAccess blockAccess, BlockPos pos, IBlockState state)
	{
		return new GenesisPortal(blockAccess, pos, state);
	}

	public static GenesisPortal fromMenhirBlock(IBlockAccess blockAccess, BlockPos pos)
	{
		return fromMenhirBlock(blockAccess, pos, null);
	}

	/**
	 * Provides data about a portal from the position of the center of the portal.
	 */
	protected GenesisPortal(IBlockAccess blockAccess, BlockPos center)
	{
		this.blockAccess = blockAccess;
		this.center = center;
		refresh();
	}

	/**
	 * Provides data about a portal from the position of a menhir.
	 */
	protected GenesisPortal(IBlockAccess blockAccess, BlockPos pos, IBlockState state)
	{
		this.blockAccess = WorldUtils.getFakeWorld(blockAccess, pos, state);
		BlockPos menhirCenter = findCenterFromMenhir(pos, state);
		center = menhirCenter == null ? pos : menhirCenter;
		refresh();
	}

	protected boolean isValidMenhir(BlockPos pos, IBlockState state, EnumFacing facing)
	{
		return GenesisBlocks.MENHIRS.containsState(state)
				&& BlockMenhir.getFacing(state) == facing
				&& pos.getY() == new MenhirData(blockAccess, pos).getBottomPos().getY();
	}

	protected int findMenhir(BlockPos start, EnumFacing direction, int min, int max)
	{
		MutableBlockPos pos = new MutableBlockPos(start);
		PosVecIterator iter = new PosVecIterator(pos, direction, min);

		while (iter.hasNext())
		{
			iter.next();
			if (isBlockingPortal(blockAccess, pos))
				return -1;
		}

		iter = new PosVecIterator(pos, direction, max - min);

		while (iter.hasNext())
		{
			iter.next();
			IBlockState checkState = blockAccess.getBlockState(pos);

			if (isValidMenhir(pos, checkState, direction.getOpposite()))
				return max - iter.getCountLeft();
			else if (isBlockingPortal(blockAccess, pos, checkState))
				return -1;
		}

		return -1;
	}

	protected BlockPos findCenterFromMenhir(BlockPos pos, IBlockState state)
	{
		MenhirData startMenhir = new MenhirData(blockAccess, pos);
		pos = startMenhir.getBottomPos();
		EnumFacing facing = startMenhir.getFacing();

		for (MutableBlockPos center : new PosVecIterable(pos, facing, MENHIR_MAX_DISTANCE))
		{
			if (isBlockingPortal(blockAccess, center))
			{
				break;
			}
			else if (WorldUtils.distSqr(pos, center) >= MENHIR_MIN_DISTANCE * MENHIR_MIN_DISTANCE)
			{
				Axis axis = facing.rotateY().getAxis();

				for (AxisDirection direction : AxisDirection.values())
				{
					if (findMenhir(center, FacingHelpers.getFacing(axis, direction), MENHIR_MIN_DISTANCE, MENHIR_MAX_DISTANCE) != -1)
						return center;
				}
			}
		}

		int menhirDistance = findMenhir(pos, facing, MENHIR_MIN_DISTANCE * 2, MENHIR_MAX_DISTANCE * 2);

		if (menhirDistance != -1)
			return pos.offset(facing, menhirDistance / 2);

		return pos;
	}

	public void refresh()
	{
		portal = null;
		menhirs.clear();
	}

	public IBlockAccess getBlockAccess()
	{
		return blockAccess;
	}

	public BlockPos getCenterPosition()
	{
		return center;
	}

	public void setCenterPosition(BlockPos pos)
	{
		center = pos;
		refresh();
	}

	public BlockPos getPortalPosition()
	{
		return portal == null ? center.up(PORTAL_HEIGHT) : portal;
	}

	protected void activatePortal(World world)
	{
		if (world.getBlockState(getPortalPosition()).getBlock().isReplaceable(world, getPortalPosition()))
		{
			world.setBlockState(getPortalPosition(), GenesisBlocks.PORTAL.getDefaultState());
		}
	}

	protected void deactivatePortal(World world)
	{
		if (world.getBlockState(getPortalPosition()).getBlock() == GenesisBlocks.PORTAL)
		{
			world.setBlockToAir(getPortalPosition());
		}
	}

	public boolean updatePortalStatus(World world)
	{
		Set<EnumGlyph> glyphs = EnumSet.noneOf(EnumGlyph.class);

		glyphs.addAll(getMenhirs().values().stream().filter(menhir -> menhir != null && menhir.isReceptacleActive()).map(MenhirData::getGlyph).collect(Collectors.toList()));

		if (glyphs.size() == 4 && !glyphs.contains(EnumGlyph.NONE))
		{
			activatePortal(world);
			return true;
		}

		deactivatePortal(world);
		return false;
	}

	public Map<EnumFacing, MenhirData> getMenhirs()
	{
		for (EnumFacing direction : EnumFacing.HORIZONTALS)
		{
			getMenhir(direction);
		}

		return Collections.unmodifiableMap(menhirs);
	}

	public MenhirData getMenhir(EnumFacing direction)
	{
		if (!menhirs.containsKey(direction))
		{
			menhirs.put(direction, null);

			for (int forward = 0; forward <= MENHIR_MAX_DISTANCE; forward++)
			{
				BlockPos checkPos = center.offset(direction, forward);
				IBlockState checkState = blockAccess.getBlockState(checkPos);

				if (forward >= MENHIR_MIN_DISTANCE && isValidMenhir(checkPos, checkState, direction.getOpposite()))
				{
					MenhirData menhir = new MenhirData(blockAccess, checkPos);
					menhirs.put(direction, menhir);
					break;
				}

				if (isBlockingPortal(blockAccess, checkPos, checkState))
				{
					break;
				}
			}
		}

		return menhirs.get(direction);
	}

	public int getDistance(EnumFacing direction)
	{
		if (getMenhir(direction) == null)
		{
			return -1;
		}

		BlockPos bottom = getMenhir(direction).getBottomPos();
		BlockPos center = getCenterPosition();
		return direction.getFrontOffsetX() * (bottom.getX() - center.getX()) +
				direction.getFrontOffsetY() * (bottom.getY() - center.getY()) +
				direction.getFrontOffsetZ() * (bottom.getZ() - center.getZ());
	}

	public void placeMenhir(World world, BlockPos pos, EnumFacing facing, EnumGlyph glyph, boolean active)
	{
		world.setBlockState(pos, GenesisBlocks.MENHIRS.getBlockState(EnumMenhirPart.GLYPH).withProperty(BlockMenhir.FACING, facing));
		BlockMenhir.getGlyphTileEntity(world, pos).setGlyph(glyph);

		world.setBlockState(pos = pos.up(), GenesisBlocks.MENHIRS.getBlockState(EnumMenhirPart.RECEPTACLE).withProperty(BlockMenhir.FACING, facing));

		if (active)
		{
			BlockMenhir.getReceptacleTileEntity(world, pos).setContainedItem(glyph.getActivator(world.rand));
		}

		world.setBlockState(pos = pos.up(), GenesisBlocks.MENHIRS.getBlockState(EnumMenhirPart.TOP).withProperty(BlockMenhir.FACING, facing));
	}

	public int getDistanceWithDefault(EnumFacing direction, int def)
	{
		int distance = getDistance(direction);
		return distance == -1 ? def : distance;
	}

	public boolean setPlacementPosition(World world)
	{
		double distance = -1;
		BlockPos portalPos = null;

		nextCenter:
		for (BlockPos checkCenter : WorldUtils.getAreaWithHeight(getCenterPosition(), 64, 0, world.getActualHeight()))
		{
			int startR = 0;

			for (EnumFacing dir : EnumFacing.HORIZONTALS)
			{
				int menhirDistance = getDistanceWithDefault(dir, MENHIR_DEFAULT_DISTANCE);

				for (int r = startR; r <= menhirDistance; r++)
				{
					for (int h = -1; h <= PORTAL_HEIGHT; h++)
					{
						BlockPos checkPos = checkCenter.offset(dir, r).up(h);
						IBlockState checkState = world.getBlockState(checkPos);

						if (r < menhirDistance)
						{	// Check line to menhir
							if (h == 0)
							{	// Blocks at the same level as the bottoms of the menhirs
								if (!world.canSeeSky(checkPos) || GenesisPortal.isBlockingPortal(world, checkPos))
								{
									continue nextCenter;
								}
							}
						}
						else
						{	// Check menhir position
							if (h == -1)
							{
								if (!world.isSideSolid(checkPos, EnumFacing.UP))
								{	// Block below menhir.
									continue nextCenter;
								}
							}
							else if (checkState.getMaterial().isLiquid() || !checkState.getBlock().isReplaceable(world, checkPos))
							{	// Blocks in the way of the menhir
								continue nextCenter;
							}
						}
					}
				}

				startR = 1;
			}

			double checkDistance = getCenterPosition().distanceSq(checkCenter);

			if (distance < 0 || checkDistance < distance)
			{
				distance = checkDistance;
				portalPos = checkCenter;
			}
		}

		if (portalPos != null)
		{
			setCenterPosition(portalPos);
			return true;
		}

		return false;
	}

	public void createPlatform(World world)
	{
		BlockPos center = null;
		Iterable<BlockPos> platform = null;

		findPlatform:
		for (int y = world.getActualHeight(); y >= 0 || y == world.getActualHeight(); y--)
		{
			center = new BlockPos(getCenterPosition().getX(), y, getCenterPosition().getZ());
			BlockPos start = center;
			BlockPos end = start;

			for (EnumFacing dir : EnumFacing.HORIZONTALS)
			{
				if (dir.getAxisDirection().getOffset() <= 0)
					start = start.offset(dir, getDistanceWithDefault(dir, MENHIR_DEFAULT_DISTANCE));
				else
					end = end.offset(dir, getDistanceWithDefault(dir, MENHIR_DEFAULT_DISTANCE));
			}

			platform = WorldUtils.getArea(start, end);

			for (BlockPos pos : platform)
			{
				if (!blockAccess.isAirBlock(pos.down()))
				{
					break findPlatform;
				}
			}
		}

		setCenterPosition(center.up());
		IBlockState place = GenesisDimensions.isGenesis(world) ? GenesisBlocks.MOSS.getDefaultState() : Blocks.GRASS.getDefaultState();

		for (BlockPos pos : platform)
		{
			world.setBlockState(pos, place);
		}
	}

	public boolean makePortal(World world, Random random, boolean active)
	{
		EnumSet<EnumGlyph> glyphs = EnumSet.allOf(EnumGlyph.class);
		glyphs.remove(EnumGlyph.NONE);

		for (boolean check : new boolean[]{true, false})
		{
			// Place a menhir on each horizontal direction.
			for (EnumFacing dir : EnumFacing.HORIZONTALS)
			{
				BlockPos placePos = getCenterPosition().offset(dir, getDistanceWithDefault(dir, MENHIR_DEFAULT_DISTANCE));

				if (check)
				{
					if (!world.getBlockState(placePos).getBlock().isReplaceable(world, placePos))
					{
						return false;
					}
				}
				else
				{
					// Get random glyph from the set.
					EnumGlyph glyph = null;
					int glyphIndex = random.nextInt(glyphs.size());
					Iterator<EnumGlyph> glyphsIter = glyphs.iterator();

					for (int i = 0; i <= glyphIndex; i++)
					{
						glyph = glyphsIter.next();
					}

					glyphs.remove(glyph);

					// Place the menhir.
					placeMenhir(world, placePos, dir.getOpposite(), glyph, active);
				}
			}
		}

		refresh();
		updatePortalStatus(world);
		genStructure(world, center, GenesisDimensions.isGenesis(world));
		return true;
	}

	public void duplicatePortal(World world, GenesisPortal fromPortal)
	{
		Genesis.logger.info("Duplicating portal " + fromPortal + " to portal " + this + ".");
		BlockPos posDiff = getCenterPosition().subtract(fromPortal.getCenterPosition());
		IBlockAccess fromWorld = fromPortal.getBlockAccess();

		fromPortal.getMenhirs().values().stream().filter(menhir -> menhir != null).forEach(menhir -> {
			for (BlockPos from : new MenhirIterator(fromWorld, menhir.getBottomPos(), true)) {
				BlockPos newPos = from.add(posDiff);
				IBlockState fromState = fromWorld.getBlockState(from);
				world.setBlockState(newPos, fromState);
				TileEntity te = fromWorld.getTileEntity(from);

				if (te != null) {
					NBTTagCompound compound = new NBTTagCompound();
					te.writeToNBT(compound);
					TileEntity newTE = TileEntity.create(world, compound);
					world.removeTileEntity(newPos);
					world.setTileEntity(newPos, newTE);
				}

				world.notifyBlockUpdate(newPos, fromState, fromState, 0b1000);
			}
		});

		refresh();
		updatePortalStatus(world);
		genStructure(world, center, GenesisDimensions.isGenesis(world));
	}

	@Override
	public String toString()
	{
		return "portal at " + getCenterPosition() + " with menhirs " + getMenhirs();
	}
}
