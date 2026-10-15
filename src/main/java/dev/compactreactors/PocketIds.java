package dev.compactreactors;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.WorldServer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.DimensionManager;
public class PocketIds extends WorldSavedData {
 private int next;
 public PocketIds() { super("compactreactors_ids"); }
 public PocketIds(String name) { super(name); }
 public static int allocate() {
  WorldServer world=DimensionManager.getWorld(0);
  PocketIds data=(PocketIds)world.mapStorage.loadData(PocketIds.class,"compactreactors_ids");
  if(data==null) { data=new PocketIds(); world.mapStorage.setData("compactreactors_ids",data); }
  if(data.next>=PocketLayout.LIMIT) throw new IllegalStateException("CompactReactors pocket limit reached");
  int id=data.next++; data.markDirty(); return id;
 }
 @Override public void readFromNBT(NBTTagCompound n) { next=Math.max(0,n.getInteger("Next")); }
 @Override public void writeToNBT(NBTTagCompound n) { n.setInteger("Next",next); }
}
