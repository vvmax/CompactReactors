package dev.compactreactors;
/** Cuboids in sixteenths of a block. No client classes: geometry is independently testable. */
public final class ReactorShape {
 public static final int FRAME=0,SIDE=1,FRONT=2,TOP=3,CORE=4,BASE=5;
 // x0,y0,z0,x1,y1,z1,material; north is the default front.
 public static final double[][] PARTS={
  {0,0,0,16,2,16,FRAME},{3,2,3,13,14,13,BASE},
  {0,2,0,2,14,2,FRAME},{14,2,0,16,14,2,FRAME},
  {0,2,14,2,14,16,FRAME},{14,2,14,16,14,16,FRAME},
  {0,14,0,16,16,2,FRAME},{0,14,14,16,16,16,FRAME},
  {0,14,2,2,16,14,FRAME},{14,14,2,16,16,14,FRAME},
  {2,2,14,14,14,15,SIDE},{1,2,2,2,14,14,SIDE},{14,2,2,15,14,14,SIDE},
  {2,3,0,3,13,2,FRAME},{13,3,0,14,13,2,FRAME},
  {3,2,0,13,3,2,FRAME},{3,13,0,13,14,2,FRAME},
  {3,3,1,13,13,2,FRONT},
  {4.5,4,.5,6,12,1,CORE},{7.25,4,.5,8.75,12,1,CORE},{10,4,.5,11.5,12,1,CORE},
  {2,14,2,14,15,14,TOP}
 };
 private ReactorShape() {}
 public static double[] rotate(double[] part,int facing) {
  double x0=part[0]/16,y0=part[1]/16,z0=part[2]/16,x1=part[3]/16,y1=part[4]/16,z1=part[5]/16;
  if(facing==3) return new double[]{1-x1,y0,1-z1,1-x0,y1,1-z0};
  if(facing==4) return new double[]{z0,y0,1-x1,z1,y1,1-x0};
  if(facing==5) return new double[]{1-z1,y0,x0,1-z0,y1,x1};
  return new double[]{x0,y0,z0,x1,y1,z1};
 }
}
