package com.anonymous.test.benchmark.headchunk.geohash;

import com.anonymous.test.benchmark.StatusRecorder;
import com.anonymous.test.benchmark.SyntheticDataGenerator;
import com.anonymous.test.common.Point;
import com.anonymous.test.common.SpatialBoundingBox;
import com.anonymous.test.common.TrajectoryPoint;
import com.anonymous.test.store.HeadChunkIndexWithGeoHash;
import com.anonymous.test.store.SeriesStore;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author anonymous
 * @create 2021-12-24 2:49 PM
 **/
public class GeoHashQueryBenchmark {

    @State(Scope.Benchmark)
    public static class BenchmarkStateRandom {
        @Param({"10000000"})
        public long listSize;

        @Param({"500"})
        public int querySetSize;

        @Param({"0.001", "0.01", "0.1"})
        public double queryRegion;

        @Param({"8", "12", "16"})
        public int shiftLength;

        public List<SpatialBoundingBox> queryPredicateList;

        HeadChunkIndexWithGeoHash indexWithGeoHash;

        SeriesStore seriesStore;

        @Setup(Level.Trial)
        public void setup() {
            queryPredicateList = new ArrayList<>();
            indexWithGeoHash = new HeadChunkIndexWithGeoHash(shiftLength);
            seriesStore = SeriesStore.initNewStoreForInMemTest();

            List<TrajectoryPoint> trajectoryPoints = SyntheticDataGenerator.generateRandomDistributedDataset(listSize, 1, 1);

            for (int i = 0; i < trajectoryPoints.size(); i++) {
                TrajectoryPoint point = trajectoryPoints.get(i);
                seriesStore.appendSeriesPoint(point);
                indexWithGeoHash.updateIndex(point);
            }

            Random random = new Random(1);
            for (int i = 0; i < querySetSize; i++) {
                double xLow = random.nextDouble()*1;
                double xHigh = xLow + 1 * queryRegion;
                double yLow = random.nextDouble()*1;
                double yHigh = yLow + 1 * queryRegion;
                queryPredicateList.add(new SpatialBoundingBox(new Point(xLow, yLow), new Point(xHigh, yHigh)));
            }
        }

        @TearDown(Level.Trial)
        public void printStatus() {
            String status = indexWithGeoHash.printStatus();
            StatusRecorder.recordStatus("/home/anonymous/IdeaProjects/trajectory-index/benchmark-log/headchunk/status/geohash-query-random.log", status);
        }

    }

    @State(Scope.Benchmark)
    public static class BenchmarkStateGaussian {
        @Param({"10000000"})
        public long listSize;

        @Param({"500"})
        public int querySetSize;

        @Param({"0.001", "0.01", "0.1"})
        public double queryRegion;

        @Param({"8", "12", "16"})
        public int shiftLength;

        public List<SpatialBoundingBox> queryPredicateList;

        HeadChunkIndexWithGeoHash indexWithGeoHash;

        SeriesStore seriesStore;

        @Setup(Level.Trial)
        public void setup() {
            queryPredicateList = new ArrayList<>();
            indexWithGeoHash = new HeadChunkIndexWithGeoHash(shiftLength);
            seriesStore = SeriesStore.initNewStoreForInMemTest();

            List<TrajectoryPoint> trajectoryPoints = SyntheticDataGenerator.generateGaussianDistributionDataSet(listSize, 1, 1);

            for (int i = 0; i < trajectoryPoints.size(); i++) {
                TrajectoryPoint point = trajectoryPoints.get(i);
                seriesStore.appendSeriesPoint(point);
                indexWithGeoHash.updateIndex(point);
            }

            Random random = new Random(1);
            for (int i = 0; i < querySetSize; i++) {
                int index = random.nextInt(trajectoryPoints.size());
                TrajectoryPoint point = trajectoryPoints.get(index);

                double xLow = point.getLongitude();
                double xHigh = xLow + 1 * queryRegion;
                double yLow = point.getLatitude();
                double yHigh = yLow + 1 * queryRegion;
                queryPredicateList.add(new SpatialBoundingBox(new Point(xLow, yLow), new Point(xHigh, yHigh)));
            }
        }

        @TearDown(Level.Trial)
        public void printStatus() {
            String status = indexWithGeoHash.printStatus();
            StatusRecorder.recordStatus("/home/anonymous/IdeaProjects/trajectory-index/benchmark-log/headchunk/status/geohash-query-gaussian.log", status);
        }

    }

    @Fork(value = 1)
    @Warmup(iterations = 2, time = 5)
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OperationsPerInvocation(500)
    @Measurement(time = 5, iterations = 3)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void queryRandom(Blackhole blackhole, BenchmarkStateRandom stateRandom) {
        for (SpatialBoundingBox spatialBoundingBox : stateRandom.queryPredicateList) {
            Set<String> result = stateRandom.indexWithGeoHash.searchForSpatial(spatialBoundingBox);
            //System.out.println(result.size());
            List<TrajectoryPoint> finalResult = stateRandom.seriesStore.refineReturnPoints(result, spatialBoundingBox);
            //System.out.println(finalResult.size());
            blackhole.consume(finalResult);
        }
    }

    @Fork(value = 1)
    @Warmup(iterations = 2, time = 5)
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OperationsPerInvocation(500)
    @Measurement(time = 5, iterations = 3)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void queryGaussian(Blackhole blackhole, BenchmarkStateGaussian stateGaussian) {
        for (SpatialBoundingBox spatialBoundingBox : stateGaussian.queryPredicateList) {
            Set<String> result = stateGaussian.indexWithGeoHash.searchForSpatial(spatialBoundingBox);
            //System.out.println(result.size());
            List<TrajectoryPoint> finalResult = stateGaussian.seriesStore.refineReturnPoints(result, spatialBoundingBox);
            //System.out.println(finalResult.size());
            blackhole.consume(finalResult);
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(GeoHashQueryBenchmark.class.getSimpleName())
                .output("/home/anonymous/IdeaProjects/trajectory-index/benchmark-log/headchunk/geohash/geohash-query.log")
                .build();

        new Runner(opt).run();
    }

}
