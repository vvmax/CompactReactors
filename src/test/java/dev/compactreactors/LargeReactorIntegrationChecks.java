package dev.compactreactors;
import erogenousbeef.bigreactors.common.BigReactors;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S30PacketWindowItems;
import net.minecraftforge.common.config.Configuration;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.util.*;
public final class LargeReactorIntegrationChecks {
 private static void check(boolean ok,String message) { if(!ok) throw new AssertionError(message); }
 public static void run() throws Exception {
  int oldX=CompactReactors.advancedMaxWidth,oldY=CompactReactors.advancedMaxHeight,oldZ=CompactReactors.advancedMaxDepth,oldDimension=CompactReactors.dimension;
  int oldNativeX=BigReactors.maximumReactorSize,oldNativeY=BigReactors.maximumReactorHeight;
  try {
   config(); BigReactors.maximumReactorSize=64; BigReactors.maximumReactorHeight=64;
   CompactReactors.advancedMaxWidth=32; CompactReactors.advancedMaxHeight=32; CompactReactors.advancedMaxDepth=32;
   AdvancedReactorTile tile=new AdvancedReactorTile(); check(tile.resize(32,32,32),"default large dimensions rejected");
   check(!tile.resize(33,32,32) && tile.resizeError==5,"configured cap ignored");
   CompactReactors.advancedMaxWidth=24; CompactReactors.advancedMaxHeight=48; CompactReactors.advancedMaxDepth=16;
   check(tile.resize(24,48,16),"asymmetric configuration ignored"); tile.refreshLimits();
   check(tile.limitX==24 && tile.limitY==48 && tile.limitZ==16,"server cap snapshot wrong");
   check(!tile.resize(25,48,16) && !tile.resize(24,49,16) && !tile.resize(24,48,17),"per-axis configuration bypassed");
   BigReactors.maximumReactorSize=20; BigReactors.maximumReactorHeight=40; tile.refreshLimits();
   check(tile.limitX==20 && tile.limitY==40 && tile.limitZ==16,"native cap not reflected on client limits");
   check(tile.resize(20,40,16) && !tile.resize(21,40,16) && tile.resizeError==4,"native max ignored");
   BigReactors.maximumReactorSize=64; BigReactors.maximumReactorHeight=64;
   CompactReactors.advancedMaxWidth=64; CompactReactors.advancedMaxHeight=64; CompactReactors.advancedMaxDepth=64;
   paging(); savesAndTransfer(); legacy(); allocation();
  } finally {
   CompactReactors.advancedMaxWidth=oldX; CompactReactors.advancedMaxHeight=oldY; CompactReactors.advancedMaxDepth=oldZ; CompactReactors.dimension=oldDimension;
   BigReactors.maximumReactorSize=oldNativeX; BigReactors.maximumReactorHeight=oldNativeY;
  }
  System.out.println("PASS: configurable 32/64 axis limits, 10x10 pages covering every 64-cube cell, high-index integer NBT, bounded vanilla packets, old service origins and multi-chunk isolation");
 }
 private static void config() throws Exception {
  java.lang.reflect.Field home=cpw.mods.fml.relauncher.FMLInjectionData.class.getDeclaredField("minecraftHome"); home.setAccessible(true); home.set(null,new File("build/large-tests"));
  File file=new File("build/large-tests/compactreactors.cfg"); file.getParentFile().mkdirs();
  Configuration configuration=new Configuration(file); configuration.load();
  configuration.get("advanced","maxWidth",32).set(48);
  configuration.get("advanced","maxHeight",32).set(1000);
  configuration.get("advanced","maxDepth",32).set(2); configuration.save();
  CompactReactors.readConfiguration(new Configuration(file));
  check(CompactReactors.advancedMaxWidth==48 && CompactReactors.advancedMaxHeight==64 && CompactReactors.advancedMaxDepth==5,"Forge configuration not loaded or bounded");
 }
 private static void paging() {
  for(int[] shape:new int[][]{{10,10,10},{11,12,13},{32,20,24},{64,64,64},{6,16,32},{32,6,6}}) {
   AdvancedReactorTile tile=new AdvancedReactorTile(); check(tile.resize(shape[0],shape[1],shape[2]),"large shape rejected");
   ReactorContainer container=new ReactorContainer(new InventoryPlayer(null),tile);
   check(container.inventorySlots.size()==140,"container grows with storage volume");
   check(ReactorScreenLayout.paged(tile)==(shape[0]>10 || shape[2]>10),"paging threshold wrong");
   boolean[] seen=new boolean[PocketLayout.CELLS];
   for(int y=0;y<shape[1];y++) for(int z=0;z<shape[2];z+=10) for(int x=0;x<shape[0];x+=10) {
    container.setLayer(y); container.setView(x,z); int shown=0;
    for(int page=0;page<100;page++) {
     int index=container.cellForPage(page); Slot slot=(Slot)container.inventorySlots.get(page);
     check(slot.getSlotIndex()==index,"backing coordinate disagrees with page mapping");
     if(index<0) { check(!slot.canTakeStack(null) && slot.yDisplayPosition<0,"inactive page slot accessible"); continue; }
     seen[index]=true; shown++;
     check(tile.contains(index) && PocketLayout.y(index)==y,"page contains a wrong layer");
     check(slot.xDisplayPosition>=12 && slot.xDisplayPosition+16<=192 && slot.yDisplayPosition>=36+ReactorScreenLayout.header(tile),"page outside grid");
     check(slot.yDisplayPosition+16<130+ReactorScreenLayout.extra(tile),"page overlaps buttons");
    }
    check(shown==Math.min(10,shape[0])*Math.min(10,shape[2]),"wrong visible page size");
   }
   int count=0; for(boolean visited:seen) if(visited) count++;
   check(count==shape[0]*shape[1]*shape[2],"some large cells are unreachable");
   container.setLayer(1000); container.setView(1000,1000);
   check(container.layer==shape[1]-1 && container.column==Math.max(0,shape[0]-10) && container.row==Math.max(0,shape[2]-10),"page clamp incorrect");
   for(Object object:container.inventorySlots) {
    Slot slot=(Slot)object; if(slot.yDisplayPosition<0) continue;
    check(slot.slotNumber<140 && slot.xDisplayPosition+16<ReactorScreenLayout.width(tile) && slot.yDisplayPosition+16<ReactorScreenLayout.height(tile),"network slot or position exceeds bounds");
   }
  }
 }
 private static void savesAndTransfer() throws Exception {
  AdvancedReactorTile tile=new AdvancedReactorTile(); tile.resize(64,64,64); tile.energy=12345; tile.pocket=71;
  int far=PocketLayout.slot(62,62,62),last=PocketLayout.slot(63,63,63);
  check(far>65535 && last==PocketLayout.CELLS-1,"test does not cover high indices");
  ItemStack water=new ItemStack(Items.water_bucket); water.setTagCompound(new NBTTagCompound()); water.getTagCompound().setString("marker","64-cube");
  ItemStack block=new ItemStack(Blocks.iron_block); block.setTagCompound(new NBTTagCompound()); block.getTagCompound().setString("marker","last cell");
  tile.setInventorySlotContents(far,water.copy()); tile.setInventorySlotContents(last,block.copy());
  tile.setInventorySlotContents(PocketLayout.FUEL,new ItemStack(Items.redstone,12));
  NBTTagCompound saved=new NBTTagCompound(); tile.writeToNBT(saved);
  AdvancedReactorTile restored=new AdvancedReactorTile(); restored.readFromNBT(saved);
  check(restored.largePocket && restored.dimensions().equals("64x64x64") && restored.pocket==71,"large origin or dimensions lost");
  check(ItemStack.areItemStacksEqual(restored.getStackInSlot(far),water) && ItemStack.areItemStacksEqual(restored.getStackInSlot(last),block),"integer slot indices truncated or NBT lost");
  check(restored.getStackInSlot(PocketLayout.FUEL).stackSize==12,"high-index fuel slot lost");
  CompactReactors.advancedMaxWidth=16; AdvancedReactorTile reduced=new AdvancedReactorTile(); reduced.readFromNBT(saved);
  check(reduced.sizeX()==64 && reduced.hasLayout(),"config reduction destroyed saved layout");
  reduced.action(3); check(!reduced.deployed && reduced.resizeError==5,"config reduction allowed rebuilding oversized structure");
  CompactReactors.advancedMaxWidth=64;
  InventoryPlayer inventory=new InventoryPlayer(null); ReactorContainer container=new ReactorContainer(inventory,restored);
  container.setLayer(63); container.setView(54,54);
  Slot lastSlot=container.slotForCell(last); check(ItemStack.areItemStacksEqual(lastSlot.getStack(),block),"last page did not expose last cell");
  check(container.transferStackInSlot(null,lastSlot.slotNumber)!=null && restored.getStackInSlot(last)==null,"last-page shift extraction failed");
  container.setLayer(62);
  List<ItemStack> values=new ArrayList<ItemStack>(); for(Object object:container.inventorySlots) values.add(((Slot)object).getStack());
  S30PacketWindowItems packet=new S30PacketWindowItems(4,values); PacketBuffer bytes=new PacketBuffer(Unpooled.buffer()); packet.writePacketData(bytes);
  S30PacketWindowItems decoded=new S30PacketWindowItems(); decoded.readPacketData(bytes); bytes.release();
  check(decoded.func_148910_d().length==140,"large container exceeds vanilla packet slot count");
  check(container.clearLayout() && !restored.hasLayout() && restored.dimensions().equals("64x64x64") && restored.energy==12345,"large quick clear changed state");
  int waterCount=0; for(int i=0;i<36;i++) if(ItemStack.areItemStacksEqual(inventory.getStackInSlot(i),water)) waterCount++;
  check(waterCount==1 && restored.getStackInSlot(PocketLayout.FUEL).stackSize==12,"large quick clear lost/duplicated liquid or consumed fuel");
 }
 private static void legacy() {
  NBTTagCompound old=new NBTTagCompound(); old.setString("id","compactreactors:advanced_reactor");
  old.setInteger("LayoutVersion",4); old.setInteger("SizeX",9); old.setInteger("SizeY",9); old.setInteger("SizeZ",9);
  old.setInteger("Pocket",33); old.setBoolean("Deployed",true); old.setBoolean("Enabled",true);
  NBTTagList items=new NBTTagList(); NBTTagCompound entry=new NBTTagCompound(); new ItemStack(Items.water_bucket).writeToNBT(entry); entry.setShort("Slot",(short)637); items.appendTag(entry); old.setTag("Items",items);
  AdvancedReactorTile tile=new AdvancedReactorTile(); tile.readFromNBT(old);
  check(!tile.largePocket && tile.deployed && tile.enabled && tile.pocket==33 && tile.getStackInSlot(PocketLayout.slot(7,7,7))!=null,"0.3.0 structure moved or lost items");
  check(PocketLayout.serviceChunkX(33,tile.largePocket)==PocketLayout.chunkX(33),"old service origin changed");
  tile.deployed=false; new ReactorContainer(new InventoryPlayer(null),tile).clearLayout();
  check(tile.resize(32,32,32) && tile.largePocket,"empty old advanced structure cannot move to isolated large area");
 }
 private static void allocation() {
  check(PocketLayout.chunkSpan(64)==5 && PocketLayout.chunkSpan(32)==3 && PocketLayout.chunkSpan(9)==1,"chunk coverage wrong");
  for(int id:new int[]{0,1,1023,1024,PocketLayout.LIMIT-1}) {
   int x=PocketLayout.serviceChunkX(id,true),z=PocketLayout.serviceChunkZ(id,true);
   check(x>40000 && z>40000 && x*16+68<30000000 && z*16+68<30000000,"new service region overlaps old or world bounds");
   if(id%1024<1023) check(PocketLayout.serviceChunkX(id+1,true)-x>5,"adjacent large regions overlap");
  }
 }
}
