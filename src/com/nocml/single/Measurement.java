//package com.nocml.single;
//
//
//import com.nocml.calculation.GeoHash;
//import com.nocml.calculation.TraClus;
//import com.nocml.pojo.*;
//import org.apache.flink.api.common.functions.MapFunction;
//import org.apache.flink.api.common.state.*;
//import org.apache.flink.api.java.functions.KeySelector;
//import org.apache.flink.api.java.tuple.*;
//import org.apache.flink.configuration.Configuration;
//import org.apache.flink.streaming.api.TimeCharacteristic;
//import org.apache.flink.streaming.api.datastream.DataStreamSource;
//import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
//import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
//import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
//import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
//import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
//import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
//import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
//import org.apache.flink.streaming.api.functions.windowing.RichAllWindowFunction;
//import org.apache.flink.streaming.api.functions.windowing.RichWindowFunction;
//import org.apache.flink.streaming.api.watermark.Watermark;
//import org.apache.flink.streaming.api.windowing.time.Time;
//import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
//import org.apache.flink.util.Collector;
//
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.concurrent.LinkedTransferQueue;
//
//import static com.nocml.calculation.Config.*;
//
//public class Measurement{
//    public static void main(String[] args) throws Exception {
//        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//        env.setParallelism(1);
//        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
//        env.getConfig().registerPojoType(com.nocml.pojo.Trajectory.class);
//        env.getConfig().registerPojoType(com.nocml.pojo.Line.class);
//        env.getConfig().registerPojoType(com.nocml.pojo.Point.class);
//        env.getConfig().registerPojoType(com.nocml.pojo.TF.class);
//        env.getConfig().registerPojoType(com.nocml.pojo.LF.class);
//        String file;
//        if (args.length != 0){
////            System.out.println(111);
//            file = args[0];
//        }else {
//            file=datasourceFile;
////            System.out.println(222);
//
//        }
//        DataStreamSource<String> stringDataStreamSource = env.readTextFile(file);
//        stringDataStreamSource.map(new MapFunction<String, Tuple5<String, String, Long, Double, Double>>(){
//            @Override
//            //(lat, lon) => (small, large)
//            public Tuple5<String, String, Long, Double, Double> map(String s) throws Exception {
//                String[] t = s.split(",");
//                return new Tuple5<String, String, Long, Double, Double>(t[0],t[1],Long.parseLong(t[2]+"000"),Double.parseDouble(t[4]),Double.parseDouble(t[3]));
//            }
//        }).writeAsCsv("/Users/chenliang/Downloads/10xlsx.csv");
//
//        long start = System.currentTimeMillis();
//        System.out.println("Start:"+start);
//        env.execute("1");
//        long end = System.currentTimeMillis();
//        System.out.println("Total time consumed:" + (end-start));
//    }
//
//
//}