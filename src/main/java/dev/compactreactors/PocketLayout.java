package dev.compactreactors;
/** Sparse 64-cube storage; the GUI exposes only a bounded page of cells. */
public final class PocketLayout {
 public static final int LIMIT=1048576, SIZE=5, MAX_SIZE=64, LAYER_CELLS=4096, CELLS=262144, FUEL=CELLS, WASTE=CELLS+1, MODULE_1=CELLS+2, MODULE_2=CELLS+3, INVENTORY=CELLS+4;
 public static final int CASING=1, CONTROL=2, SHELL_PART=3, GLASS=4, INTERIOR=5;
 private PocketLayout() {}
 public static int chunkX(int id) { check(id); return (id%1024)*2; }
 public static int chunkZ(int id) { check(id); return (id/1024)*2; }
 public static int serviceChunkX(int id,boolean large) { check(id); return large?62500+(id%1024)*6:chunkX(id); }
 public static int serviceChunkZ(int id,boolean large) { check(id); return large?62500+(id/1024)*6:chunkZ(id); }
 public static int chunkSpan(int extent) { return (4+extent-1)/16+1; }
 private static void check(int id) { if(id<0 || id>=LIMIT) throw new IllegalArgumentException("Invalid pocket ID: "+id); }
 public static boolean validSize(int size) { return size==5 || size==7 || size==9; }
 public static boolean validExtent(int size) { return size>=5 && size<=MAX_SIZE; }
 public static boolean validShape(int width,int height,int depth) { return validExtent(width) && validExtent(height) && validExtent(depth); }
 public static int slot(int x,int y,int z) { return y*LAYER_CELLS+z*MAX_SIZE+x; }
 public static int x(int slot) { return slot%MAX_SIZE; }
 public static int y(int slot) { return slot/LAYER_CELLS; }
 public static int z(int slot) { return (slot%LAYER_CELLS)/MAX_SIZE; }
 public static boolean contains(int slot,int size) { return contains(slot,size,size,size); }
 public static boolean contains(int slot,int width,int height,int depth) { return slot>=0 && slot<CELLS && x(slot)<width && y(slot)<height && z(slot)<depth; }
 public static boolean boundary(int slot) { return boundary(slot,SIZE); }
 public static boolean boundary(int slot,int size) { return boundary(slot,size,size,size); }
 public static boolean boundary(int slot,int width,int height,int depth) { return contains(slot,width,height,depth) && (x(slot)==0 || x(slot)==width-1 || y(slot)==0 || y(slot)==height-1 || z(slot)==0 || z(slot)==depth-1); }
 public static boolean frame(int slot) { return frame(slot,SIZE); }
 public static boolean frame(int slot,int size) { return frame(slot,size,size,size); }
 public static boolean frame(int slot,int width,int height,int depth) { return contains(slot,width,height,depth) && ((x(slot)==0 || x(slot)==width-1)?1:0)+((y(slot)==0 || y(slot)==height-1)?1:0)+((z(slot)==0 || z(slot)==depth-1)?1:0)>=2; }
 public static boolean accepts(int slot,int kind) { return accepts(slot,kind,SIZE); }
 public static boolean accepts(int slot,int kind,int size) {
  return validSize(size) && accepts(slot,kind,size,size,size);
 }
 public static boolean accepts(int slot,int kind,int width,int height,int depth) {
  if(!validShape(width,height,depth) || !contains(slot,width,height,depth)) return false;
  if(!boundary(slot,width,height,depth)) return kind==INTERIOR;
  if(frame(slot,width,height,depth)) return kind==CASING;
  return kind==CASING || kind==SHELL_PART || kind==GLASS || (kind==CONTROL && y(slot)==height-1);
 }
 /** 0.1.1-0.1.5 used dense 5x5x5 storage plus fuel and waste. */
 public static int versionTwoSlot(int old) {
  if(old>=0 && old<125) return slot(old%5,old/25,(old%25)/5);
  if(old==125) return FUEL;
  if(old==126) return WASTE;
  return -1;
 }
 /** 0.2.0-0.3.0 stored sparse coordinates inside a fixed 9-cube. */
 public static int versionFourSlot(int old) {
  if(old>=0 && old<729) return slot(old%9,old/81,(old%81)/9);
  return old>=729 && old<=732?FUEL+old-729:-1;
 }
 /** 0.1.0 stored 27 interior cells, nine roof cells and two inventory slots. */
 public static int legacySlot(int old) {
  if(old>=0 && old<27) return slot(old%3+1,old/9+1,(old%9)/3+1);
  if(old>=27 && old<36) return slot((old-27)%3+1,4,(old-27)/3+1);
  if(old==36) return FUEL;
  if(old==37) return WASTE;
  throw new IllegalArgumentException("Invalid legacy slot: "+old);
 }
}
