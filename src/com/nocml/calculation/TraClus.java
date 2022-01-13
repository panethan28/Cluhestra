package com.nocml.calculation;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import cn.nocml.FileTool;
import cn.nocml.MathTool;
import cn.nocml.Pair;

import com.nocml.pojo.Line;
import com.nocml.pojo.Point;
import com.nocml.pojo.Trajectory;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

public class TraClus {
	
	private int MDL_COST_ADVANTAGE = 0; 
	/*
	 * partition 之后的线段
	 */
	private ArrayList<Line> lines = new ArrayList<Line>();
	/*
	 * 初始轨迹
	 */
	private HashMap<Long , Trajectory> trajectorys = new HashMap<Long, Trajectory>();
	private HashMap<Integer , ArrayList<Line>> cluster = new HashMap<Integer, ArrayList<Line>>();
	RTra rtra = new RTra();
	int minLines = 8;
	double eps = 29;
	public ArrayList<Line> getLines() {
		return lines;
	}

	public void setMDL_COST_ADVANTAGE(int mDL_COST_ADVANTAGE) {
		MDL_COST_ADVANTAGE = mDL_COST_ADVANTAGE;
	}

	public HashMap<Long, Trajectory> getTrajectorys() {
		return trajectorys;
	}
	
	public void setParameter(int minLines , double eps){
		this.minLines = minLines;
		this.eps = eps;
	}
	
	public void cluster(){
//		System.out.println("cluster...");
//		System.out.println("line size : " + lines.size());
		int end = lines.size();
//		System.out.println("linesize"+end);
		int clusterId = 1;
		for(int i = 0 ; i < end ; i++){
//			if(i % 10 == 0)
//				System.out.println("processed(cluster) " + i + "...");
			Line l = lines.get(i);
			if(l.getClassifiy() == 0){

				ArrayList<Integer> neighbor = getNeighbor(i);
//                System.out.println("in while " + neighbor.size());
				if(neighbor.size() + 1 >= minLines){
					//将一个cluster的lineNum存储在一起。classify疑似判断是否已聚类
					//classify：0->未访问，1->访问了但聚类失败，2->访问且聚类成功
					lines.get(i).setClassifiy(2);
					lines.get(i).addCluster(neighbor);
					lines.get(i).addCluster(i);
					lines.get(i).setClusterId(clusterId);
					for(int ndx : neighbor){
						lines.get(ndx).setClassifiy(2);
					}
					ExpandCluster(i , neighbor);
					clusterId++;
//                    System.out.println("clusterId Size" + clusterId);
				}else{
					l.setClassifiy(1);
				}
			}
		}
		
		//将拥有聚类但提出，放入cluster中->[clusterId:Int, ls:ArrayList[Line]]
		//局部聚类线段
		for(int i = 0 ; i < lines.size() ; i++){
			if(lines.get(i).getClusterId() > 0){
				ArrayList<Line> ls = new ArrayList<Line>();
				clusterId = lines.get(i).getClusterId();
				for(int j : lines.get(i).getCluster()){
//					if (j>lines.size()){
//						System.out.println("large j:\t"+j);
//					}
					if(j<lines.size()){
						ls.add(lines.get(j));
					}
				}
				cluster.put(clusterId, ls);
			}
		}

//		lines.clear();
//		System.out.println("cluster end...");
	}

