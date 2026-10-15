package dev.compactreactors;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;
/** The network slot number stays below 100; its backing storage coordinate may be much larger. */
final class ReactorCellSlot extends Slot {
 private final ReactorContainer container;
 private final int page;
 private final boolean client;
 private ItemStack displayed;
 ReactorCellSlot(ReactorContainer container,int page) {
  super(container.tile,0,12,-1000); this.container=container; this.page=page;
  client=container.tile.getWorldObj()!=null && container.tile.getWorldObj().isRemote;
 }
 @Override public int getSlotIndex() { return container.cellForPage(page); }
 @Override public ItemStack getStack() { return client?displayed:container.tile.getStackInSlot(getSlotIndex()); }
 @Override public void putStack(ItemStack stack) { if(client) displayed=stack; else if(getSlotIndex()>=0) container.tile.setInventorySlotContents(getSlotIndex(),stack); onSlotChanged(); }
 @Override public ItemStack decrStackSize(int amount) {
  if(!client) return container.tile.decrStackSize(getSlotIndex(),amount);
  if(displayed==null) return null;
  if(displayed.stackSize<=amount) { ItemStack result=displayed; displayed=null; return result; }
  return displayed.splitStack(amount);
 }
 @Override public int getSlotStackLimit() { return 1; }
 @Override public boolean isItemValid(ItemStack stack) { return getSlotIndex()>=0 && !LiquidCoolants.bulkContainer(stack) && container.tile.isItemValidForSlot(getSlotIndex(),stack); }
 @Override public boolean canTakeStack(EntityPlayer player) { return getSlotIndex()>=0 && !container.tile.deployed; }
 @Override public boolean isSlotInInventory(net.minecraft.inventory.IInventory inventory,int index) { return inventory==container.tile && index==getSlotIndex(); }
}
