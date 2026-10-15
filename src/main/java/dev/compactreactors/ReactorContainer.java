package dev.compactreactors;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
public class ReactorContainer extends Container {
 public final ReactorTile tile;
 public static final int PAGE_CELLS=100,FUEL_SLOT=100,WASTE_SLOT=101,MODULE_1_SLOT=102,MODULE_2_SLOT=103,PLAYER_START=104;
 public int layer=0,column=0,row=0;
 private String layoutShape="";
 public ReactorContainer(InventoryPlayer inv,ReactorTile tile) {
  this.tile=tile;
  for(int i=0;i<PAGE_CELLS;i++) addSlotToContainer(new ReactorCellSlot(this,i));
  addSlotToContainer(new Slot(tile,PocketLayout.FUEL,184,100) { @Override public boolean isItemValid(ItemStack s) { return Parts.fuel(s); } });
  addSlotToContainer(new Slot(tile,PocketLayout.WASTE,248,100) { @Override public boolean isItemValid(ItemStack s) { return false; } });
  for(int i=0;i<2;i++) { final int index=PocketLayout.MODULE_1+i;
   addSlotToContainer(new Slot(tile,index,260+i*24,170) {
    @Override public int getSlotStackLimit() { return 1; }
    @Override public boolean isItemValid(ItemStack s) { return tile.isItemValidForSlot(index,s); }
    @Override public boolean canTakeStack(EntityPlayer p) { return tile.canRemoveModule(index); }
   });
  }
  for(int y=0;y<3;y++) for(int x=0;x<9;x++) addSlotToContainer(new Slot(inv,x+y*9+9,78+x*18,152+y*18));
  for(int x=0;x<9;x++) addSlotToContainer(new Slot(inv,x,78+x*18,210));
  refreshLayout();
 }
 public void setLayer(int n) {
  layer=Math.max(0,Math.min(tile.sizeY()-1,n)); refreshLayout();
 }
 public int cellForPage(int page) {
  int x=column+page%10,z=row+page/10;
  return page>=0 && page<PAGE_CELLS && x<tile.sizeX() && z<tile.sizeZ()?PocketLayout.slot(x,layer,z):-1;
 }
 public Slot slotForCell(int index) {
  int x=PocketLayout.x(index)-column,z=PocketLayout.z(index)-row;
  return PocketLayout.y(index)==layer && x>=0 && x<10 && z>=0 && z<10?(Slot)inventorySlots.get(z*10+x):null;
 }
 public void setView(int x,int z) { column=Math.max(0,Math.min(tile.sizeX()-10,x)); row=Math.max(0,Math.min(tile.sizeZ()-10,z)); refreshLayout(); }
 public void refreshLayout() {
  layoutShape=tile.dimensions();
  layer=Math.max(0,Math.min(tile.sizeY()-1,layer));
  column=Math.max(0,Math.min(tile.sizeX()-10,column)); row=Math.max(0,Math.min(tile.sizeZ()-10,row));
  int shift=ReactorScreenLayout.shift(tile);
  for(int i=0;i<PAGE_CELLS;i++) {
   Slot slot=(Slot)inventorySlots.get(i);
   slot.xDisplayPosition=12+(Math.max(9,Math.min(10,tile.sizeX()))-Math.min(10,tile.sizeX()))*9+(i%10)*18;
   slot.yDisplayPosition=cellForPage(i)>=0?36+ReactorScreenLayout.header(tile)+(i/10)*18:-1000;
  }
  int extra=ReactorScreenLayout.extra(tile),header=ReactorScreenLayout.header(tile);
  ((Slot)inventorySlots.get(FUEL_SLOT)).yDisplayPosition=100+header; ((Slot)inventorySlots.get(FUEL_SLOT)).xDisplayPosition=184+shift;
  ((Slot)inventorySlots.get(WASTE_SLOT)).yDisplayPosition=100+header; ((Slot)inventorySlots.get(WASTE_SLOT)).xDisplayPosition=248+shift;
  for(int i=0;i<2;i++) { Slot slot=(Slot)inventorySlots.get(MODULE_1_SLOT+i); slot.yDisplayPosition=tile.isAdvanced()?-1000:170+extra; slot.xDisplayPosition=260+i*24+shift; }
  for(int i=0;i<36;i++) { Slot slot=(Slot)inventorySlots.get(PLAYER_START+i); slot.yDisplayPosition=(i<27?152+(i/9)*18:210)+extra; slot.xDisplayPosition=78+(i%9)*18+shift/2; }
 }
 @Override public boolean canInteractWith(EntityPlayer p) { return tile.isUseableByPlayer(p); }
 @Override public boolean enchantItem(EntityPlayer p,int id) {
  if(!canInteractWith(p)) return false;
  if(id>=10 && id<=18) setLayer(id-10);
  else if(id==30 || id==31) setLayer(layer+(id==30?-1:1));
  else if(id>=50 && id<=53) setView(column+(id==50?-10:id==51?10:0),row+(id==52?-10:id==53?10:0));
  else if(id>=0 && id<=6) { tile.action(id); refreshLayout(); }
  else if(id>=40 && id<=45 && tile.isAdvanced()) { tile.action(id); refreshLayout(); }
  else if(id==7) return clearLayout();
  else return false;
  if((id==30 || id==31 || id>=40) && p instanceof net.minecraft.entity.player.EntityPlayerMP)
   ((net.minecraft.entity.player.EntityPlayerMP)p).sendContainerToPlayer(this);
  return true;
 }
 // Return every layer, including off-screen cells. Only the server button handler invokes this.
 boolean clearLayout() {
  if(tile.deployed) return false;
  java.util.List<ItemStack> overflow=new java.util.ArrayList<ItemStack>();
  for(int i:tile.occupiedLayout()) {
   ItemStack stack=tile.decrStackSize(i,Integer.MAX_VALUE);
   if(stack==null) continue;
   mergeItemStack(stack,PLAYER_START,inventorySlots.size(),false);
   for(ItemStack pending:overflow) {
    if(stack.stackSize<=0) break;
    if(!pending.isItemEqual(stack) || !ItemStack.areItemStackTagsEqual(pending,stack)) continue;
    int moved=Math.min(stack.stackSize,Math.min(64,pending.getMaxStackSize())-pending.stackSize);
    if(moved>0) { pending.stackSize+=moved; stack.stackSize-=moved; }
   }
   if(stack.stackSize>0) overflow.add(stack);
  }
  for(ItemStack stack:overflow) tile.drop(stack);
  tile.resizeError=0; tile.markDirty();
  return true;
 }
 // Shift-click fills only eligible cells of the currently visible layer.
 @Override public ItemStack slotClick(int index,int button,int mode,EntityPlayer player) {
  ItemStack held=player.inventory.getItemStack();
  if(index>=PLAYER_START && index<inventorySlots.size() && mode==0 && (button==0 || button==1) && LiquidCoolants.bulkContainer(held)) {
   Slot slot=(Slot)inventorySlots.get(index); ItemStack portions=slot.getStack();
   if(portions!=null && portions.getItem() instanceof CoolantCell) {
    ItemStack original=portions.copy();
    refillInventoryTank(held,index,button==1?1:portions.stackSize);
    return original;
   }
  }
  if(index>=0 && index<PAGE_CELLS && mode==0 && (button==0 || button==1) && LiquidCoolants.bulkContainer(held)) {
   ItemStack previous=((Slot)inventorySlots.get(index)).getStack();
   if(!tile.deployed) {
    if(previous==null) fillFromTank(held,index);
    else refillTank(held,index);
   }
   return previous==null?null:previous.copy();
  }
  return super.slotClick(index,button,mode,player);
 }
 boolean refillInventoryTank(ItemStack tank,int index,int maximum) {
  if(index<PLAYER_START || index>=inventorySlots.size()) return false;
  Slot slot=(Slot)inventorySlots.get(index); ItemStack portions=slot.getStack();
  if(LiquidCoolants.recoverPortions(tank,portions,maximum)==0) return false;
  if(portions.stackSize==0) slot.putStack(null); else slot.onSlotChanged();
  return true;
 }
 boolean refillTank(ItemStack tank,int page) {
  if(tile.deployed || page<0 || page>=PAGE_CELLS || cellForPage(page)<0) return false;
  Slot slot=(Slot)inventorySlots.get(page); ItemStack refilled=LiquidCoolants.refillPortion(tank,slot.getStack());
  if(refilled==null) return false;
  LiquidCoolants.commitTank(tank,refilled); slot.putStack(null); return true;
 }
 int fillFromTank(ItemStack tank,int page) {
  if(tile.deployed || !LiquidCoolants.bulkContainer(tank)) return 0;
  int filled=0,start=page<0?0:page,end=page<0?PAGE_CELLS:page+1;
  for(int i=start;i<end;i++) {
   if(i<0 || i>=PAGE_CELLS || cellForPage(i)<0) continue;
   Slot slot=(Slot)inventorySlots.get(i);
   if(slot.getHasStack() || tile.boundary(cellForPage(i))) continue;
   LiquidCoolants.Pour pour=LiquidCoolants.drainPortion(tank);
   if(pour==null) break;
   if(!slot.isItemValid(pour.cell)) continue;
   LiquidCoolants.commitTank(tank,pour.tank); slot.putStack(pour.cell); filled++;
  }
  return filled;
 }
 @Override public ItemStack transferStackInSlot(EntityPlayer player,int index) {
  if(index<0 || index>=inventorySlots.size()) return null;
  Slot source=(Slot)inventorySlots.get(index);
  if(!source.getHasStack() || !source.canTakeStack(player)) return null;
  ItemStack stack=source.getStack(),original=stack.copy();
  if(index>=PLAYER_START && LiquidCoolants.bulkContainer(stack)) {
   if(fillFromTank(stack,-1)==0) return null;
   source.onSlotChanged(); return original;
  }
  if(index<PLAYER_START) {
   if(!mergeItemStack(stack,PLAYER_START,inventorySlots.size(),true)) return null;
  } else if(ExpansionModule.matches(stack,1)) insert(stack,MODULE_1_SLOT);
  else if(ExpansionModule.matches(stack,2)) insert(stack,MODULE_2_SLOT);
  else if(Parts.fuel(stack)) insert(stack,FUEL_SLOT);
  else for(int i=0;i<PAGE_CELLS && stack.stackSize>0;i++) if(cellForPage(i)>=0) insert(stack,i);
  if(stack.stackSize==original.stackSize) return null;
  if(stack.stackSize==0) source.putStack(null); else source.onSlotChanged();
  source.onPickupFromSlot(player,stack);
  return original;
 }
 private void insert(ItemStack source,int index) {
  Slot dest=(Slot)inventorySlots.get(index);
  if(!dest.isItemValid(source)) return;
  ItemStack current=dest.getStack();
  if(current!=null && (!current.isItemEqual(source) || !ItemStack.areItemStackTagsEqual(current,source))) return;
  int limit=Math.min(dest.getSlotStackLimit(),source.getMaxStackSize());
  int amount=Math.min(source.stackSize,limit-(current==null?0:current.stackSize));
  if(amount<=0) return;
  if(current==null) { current=source.copy(); current.stackSize=amount; dest.putStack(current); }
  else { current.stackSize+=amount; dest.onSlotChanged(); }
  source.stackSize-=amount;
 }
 @Override public void addCraftingToCrafters(ICrafting crafter) {
  super.addCraftingToCrafters(crafter);
  if(crafter instanceof net.minecraft.entity.player.EntityPlayerMP)
   CatalogNetwork.send(windowId,(net.minecraft.entity.player.EntityPlayerMP)crafter);
 }
 @Override public void detectAndSendChanges() {
  if(!layoutShape.equals(tile.dimensions())) refreshLayout();
  tile.refreshLimits();
  super.detectAndSendChanges();
  int[] values={tile.energy,tile.fuel,tile.waste,tile.insertion,tile.production,tile.status,tile.heat,tile.enabled?1:0,tile.deployed?1:0,tile.size,tile.resizeError,tile.sizeX(),tile.sizeY(),tile.sizeZ(),tile.limitX,tile.limitY,tile.limitZ,tile.hasLayout()?1:0};
  for(Object o:crafters) { ICrafting c=(ICrafting)o; for(int i=0;i<values.length;i++) { c.sendProgressBarUpdate(this,i*2,values[i]&65535); c.sendProgressBarUpdate(this,i*2+1,values[i]>>>16); } }
 }
 private final int[] halves=new int[36];
 @Override public void updateProgressBar(int id,int value) {
  if(id<0 || id>=halves.length) return;
  halves[id]=value&65535; int v=halves[(id/2)*2] | (halves[(id/2)*2+1]<<16);
  switch(id/2) { case 0:tile.energy=v;break;case 1:tile.fuel=v;break;case 2:tile.waste=v;break;
   case 3:tile.insertion=v;break;case 4:tile.production=v;break;case 5:tile.status=v;break;
   case 6:tile.heat=v;break;case 7:tile.enabled=v!=0;break;case 8:tile.deployed=v!=0;break;
   case 9:if(PocketLayout.validSize(v)) { tile.size=v; refreshLayout(); } break;case 10:tile.resizeError=v;break;
   case 11:if(tile.isAdvanced() && PocketLayout.validExtent(v)) { tile.width=v; refreshLayout(); } break;
   case 12:if(tile.isAdvanced() && PocketLayout.validExtent(v)) { tile.height=v; refreshLayout(); } break;
   case 13:if(tile.isAdvanced() && PocketLayout.validExtent(v)) { tile.depth=v; refreshLayout(); } break;
   case 14:tile.limitX=v;break;case 15:tile.limitY=v;break;case 16:tile.limitZ=v;break;case 17:tile.layoutPresent=v!=0;break; }
 }
}
