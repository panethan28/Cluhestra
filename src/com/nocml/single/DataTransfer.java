package com.nocml.single;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.*;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.util.ArrayList;

public class DataTransfer {
    private final static String rFile = "/Users/chenliang/Downloads/10";
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1).setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        SingleOutputStreamOperator<Double> test = env.readTextFile(rFile).map(new MapFunction<String, Tuple3<String, Long, Long>>() {
            @Override
            public Tuple3<String, Long, Long> map(String s) throws Exception {
                String[] con = s.split(",");
                return new Tuple3<String, Long, Long>(con[0],Long.parseLong(con[2] + "000"), 1L);
            }
        }).keyBy(0).map(new MapFunction<Tuple3<String, Long, Long>, Tuple2<Long, Long>>() {
            @Override
            public Tuple2<Long, Long> map(Tuple3<String, Long, Long> stringLongLongTuple3) throws Exception {
                return new Tuple2<Long,Long>(stringLongLongTuple3.f1, stringLongLongTuple3.f2);
            }
        }).assignTimestampsAndWatermarks(new BoundedOutOfOrdernessTimestampExtractor<Tuple2<Long, Long>>(Time.seconds(5)) {
            @Override
            public long extractTimestamp(Tuple2<Long, Long> longLongTuple2) {
                return longLongTuple2.f0;
            }
        }).timeWindowAll(Time.minutes(60), Time.minutes(10)).process(new ProcessAllWindowFunction<Tuple2<Long, Long>, Tuple2<Long, Long>, TimeWindow>() {
            private ValueState<Tuple2<Long, Long>> vs;
            private ValueState<Long> last;

            @Override
            public void open(Configuration parameters) throws Exception {
                super.open(parameters);
                ValueStateDescriptor des = new ValueStateDescriptor("timeAvg", TypeInformation.of(new TypeHint<Tuple2<Long, Long>>() {
                }));
                vs = getRuntimeContext().getState(des);
                ValueStateDescriptor las = new ValueStateDescriptor("lastTime", TypeInformation.of(new TypeHint<Long>() {
                }));
                last = getRuntimeContext().getState(las);
            }

            @Override
            public void process(Context context, Iterable<Tuple2<Long, Long>> iterable, Collector<Tuple2<Long, Long>> collector) throws Exception {
                Tuple2<Long, Long> tmp = vs.value();
                Long tmp1 = last.value();
                if(tmp==null){
                    vs.update(iterable.iterator().next());
                    last.update(iterable.iterator().next().f0);
                    collector.collect(vs.value());
                }
                else if (iterable.iterator().hasNext()) {
                    Long center = iterable.iterator().next().f0;

                    vs.update(new Tuple2<Long, Long>(tmp.f0 + center - tmp1, tmp.f1 + 1L));
                    last.update(center);
                    collector.collect(vs.value());
                }

            }
        }).map(new MapFunction<Tuple2<Long, Long>, Double>() {
            @Override
            public Double map(Tuple2<Long, Long> longLongTuple2) throws Exception {
                return longLongTuple2.f0 * 1.0 / longLongTuple2.f1;
            }
        });

        test.print();

        env.execute("okok");
    }
}
