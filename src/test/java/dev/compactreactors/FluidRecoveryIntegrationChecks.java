package dev.compactreactors;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.*;
public final class FluidRecoveryIntegrationChecks {
 private static void check(boolean ok,String message) { if(!ok) throw new AssertionError(message); }
 private static ItemStack portions(FluidStack fluid,int count) {
  ItemStack stack=new ItemStack(CompactReactors.coolantCell);
  check(CompactReactors.coolantCell.fill(stack,fluid,true)==1000,"test portion rejected"); stack.stackSize=count; return stack;
 }
 public static void run() {
  FluidStack water=new FluidStack(FluidRegistry.WATER,1000);
  ItemFluidContainer item=new ItemFluidContainer(0,4000); Item.itemRegistry.addObject(31010,"compactreactors:recovery_tank",item);
  ItemStack tank=new ItemStack(item); NBTTagCompound tags=new NBTTagCompound(); tags.setString("Owner","unchanged"); tags.setInteger("Tier",2); tank.setTagCompound(tags);
  InventoryPlayer inventory=new InventoryPlayer(null); ReactorTile tile=new ReactorTile(); tile.deployed=true;
  ReactorContainer gui=new ReactorContainer(inventory,tile);
  inventory.setInventorySlotContents(9,portions(water,7));
  check(gui.refillInventoryTank(tank,ReactorContainer.PLAYER_START,1),"right-click inventory recovery failed");
  check(LiquidCoolants.contents(tank).amount==1000 && inventory.getStackInSlot(9).stackSize==6,"one portion was not recovered");
  check(gui.refillInventoryTank(tank,ReactorContainer.PLAYER_START,64),"left-click inventory recovery failed");
  check(LiquidCoolants.contents(tank).amount==4000 && inventory.getStackInSlot(9).stackSize==3,"inventory recovery overflowed tank or lost remainder");
  check(!gui.refillInventoryTank(tank,ReactorContainer.PLAYER_START,64),"full tank consumed portions");
  check(tank.getTagCompound().getString("Owner").equals("unchanged") && tank.getTagCompound().getInteger("Tier")==2,"tank metadata lost on recovery");
  item.drain(tank,4000,true); check(gui.refillInventoryTank(tank,ReactorContainer.PLAYER_START,64) && inventory.getStackInSlot(9)==null && LiquidCoolants.contents(tank).amount==3000,"last inventory portion left an empty item");
  check(!gui.refillInventoryTank(tank,0,1),"inventory operation reached reactor structure");
  item.fill(tank,new FluidStack(FluidRegistry.WATER,500),true);
  ItemStack remaining=portions(water,2); check(LiquidCoolants.recoverPortions(tank,remaining,64)==0 && remaining.stackSize==2 && LiquidCoolants.contents(tank).amount==3500,"partial free space consumed a portion");
  ItemStack incompatible=portions(new FluidStack(FluidRegistry.getFluid("compact_test_coolant"),1000),2);
  item.drain(tank,1000,true); check(LiquidCoolants.recoverPortions(tank,incompatible,64)==0 && incompatible.stackSize==2,"incompatible fluids mixed");

  TileFluidHandler worldTank=new TileFluidHandler() { { tank.setCapacity(3500); }
   @Override public int fill(ForgeDirection side,FluidStack fluid,boolean commit) { return side==ForgeDirection.NORTH?super.fill(side,fluid,commit):0; }
  };
  ItemStack stack=portions(water,8);
  check(LiquidCoolants.pourIntoHandler(stack,worldTank,ForgeDirection.SOUTH,64)==0 && stack.stackSize==8,"wrong side accepted fluid");
  check(LiquidCoolants.pourIntoHandler(stack,worldTank,ForgeDirection.NORTH,1)==1 && stack.stackSize==7,"single world-tank pour failed");
  check(LiquidCoolants.pourIntoHandler(stack,worldTank,ForgeDirection.NORTH,64)==2 && stack.stackSize==5,"stack pour lost liquid or ignored capacity");
  check(LiquidCoolants.pourIntoHandler(stack,worldTank,ForgeDirection.NORTH,64)==0 && stack.stackSize==5,"nearly full world tank consumed a portion");
  check(worldTank.drain(ForgeDirection.NORTH,4000,true).amount==3000,"world tank received wrong total");
  FluidStack tagged=water.copy(); tagged.tag=new NBTTagCompound(); tagged.tag.setString("Variant","preserve");
  stack=portions(tagged,1); check(LiquidCoolants.pourIntoHandler(stack,worldTank,ForgeDirection.NORTH,1)==1 && stack.stackSize==0,"last portion not consumed");
  check(worldTank.drain(ForgeDirection.NORTH,1000,true).isFluidEqual(tagged),"fluid NBT lost in world tank");

  CoolantBucketRecipe recipe=new CoolantBucketRecipe();
  Container dummy=new Container() { @Override public boolean canInteractWith(EntityPlayer player) { return true; } };
  InventoryCrafting grid=new InventoryCrafting(dummy,2,2);
  ItemStack source=portions(water,32); NBTTagCompound before=(NBTTagCompound)source.getTagCompound().copy();
  grid.setInventorySlotContents(0,new ItemStack(Items.bucket,16)); grid.setInventorySlotContents(3,source);
  ItemStack result=recipe.getCraftingResult(grid);
  check(recipe.matches(grid,null) && result.getItem()==Items.water_bucket && result.stackSize==1,"water bucket recipe failed");
  check(source.stackSize==32 && source.getTagCompound().equals(before) && grid.getStackInSlot(0).stackSize==16,"recipe preview consumed fluid or bucket");
  result.stackSize=10; check(recipe.getCraftingResult(grid).stackSize==1,"recipe output shared mutable state");
  grid.setInventorySlotContents(1,new ItemStack(Items.diamond)); check(!recipe.matches(grid,null),"extra recipe ingredient ignored"); grid.setInventorySlotContents(1,null);
  grid.setInventorySlotContents(3,portions(tagged,1)); check(!recipe.matches(grid,null),"bucket conversion discarded custom fluid NBT");
  grid.setInventorySlotContents(3,incompatible.copy()); check(!recipe.matches(grid,null),"unregistered fluid bucket fabricated");
  Item modBucket=new Item(); Item.itemRegistry.addObject(31011,"compactreactors:recovery_mod_bucket",modBucket);
  FluidContainerRegistry.registerFluidContainer(new FluidStack(FluidRegistry.getFluid("compact_test_coolant"),1000),new ItemStack(modBucket),new ItemStack(Items.bucket));
  check(recipe.matches(grid,null) && recipe.getCraftingResult(grid).getItem()==modBucket,"registered mod-fluid bucket not used");
  grid.setInventorySlotContents(0,null); check(!recipe.matches(grid,null),"recipe produced free bucket");
  System.out.println("PASS: inventory and world tank recovery, one/stack transfer, full/partial/wrong-fluid/wrong-side refusal, NBT preservation and vanilla/mod bucket crafting without preview mutation");
 }
}
