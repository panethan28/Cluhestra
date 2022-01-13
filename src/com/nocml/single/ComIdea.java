package com.nocml.single;


import com.nocml.calculation.GeoHash;
import com.nocml.calculation.TraClus;
import com.nocml.pojo.*;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.*;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.functions.windowing.RichAllWindowFunction;
import org.apache.flink.streaming.api.functions.windowing.RichWindowFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import scala.Int;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.LinkedTransferQueue;

import static com.nocml.calculation.Config.*;

public class ComIdea{
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(5);
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        env.getConfig().registerPojoType(com.nocml.pojo.Trajectory.class);
        env.getConfig().registerPojoType(com.nocml.pojo.Line.class);
        env.getConfig().registerPojoType(com.nocml.pojo.Point.class);
        env.getConfig().registerPojoType(com.nocml.pojo.TF.class);
        env.getConfig().registerPojoType(com.nocml.pojo.LF.class);
        String file;
        if (args.length != 0){
//            System.out.println(111);
            file = args[0];
        }else {
            file=datasourceFile;
//            System.out.println(222);

        }

        DataStreamSource<String> stringDataStreamSource = env.readTextFile(file);
        stringDataStreamSource.map(new MapFunction<String, Tuple4<String, String, Long, Point>>(){
            @Override
            //(lat, lon) => (small, large)
            public Tuple4<String, String, Long, Point> map(String s) throws Exception {
                String[] t = s.split(",");
                return new Tuple4<String, String, Long, Point>(t[0],t[1],Long.parseLong(t[2]+"000"),new Point(Double.parseDouble(t[4]),Double.parseDouble(t[3])));
            }
        })
                .assignTimestampsAndWatermarks(new AssignerWithPeriodicWatermarks<Tuple4<String, String, Long, Point>>() {
                    Long currentMaxTimestamp = 0L;
                    final Long maxOutOfOrderness = 5000L;
                    @Override
                    public long extractTimestamp(Tuple4<String, String, Long, Point> element, long previousElementTimestamp) {
                        Long timestamp = element.f2;
                        currentMaxTimestamp = Math.max(timestamp, currentMaxTimestamp);
//                        System.out.println(new Date(timestamp));
                        return timestamp;
                    }
                    @Override
                    public Watermark getCurrentWatermark() {
                        // return the watermark as current highest timestamp minus the out-of-orderness bound
                        return new Watermark(currentMaxTimestamp - maxOutOfOrderness);
                    }
                })
                .keyBy(new myKeySelector())
                .timeWindow(Time.seconds(360),Time.seconds(180))
                .process(new ProcessWindowFunction<Tuple4<String, String, Long, Point>, TF, String, TimeWindow>() {
                    private ValueState<TF> tf;
//                    private ValueState<Integer> count;

                    @Override
                    public void open(Configuration parameters) throws Exception {

//                StateTtlConfig ttlConfig = StateTtlConfig.newBuilder(org.apache.flink.api.common.time.Time.minutes(5))   // 存活时间
//                        .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)  // 永远不返回过期的用户数据
//                        .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)  // 每次写操作创建和更新时,修改上次访问时间戳
//                        .setTimeCharacteristic(StateTtlConfig.TimeCharacteristic.ProcessingTime) // 目前只支持 ProcessingTime
//                        .build();

                        ValueStateDescriptor<TF> tfdes = new ValueStateDescriptor<TF>("tfdescriber", TF.class);
//                ValueStateDescriptor<Integer> countdes = new ValueStateDescriptor<Integer>("countTest", Integer.class);

//                tfdes.enableTimeToLive(ttlConfig);


                        tf = getRuntimeContext().getState(tfdes);
//                count = getRuntimeContext().getState(countdes);
//                super.open(parameters);
                    }

                    @Override
                    public void process(String tuple, Context context, Iterable<Tuple4<String, String, Long, Point>> iterable, Collector<TF> collector) throws Exception {
                        TF tfpast = tf.value();
                        for (Tuple4<String, String, Long, Point> e:iterable){
                            if (tfpast==null){
//                        tfpast = new TF();
//                        System.out.println("111");
                                tf.update(new TF(e.f3,0.0, e.f3, e.f3,1L, e.f2, e.f0));
//                        System.out.println(tfpast);
//                        System.out.println("000"+tf.value());
                            }else{
//                        System.out.println("222");

                                tf.update(new TF(e.f3.add(tfpast.getSC()).div(2L),//平均点
                                        //偏向角度？？
                                        tfpast.getSC().angle(e.f3),//转向角
                                        tfpast.BL.min(e.f3),//最小矩形框
                                        tfpast.TR.max(e.f3),
                                        tfpast.getN()+1L,//总长度（点数）
                                        e.f2,//时间关系
                                        e.f0
                                ));
//                        System.out.println(tf.value().SC);
                            }
                        }
                        if (tfpast!=null){
                            collector.collect(tfpast);
                        }
                    }
                })
                //和keyby中重复了，不需要轮训来负载均衡。？大概？
//                .partitionCustom(new Partitioner<TF>() {
//                    @Override
//                    public int partition(TF k, int numPartitions) {
//                        Point key = k.getSC();
//                        int keyID = new HashGrid(key.getX(), key.getY()).hashCode() % numPartitions;
////                        System.out.println(keyID);
//                        return keyID;
//                    }
//                }, new KeySelector<TF, TF>() {
//                    @Override
//                    public TF getKey(TF value) throws Exception {
//                        return value;
//                    }
//                })
//                .map(new MapFunction<TF, TF>() {
//            @Override
//            public TF map(TF value) throws Exception {
////                System.out.println("map" + Thread.currentThread().getId());
//                return value;
//            }
//        })
                .keyBy(new KeySelector<TF, String>() {
                    @Override
                    public String getKey(TF value) throws Exception {
                        Point key = value.getSC();
                        return "1";
//                        return new GeoHash().encode(key.getX(), key.getY());
                    }
                })
                .process(new KeyedProcessFunction<String, TF, LF>() {
                    @Override
                    public void processElement(TF value, Context ctx, Collector<LF> out) throws Exception {
//                        if(ctx.getCurrentKey()>=100){
//                            System.out.println("CurKey\t"+ctx.getCurrentKey()+":"+"Tread:\t"+Thread.currentThread().getId());
//                        }
                        Point key = value.getSC();

//                        out.collect(new LF(ctx.getCurrentKey(),
//                                value.SA,
//                                new Line(value.getBL(), value.getTR()),
//                                value.getN(),
//                                value.getT()));
                        int count = 0;
                        Point tmps = new Point();
                        Point tmpe = new Point();
                        Point tmp = new Point();
                        Double k = Math.tan(value.SA*(Math.PI)/180.0);
                        Double b = (value.getSC().y-k*value.getSC().x);
                        //for x
                        if((k*value.getBL().x+b) <= value.getTR().y && (k*value.getBL().x+b) >= value.getBL().y) {
                            count = count + 1;
                            tmps.setX(value.getBL().x);
                            tmps.setY(k*value.getBL().x+b);
                        }
                        if((k*value.getTR().x+b) <= value.getTR().y && (k*value.getTR().x+b) >= value.getBL().y) {
                            count = count + 1;
                            tmpe.setX(value.getTR().x);
                            tmpe.setY(k*value.getTR().x+b);
                        }
                        if (count == 2){

                        } else {
                            if((value.getBL().y-b)/k <= value.getTR().x && (value.getBL().y-b)/k >= value.getBL().x){
                                tmps.setX((value.getBL().y-b)/k);
                                tmps.setY(value.getBL().y);
                            }
                            if((value.getTR().y-b)/k <= value.getTR().x && (value.getTR().y-b)/k >= value.getBL().x) {
                                tmpe.setX(value.getTR().y-b);
                                tmpe.setY(value.getTR().y);
                            }
                        }

                        if (value.getSA()/Math.PI - (2*((int)(value.getSA()/Math.PI/2)))>=0.5
                                && value.getSA()/Math.PI - (2*((int)(value.getSA()/Math.PI/2)))<1.5){
                            tmp = tmps;
                            tmps = tmpe;
                            tmpe = tmp;
                        }
                        value.setBL(tmps);
                        value.setTR(tmpe);
                        out.collect(new LF(ctx.getCurrentKey(),
                                value.SA,
                                new Line(value.getBL(), value.getTR()),
                                value.getN(),
                                value.getT(),
                                value.getUID()));
                    }
                }).keyBy(new KeySelector<LF, String>() {
            @Override
            public String getKey(LF value) throws Exception {
                return value.KeyID;
            }
        })
                .timeWindow(Time.seconds(720),Time.seconds(360))
                .apply(new RichWindowFunction<LF, Object, String, TimeWindow>() {
                    private ValueState<Integer> winInid;

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        super.open(parameters);
                        ValueStateDescriptor<Integer> winInidDes = new ValueStateDescriptor<Integer>("lineNumDes", Integer.class );
                        winInid = getRuntimeContext().getState(winInidDes);
                    }

                    @Override
                    public void apply(String key, TimeWindow window, Iterable<LF> values, Collector<Object> out) throws Exception {
                        try{
                            ArrayList<Line> ls = new ArrayList<Line>();
                            Integer tmpid = winInid.value();
                            if (tmpid == null){
                                tmpid = 0;
                            }
                            for(LF tmp:values){
                                long order = tmp.getT(); //改成时间戳
//                                String lineNum = tmp.getKeyID()+tmpid.toString();//key分区后再加本组内的排序号 => key+(id++)2021update
//                                tmpid++;
                                String lineNum = tmp.getUID();
                                winInid.update(tmpid);
                                Line line = tmp.L;
                                line.setNum(lineNum);
                                line.setOrder(order);

                                ls.add(line);
                            }
//                            if (ls.size()>50) {
                            System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(window.getStart())+"-"+ new SimpleDateFormat("HH:mm:ss").format(window.getEnd())
                                    +"-"+key+":"+ls.size());
                            TraClus.go(ls,window.getStart(),key);
//                            }

                        }catch(Exception e){
                            e.printStackTrace();
                        }
//                        out.collect(window.getStart()+":"+window.getEnd());
                    }
                })
                .print();
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