package dev.compactreactors;
import erogenousbeef.bigreactors.api.registry.ReactorInterior;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.*;
import cpw.mods.fml.common.registry.GameRegistry;
import io.netty.buffer.*;
import java.util.*;
public class LiquidIntegrationChecks {
 private static void check(boolean ok,String message) { if(!ok) throw new AssertionError(message); }
 public static void run() throws Exception {
  java.lang.reflect.Field side=cpw.mods.fml.relauncher.FMLRelaunchLog.class.getDeclaredField("side");
  side.setAccessible(true); side.set(null,cpw.mods.fml.relauncher.Side.SERVER);
  Bootstrap.func_151354_b();
  ReactorInterior.registerFluid("water",.33f,.5f,1.33f,.1f);
  ItemStack water=new ItemStack(Items.water_bucket);
  check(LiquidCoolants.fluidForCell(water).amount==1000,"water bucket rejected");
  check(LiquidCoolants.blockForCell(water)==Blocks.water,"water source block mapping");
  check(LiquidCoolants.fluidForCell(new ItemStack(Items.bucket))==null,"empty bucket accepted");
  check(LiquidCoolants.fluidForCell(new ItemStack(Items.lava_bucket))==null,"unregistered lava accepted");
  Fluid modFluid=new Fluid("compact_test_coolant"); FluidRegistry.registerFluid(modFluid);
  Block fluidBlock=new BlockFluidClassic(modFluid,Material.water); modFluid.setBlock(fluidBlock);
  check(!LiquidCoolants.supported(modFluid),"unregistered mod fluid accepted");
  ReactorInterior.registerFluid(modFluid.getName(),.4f,.8f,3f,2f);
  check(LiquidCoolants.supported(modFluid),"registered IFluidBlock coolant rejected");
  Fluid ghost=new Fluid("compact_test_no_block"); FluidRegistry.registerFluid(ghost);
  ReactorInterior.registerFluid(ghost.getName(),1,1,1,1);
  check(!LiquidCoolants.supported(ghost),"fluid without block accepted");
  ItemFluidContainer tank=new ItemFluidContainer(0,4000);
  Item.itemRegistry.addObject(31000,"compactreactors:test_fluid_container",tank);
  ItemStack cell=new ItemStack(tank); tank.fill(cell,new FluidStack(modFluid,1000),true);
  NBTTagCompound before=(NBTTagCompound)cell.getTagCompound().copy();
  check(LiquidCoolants.fluidForCell(cell).amount==1000 && before.equals(cell.getTagCompound()),"NBT container mutated while inspecting");
  check(LiquidCoolants.blockForCell(cell)==fluidBlock,"mod fluid source mapping");
  for(int amount:new int[]{0,250,999,1001,4000}) {
   ItemStack wrong=new ItemStack(tank); if(amount>0) tank.fill(wrong,new FluidStack(modFluid,amount),true);
   check(LiquidCoolants.fluidForCell(wrong)==null,"wrong liquid volume accepted: "+amount);
  }
  cell.stackSize=4; check(LiquidCoolants.fluidForCell(cell).amount==1000 && cell.stackSize==4,"stack of fluid cells changed"); cell.stackSize=1;
  ReactorTile tile=new ReactorTile(); int center=PocketLayout.slot(2,2,2);
  check(!tile.isItemValidForSlot(center,cell),"large container should pour instead of being locked into a cell");
  check(!tile.isItemValidForSlot(PocketLayout.slot(0,2,2),cell),"liquid accepted in shell");
  tile.setInventorySlotContents(center,cell.copy()); tile.deployed=true;
  check(!tile.isItemValidForSlot(center,cell),"deployed liquid cell is editable");
  GameRegistry.registerTileEntity(ReactorTile.class,"compactreactors:reactor");
  NBTTagCompound saved=new NBTTagCompound(); tile.writeToNBT(saved);
  ReactorTile restored=new ReactorTile(); restored.readFromNBT(saved);
  check(restored.deployed && ItemStack.areItemStacksEqual(restored.getStackInSlot(center),cell),"liquid cell lost on save/reload");
  restored.deployed=false; ItemStack returned=restored.decrStackSize(center,1);
  check(LiquidCoolants.fluidForCell(returned).amount==1000 && restored.getStackInSlot(center)==null,"fluid container was duplicated or lost");
  CoolantEntry entry=new CoolantEntry(CoolantEntry.FLUID,"water",.33f,.5f,1.33f,.1f,true,true,water);
  CatalogNetwork.Packet packet=new CatalogNetwork.Packet(9,1,0,Arrays.asList(entry)); ByteBuf bytes=Unpooled.buffer(); packet.toBytes(bytes);
  CatalogNetwork.Packet decoded=new CatalogNetwork.Packet(); decoded.fromBytes(bytes); bytes.release();
  check(decoded.entries[0].usable && ItemStack.areItemStacksEqual(decoded.entries[0].icon,water),"fluid catalog icon/availability lost in transit");
  System.out.println("PASS: vanilla and mod liquid containers, exact volume, source blocks, NBT immutability, shell/lock rules, save/reload and container return");
  ExpansionIntegrationChecks.run();
 }
}
