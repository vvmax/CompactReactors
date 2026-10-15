import dev.compactreactors.PocketLayout;
public class PocketLayoutTest {
 private static void check(boolean b,String message) { if(!b) throw new AssertionError(message); }
 public static void main(String[] args) {
  // All allocatable pockets stay inside world bounds, within one chunk, with spacing.
  for(int id=0;id<PocketLayout.LIMIT;id++) {
   int x=PocketLayout.chunkX(id),z=PocketLayout.chunkZ(id);
   check(x%2==0 && z%2==0,"spacing");
   check((z/2)*1024+x/2==id,"unique reversible allocation");
   check(x*16+12<30000000 && z*16+12<30000000,"world border");
   check((x*16+4)>>4==x && (x*16+12)>>4==x,"one chunk X at maximum size");
   check((z*16+4)>>4==z && (z*16+12)>>4==z,"one chunk Z at maximum size");
  }
  for(int size:new int[]{5,7,9}) verifySize(size);
  boolean[] migrated=new boolean[PocketLayout.INVENTORY];
  for(int old=0;old<127;old++) {
   int cell=PocketLayout.versionTwoSlot(old); check(cell>=0 && !migrated[cell],"version two migration collision"); migrated[cell]=true;
   if(old<125) check(PocketLayout.x(cell)==old%5 && PocketLayout.y(cell)==old/25 && PocketLayout.z(cell)==(old%25)/5,"version two coordinates moved");
  }
  check(PocketLayout.versionTwoSlot(125)==PocketLayout.FUEL && PocketLayout.versionTwoSlot(126)==PocketLayout.WASTE,"version two fuel/waste migration");
  check(PocketLayout.versionTwoSlot(127)==-1,"version two invalid slot");
  java.util.Arrays.fill(migrated,false);
  for(int old=0;old<38;old++) {
   int cell=PocketLayout.legacySlot(old); check(!migrated[cell],"migration collision"); migrated[cell]=true;
   if(old<27) check(!PocketLayout.boundary(cell),"legacy interior moved into shell");
   else if(old<36) check(PocketLayout.y(cell)==4 && !PocketLayout.frame(cell),"legacy control rod mapping");
  }
  check(PocketLayout.legacySlot(0)==PocketLayout.slot(1,1,1),"legacy bottom corner");
  check(PocketLayout.legacySlot(26)==PocketLayout.slot(3,3,3),"legacy top corner");
  check(PocketLayout.legacySlot(36)==PocketLayout.FUEL && PocketLayout.legacySlot(37)==PocketLayout.WASTE,"fuel/waste migration");
  try { PocketLayout.chunkX(-1); throw new AssertionError("negative accepted"); } catch(IllegalArgumentException expected) {}
  try { PocketLayout.chunkZ(PocketLayout.LIMIT); throw new AssertionError("overflow accepted"); } catch(IllegalArgumentException expected) {}
  System.out.println("PASS: pocket isolation through 9x9x9, all size boundaries, controls and both legacy layout migrations");
 }
 private static void verifySize(int size) {
  boolean[] seen=new boolean[PocketLayout.CELLS];
  int boundary=0,interior=0,roofControls=0;
  for(int y=0;y<size;y++) for(int z=0;z<size;z++) for(int x=0;x<size;x++) {
   int cell=PocketLayout.slot(x,y,z);
   check(cell>=0 && cell<PocketLayout.CELLS && !seen[cell],"full editor bijection"); seen[cell]=true;
   check(PocketLayout.x(cell)==x && PocketLayout.y(cell)==y && PocketLayout.z(cell)==z,"coordinate round trip");
   boolean shell=x==0 || x==size-1 || y==0 || y==size-1 || z==0 || z==size-1;
   // Regression: Reactor Casing must be accepted in every shell position, never inside.
   check(PocketLayout.accepts(cell,PocketLayout.CASING,size)==shell,"casing placement");
   check(PocketLayout.accepts(cell,PocketLayout.INTERIOR,size)==!shell,"fuel/moderator placement");
   if(shell) boundary++; else interior++;
   boolean top=y==size-1 && x>0 && x<size-1 && z>0 && z<size-1;
   check(PocketLayout.accepts(cell,PocketLayout.CONTROL,size)==top,"control rods belong above interior");
   if(top) roofControls++;
   if(PocketLayout.frame(cell,size)) {
    check(!PocketLayout.accepts(cell,PocketLayout.GLASS,size),"glass in frame");
    check(!PocketLayout.accepts(cell,PocketLayout.SHELL_PART,size),"ports in frame");
   }
  }
  check(interior==(size-2)*(size-2)*(size-2) && boundary==size*size*size-interior && roofControls==(size-2)*(size-2),"reactor volume");
  check(!PocketLayout.accepts(-1,PocketLayout.CASING,size),"negative slot");
  check(!PocketLayout.accepts(PocketLayout.FUEL,PocketLayout.CASING,size),"fuel slot is not construction");
  for(int i=0;i<PocketLayout.CELLS;i++) if(!seen[i]) check(!PocketLayout.accepts(i,PocketLayout.INTERIOR,size) && !PocketLayout.accepts(i,PocketLayout.CASING,size),"inactive cell accepted");
 }
}
