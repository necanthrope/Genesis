package genesis.block.tileentity.portal;

import java.util.*;

import genesis.combo.variant.EnumMenhirActivator;
import genesis.combo.variant.IMetadata;
import genesis.common.GenesisItems;
import genesis.util.ItemStackKey;
import net.minecraft.item.ItemStack;

public enum EnumGlyph implements IMetadata<EnumGlyph>
{
	NONE("none"),
	VEGETAL("vegetal",
			GenesisItems.MENHIR_ACTIVATORS.getStack(EnumMenhirActivator.ANCIENT_AMBER),
			GenesisItems.MENHIR_ACTIVATORS.getStack(EnumMenhirActivator.SACRED_TREE_STAR)),
	ANIMAL("animal",
			GenesisItems.MENHIR_ACTIVATORS.getStack(EnumMenhirActivator.FOSSILIZED_EGG)),
	ETHER("ether",
			GenesisItems.MENHIR_ACTIVATORS.getStack(EnumMenhirActivator.BROKEN_SPIRIT_MASK)),
	COSMOS("cosmos",
			GenesisItems.MENHIR_ACTIVATORS.getStack(EnumMenhirActivator.PRIMITIVE_DUST));
	
	public static final EnumGlyph[] VALID = {VEGETAL, ANIMAL, ETHER, COSMOS};
	
	final String name;
	final String unlocalizedName;
	final List<ItemStackKey> activators = new ArrayList<>();
	
	EnumGlyph(String name, String unlocalizedName, Object... activators)
	{
		this.name = name;
		this.unlocalizedName = unlocalizedName;
		
		for (Object activator : activators)
		{
			if (activator instanceof ItemStack)
			{
				addActivator((ItemStack) activator);
			}
			else if (activator instanceof ItemStackKey)
			{
				addActivator((ItemStackKey) activator);
			}
			else
			{
				throw new IllegalArgumentException("Invalid activator item " + activator + " for glyph with name \"" + name + "\".");
			}
		}
	}
	
	EnumGlyph(String name, Object... activators)
	{
		this(name, name, activators);
	}
	
	@Override
	public String getName()
	{
		return name;
	}
	
	@Override
	public String getUnlocalizedName()
	{
		return unlocalizedName;
	}
	
	public Collection<ItemStackKey> getActivators()
	{
		return Collections.unmodifiableList(activators);
	}
	
	public boolean isActivator(ItemStack stack)
	{
		return activators.contains(new ItemStackKey(stack));
	}
	
	public ItemStackKey addActivator(ItemStackKey stack)
	{
		activators.add(stack);
		return stack;
	}
	
	public ItemStackKey addActivator(ItemStack stack)
	{
		return addActivator(new ItemStackKey(stack));
	}
	
	public ItemStack getActivator(Random rand)
	{
		int size = activators.size();
		
		if (size > 0)
		{
			return activators.get(rand.nextInt(size)).createNewStack();
		}
		
		return null;
	}
}