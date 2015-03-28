package com.leav.worldtree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.leav.worldtree.Tree.GrowthPoint;

import scala.xml.dtd.impl.WordExp.Letter;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.FMLLog;

public class MathHelper {

    public static class Pair<E> {
        public E a;
        public E b;

        public boolean contains(E o) {
            return (a.equals(o) || b.equals(o));
        }

        public boolean same() {
            return a.equals(b);
        }
        
        public Pair() {
            // TODO Auto-generated constructor stub
        }
        
        public Pair(E a, E b) {
            this.a = a;
            this.b = b;
        }
        
        public Pair(List<E> c) {
            if (c.size() >= 1) {
                a = c.get(0);
            }
            if (c.size() >= 2) {
                b = c.get(1);
            }
            
        }
    }
    
    public static interface IConstraint {
        public boolean contains(Vec3 v);
        /**
         * 
         * @return ratio of volume in box
         */
        public double volumeRatio();
    }
    
    public static class ConstraintEllipsoid implements IConstraint {
        
        double aSquared, bSquared, cSquared;
        public ConstraintEllipsoid(double a, double b, double c) {
             this.aSquared = a * a;
             this.bSquared = b * b;
             this.cSquared = c * c;
        }
        
        @Override
        public boolean contains(Vec3 v) {
            return (v.xCoord * v.xCoord / this.aSquared + v.yCoord * v.yCoord / this.bSquared + v.zCoord * v.zCoord / this.cSquared <= 1);
        }

        @Override
        public double volumeRatio() {
            return Math.PI * 4 / 3 / 8;
        }
    }
    
    /**
     * tip must be above ground
     * @author leav
     *
     */
    public static class ConstraintCone implements IConstraint {

        Vec3 tip;
        double x, z;
        
        public ConstraintCone(Vec3 tip, double x, double z) {
            this.tip = tip;
            this.x = x;
            this.z = z;
        }
        
        @Override
        public boolean contains(Vec3 v) {
            if (v.yCoord < 0 || v.yCoord > tip.yCoord) return false;
            double yRatio = 1 - v.yCoord / tip.yCoord;
            double xV = x * yRatio;
            double zV = z * yRatio;
            return v.xCoord * v.xCoord / (xV * xV) + v.zCoord * v.zCoord / (zV * zV) <= 1;
        }

        @Override
        public double volumeRatio() {
            return Math.PI / 12;
        }
        
    }

    private static Random random = new Random();

    public static Random random() {
        return random;
    }

