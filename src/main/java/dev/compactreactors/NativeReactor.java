package dev.compactreactors;
import java.util.List;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorPartBase;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorAccessPort;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
final class NativeReactor {
 private final ReactorTile owner;
 private ForgeChunkManager.Ticket ticket;
 private WorldServer world;
 private int x,z;
 private int controllerIndex=-1;
 private final java.util.List<Integer> accessIndices=new java.util.ArrayList<Integer>();
 NativeReactor(ReactorTile owner) { this.owner=owner; }
 boolean connect() {
  if(owner.pocket<0) { owner.pocket=PocketIds.allocate(); owner.markDirty(); }
  if(world==null) {
   if(DimensionManager.getWorld(CompactReactors.dimension)==null) DimensionManager.initDimension(CompactReactors.dimension);
   world=DimensionManager.getWorld(CompactReactors.dimension);
   if(world==null) return false;
   int cx=PocketLayout.serviceChunkX(owner.pocket,owner.largePocket), cz=PocketLayout.serviceChunkZ(owner.pocket,owner.largePocket);
   x=cx*16+4; z=cz*16+4;
   ticket=ForgeChunkManager.requestTicket(CompactReactors.instance,world,ForgeChunkManager.Type.NORMAL);
   if(ticket==null) { world=null; return false; }
   int spanX=PocketLayout.chunkSpan(owner.sizeX()),spanZ=PocketLayout.chunkSpan(owner.sizeZ());
   if(ticket.getChunkListDepth()>0 && spanX*spanZ>ticket.getChunkListDepth()) { ForgeChunkManager.releaseTicket(ticket); ticket=null; world=null; return false; }
   for(int dz=0;dz<spanZ;dz++) for(int dx=0;dx<spanX;dx++) ForgeChunkManager.forceChunk(ticket,new ChunkCoordIntPair(cx+dx,cz+dz));
   controllerIndex=-1; accessIndices.clear();
   for(int i:owner.occupiedLayout()) {
    if(controllerIndex<0 && Parts.part(owner.getStackInSlot(i),1)) controllerIndex=i;
    if(Parts.part(owner.getStackInSlot(i),4)) accessIndices.add(i);
   }
  }
  return true;
 }
 MultiblockReactor controller() {
  if(world==null) return null;
  if(controllerIndex>=0) { int i=controllerIndex;
   TileEntity t=world.getTileEntity(x+PocketLayout.x(i),64+PocketLayout.y(i),z+PocketLayout.z(i));
   if(t instanceof TileEntityReactorPartBase) {
    MultiblockReactor r=((TileEntityReactorPartBase)t).getReactorController(); if(r!=null) return r;
   }
  }
  return null;
 }
 private java.util.List<TileEntityReactorAccessPort> ports() {
  java.util.List<TileEntityReactorAccessPort> ports=new java.util.ArrayList<TileEntityReactorAccessPort>();
  if(world!=null) for(int i:accessIndices) {
   TileEntity t=world.getTileEntity(x+PocketLayout.x(i),64+PocketLayout.y(i),z+PocketLayout.z(i));
   if(t instanceof TileEntityReactorAccessPort) ports.add((TileEntityReactorAccessPort)t);
  }
  return ports;
 }
 void build() {
  // Enclose the structure before placing source liquids; ticking remains native to BR/Forge.
  for(int dy=0;dy<owner.sizeY();dy++) for(int dz=0;dz<owner.sizeZ();dz++) for(int dx=0;dx<owner.sizeX();dx++) { int i=PocketLayout.slot(dx,dy,dz);
   ItemStack stack=owner.contains(i)?owner.getStackInSlot(i):null;
   Block liquid=LiquidCoolants.blockForCell(stack);
   Block block=stack==null || liquid!=null?Blocks.air:Block.getBlockFromItem(stack.getItem());
   int meta=stack==null || liquid!=null?0:stack.getItem().getMetadata(stack.getItemDamage());
   world.setBlock(x+PocketLayout.x(i),64+PocketLayout.y(i),z+PocketLayout.z(i),block,meta,3);
  }
  for(int i:owner.occupiedLayout()) {
   Block liquid=owner.contains(i)?LiquidCoolants.blockForCell(owner.getStackInSlot(i)):null;
   if(liquid!=null) world.setBlock(x+PocketLayout.x(i),64+PocketLayout.y(i),z+PocketLayout.z(i),liquid,0,3);
  }
 }
 void transfer() {
  for(TileEntityReactorAccessPort p:ports()) {
  ItemStack in=owner.getStackInSlot(PocketLayout.FUEL), target=p.getStackInSlot(0);
  if(in!=null && p.isItemValidForSlot(0,in) && (target==null || same(in,target))) {
   int n=Math.min(in.stackSize,target==null?Math.min(64,in.getMaxStackSize()):Math.min(64,target.getMaxStackSize())-target.stackSize);
   if(n>0) { if(target==null) { target=in.copy(); target.stackSize=n; } else target.stackSize+=n;
    owner.decrStackSize(PocketLayout.FUEL,n); p.setInventorySlotContents(0,target); p.markDirty(); }
  }
  ItemStack out=p.getStackInSlot(1), dest=owner.getStackInSlot(PocketLayout.WASTE);
  if(out!=null && (dest==null || same(out,dest))) {
   int n=Math.min(out.stackSize,dest==null?Math.min(64,out.getMaxStackSize()):Math.min(64,dest.getMaxStackSize())-dest.stackSize);
   if(n>0) { ItemStack moved=p.decrStackSize(1,n); if(dest==null) owner.setInventorySlotContents(PocketLayout.WASTE,moved); else { dest.stackSize+=n; owner.markDirty(); } p.markDirty(); }
  }
 }
 }
 private boolean same(ItemStack a,ItemStack b) { return a.isItemEqual(b) && ItemStack.areItemStackTagsEqual(a,b); }
 // Drain native inventories before removing the service structure. BR owns tank accounting.
 void destroy(boolean drops) {
  if(!connect()) return;
  MultiblockReactor r=controller();
  if(r!=null) {
   r.setActive(false);
   for(int i=0;i<4;i++) { for(TileEntityReactorAccessPort p:ports()) r.ejectWaste(false,new erogenousbeef.core.common.CoordTriplet(p.xCoord,p.yCoord,p.zCoord)); drainPorts(drops); r.ejectFuel(false,null); drainPorts(drops); }
   r.ejectWaste(true,null); r.ejectFuel(true,null);
  }
  drainPorts(drops);
  for(int dy=0;dy<owner.sizeY();dy++) for(int dz=0;dz<owner.sizeZ();dz++) for(int dx=0;dx<owner.sizeX();dx++) world.setBlockToAir(x+dx,64+dy,z+dz);
  close();
 }
 private void drainPorts(boolean drops) {
  for(TileEntityReactorAccessPort p:ports()) {
  for(int i=0;i<p.getSizeInventory();i++) { ItemStack s=p.getStackInSlot(i); if(s!=null) { if(drops) owner.drop(s.copy()); p.setInventorySlotContents(i,null); } }
 }
 }
 void markDirty() {
  if(world!=null) for(int dz=0;dz<PocketLayout.chunkSpan(owner.sizeZ());dz++) for(int dx=0;dx<PocketLayout.chunkSpan(owner.sizeX());dx++) world.getChunkFromBlockCoords(x+dx*16,z+dz*16).setChunkModified();
 }
 void close() {
  MultiblockReactor r=controller(); if(r!=null) { r.setActive(false); markDirty(); }
  if(ticket!=null) ForgeChunkManager.releaseTicket(ticket);
  ticket=null; world=null; controllerIndex=-1; accessIndices.clear();
 }
 static class RestoreTickets implements ForgeChunkManager.LoadingCallback {
  @Override public void ticketsLoaded(List<ForgeChunkManager.Ticket> tickets,World w) {
   for(ForgeChunkManager.Ticket t:tickets) ForgeChunkManager.releaseTicket(t);
  }
 }
}
