package dev.compactreactors;
import erogenousbeef.bigreactors.api.registry.ReactorInterior;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.*;
/** A layout cell holds the original full container as escrow for one source block. */
public final class LiquidCoolants {
 public static final int CELL_VOLUME=FluidContainerRegistry.BUCKET_VOLUME;
 private LiquidCoolants() {}
 public static boolean bulkContainer(ItemStack stack) {
  if(stack==null || stack.stackSize!=1 || !(stack.getItem() instanceof IFluidContainerItem)) return false;
  ItemStack copy=stack.copy();
  return ((IFluidContainerItem)copy.getItem()).getCapacity(copy)>CELL_VOLUME;
 }
 public static FluidStack contents(ItemStack stack) {
  if(stack==null) return null;
  ItemStack copy=stack.copy(); copy.stackSize=1;
  FluidStack fluid=copy.getItem() instanceof IFluidContainerItem?((IFluidContainerItem)copy.getItem()).getFluid(copy):FluidContainerRegistry.getFluidForFilledItem(copy);
  return fluid==null?null:fluid.copy();
 }
 public static final class Pour {
  public final ItemStack tank,cell;
  private Pour(ItemStack tank,ItemStack cell) { this.tank=tank; this.cell=cell; }
 }
 public static Pour drainPortion(ItemStack source) {
  if(!bulkContainer(source) || CompactReactors.coolantCell==null) return null;
  ItemStack candidate=source.copy(); IFluidContainerItem handler=(IFluidContainerItem)candidate.getItem();
  FluidStack before=contents(candidate);
  if(before==null || before.amount<CELL_VOLUME || !supported(before.getFluid())) return null;
  FluidStack drained=handler.drain(candidate,CELL_VOLUME,true),after=contents(candidate);
  if(drained==null || drained.amount!=CELL_VOLUME || !before.isFluidEqual(drained) || candidate.getItem()!=source.getItem() || candidate.stackSize!=1) return null;
  if(after!=null && !before.isFluidEqual(after)) return null;
  if(before.amount-(after==null?0:after.amount)!=CELL_VOLUME) return null;
  ItemStack cell=new ItemStack(CompactReactors.coolantCell);
  if(CompactReactors.coolantCell.fill(cell,drained,true)!=CELL_VOLUME) return null;
  return new Pour(candidate,cell);
 }
 public static ItemStack refillPortion(ItemStack tank,ItemStack cell) {
  if(!bulkContainer(tank) || cell==null || !(cell.getItem() instanceof CoolantCell)) return null;
  FluidStack fluid=fluidForCell(cell); if(fluid==null) return null;
  ItemStack candidate=tank.copy(); IFluidContainerItem handler=(IFluidContainerItem)candidate.getItem();
  FluidStack before=contents(candidate); if(before!=null && !before.isFluidEqual(fluid)) return null;
  if(handler.fill(candidate,fluid,true)!=CELL_VOLUME) return null;
  FluidStack after=contents(candidate);
  if(after==null || !after.isFluidEqual(fluid) || after.amount-(before==null?0:before.amount)!=CELL_VOLUME) return null;
  return candidate;
 }
 public static void commitTank(ItemStack target,ItemStack candidate) {
  target.setItemDamage(candidate.getItemDamage());
  target.setTagCompound(candidate.hasTagCompound()?(net.minecraft.nbt.NBTTagCompound)candidate.getTagCompound().copy():null);
 }
 /** Move complete portions only; a nearly full tank leaves the remainder in the stack. */
 public static int recoverPortions(ItemStack tank,ItemStack portions,int maximum) {
  if(portions==null || !(portions.getItem() instanceof CoolantCell)) return 0;
  int moved=0;
  while(portions.stackSize>0 && moved<maximum) {
   ItemStack candidate=refillPortion(tank,portions);
   if(candidate==null) break;
   commitTank(tank,candidate); portions.stackSize--; moved++;
  }
  return moved;
 }
 public static int pourIntoHandler(ItemStack portions,IFluidHandler handler,net.minecraftforge.common.util.ForgeDirection side,int maximum) {
  if(portions==null || !(portions.getItem() instanceof CoolantCell) || handler==null) return 0;
  FluidStack fluid=fluidForCell(portions); if(fluid==null) return 0;
  int moved=0;
  while(portions.stackSize>0 && moved<maximum) {
   if(handler.fill(side,fluid.copy(),false)!=CELL_VOLUME) break;
   if(handler.fill(side,fluid.copy(),true)!=CELL_VOLUME) break;
   portions.stackSize--; moved++;
  }
  return moved;
 }
 public static ItemStack filledBucket(ItemStack portion) {
  if(portion==null || !(portion.getItem() instanceof CoolantCell)) return null;
  FluidStack fluid=fluidForCell(portion); if(fluid==null) return null;
  ItemStack bucket=FluidContainerRegistry.fillFluidContainer(fluid.copy(),new ItemStack(net.minecraft.init.Items.bucket));
  FluidStack result=contents(bucket);
  return result!=null && result.amount==CELL_VOLUME && result.isFluidEqual(fluid)?bucket:null;
 }
 public static boolean supported(Fluid fluid) {
  if(fluid==null || ReactorInterior.getFluidData(fluid.getName())==null) return false;
  Block block=fluid.getBlock();
  if(block==null || block==Blocks.air) return false;
  if(block instanceof IFluidBlock) {
   Fluid actual=((IFluidBlock)block).getFluid();
   return actual!=null && actual.getName().equals(fluid.getName());
  }
  return block.getMaterial()==Material.water && "water".equals(fluid.getName());
 }
 public static FluidStack fluidForCell(ItemStack stack) {
  if(stack==null || stack.getItem()==null) return null;
  // Never drain or mutate the player's original stack, including NBT-based mod containers.
  ItemStack single=stack.copy(); single.stackSize=1;
  FluidStack contents=single.getItem() instanceof IFluidContainerItem?
   ((IFluidContainerItem)single.getItem()).getFluid(single):FluidContainerRegistry.getFluidForFilledItem(single);
  if(contents==null || contents.amount!=CELL_VOLUME || !supported(contents.getFluid())) return null;
  return contents.copy();
 }
 public static Block blockForCell(ItemStack stack) {
  FluidStack contents=fluidForCell(stack);
  return contents==null?null:contents.getFluid().getBlock();
 }
 public static ItemStack sampleContainer(Fluid fluid) {
  if(fluid==null) return null;
  for(FluidContainerRegistry.FluidContainerData data:FluidContainerRegistry.getRegisteredFluidContainerData()) {
   if(data.fluid!=null && data.fluid.amount==CELL_VOLUME && data.fluid.getFluid()==fluid) {
    ItemStack copy=data.filledContainer.copy(); copy.stackSize=1; return copy;
   }
  }
  return null;
 }
}