    public static int manhattanDist(BlockPos a, BlockPos b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY())
                + Math.abs(a.getZ() - b.getZ());
    }

    public static double dist(BlockPos a, BlockPos b) {
        return Math.sqrt((a.getX() - b.getX()) * (a.getX() - b.getX())
                + (a.getY() - b.getY()) * (a.getY() - b.getY())
                + (a.getZ() - b.getZ()) * (a.getZ() - b.getZ()));
    }

    public static double distSquared(BlockPos a, BlockPos b) {
        return (a.getX() - b.getX()) * (a.getX() - b.getX())
                + (a.getY() - b.getY()) * (a.getY() - b.getY())
                + (a.getZ() - b.getZ()) * (a.getZ() - b.getZ());
    }

    public static Vec3 multiply(Vec3 v, double m) {
        return new Vec3(v.xCoord * m, v.yCoord * m, v.zCoord * m);
    }

    public static Vec3 average(Collection<Vec3> points) {
        double x = 0, y = 0, z = 0;
        double size = points.size();
        assert size > 0;
        for (Vec3 p : points) {
            x += p.xCoord;
            y += p.yCoord;
            z += p.zCoord;
        }
        return new Vec3(x / size, y / size, z / size);
    }

    /**
     * @param p
     * @param s0
     * @param s1
     * @return The square distance from point p to segment s0-s1
     */
    public static double distToSegmentSquared(Vec3 p, Vec3 s0, Vec3 s1) {
        Vec3 v = s1.subtract(s0);
        Vec3 w = p.subtract(s0);
        double c1 = v.dotProduct(w);
        if (c1 <= 0) {
            return p.squareDistanceTo(s0);
        }
        double c2 = v.dotProduct(v);
        if (c2 <= c1) {
            return p.squareDistanceTo(s1);
        }
        double b = c1 / c2;
        Vec3 pb = s0.add(multiply(v, b));
        return p.squareDistanceTo(pb);
    }

    /**
     * @param p
     * @param s0
     * @param s1
     * @return The distance from point p to segment s0-s1
     */
    public static double distToSegment(Vec3 p, Vec3 s0, Vec3 s1) {
        return Math.sqrt(distToSegmentSquared(p, s0, s1));
    }
    
    public static double getRandomVariance(double d, double variance)
    {
        return d * (1 + (random().nextBoolean() ? 1 : -1) * random().nextDouble() * variance);
    }
    
    public static double getRandomRange(double d, double range)
    {
        return d + (random().nextBoolean() ? 1 : -1) * random().nextDouble() * range;
    }
    
    public static List<Vec3> getIntVec3sInConstraint(double minX, double maxX, double minY, double maxY, double minZ, double maxZ,
            Collection<IConstraint> constraints)
    {
        ArrayList<Vec3> result = new ArrayList<Vec3>();
        for (int x = (int) Math.ceil(minX); x <= maxX; x++) {
            for (int y = (int) Math.ceil(minY); y <= maxY; y++) {
                for (int z = (int) Math.ceil(minZ); z <= maxZ; z++) {
                    boolean contains = true;
                    for (IConstraint c : constraints) {
                        if (!c.contains(new Vec3(x, y, z))) {
                            contains = false;
                            break;
                        }
                    }
                    if (contains) {
                        result.add(new Vec3(x, y, z));
                    }
                }   
            }
        }
        return result;
    }

    public static Vec3 getRandomVec3InSphere(double r) {
        if (r == 0) {
            return new Vec3(0, 0, 0);
        }

        double rSquared = r * r;
        int i = 0;
        while (true) {
            double x = random().nextDouble()
                    * (random().nextBoolean() ? 1 : -1) * r;
            double y = random().nextDouble()
                    * (random().nextBoolean() ? 1 : -1) * r;
            double z = random().nextDouble()
                    * (random().nextBoolean() ? 1 : -1) * r;
            if (x * x + y * y + z * z <= rSquared) {
                return new Vec3(x, y, z);
            }
            i++;
            assert i < 100;
        }
    }
    
    public static Vec3 getRandomVec3InBox(double minX, double maxX, double minY, double maxY, double minZ, double maxZ) {
        return new Vec3(
                random().nextDouble() * (maxX - minX) + minX,
                random().nextDouble() * (maxY - minY) + minY,
                random().nextDouble() * (maxZ - minZ) + minZ);
    }
    
    public static Vec3 getRandomVec3(double minX, double maxX, double minY, double maxY, double minZ, double maxZ,
            Collection<IConstraint> constraints) {
        double numberOfTries = 100;
        for (IConstraint c : constraints) {
            numberOfTries /= c.volumeRatio();
        }
        
        for (; numberOfTries > 0; numberOfTries--)
        {
            boolean found = true;
            Vec3 v = getRandomVec3InBox(minX, maxX, minY, maxY, minZ, maxZ);
            for (IConstraint c : constraints) {
                if (!c.contains(v)) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return v;   
            }
        }
        
        FMLLog.severe("getRandomVec3() exceeds number of tries. %s", Arrays.toString(Thread.currentThread().getStackTrace()));
        return new Vec3(0, 0, 0);
    }

    /**
     * Get closest approach by p-r and q-s
     * 
     * @param p
     * @param r
     * @param q
     * @param s
     * @return null if parallel
     */
    public static Pair<Vec3> getClosestApproach(Vec3 p, Vec3 r, Vec3 q, Vec3 s) {
        Vec3 rxs = r.crossProduct(s);
        double rxsLength = rxs.lengthVector();
        if (rxsLength == 0)
            return null;
        Vec3 qps = q.subtract(p).crossProduct(s);
        double t = qps.lengthVector() / rxsLength;
        Vec3 qpr = q.subtract(p).crossProduct(r);
        double u = qpr.lengthVector() / rxsLength;
        Pair<Vec3> result = new Pair<Vec3>();
        result.a = p.add(multiply(r, t));
        result.b = q.add(multiply(s, u));
        return result;
    }

    public static Vec3 getRandomDirection() {
        return getRandomVec3InSphere(1.0).normalize();
    }

    public static List<Vec3> getPointsWithinDist(Collection<Vec3> points,
            Vec3 origin, double d) {
        ArrayList<Vec3> result = new ArrayList<Vec3>();
        double dSquared = d * d;
        for (Vec3 p : points) {
            if (origin.squareDistanceTo(p) <= dSquared) {
                result.add(p);
            }
        }
        return result;
    }

    public static Pair<Vec3> getClosestPair(List<Vec3> points) {
        int size = points.size();
        if (size <= 1)
            return null;
        Pair<Vec3> result = new Pair<Vec3>();
        if (size == 2) {
            result.a = points.get(0);
            result.b = points.get(1);
            return result;
        }
        double min = Double.MAX_VALUE;
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                double dist = points.get(i).squareDistanceTo(points.get(j));
                if (dist < min) {
                    result.a = points.get(i);
                    result.b = points.get(j);
                    min = dist;
                }
            }
        }
        assert result.a != null;
        assert result.b != null;
        return result;
    }

    public static Pair<Tree.GrowthPoint> getClosestPairGrowthPointSlow(
            List<Tree.GrowthPoint> points) {
        int size = points.size();
        if (size <= 1)
            return null;
        Pair<Tree.GrowthPoint> result = new Pair<Tree.GrowthPoint>();
        if (size == 2) {
            result.a = points.get(0);
            result.b = points.get(1);
            return result;
        }
        double min = Double.MAX_VALUE;
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                double dist = points.get(i).squareDistanceTo(points.get(j));
                if (dist < min) {
                    result.a = points.get(i);
                    result.b = points.get(j);
                    min = dist;
                }
            }
        }
        assert result.a != null;
        assert result.b != null;
        return result;
    }
    
    public static Pair<Tree.GrowthPoint> getClosestPairGrowthPoint(List<Tree.GrowthPoint> points) {
        GetClosestPairGrowthPointResult result = getClosestPairGrowthPointInternal(points);
        return result != null ? result.pair : null;
    }
    
    public static class GetClosestPairGrowthPointResult {
        Pair<Tree.GrowthPoint> pair;
        double distSquared;
        public GetClosestPairGrowthPointResult(Pair<Tree.GrowthPoint> pair, double distSquared) {
            this.pair = pair;
            this.distSquared = distSquared;
        }
    }
    
    public static GetClosestPairGrowthPointResult getClosestPairGrowthPointInternal(List<Tree.GrowthPoint> points) {
        if (points.size() <= 1)
        {
            return null;
        }
        if (points.size() == 2)
        {
            return new GetClosestPairGrowthPointResult(new Pair<Tree.GrowthPoint>(points), points.get(0).squareDistanceTo(points.get(1)));
        }
        if (points.size() == 3)
        {
            double distAB = points.get(0).squareDistanceTo(points.get(1));
            double distBC = points.get(1).squareDistanceTo(points.get(2));
            double distAC = points.get(0).squareDistanceTo(points.get(2));
            if (distAB <= distBC && distAB <= distAC) {
                return new GetClosestPairGrowthPointResult(new Pair<Tree.GrowthPoint>(points.get(0), points.get(1)), distAB);
            }
            if (distBC <= distAB && distBC <= distAC) {
                return new GetClosestPairGrowthPointResult(new Pair<Tree.GrowthPoint>(points.get(1), points.get(2)), distBC);
            }
            return new GetClosestPairGrowthPointResult(new Pair<Tree.GrowthPoint>(points.get(0), points.get(2)), distAC);
        }
        List<Tree.GrowthPoint> left = new ArrayList<Tree.GrowthPoint>();
        List<Tree.GrowthPoint> right = new ArrayList<Tree.GrowthPoint>();
        Collections.sort(points, Tree.GrowthPoint.comparatorX);
        int divide = points.size() / 2;
        for (int i = 0; i < divide; i++) {
            left.add(points.get(i));
        }
        for (int i = divide; i < points.size(); i++) {
            right.add(points.get(i));
        }
        GetClosestPairGrowthPointResult resultLeft = getClosestPairGrowthPointInternal(left);
        GetClosestPairGrowthPointResult resultRight = getClosestPairGrowthPointInternal(right);
        double minDist;
        Pair<GrowthPoint> result;
        if (resultLeft.distSquared <= resultRight.distSquared) {
            minDist = resultLeft.distSquared;
            result = resultLeft.pair;
        }
        else {
            minDist = resultRight.distSquared;
            result = resultRight.pair;
        }
        List<Tree.GrowthPoint> middle = new ArrayList<Tree.GrowthPoint>();
        double divideX = points.get(divide).xCoord;
        for (Tree.GrowthPoint p : points) {
            if (p.xCoord >= divideX - minDist && p.xCoord <= divideX + minDist) {
                middle.add(p);
            }
        }
        Collections.sort(middle, Tree.GrowthPoint.comparatorY);
        for (int i = 0; i < middle.size() - 1; i++) {
            for (int j = i + 1; j < middle.size(); j++) {
                GrowthPoint p1 = middle.get(i);
                GrowthPoint p2 = middle.get(j);
                if (p1.yCoord + minDist <= p2.yCoord) {
                    break;
                }
                double distSquared = p1.squareDistanceTo(p2);
                if (distSquared < minDist) {
                    minDist = distSquared;
                    result = new Pair<Tree.GrowthPoint>(p1, p2);
                }
            }
        }
        return new GetClosestPairGrowthPointResult(result, minDist);
    }

    public static Vec3 toVec3(BlockPos pos) {
        return new Vec3(pos.getX(), pos.getY(), pos.getZ());
    }

    public static BlockPos toPos(Vec3 vec3) {
        return new BlockPos(Math.round(vec3.xCoord), Math.round(vec3.yCoord),
                Math.round(vec3.zCoord));
    }

    private static double TO_POS_STEP = 0.9;

    public static Collection<BlockPos> toPos(Vec3 v, Vec3 w) {
        ArrayList<BlockPos> result = new ArrayList<BlockPos>();
        BlockPos last = null;
        Vec3 diff = w.subtract(v);
        Vec3 dir = diff.normalize();
        double dist = diff.lengthVector();
        for (double i = 0; i < dist; i += TO_POS_STEP) {
            BlockPos pos = toPos(v.add(multiply(dir, i)));
            if (!pos.equals(last)) {
                result.add(pos);
                last = pos;
            }
        }
        BlockPos pos = toPos(w);
        if (!pos.equals(last)) {
            result.add(pos);
        }
        return result;
    }

    /**
     * 
     * @param v
     * @param w
     * @param r
     * @return The BlockPos enclosed by the capsule with radius r
     */
    public static Collection<BlockPos> toPos(Vec3 v, Vec3 w, double r) {
        ArrayList<BlockPos> result = new ArrayList<BlockPos>();
        if (r < 0) {
            return result;
        }
        double rSquared = r * r;
        int xMin = (int) Math.floor(Math.min(v.xCoord, w.xCoord) - r);
        int xMax = (int) Math.ceil(Math.max(v.xCoord, w.xCoord) + r);
        int yMin = (int) Math.floor(Math.min(v.yCoord, w.yCoord) - r);
        int yMax = (int) Math.ceil(Math.max(v.yCoord, w.yCoord) + r);
        int zMin = (int) Math.floor(Math.min(v.zCoord, w.zCoord) - r);
        int zMax = (int) Math.ceil(Math.max(v.zCoord, w.zCoord) + r);
        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                for (int z = zMin; z <= zMax; z++) {
                    if (distToSegmentSquared(new Vec3(x, y, z), v, w) <= rSquared) {
                        result.add(new BlockPos(x, y, z));
                    }
                }
            }
        }
        return result;
    }

    /**
     * 
     * @param v
     * @param w
     * @param rv
     *            radius at v
     * @param rw
     *            radius at w
     * @return The BlockPos enclosed by a cone v-w with radius rv at v and
     *         radius rw at w
     */
    public static Collection<BlockPos> toPos(Vec3 v, Vec3 w, double rv,
            double rw) {
        ArrayList<BlockPos> result = new ArrayList<BlockPos>();
        if (rv < 0 || rw < 0) {
            return result;
        }
        double rvSquared = rv * rv;
        double rwSquared = rw * rw;
        double rwv = rw - rv;
        double r = Math.max(rv, rw);
        Vec3 l = w.subtract(v);
        int xMin = (int) Math.floor(Math.min(v.xCoord, w.xCoord) - r);
        int xMax = (int) Math.ceil(Math.max(v.xCoord, w.xCoord) + r);
        int yMin = (int) Math.floor(Math.min(v.yCoord, w.yCoord) - r);
        int yMax = (int) Math.ceil(Math.max(v.yCoord, w.yCoord) + r);
        int zMin = (int) Math.floor(Math.min(v.zCoord, w.zCoord) - r);
        int zMax = (int) Math.ceil(Math.max(v.zCoord, w.zCoord) + r);
        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                for (int z = zMin; z <= zMax; z++) {
                    boolean contains = false;
                    Vec3 p = new Vec3(x, y, z);
                    Vec3 d = p.subtract(v);
                    double c1 = l.dotProduct(d);
                    double c2 = l.dotProduct(l);
                    if (c1 <= 0) {
                        if (p.squareDistanceTo(v) <= rvSquared) {
                            contains = true;
                        }
                    } else if (c2 <= c1) {
                        if (p.squareDistanceTo(w) <= rwSquared) {
                            contains = true;
                        }
                    } else {
                        double b = c1 / c2;
                        Vec3 pb = v.add(multiply(l, b));
                        if (p.distanceTo(pb) <= rv + rwv * b) {
                            contains = true;
                        }
                    }
                    if (contains) {
                        result.add(new BlockPos(p));
                    }
                }
            }
        }
        return result;
    }

    public static void test() {
        Vec3 o = new Vec3(0, 0, 0);
        Vec3 a = new Vec3(1, 1, 1);
        Vec3 b = new Vec3(5, 5, 5);
        List<Vec3> list = new ArrayList<Vec3>();
        list.add(o);
        list.add(a);
        list.add(b);
        List<Vec3> listResult = new ArrayList<Vec3>();

        // average
        assert average(list).equals(new Vec3(2, 2, 2));
        // getPointsWithinDist
        listResult = getPointsWithinDist(list, new Vec3(0.1, 0, 0), 0.1);
        assert listResult.size() == 1;
        for (Vec3 v : listResult) {
            assert v.equals(o);
        }
        // getClosestPair
        Pair<Vec3> pair = getClosestPair(list);
        assert pair.contains(o);
        assert pair.contains(a);
        assert !pair.contains(b);
        // getClosestApproach
        pair = getClosestApproach(new Vec3(0, 0, 0), new Vec3(0, 1, 0),
                new Vec3(3, 0, 0), new Vec3(0, 4, 0));
        assert pair.same();
        assert pair.contains(new Vec3(0, 4, 0));

        FMLLog.info("MathHelper.test() done");
    }
}
