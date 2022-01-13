package com.nocml.single;

import com.nocml.pojo.Point;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.*;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

import static com.nocml.calculation.Config.datasourceFile;

public class BoundDetect {
    private static Point boundmax=null;
    private static Point boundmin=null;
    public static void main(String[] args) throws Exception {
//        final StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();
//        DataStreamSource<String> datasource = environment.readTextFile(datasourceFile);
//        datasource.map(new MapFunction<String, Tuple4<String, String, Long, Point>>(){
//            @Override
//            //(lat, lon) => (small, large)
//            public Tuple4<String, String, Long, Point> map(String s) throws Exception {
//                String[] t = s.split(",");
//                return new Tuple4<String, String, Long, Point>(t[0],t[1],Long.parseLong(t[2]+"000"),new Point(Double.parseDouble(t[4]),Double.parseDouble(t[3])));
//            }
//        })
//                .process(new ProcessFunction<Tuple4<String, String, Long, Point>, Object>() {
////            private ValueState<Point> boundmax;
////            private ValueState<Point> boundmin;
////
////            @Override
////            public void open(Configuration parameters) throws Exception {
////                super.open(parameters);
////                ValueStateDescriptor<Point> boundmaxDes = new ValueStateDescriptor<Point>("boundmaxDes", Point.class);
////                boundmax = getRuntimeContext().getState(boundmaxDes);
////                ValueStateDescriptor<Point> boundminDes = new ValueStateDescriptor<Point>("boundminDes", Point.class);
////                boundmin = getRuntimeContext().getState(boundminDes);
////            }
//
//            @Override
//            public void processElement(Tuple4<String, String, Long, Point> value, Context ctx, Collector<Object> out) throws Exception {
////                Point boundmaxtmp = boundmax.value();
////                Point boundmintmp = boundmin.value();
//
//                if (boundmax == null){
//                    boundmax = value.f3;
//                }
//                if (boundmin == null){
//                    boundmin = value.f3;
//                }
//                if (value.f3.getX()>boundmax.getX()){
//                    boundmax.setX(value.f3.getX());
//                }
//                if (value.f3.getY()>boundmax.getY()){
//                    boundmax.setY(value.f3.getY());
//                }
//                if (value.f3.getX()<boundmin.getX()){
//                    boundmin.setX(value.f3.getX());
//                }
//                if (value.f3.getY()<boundmin.getY()){
//                    boundmin.setY(value.f3.getY());
//                }
//
//
//                out.collect("Max:"+boundmax+";\tMin:"+boundmin);
//            }
//        }).setParallelism(1).print();
//        environment.execute("Bound");
        final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        DataSet<String> text = env.readTextFile(datasourceFile);

        text.map(new MapFunction<String, Tuple5<String, String, Long, Double, Double>>(){
                     @Override
                     //(lat, lon) => (small, large)
                     public Tuple5<String, String, Long, Double, Double> map(String s) throws Exception {
                         String[] t = s.split(",");
                         return new Tuple5<String, String, Long, Double, Double>(t[0],t[1],Long.parseLong(t[2]+"000"),Double.parseDouble(t[4]),Double.parseDouble(t[3]));
                     }
                 }).max(4).print();

    }
}
