package dev.compactreactors;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
/** One portion and one empty bucket; the registry supplies the actual fluid bucket. */
public final class CoolantBucketRecipe implements IRecipe {
 @Override public boolean matches(InventoryCrafting grid,World world) { return getCraftingResult(grid)!=null; }
 @Override public ItemStack getCraftingResult(InventoryCrafting grid) {
  ItemStack portion=null; boolean bucket=false;
  for(int i=0;i<grid.getSizeInventory();i++) {
   ItemStack stack=grid.getStackInSlot(i); if(stack==null) continue;
   if(stack.stackSize<=0) return null;
   if(stack.getItem() instanceof CoolantCell && portion==null) portion=stack;
   else if(stack.getItem()==Items.bucket && !bucket) bucket=true;
   else return null;
  }
  return bucket?LiquidCoolants.filledBucket(portion):null;
 }
 @Override public int getRecipeSize() { return 2; }
 @Override public ItemStack getRecipeOutput() { return null; }
}
