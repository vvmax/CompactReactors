package dev.compactreactors;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
public class ReactorGuiHandler implements IGuiHandler {
 @Override public Object getServerGuiElement(int id,EntityPlayer p,World w,int x,int y,int z) {
  TileEntity te=w.getTileEntity(x,y,z); return id==0 && te instanceof ReactorTile ? new ReactorContainer(p.inventory,(ReactorTile)te):null;
 }
 @Override @SideOnly(Side.CLIENT) public Object getClientGuiElement(int id,EntityPlayer p,World w,int x,int y,int z) {
  TileEntity te=w.getTileEntity(x,y,z); return id==0 && te instanceof ReactorTile ? new ReactorGui(new ReactorContainer(p.inventory,(ReactorTile)te)):null;
 }
}
