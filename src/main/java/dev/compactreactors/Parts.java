package dev.compactreactors;
import erogenousbeef.bigreactors.api.registry.Reactants;
import erogenousbeef.bigreactors.api.registry.ReactorInterior;
import erogenousbeef.bigreactors.common.BigReactors;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
final class Parts {
 static ItemStack helpIcon(int index) {
  if(index>=0 && index<5) {
   Item item=Item.getItemFromBlock(reactorPart());
   return item==null?null:new ItemStack(item,1,new int[]{0,1,3,4,2}[index]);
  }
  if(index==5) {
   for(ItemStack glass:OreDictionary.getOres("glassReactor")) if(glass!=null && glass.getItem()!=null) {
    ItemStack icon=glass.copy(); icon.stackSize=1;
    if(icon.getItemDamage()==OreDictionary.WILDCARD_VALUE) icon.setItemDamage(0);
    return icon;
   }
  }
  if(index==6) {
   Item item=Item.getItemFromBlock(BigReactors.blockYelloriumFuelRod);
   return item==null?null:new ItemStack(item);
  }
  return null;
 }
 static boolean rod(ItemStack s) { return s!=null && s.getItem()==Item.getItemFromBlock(BigReactors.blockYelloriumFuelRod); }
 static net.minecraft.block.Block reactorPart() { return cpw.mods.fml.common.registry.GameRegistry.findBlock("BigReactors","BRReactorPart"); }
 static boolean part(ItemStack s,int meta) { return s!=null && s.getItem()==Item.getItemFromBlock(reactorPart()) && s.getItemDamage()==meta; }
 static int kind(ItemStack s) {
  if(s==null) return 0;
  if(part(s,0)) return PocketLayout.CASING;
  if(part(s,2)) return PocketLayout.CONTROL;
  if(part(s,1) || part(s,3) || part(s,4)) return PocketLayout.SHELL_PART;
  for(int id:OreDictionary.getOreIDs(s)) if("glassReactor".equals(OreDictionary.getOreName(id))) return PocketLayout.GLASS;
  return interior(s)?PocketLayout.INTERIOR:0;
 }
 static boolean fuel(ItemStack s) { return s!=null && Reactants.isFuel(s); }
 static boolean interior(ItemStack s) {
  if(LiquidCoolants.fluidForCell(s)!=null) return true;
  if(s==null || !(s.getItem() instanceof ItemBlock)) return false;
  if(rod(s)) return true;
  Block b=Block.getBlockFromItem(s.getItem());
  if(b==Blocks.glass || b==Blocks.ice || b==Blocks.snow) return true;
  for(int id:OreDictionary.getOreIDs(s)) if(ReactorInterior.getBlockData(OreDictionary.getOreName(id))!=null) return true;
  return false;
 }
}
