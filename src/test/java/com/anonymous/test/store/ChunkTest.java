package com.anonymous.test.store;

import com.anonymous.test.benchmark.PortoTaxiRealData;
import com.anonymous.test.common.TrajectoryPoint;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ChunkTest {

    @Test
    public void deserialize() {


        PortoTaxiRealData data = new PortoTaxiRealData("/home/anonymous/Data/DataSet/Trajectory/TaxiPorto/archive/porto_data_v1_1000w.csv");
        TrajectoryPoint point;
        List<TrajectoryPoint> dataBatch = new ArrayList<>();
        List<String> chunkStringList = new ArrayList<>();
        while ((point = data.nextPointFromPortoTaxis()) != null) {
            dataBatch.add(point);
            if (dataBatch.size() == 100) {
                Chunk chunk = new Chunk("testsid", "testchunkid");
                chunk.setChunk(dataBatch);
                String chunkString = Chunk.serialize(chunk);
                chunkStringList.add(chunkString);
                dataBatch.clear();
            }
            if (chunkStringList.size() > 1000) {
                break;
            }
        }

        long start = System.currentTimeMillis();

        for (String chunkString : chunkStringList) {
            Chunk.deserialize(chunkString);
        }
        long stop = System.currentTimeMillis();
        System.out.println("deserialize takes " + (stop - start) + " ms");

    }

    @Test
    public void deserialize2() {
        PortoTaxiRealData data = new PortoTaxiRealData("/home/anonymous/Data/DataSet/Trajectory/TaxiPorto/archive/porto_data_v1_1000w.csv");
        TrajectoryPoint point;

        List<String> stringList = new ArrayList<>();
        while ((point = data.nextPointFromPortoTaxis()) != null) {
            stringList.add(point.toString());
            if (stringList.size() > 100000) {
                break;
            }
        }

        long start = System.currentTimeMillis();

        for (String string : stringList) {
            string.split(",");
        }
        long stop = System.currentTimeMillis();
        System.out.println("deserialize takes " + (stop - start) + " ms");
    }

    @Test
    public void deserialize3() {
        PortoTaxiRealData data = new PortoTaxiRealData("/home/anonymous/Data/DataSet/Trajectory/TaxiPorto/archive/porto_data_v1_1000w.csv");
        TrajectoryPoint point;

        List<String> stringList = new ArrayList<>();
        while ((point = data.nextPointFromPortoTaxis()) != null) {
            String string = String.format("%s,%f,%f,%d,%s", point.getOid(), point.getLongitude(), point.getLatitude(), point.getTimestamp(), point.getPayload());
            stringList.add(string);
            if (stringList.size() > 100000) {
                break;
            }

        }

        long start = System.currentTimeMillis();

        for (String string : stringList) {
            string.split(",");
        }
        long stop = System.currentTimeMillis();
        System.out.println("deserialize takes " + (stop - start) + " ms");
    }

}