	/***
	 * 按照Num排序，但是可以相应的改成Flink的TimeStamp
	 */
	public void sortLine(){
		try{
			Collections.sort(lines, new Comparator<Line>(){
				@Override
				public int compare(Line l1, Line l2) {
//					return (int)(l1.getNum() - l2.getNum());
					return (int)(l1.getOrder()-l2.getOrder());
				}
			});
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public HashMap<Integer , ArrayList<Line>> getCluster(){
		return cluster;
	}
	public void outputCluster(String ofile){
		ArrayList<String> save = new ArrayList<String>();
		for(Entry<Integer , ArrayList<Line>> en : this.cluster.entrySet()){
			for(Line l : en.getValue()){
				//key为clusterid
				save.add(en.getKey() +"," + l.toString());
			}
		}
		try {
			FileTool.SaveListToFile(save, ofile, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void ExpandCluster(int center , ArrayList<Integer> neighbor){
//		System.out.println("Expanding...");
		while(neighbor.size() > 0){
			int index = neighbor.get(0);
			ArrayList<Integer> sub_neighbor = getNeighbor(index);
			if (sub_neighbor.size() + 1>= minLines) {
				for (int ndx : sub_neighbor) {
					if (lines.get(ndx).getClassifiy() == 0 || lines.get(ndx).getClassifiy() == 1) {
						lines.get(center).addCluster(ndx);
						lines.get(ndx).setClassifiy(2);
					}
					if(lines.get(ndx).getClassifiy() == 0){
						neighbor.add(ndx);
					}
				}
			}
			neighbor.remove(0);
		}
	}
	
	private ArrayList<Integer> getNeighbor(int index){
		ArrayList<Integer> ndxs = new ArrayList<Integer>();
		Line l = lines.get(index);
		Line llong = new Line();
		Line lshort = new Line();
		for(int i = 0 ; i < lines.size() ;i++){
			if(i == index)
				continue;
			Line ltemp = lines.get(i);
			if (Distance.distance(l) >= Distance.distance(ltemp)) {
				llong = l ; lshort = ltemp;
			}else{
				llong = ltemp ; lshort = l;
			}
			double dis = Distance.dist(llong,lshort);
			if (dis <= eps) {
//				System.out.println("dis:"+dis);
				ndxs.add(i);
			}
		}
		return ndxs;
	}
//	public void ouputLines(String ofile){
//		List<String> ls = new ArrayList<String>();
//		HashMap<Long, Long> map = new HashMap<Long, Long>();
//		int order = 0;
//		for(Line l : lines){
//			l.setOrder(order++);
//			ls.add(l.toString());
//			if(map.containsKey(l.getNum())){
//				//不断更新
//				map.put(l.getNum(), map.get(l.getNum()) + 1);
//			}else{
//				map.put(l.getNum() , 1L);
//			}
//		}
//		try {
//			FileTool.SaveListToFile(ls, ofile, false);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
////		for(Entry<Long , Long> en : map.entrySet()){
//////			System.out.println(en.getKey() + "\t" + en.getValue());
////		}
//	}

	/***
	 * 根据初始聚类结果和均值结果计算来计算RT线
	 * @param min
	 * @param radius
	 * @return
	 */
	public ArrayList<Trajectory> getRTrajectory( int min  , int radius){
		rtra.setParameter(min, radius);
		for(Entry<Integer, ArrayList<Line>> en : cluster.entrySet()){
			ArrayList<Line> ctra = new ArrayList<Line>();
			ctra.addAll(en.getValue());
			rtra.setCluster(ctra);
			rtra.getRTra();
			rtra.clearData();
		}
		return rtra.getRTrajectory();
	}

	/***
	 * trajectory类型转换成string，保存写入文件
	 * @param filepath
	 */
	public void saveRtrajectory(String filepath){
		ArrayList<Trajectory> rTrajectory  = rtra.getRTrajectory();
		ArrayList<String> list = new ArrayList<String>();
		for(int i = 0 ; i < rTrajectory.size() ; i++){
			Trajectory rt = rTrajectory.get(i);
			for(Point p : rt.getPoints()){
				list.add(i + "\t" + p.toString());
			}
		}
		try {
			FileTool.SaveListToFile(list, filepath, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void check_tra_num(int n){
		HashMap<Integer , ArrayList<Line>> tempCluster = new HashMap<Integer, ArrayList<Line>>(cluster);
		for(Entry<Integer , ArrayList<Line>> en : tempCluster.entrySet()){
			HashSet<String> set = new HashSet<String>();
			for(Line l : en.getValue()){
				set.add(l.getNum());
				//记录某一聚类内所有的轨迹id，非重复
			}
			if(set.size() < n){
				cluster.remove(en.getKey());
				//小于一定量的聚类则删除
			}
		}
	}

	/***
	 * 大概是个聚类质量判断函数？
	 * @return
	 */
	private cn.nocml.Pair<Double , Integer> calclateParameter(){
		int n = lines.size();
		int sigma = 0;
		double prob = 0.0;
		for(int i = 0 ; i < n ; i++){
//			if( lines.get(i).getCluster().size() == 0)
//				System.out.println("pause");
			sigma += lines.get(i).getCluster().size() + 1;
		} 
		for(int i = 0 ; i < n ;i++){
			double nx = lines.get(i).getCluster().size() + 1;
			double px = nx / sigma;
			prob +=(px * MathTool.log(px, 2));
		}
		prob = -1 * prob;
		int avg = sigma / n;
		return new Pair<Double , Integer>(prob , avg);
	}
	public static void go(ArrayList<Line> l, Long timestamp, String key) {
		try{
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String date = sdf.format(timestamp);
			TraClus traClus = new TraClus();
			traClus.lines.addAll(l);
//			traClus.trajectorys.putAll(t);
//			System.out.println(traClus.trajectorys.size());
			String root = System.getProperty("user.dir") + "/data/";
////			String filename = "data.motion.txt";
////			traClus.loadTrajectory(root + filename);
//			String filename = "Copydeer95.txt";
//			traClus.loadPoints(root + filename);
//			Draw draw = new Draw();
			traClus.setParameter(Config.minCluLines, Config.Eps);
//			traClus.setMDL_COST_ADVANTAGE(8);
//			traClus.partition();
			traClus.sortLine();
//			traClus.ouputLines(root + "ls_my.txt");
			traClus.cluster();
//			traClus.outputCluster(root + "cluster_my.txt");
//			traClus.check_tra_num(10);
//			traClus.outputCluster(root + date + "-" + key + "cluster_my_check.txt");
			traClus.outputCluster(root + date + "-" + key + "-cluster.txt");
//			System.out.println("？？？？"+traClus.calclateParameter());//只计算了一次全局的结果，参数调优
//			System.out.println(traClus.getLines().size());//输出所有线段数量
//			HashMap<Color , List<Line>> toDraw = new HashMap<Color, List<Line>>();
//			HashMap<Long, Trajectory> trajecotrys = traClus.getTrajectorys();
//			for(Entry<Long,Trajectory> en : trajecotrys.entrySet()){
//				draw.addPoints(new Color(100,100,100), en.getValue().getPoints());
//				draw.paintLines();
//			}

//			ArrayList<Trajectory> rTrajectory = traClus.getRTrajectory(5,25);
//			traClus.saveRtrajectory(root + "RTra.txt");
//			for(int i = 0 ; i < rTrajectory.size() ; i++){
//				draw.addPoints(new Color(255, 35, 43), rTrajectory.get(i).getPoints());
//				draw.paintLines();
//			}
//			draw.paintLines();

//			System.out.println("Finished");
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}

//	@Override
//	public void process(Object o, Context context, Iterable<Tuple4<String, String, Long, Tuple2<Double, Double>>> iterable, Collector<Object> collector) throws Exception {
//
//	}
}
