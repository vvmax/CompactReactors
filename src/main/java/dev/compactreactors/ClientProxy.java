package dev.compactreactors;
import cpw.mods.fml.client.registry.RenderingRegistry;
public final class ClientProxy extends CommonProxy {
 @Override public void init() {
  CompactReactors.renderType=RenderingRegistry.getNextAvailableRenderId();
  RenderingRegistry.registerBlockHandler(new ReactorRenderer());
 }
}
