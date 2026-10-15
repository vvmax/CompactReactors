package dev.compactreactors;
public class ReactorShapeTest {
 private static void check(boolean ok,String message) { if(!ok) throw new AssertionError(message); }
 public static void main(String[] args) {
  check(ReactorShape.PARTS.length>12,"model has no depth");
  for(double[] part:ReactorShape.PARTS) for(int facing=2;facing<=5;facing++) {
   double[] box=ReactorShape.rotate(part,facing);
   for(int axis=0;axis<3;axis++) check(box[axis]>=0 && box[axis+3]<=1 && box[axis]<box[axis+3],"invalid oriented model bounds");
   double before=(part[3]-part[0])*(part[4]-part[1])*(part[5]-part[2])/4096;
   double after=(box[3]-box[0])*(box[4]-box[1])*(box[5]-box[2]);
   check(Math.abs(before-after)<.000001,"rotation changed volume");
  }
  double[] north={3,3,0,13,13,1,2};
  check(ReactorShape.rotate(north,2)[2]==0,"north panel");
  check(ReactorShape.rotate(north,3)[5]==1,"south panel");
  check(ReactorShape.rotate(north,4)[0]==0,"west panel");
  check(ReactorShape.rotate(north,5)[3]==1,"east panel");
  System.out.println("PASS: model depth, one-block bounds, preserved volume and four placement orientations");
 }
}
