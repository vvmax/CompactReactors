package dev.compactreactors;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.ItemFluidContainer;
/** A measured liquid portion, without copying the tank that supplied it. */
public final class CoolantCell extends ItemFluidContainer {
 public CoolantCell() { super(0,1000); setUnlocalizedName("compactreactors.coolant_cell"); setTextureName("compactreactors:coolant_cell"); setMaxStackSize(64); }
 @Override public int fill(ItemStack stack,FluidStack fluid,boolean commit) { return stack.stackSize==1?super.fill(stack,fluid,commit):0; }
 @Override public FluidStack drain(ItemStack stack,int amount,boolean commit) { return stack.stackSize==1?super.drain(stack,amount,commit):null; }
 @Override public boolean onItemUseFirst(ItemStack stack,net.minecraft.entity.player.EntityPlayer player,net.minecraft.world.World world,int x,int y,int z,int side,float hitX,float hitY,float hitZ) {
  // Forge 1.7's client returns before sending the placement packet if this hook returns true.
  if(world.isRemote) return false;
  if(!player.canPlayerEdit(x,y,z,side,stack) || !world.canMineBlock(player,x,y,z)) return false;
  net.minecraft.tileentity.TileEntity target=world.getTileEntity(x,y,z);
  if(!(target instanceof net.minecraftforge.fluids.IFluidHandler) || LiquidCoolants.fluidForCell(stack)==null) return false;
  net.minecraftforge.common.util.ForgeDirection direction=net.minecraftforge.common.util.ForgeDirection.getOrientation(side);
  int moved=LiquidCoolants.pourIntoHandler(stack,(net.minecraftforge.fluids.IFluidHandler)target,direction,player.isSneaking()?stack.stackSize:1);
  if(moved>0) {
   if(stack.stackSize==0 && player.getCurrentEquippedItem()==stack) player.inventory.setInventorySlotContents(player.inventory.currentItem,null);
   target.markDirty(); world.markBlockForUpdate(x,y,z); player.inventory.markDirty(); player.inventoryContainer.detectAndSendChanges();
  }
  return moved>0;
 }
 @Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
 public void addInformation(ItemStack stack,net.minecraft.entity.player.EntityPlayer player,java.util.List lines,boolean advanced) {
  FluidStack fluid=getFluid(stack);
  if(fluid!=null) lines.add(fluid.getLocalizedName()+" - "+fluid.amount+" mB");
  for(String key:new String[]{"world","inventory","bucket","bucket.note"}) lines.add(net.minecraft.util.EnumChatFormatting.GRAY+net.minecraft.client.resources.I18n.format("item.compactreactors.coolant_cell."+key));
 }
}
