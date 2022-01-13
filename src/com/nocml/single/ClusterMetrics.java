//package com.nocml.single;
//
//import com.alibaba.alink.operator.batch.evaluation.EvalClusterBatchOp;
//import com.alibaba.alink.operator.batch.source.MemSourceBatchOp;
//import com.alibaba.alink.pipeline.clustering.KMeans;
//import org.apache.flink.types.Row;
//
//import java.util.Arrays;
//
//public class ClusterMetrics {
//    public static void main(String[] args) throws Exception {
//        Row[] rows = new Row[] {
//                Row.of(0, "0,0,0"),
//                Row.of(0, "0.1,0.1,0.1"),
//                Row.of(0, "0.2,0.2,0.2"),
//                Row.of(1, "9,9,9"),
//                Row.of(1, "9.1,9.1,9.1"),
//                Row.of(1, "9.2,9.2,9.2")
//        };
//
//        MemSourceBatchOp inOp = new MemSourceBatchOp(Arrays.asList(rows), new String[] {"label", "Y"});
//
//        KMeans train = new KMeans()
//                .setVectorCol("Y")
//                .setPredictionCol("pred")
//                .setK(2);
//
//        com.alibaba.alink.operator.common.evaluation.ClusterMetrics metrics = new EvalClusterBatchOp()
//                .setPredictionCol("pred")
//                .setVectorCol("Y")
//                .setLabelCol("label")
//                .linkFrom(train.fit(inOp).transform(inOp))
//                .collectMetrics();
//
//        System.out.println(metrics.getCalinskiHarabaz());
//        System.out.println(metrics.getCompactness());
//        System.out.println(metrics.getCount());
//        System.out.println(metrics.getDaviesBouldin());
//        System.out.println(metrics.getSeperation());
//        System.out.println(metrics.getK());
//        System.out.println(metrics.getSsb());
//        System.out.println(metrics.getSsw());
//        System.out.println(metrics.getPurity());
//        System.out.println(metrics.getNmi());
//        System.out.println(metrics.getAri());
//        System.out.println(metrics.getRi());
//        System.out.println(metrics.getSilhouetteCoefficient());
//    }
//}
