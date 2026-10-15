package dev.compactreactors;
import java.util.*;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
/** Item-conservation checks use real vanilla inventory merging and full-sized layouts. */
public final class ClearLayoutIntegrationChecks {
 private static void check(boolean ok,String message) { if(!ok) throw new AssertionError(message); }
 private static final class RecordingTile extends ReactorTile {
  final List<ItemStack> drops=new ArrayList<ItemStack>();
  @Override public void drop(ItemStack stack) { check(stack.stackSize>0 && stack.stackSize<=stack.getMaxStackSize(),"invalid drop stack size"); drops.add(stack.copy()); }
 }
 public static void run(ExpansionModule first,ExpansionModule second) {
  cpw.mods.fml.common.registry.GameRegistry.registerTileEntity(RecordingTile.class,"compactreactors:test_recording_reactor");
  for(int size:new int[]{5,7,9}) for(int capacity:new int[]{0,1,36}) {
   RecordingTile tile=new RecordingTile(); tile.size=size; tile.energy=314159; tile.insertion=40; tile.pocket=19; tile.resizeError=3;
   tile.setInventorySlotContents(PocketLayout.MODULE_1,new ItemStack(first));
   tile.setInventorySlotContents(PocketLayout.MODULE_2,new ItemStack(second));
   tile.setInventorySlotContents(PocketLayout.FUEL,new ItemStack(Items.redstone,12));
   tile.setInventorySlotContents(PocketLayout.WASTE,new ItemStack(Items.gold_ingot,7));
   for(int i=0;i<PocketLayout.CELLS;i++) if(PocketLayout.contains(i,size)) tile.setInventorySlotContents(i,new ItemStack(Blocks.iron_block));
   for(int y:new int[]{1,size-2}) {
    ItemStack bucket=new ItemStack(Items.water_bucket); NBTTagCompound tag=new NBTTagCompound(); tag.setString("cell",size+":"+y); bucket.setTagCompound(tag);
    tile.setInventorySlotContents(PocketLayout.slot(size-2,y,size-2),bucket);
   }
   InventoryPlayer inventory=new InventoryPlayer(null);
   if(capacity<36) {
    for(int i=0;i<36;i++) inventory.setInventorySlotContents(i,new ItemStack(Items.diamond,64));
    if(capacity==1) inventory.setInventorySlotContents(0,new ItemStack(Blocks.iron_block,63));
   }
   ReactorContainer container=new ReactorContainer(inventory,tile); container.setLayer(size-1);
   Map<String,Integer> expected=inventoryAndDrops(inventory,tile.drops);
   for(int i=0;i<PocketLayout.CELLS;i++) count(expected,tile.getStackInSlot(i));
   ItemStack[] reserved=new ItemStack[4]; for(int i=0;i<4;i++) reserved[i]=tile.getStackInSlot(PocketLayout.FUEL+i).copy();
   tile.deployed=true;
   check(!container.clearLayout() && tile.hasLayout() && tile.drops.isEmpty(),"assembled layout cleared");
   tile.deployed=false;
   check(container.clearLayout() && !tile.hasLayout(),"layout not completely cleared");
   check(expected.equals(inventoryAndDrops(inventory,tile.drops)),"item count, metadata or NBT changed during clear");
   for(int i=0;i<4;i++) check(ItemStack.areItemStacksEqual(reserved[i],tile.getStackInSlot(PocketLayout.FUEL+i)),"reserved fuel/waste/module changed");
   check(tile.size==size && tile.energy==314159 && tile.pocket==19 && tile.insertion==40 && tile.resizeError==0,"reactor settings changed");
   if(capacity==36) check(tile.drops.isEmpty(),"items dropped despite inventory space");
   else check(!tile.drops.isEmpty() && tile.drops.size()<=14,"overflow was lost or scattered into hundreds of entities");
   int dropCount=tile.drops.size(); check(container.clearLayout() && tile.drops.size()==dropCount,"repeated clear duplicated items");
   check(expected.equals(inventoryAndDrops(inventory,tile.drops)),"repeated clear changed items");
   NBTTagCompound saved=new NBTTagCompound(); tile.writeToNBT(saved); ReactorTile restored=new ReactorTile(); restored.readFromNBT(saved);
   check(!restored.hasLayout() && restored.size==size,"cleared layout returned after save/reload");
   check(tile.resize(size==5?7:5),"cleared layout still prevents resizing");
  }
  System.out.println("PASS: quick clear of every layer at all sizes, inventory/overflow item conservation and NBT, compact drops, deployed lock, repeat safety and reserved-slot preservation");
 }
 private static Map<String,Integer> inventoryAndDrops(InventoryPlayer inventory,List<ItemStack> drops) {
  Map<String,Integer> counts=new TreeMap<String,Integer>();
  for(int i=0;i<36;i++) count(counts,inventory.getStackInSlot(i));
  for(ItemStack stack:drops) count(counts,stack);
  return counts;
 }
 private static void count(Map<String,Integer> counts,ItemStack stack) {
  if(stack==null || stack.stackSize==0) return;
  String key=Item.getIdFromItem(stack.getItem())+":"+stack.getItemDamage()+":"+(stack.hasTagCompound()?stack.getTagCompound().toString():"");
  Integer previous=counts.get(key); counts.put(key,(previous==null?0:previous)+stack.stackSize);
 }
}
