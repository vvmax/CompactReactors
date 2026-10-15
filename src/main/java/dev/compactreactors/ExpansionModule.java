package dev.compactreactors;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.creativetab.CreativeTabs;
public final class ExpansionModule extends Item {
 public final int tier;
 public ExpansionModule(int tier) {
  this.tier=tier;
  setUnlocalizedName("compactreactors.expansion"+tier);
  setTextureName("compactreactors:expansion_"+tier);
  setMaxStackSize(1); setCreativeTab(CreativeTabs.tabRedstone);
 }
 public static boolean matches(ItemStack stack,int tier) {
  return stack!=null && stack.getItem() instanceof ExpansionModule && ((ExpansionModule)stack.getItem()).tier==tier;
 }
 @Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
 public void addInformation(ItemStack stack,net.minecraft.entity.player.EntityPlayer player,java.util.List lines,boolean advanced) {
  lines.add(net.minecraft.client.resources.I18n.format("item.compactreactors.expansion"+tier+".tip"));
  if(tier==2) lines.add(net.minecraft.client.resources.I18n.format("item.compactreactors.expansion2.requires"));
 }
}
