package dev.compactreactors;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
public class ReactorTile extends TileEntity implements ISidedInventory, IEnergyProvider {
 public static final int CAPACITY=1000000;
 private final java.util.SortedMap<Integer,ItemStack> items=new java.util.TreeMap<Integer,ItemStack>();
 public boolean largePocket=false;
 public int limitX=32,limitY=32,limitZ=32;
 public boolean layoutPresent=false;
 public java.util.List<Integer> occupiedSlots() { return new java.util.ArrayList<Integer>(items.keySet()); }
 public java.util.List<Integer> occupiedLayout() { return new java.util.ArrayList<Integer>(items.headMap(PocketLayout.CELLS).keySet()); }
 public void refreshLimits() {
  int horizontal=erogenousbeef.bigreactors.common.BigReactors.maximumReactorSize,vertical=erogenousbeef.bigreactors.common.BigReactors.maximumReactorHeight;
  limitX=Math.min(CompactReactors.advancedMaxWidth,horizontal<0?64:horizontal);
  limitY=Math.min(CompactReactors.advancedMaxHeight,vertical<0?64:vertical);
  limitZ=Math.min(CompactReactors.advancedMaxDepth,horizontal<0?64:horizontal);
 }
 private boolean withinConfig(int x,int y,int z) { return !isAdvanced() || (x<=CompactReactors.advancedMaxWidth && y<=CompactReactors.advancedMaxHeight && z<=CompactReactors.advancedMaxDepth); }
 public boolean enabled=false, deployed=false;
 public int pocket=-1,energy=0,fuel=0,waste=0,insertion=0,production=0,status=0,heat=0;
 public int size=5,resizeError=0;
 public int width=5,height=5,depth=5;
 public boolean isAdvanced() { return false; }
 public int sizeX() { return isAdvanced()?width:size; }
 public int sizeY() { return isAdvanced()?height:size; }
 public int sizeZ() { return isAdvanced()?depth:size; }
 public boolean contains(int slot) { return PocketLayout.contains(slot,sizeX(),sizeY(),sizeZ()); }
 public boolean boundary(int slot) { return PocketLayout.boundary(slot,sizeX(),sizeY(),sizeZ()); }
 public String dimensions() { return sizeX()+"x"+sizeY()+"x"+sizeZ(); }
 private NativeReactor nativeReactor;
 // Status: 0 editing, 1 assembling/invalid, 2 ready, 3 running, 4 chunk ticket unavailable.
 private NativeReactor bridge() { if(nativeReactor==null) nativeReactor=new NativeReactor(this); return nativeReactor; }
 @Override public void updateEntity() {
  if(worldObj.isRemote) return;
  production=0;
  if(deployed) {
   if(!bridge().connect()) status=4;
   else {
    erogenousbeef.bigreactors.common.multiblock.MultiblockReactor r=bridge().controller();
    if(r==null || !r.isAssembled()) status=1;
    else {
     r.setAllControlRodInsertionValues(insertion);
     r.setActive(enabled);
     bridge().transfer();
     energy+=r.extractEnergy(ForgeDirection.UNKNOWN,CAPACITY-energy,false);
     bridge().markDirty();
     production=Math.max(0,(int)r.getEnergyGeneratedLastTick());
     fuel=r.getFuelAmount(); waste=r.getWasteAmount(); heat=(int)r.getFuelHeat();
     status=r.getActive()?3:2;
    }
   }
  } else status=0;
  for(ForgeDirection d:ForgeDirection.VALID_DIRECTIONS) {
   if(energy<=0) break;
   TileEntity te=worldObj.getTileEntity(xCoord+d.offsetX,yCoord+d.offsetY,zCoord+d.offsetZ);
   if(te instanceof IEnergyReceiver) {
    IEnergyReceiver r=(IEnergyReceiver)te;
    if(r.canConnectEnergy(d.getOpposite())) energy-=Math.max(0,Math.min(energy,r.receiveEnergy(d.getOpposite(),Math.min(10000,energy),false)));
   }
  }
  markDirty();
 }
 public void action(int id) {
  if(id==0 && deployed) enabled=!enabled;
  if(id==1) insertion=Math.max(0,insertion-10);
  if(id==2) insertion=Math.min(100,insertion+10);
  if(id==3 && !deployed) {
   if(!isAdvanced() && size>unlockedSize()) resizeError=2;
   else if(!withinConfig(sizeX(),sizeY(),sizeZ())) resizeError=5;
   else if(!withinNativeLimit(sizeX(),sizeY(),sizeZ())) resizeError=4;
   else if(bridge().connect()) { bridge().build(); deployed=true; enabled=false; resizeError=0; }
  }
  if(id==4 && deployed && !enabled) { bridge().destroy(true); deployed=false; enabled=false; fuel=0; waste=0; heat=0; }
  if(!isAdvanced() && (id==5 || id==6)) resize(size+(id==5?-2:2));
  if(isAdvanced() && id>=40 && id<=45) {
   int axis=(id-40)/2,delta=id%2==0?-1:1;
   resize(sizeX()+(axis==0?delta:0),sizeY()+(axis==1?delta:0),sizeZ()+(axis==2?delta:0));
  }
  markDirty();
 }
 public int unlockedSize() {
  if(isAdvanced()) return PocketLayout.MAX_SIZE;
  return !ExpansionModule.matches(getStackInSlot(PocketLayout.MODULE_1),1)?5:ExpansionModule.matches(getStackInSlot(PocketLayout.MODULE_2),2)?9:7;
 }
 public boolean canRemoveModule(int slot) {
  if(isAdvanced()) return !deployed;
  return !deployed && (slot==PocketLayout.MODULE_1?size==5 && getStackInSlot(PocketLayout.MODULE_2)==null:slot==PocketLayout.MODULE_2 && size<=7);
 }
 private boolean withinNativeLimit(int x,int y,int z) {
  int horizontal=erogenousbeef.bigreactors.common.BigReactors.maximumReactorSize;
  int vertical=erogenousbeef.bigreactors.common.BigReactors.maximumReactorHeight;
  return (horizontal<0 || (x<=horizontal && z<=horizontal)) && (vertical<0 || y<=vertical);
 }
 public boolean hasLayout() {
  if(worldObj!=null && worldObj.isRemote) return layoutPresent;
  return !items.headMap(PocketLayout.CELLS).isEmpty();
 }
 public boolean resize(int target) {
  return resize(target,target,target);
 }
 public boolean resize(int x,int y,int z) {
  resizeError=0;
  if(deployed) resizeError=1;
  else if(!PocketLayout.validShape(x,y,z)) resizeError=isAdvanced()?5:2;
  else if(!withinConfig(x,y,z)) resizeError=5;
  else if(!isAdvanced() && (x!=y || y!=z || !PocketLayout.validSize(x) || x>unlockedSize())) resizeError=2;
  else if(!withinNativeLimit(x,y,z)) resizeError=4;
  else if((x!=sizeX() || y!=sizeY() || z!=sizeZ()) && hasLayout()) resizeError=3;
  if(resizeError==0) { if(isAdvanced()) { width=x; height=y; depth=z; if(!hasLayout()) largePocket=true; } else size=x; }
  markDirty(); return resizeError==0;
 }
 public void drop(ItemStack s) { worldObj.spawnEntityInWorld(new net.minecraft.entity.item.EntityItem(worldObj,xCoord+.5,yCoord+.5,zCoord+.5,s)); }
 public void removed() { if(deployed) { bridge().destroy(true); deployed=false; } }
 @Override public void invalidate() { if(nativeReactor!=null) nativeReactor.close(); super.invalidate(); }
 @Override public void onChunkUnload() { if(nativeReactor!=null) nativeReactor.close(); super.onChunkUnload(); }
 @Override public int getSizeInventory() { return PocketLayout.INVENTORY; }
 @Override public ItemStack getStackInSlot(int i) { return items.get(i); }
 @Override public ItemStack decrStackSize(int i,int n) {
  ItemStack current=items.get(i); if(current==null || n<=0) return null;
  ItemStack s;
  if(current.stackSize<=n) { s=current; items.remove(i); } else s=current.splitStack(n);
  markDirty(); return s;
 }
 @Override public ItemStack getStackInSlotOnClosing(int i) { ItemStack s=items.remove(i); markDirty(); return s; }
 @Override public void setInventorySlotContents(int i,ItemStack s) { if(i<0 || i>=PocketLayout.INVENTORY) return; if(s==null || s.stackSize<=0) items.remove(i); else { s.stackSize=Math.min(s.stackSize,i<PocketLayout.CELLS || i>=PocketLayout.MODULE_1?1:Math.min(64,s.getMaxStackSize())); items.put(i,s); } markDirty(); }
 @Override public String getInventoryName() { return "tile.compactreactors.reactor.name"; }
 @Override public boolean hasCustomInventoryName() { return false; }
 @Override public int getInventoryStackLimit() { return 64; }
 @Override public boolean isUseableByPlayer(EntityPlayer p) { return worldObj.getTileEntity(xCoord,yCoord,zCoord)==this && p.getDistanceSq(xCoord+.5,yCoord+.5,zCoord+.5)<=64; }
 @Override public void openInventory() {}
 @Override public void closeInventory() {}
 @Override public boolean isItemValidForSlot(int i,ItemStack s) {
  if(i>=0 && i<PocketLayout.CELLS && LiquidCoolants.bulkContainer(s)) return false;
  if(i==PocketLayout.MODULE_1 || i==PocketLayout.MODULE_2)
   return !isAdvanced() && !deployed && ExpansionModule.matches(s,i==PocketLayout.MODULE_1?1:2) && (i==PocketLayout.MODULE_1 || ExpansionModule.matches(getStackInSlot(PocketLayout.MODULE_1),1));
  return i>=0 && i<PocketLayout.CELLS ? !deployed && PocketLayout.accepts(i,Parts.kind(s),sizeX(),sizeY(),sizeZ()) : i==PocketLayout.FUEL && Parts.fuel(s);
 }
 @Override public int[] getAccessibleSlotsFromSide(int side) { return new int[]{PocketLayout.FUEL,PocketLayout.WASTE}; }
 @Override public boolean canInsertItem(int i,ItemStack s,int side) { return i==PocketLayout.FUEL && Parts.fuel(s); }
 @Override public boolean canExtractItem(int i,ItemStack s,int side) { return i==PocketLayout.WASTE; }
 @Override public boolean canConnectEnergy(ForgeDirection side) { return side!=ForgeDirection.UNKNOWN; }
 @Override public int extractEnergy(ForgeDirection side,int max,boolean simulate) {
  if(!canConnectEnergy(side)) return 0;
  int n=Math.min(energy,Math.max(0,Math.min(10000,max))); if(!simulate && n>0) { energy-=n; markDirty(); } return n;
 }
 @Override public int getEnergyStored(ForgeDirection side) { return energy; }
 @Override public int getMaxEnergyStored(ForgeDirection side) { return CAPACITY; }
 @Override public void writeToNBT(NBTTagCompound n) {
  super.writeToNBT(n); NBTTagList list=new NBTTagList();
  for(int i:items.keySet()) { NBTTagCompound t=new NBTTagCompound(); t.setInteger("Slot",i); items.get(i).writeToNBT(t); list.appendTag(t); }
  n.setInteger("LayoutVersion",5); n.setBoolean("LargePocket",largePocket); n.setInteger("Size",size); n.setInteger("SizeX",sizeX()); n.setInteger("SizeY",sizeY()); n.setInteger("SizeZ",sizeZ()); n.setTag("Items",list); n.setInteger("Energy",energy); n.setInteger("Fuel",fuel); n.setInteger("Insertion",insertion); n.setBoolean("Enabled",enabled); n.setBoolean("Deployed",deployed); n.setInteger("Pocket",pocket);
 }
 @Override public void readFromNBT(NBTTagCompound n) {
  super.readFromNBT(n); deployed=false; items.clear();
  int version=n.getInteger("LayoutVersion"); boolean legacy=version<2;
  largePocket=isAdvanced() && version>=5 && n.getBoolean("LargePocket");
  size=version>=3 && PocketLayout.validSize(n.getInteger("Size"))?n.getInteger("Size"):5;
  width=height=depth=5;
  if(isAdvanced() && version>=4 && PocketLayout.validShape(n.getInteger("SizeX"),n.getInteger("SizeY"),n.getInteger("SizeZ"))) {
   width=n.getInteger("SizeX"); height=n.getInteger("SizeY"); depth=n.getInteger("SizeZ");
  }
  resizeError=0;
  // Preserve existing 0.1.0 reactors, including the previously implicit shell.
  if(legacy) {
   for(int y=0;y<5;y++) for(int z=0;z<5;z++) for(int x=0;x<5;x++) { int i=PocketLayout.slot(x,y,z); if(PocketLayout.boundary(i)) setInventorySlotContents(i,new ItemStack(Parts.reactorPart(),1,0)); }
   setInventorySlotContents(PocketLayout.slot(2,1,0),new ItemStack(Parts.reactorPart(),1,1));
   setInventorySlotContents(PocketLayout.slot(0,1,2),new ItemStack(Parts.reactorPart(),1,4));
   setInventorySlotContents(PocketLayout.slot(4,1,2),new ItemStack(Parts.reactorPart(),1,3));
  }
  NBTTagList list=n.getTagList("Items",10);
  for(int j=0;j<list.tagCount();j++) {
   NBTTagCompound t=list.getCompoundTagAt(j); int i=version>=5?t.getInteger("Slot"):version>=3?t.getShort("Slot")&65535:t.getByte("Slot")&255;
   if(legacy) { if(i>=38) continue; i=PocketLayout.legacySlot(i); }
   else if(version==2) i=PocketLayout.versionTwoSlot(i);
   else if(version<5) i=PocketLayout.versionFourSlot(i);
   if(i>=0 && i<PocketLayout.INVENTORY) {
    ItemStack stack=ItemStack.loadItemStackFromNBT(t);
    // Restore before validating modules: entries may arrive in any order. Keep saved materials intact.
    if(stack!=null) setInventorySlotContents(i,stack);
   }
  }
  energy=Math.max(0,Math.min(CAPACITY,n.getInteger("Energy"))); fuel=Math.max(0,n.getInteger("Fuel")); insertion=Math.max(0,Math.min(100,n.getInteger("Insertion"))); enabled=n.getBoolean("Enabled"); deployed=n.getBoolean("Deployed"); pocket=n.hasKey("Pocket")?n.getInteger("Pocket"):-1;
 }
}
