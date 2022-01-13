package com.nocml.single;


import com.nocml.calculation.GeoHash;
import com.nocml.calculation.TraClus;
import com.nocml.pojo.*;
import org.apache.calcite.linq4j.Ord;
import org.apache.flink.api.common.functions.GroupCombineFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.operators.Order;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.operators.SortedGrouping;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.java.BatchTableEnvironment;
import org.apache.flink.util.Collector;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.LinkedTransferQueue;

import static com.nocml.calculation.Config.*;

public class Test{
    /**
     * Simple POJO.
     */
    public static class MyOrder {
        public String userId;
        public String orderId;
        public Long t;
        public Double x;
        public Double y;

        public MyOrder() {
        }

        public MyOrder(String userId, String orderId, Long t, Double x, Double y) {
            this.userId = userId;
            this.orderId = orderId;
            this.t = t;
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "Order{" +
                    "userId=" + userId +
                    ", orderId='" + orderId + '\'' +
                    ", t=" + t + '\'' +
                    ", x=" + x + '\'' +
                    ", y=" + y +
                    '}';
        }
    }
    public static void main(String[] args) throws Exception {
//        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        String file = "/Users/chenliang/Downloads/10w";
        DataSet<MyOrder> csvInput = env.readCsvFile(file)

                //指定对应的实体类型                 //指定对应的属性名
                .pojoType(MyOrder.class,"userId","orderId","t","x","y");

        SortedGrouping<MyOrder> myOrderSortedGrouping = csvInput.groupBy("userId")
                .sortGroup("t", Order.ASCENDING);

        myOrderSortedGrouping.combineGroup(new GroupCombineFunction<MyOrder, Tuple2<Integer,String>>() {

            public void combine(Iterable<MyOrder> words, Collector<Tuple2<Integer,String>> out) { // combine
                String key = null;
                int count = 0;

                for (MyOrder word : words) {
                    key = word.toString();
                    count++;
                }
                // emit tuple with word and count
                out.collect(new Tuple2(count, key));
            }
        })
                .writeAsCsv("/Users/chenliang/Downloads/transfer.csv", FileSystem.WriteMode.OVERWRITE);

            env.execute();
    }


}