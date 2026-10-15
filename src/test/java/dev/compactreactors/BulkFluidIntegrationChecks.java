package dev.compactreactors;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.*;
public final class BulkFluidIntegrationChecks {
 private static void check(boolean ok,String message) { if(!ok) throw new AssertionError(message); }
 public static void run() {
  CompactReactors.coolantCell=new CoolantCell(); Item.itemRegistry.addObject(31003,"compactreactors:coolant_cell",CompactReactors.coolantCell);
  ItemFluidContainer tankItem=new ItemFluidContainer(0,32000); Item.itemRegistry.addObject(31004,"compactreactors:test_bulk_tank",tankItem);
  AdvancedReactorTile tile=new AdvancedReactorTile(); check(tile.resize(9,9,9),"test shape rejected");
  InventoryPlayer inventory=new InventoryPlayer(null); ReactorContainer container=new ReactorContainer(inventory,tile); container.setLayer(4);
  ItemStack tank=tank(tankItem,32000); NBTTagCompound original=(NBTTagCompound)tank.getTagCompound().copy();
  inventory.setInventorySlotContents(9,tank);
  check(container.transferStackInSlot(null,ReactorContainer.PLAYER_START)!=null,"bulk shift-click rejected");
  check(amount(tank)==0 && tank.stackSize==1 && tank.getItem()==tankItem && tile.occupiedLayout().size()==32,"32 buckets did not fill exactly 32 cells");
  check(tank.getTagCompound().getString("Owner").equals("keep") && tank.getTagCompound().getInteger("Tier")==2,"tank upgrades or ownership lost");
  int liquid=0;
  for(int i:tile.occupiedLayout()) {
   ItemStack cell=tile.getStackInSlot(i); check(cell.getItem() instanceof CoolantCell,"tank copied into construction cell");
   FluidStack fluid=LiquidCoolants.fluidForCell(cell); check(fluid!=null && fluid.amount==1000 && fluid.tag.getString("FluidTag").equals("preserve"),"portion amount or fluid NBT changed");
   check(!tile.boundary(i) && PocketLayout.y(i)==4,"bulk pour entered shell or another layer"); liquid+=fluid.amount;
  }
  check(liquid==32000,"liquid duplicated or lost");
  for(int page=0;page<100;page++) if(((Slot)container.inventorySlots.get(page)).getHasStack()) check(container.refillTank(tank,page),"cannot recover portion into source tank");
  check(amount(tank)==32000 && !tile.hasLayout() && original.equals(tank.getTagCompound()),"round trip changed tank NBT or fluid amount");
  check(!container.refillTank(tank,0),"empty cell returned fluid twice");
  inventory.setInventorySlotContents(9,tank(tankItem,18500)); tank=inventory.getStackInSlot(9);
  check(container.transferStackInSlot(null,ReactorContainer.PLAYER_START)!=null && tile.occupiedLayout().size()==18 && amount(tank)==500,"fractional remainder lost or partial cell created");
  NBTTagCompound saved=new NBTTagCompound(); tile.writeToNBT(saved); AdvancedReactorTile restored=new AdvancedReactorTile(); restored.readFromNBT(saved);
  check(restored.occupiedLayout().size()==18,"portions lost on save/reload");
  NBTTagCompound unchanged=(NBTTagCompound)tank.getTagCompound().copy(); tile.deployed=true;
  check(container.fillFromTank(tank,-1)==0 && !container.refillTank(tank,11) && unchanged.equals(tank.getTagCompound()),"assembled layout accepted fluid changes");
  tile.deployed=false;
  check(container.clearLayout() && !tile.hasLayout(),"bulk portions not cleared");
  int returned=0; for(int i=0;i<36;i++) { ItemStack stack=inventory.getStackInSlot(i); if(stack!=null && stack.getItem() instanceof CoolantCell) { returned+=amount(stack)*stack.stackSize; if(stack.stackSize>1) check(CompactReactors.coolantCell.drain(stack,1000,true)==null,"stacked portions drained as one"); } }
  check(returned+amount(tank)==18500,"quick clear lost bulk liquid");
  ItemStack single=tank(tankItem,1000); check(!tile.isItemValidForSlot(PocketLayout.slot(1,4,1),single),"whole large tank accepted as an escrow cell");
  check(container.fillFromTank(single,0)==0 && amount(single)==1000,"shell pour consumed fluid");
  check(container.fillFromTank(single,11)==1 && amount(single)==0,"single click failed to drain last bucket");
  ItemStack full=tank(tankItem,32000); NBTTagCompound fullBefore=(NBTTagCompound)full.getTagCompound().copy();
  check(!container.refillTank(full,11) && fullBefore.equals(full.getTagCompound()),"full tank consumed a portion");
  check(container.fillFromTank(full,11)==0 && fullBefore.equals(full.getTagCompound()),"occupied cell consumed fluid");
  ItemFluidContainer bad=new ItemFluidContainer(0,32000) { @Override public FluidStack drain(ItemStack stack,int amount,boolean commit) { FluidStack fluid=getFluid(stack); if(fluid!=null) fluid.amount=1000; return fluid; } };
  Item.itemRegistry.addObject(31005,"compactreactors:test_bad_tank",bad);
  check(LiquidCoolants.drainPortion(tank(bad,32000))==null,"non-draining handler created free fluid");
  ItemStack doubled=tank(tankItem,32000); doubled.stackSize=2; check(!LiquidCoolants.bulkContainer(doubled),"stacked tanks were drained as one");
  System.out.println("PASS: 32-bucket bulk tank, click/shift pouring, exact conservation and NBT round-trip, fractional remainder, save/clear, shell and deployment locks, tank protection and invalid-handler rejection");
  FluidRecoveryIntegrationChecks.run();
 }
 private static ItemStack tank(ItemFluidContainer item,int amount) {
  ItemStack tank=new ItemStack(item); FluidStack fluid=new FluidStack(FluidRegistry.WATER,amount); fluid.tag=new NBTTagCompound(); fluid.tag.setString("FluidTag","preserve");
  item.fill(tank,fluid,true); tank.getTagCompound().setString("Owner","keep"); tank.getTagCompound().setInteger("Tier",2); return tank;
 }
 private static int amount(ItemStack stack) { FluidStack fluid=LiquidCoolants.contents(stack); return fluid==null?0:fluid.amount; }
}
