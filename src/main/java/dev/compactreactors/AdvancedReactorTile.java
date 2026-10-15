package dev.compactreactors;
public class AdvancedReactorTile extends ReactorTile {
 public AdvancedReactorTile() { largePocket=true; }
 @Override public boolean isAdvanced() { return true; }
 @Override public String getInventoryName() { return "tile.compactreactors.advanced_reactor.name"; }
}
