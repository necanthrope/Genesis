package genesis.util.blocks;

import java.util.*;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.base.*;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

public class FacingProperties<V extends Comparable<V>> implements Iterable<FacingProperties.Entry<V>>
{
	public static <V extends Comparable<V>> FacingProperties<V> create(Function<EnumFacing, IProperty<V>> propertyFunc, EnumFacing... facings)
	{
		return new FacingProperties<V>(propertyFunc, facings);
	}
	
	public final Map<EnumFacing, IProperty<V>> map;
	
	public final IProperty<V> up;
	public final IProperty<V> down;
	public final IProperty<V> north;
	public final IProperty<V> south;
	public final IProperty<V> east;
	public final IProperty<V> west;
	
	private FacingProperties(Function<EnumFacing, IProperty<V>> propertyFunc, EnumFacing... facings)
	{
		ImmutableMap.Builder<EnumFacing, IProperty<V>> builder = ImmutableMap.builder();
		
		for (EnumFacing facing : facings)
		{
			builder.put(facing, propertyFunc.apply(facing));
		}
		
		map = builder.build();

		up = map.get(EnumFacing.UP);
		down = map.get(EnumFacing.DOWN);
		north = map.get(EnumFacing.NORTH);
		south = map.get(EnumFacing.SOUTH);
		east = map.get(EnumFacing.EAST);
		west = map.get(EnumFacing.WEST);
	}
	
	public boolean has(EnumFacing facing)
	{
		return map.containsKey(facing);
	}
	
	public boolean has(IProperty<V> property)
	{
		return map.containsValue(property);
	}
	
	public IProperty<V> get(EnumFacing facing)
	{
		return map.get(facing);
	}
	
	public IBlockState stateWith(IBlockState state, Function<EnumFacing, V> valueFunc)
	{
		for (Entry<V> entry : this)
		{
			state = state.withProperty(entry.property, valueFunc.apply(entry.facing));
		}
		
		return state;
	}
	
	public IBlockState stateWith(IBlockState state, V value)
	{
		return stateWith(state, (f) -> value);
	}
	
	public Set<EnumFacing> facings()
	{
		return map.keySet();
	}
	
	@Override
	public Iterator<Entry<V>> iterator()
	{
		return FluentIterable.from(map.entrySet()).transform((e) -> new Entry<V>(e.getKey(), e.getValue())).iterator();
	}
	
	public IProperty<V>[] toArray(IProperty<V>[] array)
	{
		return map.values().toArray(array);
	}
	
	public IProperty<?>[] toArray()
	{
		return map.values().toArray(new IProperty<?>[map.size()]);
	}
	
	public IProperty<?>[] toArrayWith(IProperty<?>... properties)
	{
		return ArrayUtils.addAll(toArray(), properties);
	}
	
	public static class Entry<V extends Comparable<V>>
	{
		public final EnumFacing facing;
		public final IProperty<V> property;
		
		private Entry(EnumFacing facing, IProperty<V> property)
		{
			this.facing = facing;
			this.property = property;
		}
	}
}