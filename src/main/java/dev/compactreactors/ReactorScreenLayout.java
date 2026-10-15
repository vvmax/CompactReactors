package dev.compactreactors;
/** Shared container/screen geometry, independent of the client renderer. */
public final class ReactorScreenLayout {
 public static final int WIDTH=318;
 private ReactorScreenLayout() {}
 public static int extra(int size) { return (size-5)*18; }
 public static int height(int size) { return 234+extra(size); }
 public static boolean paged(ReactorTile tile) { return tile.sizeX()>10 || tile.sizeZ()>10; }
 public static int shift(ReactorTile tile) { return Math.max(0,Math.min(10,tile.sizeX())-9)*18; }
 public static int width(ReactorTile tile) { return WIDTH+shift(tile); }
 public static int header(ReactorTile tile) { return (tile.isAdvanced()?20:0)+(paged(tile)?20:0); }
 public static int extra(ReactorTile tile) { return extra(Math.min(10,tile.sizeZ()))+header(tile); }
 public static int height(ReactorTile tile) { return 234+extra(tile); }
 public static float fitScale(int width,int height,int size) {
  return fitToHeight(width,height,height(size));
 }
 public static float fitScale(int width,int height,ReactorTile tile) { return Math.min(1f,Math.min(Math.max(1,width-2)/(float)width(tile),Math.max(1,height-2)/(float)height(tile))); }
 private static float fitToHeight(int width,int height,int windowHeight) { return Math.min(1f,Math.min(Math.max(1,width-2)/(float)WIDTH,Math.max(1,height-2)/(float)windowHeight)); }
 public static int canvasExtent(int extent,float scale) { return Math.max(1,(int)Math.ceil(extent/scale)); }
}
