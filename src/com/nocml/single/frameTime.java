package com.nocml.single;

import com.nocml.calculation.GeoHash;
import com.nocml.calculation.HashGrid;
import com.nocml.calculation.TraClus;
import com.nocml.pojo.*;
import com.twitter.chill.Tuple2LongDoubleSerializer;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.Partitioner;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.*;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.functions.windowing.RichAllWindowFunction;
import org.apache.flink.streaming.api.functions.windowing.RichWindowFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.kafka.common.protocol.types.Field;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedTransferQueue;

import static com.nocml.calculation.Config.*;

public class frameTime{
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(5);
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        env.getConfig().registerPojoType(com.nocml.pojo.Trajectory.class);
        env.getConfig().registerPojoType(com.nocml.pojo.Line.class);
        env.getConfig().registerPojoType(com.nocml.pojo.Point.class);
        env.getConfig().registerPojoType(com.nocml.pojo.TF.class);
        env.getConfig().registerPojoType(com.nocml.pojo.LF.class);

        DataStreamSource<String> stringDataStreamSource = env.readTextFile("./data/1.txt");

        long start = System.currentTimeMillis();
        System.out.println("Start:"+start);
        env.execute("1");
        long end = System.currentTimeMillis();
        System.out.println("Total time consumed:" + (end-start));
    }

    private static class myKeySelector implements org.apache.flink.api.java.functions.KeySelector<Tuple4<String, String, Long, Point>, String> {
        @Override
        public String getKey(Tuple4<String, String, Long, Point> l) throws Exception {
            return l.f0;
        }
    }

}