package dev.compactreactors;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;
@SideOnly(Side.CLIENT)
public final class ReactorRenderer implements ISimpleBlockRenderingHandler {
 @Override public int getRenderId() { return CompactReactors.renderType; }
 @Override public boolean shouldRender3DInInventory(int id) { return true; }
 @Override public boolean renderWorldBlock(IBlockAccess world,int x,int y,int z,Block block,int id,RenderBlocks renderer) {
  IIcon previous=renderer.overrideBlockTexture;
  try {
   for(double[] part:ReactorShape.PARTS) {
    double[] box=ReactorShape.rotate(part,world.getBlockMetadata(x,y,z));
    renderer.setRenderBounds(box[0],box[1],box[2],box[3],box[4],box[5]);
    renderer.setOverrideBlockTexture(previous!=null?previous:((ReactorBlock)block).modelIcon((int)part[6]));
    renderer.renderStandardBlock(block,x,y,z);
   }
  } finally { if(previous!=null) renderer.setOverrideBlockTexture(previous); else renderer.clearOverrideBlockTexture(); renderer.setRenderBoundsFromBlock(block); }
  return true;
 }
 @Override public void renderInventoryBlock(Block block,int metadata,int id,RenderBlocks renderer) {
  GL11.glPushMatrix(); GL11.glRotatef(90,0,1,0); GL11.glTranslatef(-.5f,-.5f,-.5f);
  try {
   for(double[] part:ReactorShape.PARTS) {
    double[] box=ReactorShape.rotate(part,2);
    renderer.setRenderBounds(box[0],box[1],box[2],box[3],box[4],box[5]);
    IIcon icon=((ReactorBlock)block).modelIcon((int)part[6]);
    Tessellator t=Tessellator.instance; t.startDrawingQuads();
    t.setColorOpaque_F(.55f,.55f,.55f); t.setNormal(0,-1,0); renderer.renderFaceYNeg(block,0,0,0,icon);
    t.setColorOpaque_F(1,1,1); t.setNormal(0,1,0); renderer.renderFaceYPos(block,0,0,0,icon);
    t.setColorOpaque_F(.8f,.8f,.8f); t.setNormal(0,0,-1); renderer.renderFaceZNeg(block,0,0,0,icon);
    t.setNormal(0,0,1); renderer.renderFaceZPos(block,0,0,0,icon);
    t.setColorOpaque_F(.7f,.7f,.7f); t.setNormal(-1,0,0); renderer.renderFaceXNeg(block,0,0,0,icon);
    t.setNormal(1,0,0); renderer.renderFaceXPos(block,0,0,0,icon); t.draw();
   }
  } finally { renderer.clearOverrideBlockTexture(); renderer.setRenderBoundsFromBlock(block); GL11.glPopMatrix(); GL11.glColor4f(1,1,1,1); }
 }
}
