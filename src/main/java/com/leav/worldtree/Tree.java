package com.leav.worldtree;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.message.LoggerNameAwareMessage;

import com.leav.worldtree.MathHelper.IConstraint;
import com.leav.worldtree.MathHelper.Pair;

import oracle.jrockit.jfr.settings.PresetFile;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class Tree {

    public static class GrowthPoint extends Vec3 {
        public static Comparator<GrowthPoint> comparatorX = new Comparator<GrowthPoint>() {
            @Override
            public int compare(GrowthPoint a, GrowthPoint b) {
                return Double.compare(a.xCoord, b.xCoord);
            }
        };
        public static Comparator<GrowthPoint> comparatorY = new Comparator<GrowthPoint>() {
            @Override
            public int compare(GrowthPoint a, GrowthPoint b) {
                return Double.compare(a.yCoord, b.yCoord);
            }
        };
        
        public double weight = 0;
        public ArrayList<GrowthPoint> children = new ArrayList<GrowthPoint>();

        public GrowthPoint(Vec3 vec) {
            super(vec.xCoord, vec.yCoord, vec.zCoord);
        }

        public GrowthPoint(double x, double y, double z) {
            super(x, y, z);
        }

        public void addChild(GrowthPoint p) {
            children.add(p);
            this.weight += p.weight + Math.pow(this.subtract(p).lengthVector(), 3);
        }

        public Collection<BlockPos> toPos(double a, double b) {
            ArrayList<BlockPos> result = new ArrayList<BlockPos>();
            for (GrowthPoint p : children) {
                result.addAll(MathHelper.toPos(p, this, a
                        * p.weight + b, a * this.weight + b));
                result.addAll(p.toPos(a, b));
            }
            return result;
        }

        /**
         * 
         * @param tipRadius
         * @param trunkRadius
         * @return BlockPos of branches formed by this and its children
         */
        public Collection<BlockPos> toPosWithSize(double tipRadius,
                double trunkRadius) {
            double a = (trunkRadius - tipRadius) / this.weight;
            double b = tipRadius;
            return toPos(a, b);
        }

        /**
         * 
         * @return BlockPos of this and its children
         */
        public Collection<BlockPos> toPosPointOnly() {
            ArrayList<BlockPos> result = new ArrayList<BlockPos>();
            result.add(MathHelper.toPos(this));
            for (GrowthPoint p : children) {
                result.addAll(p.toPosPointOnly());
            }
            return result;
        }
    }

    private static BlockCache cache = new BlockCache();
    private static Random random = new Random();

    public static void generateSimple(World world, BlockPos pos) {
        // generate(world, x, y, z, size, Block.getBlockFromName("log"),
        // Block.getBlockFromName("leaves"));
        Tree tree = new Tree(world, pos);
        tree.generate();
    }

    public static void revert(World world) {
        cache.revert(world);
    }
    
    public static IBlockState log() {
        return Block.getBlockFromName("log").getDefaultState();
    }

    public static IBlockState marker() {
        return Block.getBlockFromName("sea_lantern").getDefaultState();
    }

    // instance methods

    World world;

    @CommandLineAnonymous
    public String preset = "Oak";
    @CommandLine
    public BlockPos ground = new BlockPos(0, 0, 0);
    @CommandLine
    public String log = "Oak";
    @CommandLine
    public String logBlock = "log";
    @CommandLine
    public int logMeta = 0;
    @CommandLine
    public String leaves = "Oak";
    @CommandLine
    public String leavesBlock = "leaves";
    @CommandLine
    public int leavesMeta = 4;
    @CommandLineEarly
    public double size = 20;
    @CommandLine
    public String crownShape = "Ellipsoid";
    @CommandLine
    @HasRange
    public double trunkRadius = 2.0;
    @CommandLine
    public double trunkRadiusRange = 1.0;
    @CommandLine
    @HasRange
    public double tipRadius = 0.5;
    @CommandLine
    public double tipRadiusRange = 0.1;
    @CommandLine
    @HasRange
    public Vec3 crownOrigin = new Vec3(0, 20, 0);
    @CommandLine
    public Vec3 crownOriginRange = new Vec3(2, 2, 2);
    @CommandLine
    @HasRange
    public Vec3 crownTip = new Vec3(0, 5, 0);
    @CommandLine
    public Vec3 crownTipRange = new Vec3(1, 1, 1);
    @CommandLine
    @HasRange
    public double crownRadiusX = 10;
    @CommandLine
    public double crownRadiusXRange = 2;
    @CommandLine
    @HasRange
    public double crownRadiusLowerY = 1;
    @CommandLine
    public double crownRadiusLowerYRange = 0.2;
    @CommandLine
    @HasRange
    public double crownRadiusUpperY = 5;
    @CommandLine
    public double crownRadiusUpperYRange = 1;
    @CommandLine
    @HasRange
    public double crownRadiusZ = 10;
    @CommandLine
    public double crownRadiusZRange = 2;
    @CommandLine
    public double leavesRadiusX = 4;
    @CommandLine
    public double leavesRadiusXRange = 1; 
    @CommandLine
    public double leavesRadiusLowerY = 1;
    @CommandLine
    public double leavesRadiusLowerYRange = 0.25;
    @CommandLine
    public double leavesRadiusUpperY = 3;
    @CommandLine
    public double leavesRadiusUpperYRange = 0.25;
    @CommandLine
    public double leavesRadiusZ = 4;
    @CommandLine
    public double leavesRadiusZRange = 1;
    @CommandLine
    public double growthPointRatio = 0.01;
    @CommandLine
    public double branchRootRatio = -1; // 1.0 leans towards the branch
                                        // completely, 0.0 leans towards the
                                        // root completely; negative is adding
                                        // unit vector only
    @CommandLine
    public int minGrowthPoints = 3;
    
    ArrayList<IConstraint> constraintCrown = new ArrayList<IConstraint>();
    ArrayList<IConstraint> constraintLeaves = new ArrayList<IConstraint>();
    

    public Tree(World world, BlockPos ground) {
        this.world = world;
        this.ground = ground;
    }
    
    public IBlockState getLogBlockState() {
        return Block.getBlockFromName(logBlock).getStateFromMeta(logMeta);
    }
    
    public IBlockState getLeavesBlockState() {
        return Block.getBlockFromName(leavesBlock).getStateFromMeta(leavesMeta);
    }

    public void generate() {
        ArrayList<GrowthPoint> growthPoints = new ArrayList<GrowthPoint>();
        ArrayList<Pair<GrowthPoint>> branches = new ArrayList<Pair<GrowthPoint>>();
        Vec3 srcVec = MathHelper.toVec3(ground);

        processData();

        int numGrowthPoints = (int) Math.max((crownRadiusX * crownRadiusUpperY
                * crownRadiusZ * 8 * growthPointRatio), minGrowthPoints);
        Vec3 crownOriginWorld = srcVec.add(crownOrigin);
        for (int i = 0; i < numGrowthPoints; i++) {
            Vec3 randomVec3 = MathHelper.getRandomVec3(-crownRadiusX,
                    crownRadiusX, -crownRadiusLowerY, crownRadiusUpperY,
                    -crownRadiusZ, crownRadiusZ, constraintCrown);
            Vec3 vec = crownOriginWorld.add(randomVec3);
            GrowthPoint p = new GrowthPoint(vec);
            p.weight = 1;
            growthPoints.add(p);
        }
        for (GrowthPoint tip : growthPoints) {
            generateLeaves(tip);
        }
        while (growthPoints.size() > 1) {
            Pair<GrowthPoint> pair = MathHelper.getClosestPairGrowthPoint(growthPoints);
            Vec3 branchFactorA, rootFactorA, branchFactorB, rootFactorB;
            if (branchRootRatio >= 0) {
                branchFactorA = MathHelper.multiply(pair.b.subtract(pair.a),
                        branchRootRatio);
                rootFactorA = MathHelper.multiply(srcVec.subtract(pair.a),
                        1 - branchRootRatio);
                rootFactorB = MathHelper.multiply(srcVec.subtract(pair.b),
                        1 - branchRootRatio);
            } else {
                branchFactorA = pair.b.subtract(pair.a).normalize();
                rootFactorA = srcVec.subtract(pair.a).normalize();
                rootFactorB = srcVec.subtract(pair.b).normalize();
            }
            branchFactorB = MathHelper.multiply(branchFactorA, -1);

            Pair<Vec3> merge = MathHelper.getClosestApproach(pair.a,
                    branchFactorA.add(rootFactorA), pair.b,
                    branchFactorB.add(rootFactorB));
            if (merge != null) {
                GrowthPoint mergedGrowthPoint = new GrowthPoint(merge.a);
                mergedGrowthPoint.addChild(pair.a);
                mergedGrowthPoint.addChild(pair.b);

                growthPoints.add(mergedGrowthPoint);
                growthPoints.remove(pair.a);
                growthPoints.remove(pair.b);
            } else {
                growthPoints.remove(pair.a);
                pair.b.addChild(pair.a);
            }
        }

        GrowthPoint trunk = growthPoints.get(0);
        GrowthPoint srcPoint = new GrowthPoint(srcVec);
        srcPoint.addChild(trunk);

        cache.put(srcPoint.toPosWithSize(tipRadius, trunkRadius), getLogBlockState());

        cache.apply(world);
    }
    
    public void generateLeaves(Vec3 origin) {
        double x = MathHelper.getRandomRange(leavesRadiusX, leavesRadiusXRange);
        double lowerY = MathHelper.getRandomRange(leavesRadiusLowerY, leavesRadiusLowerYRange);
        double upperY = MathHelper.getRandomRange(leavesRadiusUpperY, leavesRadiusUpperYRange);
        double z = MathHelper.getRandomRange(leavesRadiusZ, leavesRadiusZRange);
        List<Vec3> poses = MathHelper.getIntVec3sInConstraint(-x, x,
                -lowerY, upperY,
                -z, z, constraintLeaves);
        Vec3 leavesOrigin = new Vec3(origin.xCoord, origin.yCoord + 1, origin.zCoord);
        for (Vec3 pos : poses) {
            cache.put(new BlockPos(pos.add(leavesOrigin)), getLeavesBlockState());
        }
    }

    public void processData() {
        if (log.equalsIgnoreCase("Oak")) {
            logBlock = "log";
            logMeta = 0;
        }
        else if (log.equalsIgnoreCase("Spruce")) {
            logBlock = "log";
            logMeta = 1;
        }
        else if (log.equalsIgnoreCase("Birch")) {
            logBlock = "log";
            logMeta = 2;
        }
        else if (log.equalsIgnoreCase("Jungle")) {
            logBlock = "log";
            logMeta = 3;
        }
        else if (log.equalsIgnoreCase("Acacia")) {
            logBlock = "log2";
            logMeta = 0;
        }
        else if (log.equalsIgnoreCase("DarkOak")) {
            logBlock = "log2";
            logMeta = 1;
        }
        else {
            logBlock = "log";
            logMeta = 0;
        }
        
        if (leaves.equalsIgnoreCase("Oak")) {
            leavesBlock = "leaves";
            leavesMeta = 4;
        }
        else if (leaves.equalsIgnoreCase("Spruce")) {
            leavesBlock = "leaves";
            leavesMeta = 5;
        }
        else if (leaves.equalsIgnoreCase("Birch")) {
            leavesBlock = "leaves";
            leavesMeta = 6;
        }
        else if (leaves.equalsIgnoreCase("Jungle")) {
            leavesBlock = "leaves";
            leavesMeta = 7;
        }
        else if (leaves.equalsIgnoreCase("Acacia")) {
            leavesBlock = "leaves2";
            leavesMeta = 4;
        }
        else if (leaves.equalsIgnoreCase("DarkOak")) {
            leavesBlock = "leaves2";
            leavesMeta = 5;
        }
        else {
            leavesBlock = "leaves";
            leavesMeta = 4;
        }
        
        for (Field f : this.getClass().getDeclaredFields()) {
            if (f.getAnnotation(HasRange.class) != null) {
                try {
                    Field range = this.getClass().getDeclaredField(f.getName() + "Range");
                    if (f.getType().isAssignableFrom(Double.TYPE) && range.getType().isAssignableFrom(Double.TYPE)) {
                        f.setDouble(this, MathHelper.getRandomRange(f.getDouble(this), range.getDouble(this)));
                    }
                    else if (f.getType().isAssignableFrom(Vec3.class) && range.getType().isAssignableFrom(Vec3.class)) {
                        Vec3 fVec3 = (Vec3) f.get(this);
                        Vec3 rangeVec3 = (Vec3) range.get(this);
                        f.set(this, new Vec3(
                                MathHelper.getRandomRange(fVec3.xCoord, rangeVec3.xCoord),
                                MathHelper.getRandomRange(fVec3.yCoord, rangeVec3.yCoord),
                                MathHelper.getRandomRange(fVec3.zCoord, rangeVec3.zCoord)
                                ));
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        
//        crownRadiusX = MathHelper.getRandomRange(crownRadiusX, crownRadiusXRange);
//        crownRadiusLowerY = MathHelper.getRandomRange(crownRadiusLowerY, crownRadiusLowerYRange);
//        crownRadiusUpperY = MathHelper.getRandomRange(crownRadiusUpperY, crownRadiusUpperYRange);
//        crownRadiusZ = MathHelper.getRandomRange(crownRadiusZ, crownRadiusZRange);
//        trunkRadius = MathHelper.getRandomRange(trunkRadius, trunkRadiusRange);
//        tipRadius = MathHelper.getRandomRange(tipRadius, tipRadiusRange);
//        crownOrigin = new Vec3(
//                MathHelper.getRandomRange(crownOrigin.xCoord, crownOriginRange.xCoord),
//                MathHelper.getRandomRange(crownOrigin.yCoord, crownOriginRange.yCoord),
//                MathHelper.getRandomRange(crownOrigin.zCoord, crownOriginRange.zCoord));
        
        for (String s : crownShape.split(" ")) {
            if (s.equalsIgnoreCase("Ellipsoid")) {
                constraintCrown.add(new MathHelper.ConstraintEllipsoid(crownRadiusX, crownRadiusUpperY, crownRadiusZ));
            }
            else if (s.equalsIgnoreCase("Cone")) {
                constraintCrown.add(new MathHelper.ConstraintCone(crownTip, crownRadiusX, crownRadiusZ));
                if (crownRadiusUpperY < crownTip.yCoord)
                {
                    crownRadiusUpperY = crownTip.yCoord;
                }
            }
        }
        
        constraintLeaves.add(new MathHelper.ConstraintEllipsoid(leavesRadiusX, leavesRadiusUpperY, leavesRadiusZ));
        
        
    }

    public void applyPreset() {
        if (preset == null || preset.isEmpty()) return;
        
        if (preset.equalsIgnoreCase("Oak") || preset.equalsIgnoreCase("DarkOak")) {
            if (preset.equalsIgnoreCase("Oak")) {
                log = "Oak";
                leaves = "Oak";
            }
            else {
                log = "DarkOak";
                leaves = "DarkOak";
            }
            crownShape = "Ellipsoid";
            trunkRadius = Math.max(size * 0.06, 0.7);
            trunkRadiusRange = trunkRadius * 0.25;
            tipRadius = 0.5;
            tipRadiusRange = 0;
            crownOrigin = new Vec3(0, size * 0.36, 0);
            crownOriginRange = new Vec3(0, size * 0.1, 0);
            crownRadiusX = crownRadiusZ = size * 0.66;
            crownRadiusXRange = crownRadiusZRange = crownRadiusX * 0.3;
            crownRadiusLowerY = size * 0.15;
            crownRadiusLowerYRange = crownRadiusLowerY * 0.3;
            crownRadiusUpperY = size * 0.64;
            crownRadiusUpperYRange = crownRadiusUpperY * 0.3;
            leavesRadiusX = leavesRadiusZ = Math.max(size * 0.1, 2);
            leavesRadiusXRange = leavesRadiusZRange = leavesRadiusX * 0.25;
            leavesRadiusLowerY = Math.max(size * 0.04, 0.8);
            leavesRadiusLowerYRange = leavesRadiusLowerY * 0.25;
            leavesRadiusUpperY = Math.max(size * 0.06, 1.2);
            leavesRadiusUpperYRange = leavesRadiusUpperY * 0.25;
            growthPointRatio = 0.005;
            minGrowthPoints = 4;
        }
        else if (preset.equalsIgnoreCase("Spruce")) {
            log = "Spruce";
            leaves = "Spruce";
            crownShape = "Cone";
            trunkRadius = Math.max(size * 0.06, 0.7);
            trunkRadiusRange = trunkRadius * 0.25;
            tipRadius = 0.1;
            tipRadiusRange = 0;
            crownOrigin = new Vec3(0, Math.max(size * 0.05, 2.0), 0);
            crownOriginRange = new Vec3(0, crownOrigin.yCoord * 0.3, 0);
            crownRadiusX = crownRadiusZ = size * 0.31;
            crownRadiusXRange = crownRadiusZRange = crownRadiusX * 0.2;
            crownRadiusLowerY = size * 0.15;
            crownRadiusLowerYRange = crownRadiusLowerY * 0.3;
            crownRadiusUpperY = size * 0.95;
            crownRadiusUpperYRange = crownRadiusUpperY * 0.3;
            crownTip = new Vec3(0, size * 0.95, 0);
            crownTipRange = new Vec3(0, crownTip.yCoord * 0.3, 0);
            leavesRadiusX = leavesRadiusZ = 1.5;
            leavesRadiusXRange = leavesRadiusZRange = leavesRadiusX * 0.25;
            leavesRadiusLowerY = 0.3;
            leavesRadiusLowerYRange = leavesRadiusLowerY * 0.25;
            leavesRadiusUpperY = 1.25;
            leavesRadiusUpperYRange = leavesRadiusUpperY * 0.25;
            growthPointRatio = 0.01;
            minGrowthPoints = 8;
            branchRootRatio = 0.99;
        }
        else if (preset.equalsIgnoreCase("Birch")) {
            log = "Birch";
            leaves = "Birch";
            crownShape = "Ellipsoid";
            trunkRadius = Math.max(size * 0.03, 1);
            trunkRadiusRange = trunkRadius * 0.25;
            tipRadius = 0.7;
            tipRadiusRange = 0;
            crownOrigin = new Vec3(0, size * 0.5, 0);
            crownOriginRange = new Vec3(0, crownOrigin.yCoord * 0.3, 0);
            crownRadiusX = crownRadiusZ = size * 0.2;
            crownRadiusXRange = crownRadiusZRange = crownRadiusX * 0.3;
            crownRadiusLowerY = size * 0.12;
            crownRadiusLowerYRange = crownRadiusLowerY * 0.3;
            crownRadiusUpperY = size * 0.5;
            crownRadiusUpperYRange = crownRadiusUpperY * 0.3;
            leavesRadiusX = leavesRadiusZ = 2;
            leavesRadiusXRange = leavesRadiusZRange = leavesRadiusX * 0.25;
            leavesRadiusLowerY = 0.3;
            leavesRadiusLowerYRange = leavesRadiusLowerY * 0.25;
            leavesRadiusUpperY = 1.25;
            leavesRadiusUpperYRange = leavesRadiusUpperY * 0.25;
            growthPointRatio = 0.001;
            minGrowthPoints = 3;
            branchRootRatio = 0.99;
        }
        else if (preset.equalsIgnoreCase("Jungle")) {
            log = "Jungle";
            leaves = "Jungle";
            crownShape = "Ellipsoid";
            trunkRadius = Math.max(size * 0.05, 1.25);
            trunkRadiusRange = trunkRadius * 0.25;
            tipRadius = Math.max(size * 0.04, 1);
            tipRadiusRange = 0;
            crownOrigin = new Vec3(0, size * 0.7, 0);
            crownOriginRange = new Vec3(0, crownOrigin.yCoord * 0.3, 0);
            crownRadiusX = crownRadiusZ = size * 0.12;
            crownRadiusXRange = crownRadiusZRange = crownRadiusX * 0.3;
            crownRadiusLowerY = size * 0.3;
            crownRadiusLowerYRange = crownRadiusLowerY * 0.3;
            crownRadiusUpperY = size * 0.3;
            crownRadiusUpperYRange = crownRadiusUpperY * 0.3;
            leavesRadiusX = leavesRadiusZ = Math.max(size * 0.20, 2);
            leavesRadiusXRange = leavesRadiusZRange = leavesRadiusX * 0.25;
            leavesRadiusLowerY = Math.max(size * 0.07, 0.7);
            leavesRadiusLowerYRange = leavesRadiusLowerY * 0.25;
            leavesRadiusUpperY = Math.max(size * 0.13, 1.3);
            leavesRadiusUpperYRange = leavesRadiusUpperY * 0.25;
            growthPointRatio = 0.001;
            minGrowthPoints = 3;
            branchRootRatio = 0.99;
        }
        else if (preset.equalsIgnoreCase("Acacia")) {
            log = "Acacia";
            leaves = "Acacia";
            crownShape = "Ellipsoid";
            trunkRadius = Math.max(size * 0.05, 1.25);
            trunkRadiusRange = trunkRadius * 0.25;
            tipRadius = 0.5;
            tipRadiusRange = 0;
            crownOrigin = new Vec3(0, size * 0.75, 0);
            crownOriginRange = new Vec3(size * 0.2, crownOrigin.yCoord * 0.3, size * 0.2);
            crownRadiusX = crownRadiusZ = size * 0.85;
            crownRadiusXRange = crownRadiusZRange = crownRadiusX * 0.3;
            crownRadiusLowerY = size * 0.1;
            crownRadiusLowerYRange = crownRadiusLowerY * 0.3;
            crownRadiusUpperY = size * 0.25;
            crownRadiusUpperYRange = crownRadiusUpperY * 0.3;
            leavesRadiusX = leavesRadiusZ = Math.max(size * 0.2, 2);
            leavesRadiusXRange = leavesRadiusZRange = leavesRadiusX * 0.25;
            leavesRadiusLowerY = Math.max(size * 0.05, 0.5);
            leavesRadiusLowerYRange = leavesRadiusLowerY * 0.25;
            leavesRadiusUpperY = Math.max(size * 0.1, 1);
            leavesRadiusUpperYRange = leavesRadiusUpperY * 0.25;
            growthPointRatio = 0.003;
            minGrowthPoints = 3;
            branchRootRatio = -1;
        }
    }
}
