package openblocks.common.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import openblocks.Config;
import openblocks.OpenBlocks;
import openblocks.common.block.BlockCanvas;
import openmods.utils.ColorUtils;
import openmods.utils.ColorUtils.ColorMeta;
import openmods.utils.ItemUtils;
import openmods.utils.render.PaintUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemPaintBrush extends Item {

	public Icon paintIcon;

	public static final int MAX_USES = 24;

	public ItemPaintBrush() {
		super(Config.itemPaintBrushId);
		setCreativeTab(OpenBlocks.tabOpenBlocks);
		setHasSubtypes(true);
		setMaxStackSize(1);
		setMaxDamage(MAX_USES); // Damage dealt in Canvas block
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean par4) {
		Integer color = getColorFromStack(itemStack);
		if (color != null) list.add(String.format("#%06X", color));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean requiresMultipleRenderPasses() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister registry) {
		itemIcon = registry.registerIcon("openblocks:paintbrush");
		paintIcon = registry.registerIcon("openblocks:paintbrush_paint");
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void getSubItems(int id, CreativeTabs par2CreativeTabs, List list) {
		list.add(new ItemStack(this));
		for (ColorMeta color : ColorUtils.getAllColors()) {
			list.add(createStackWithColor(color.rgb));
		}
	}

	public static ItemStack createStackWithColor(int color) {
		ItemStack stack = new ItemStack(OpenBlocks.Items.paintBrush);
		NBTTagCompound tag = ItemUtils.getItemTag(stack);
		tag.setInteger("color", color);
		return stack;
	}

	@Override
	public boolean shouldPassSneakingClickToBlock(World par2World, int par4, int par5, int par6) {
		return true;
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		/* Check that we can paint this */
		if (stack.getItemDamage() >= MAX_USES || getColorFromStack(stack) == null) return true;
		
		/* Check with our manager upstairs about white lists */
		if (!PaintUtils.instance().isAllowedToPaint(world, x, y, z)) return true;
		/* Do some logic to make sure it's okay to paint */
		int id = world.getBlockId(x, y, z);
		if (id == 0 || id == OpenBlocks.Blocks.canvas.blockID) return true; /* Ignore */
		Block b = Block.blocksList[id];
		if (b == null) return true;
		if (!b.canProvidePower() && !b.isAirBlock(world, x, y, z)) {
			BlockCanvas.replaceBlock(world, x, y, z);
			OpenBlocks.Blocks.canvas.onBlockActivated(world, x, y, z, player, side, hitX, hitY, hitZ);
		}
		return true;
	}

	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, int X, int Y, int Z, EntityPlayer player) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack itemStack, int pass) {
		if (pass == 1) {
			Integer color = getColorFromStack(itemStack);
			if (color != null) return color;
		}

		return 0xFFFFFF;
	}

	@Override
	public Icon getIconFromDamageForRenderPass(int dmg, int pass) {
		return pass == 1? paintIcon : getIconFromDamage(dmg);
	}

	public static Integer getColorFromStack(ItemStack stack) {
		if (stack.hasTagCompound()) {
			NBTTagCompound tag = stack.getTagCompound();
			if (tag.hasKey("color")) { return tag.getInteger("color"); }
		}
		return null;
	}
}
