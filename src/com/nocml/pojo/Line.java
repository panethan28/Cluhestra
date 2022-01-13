package com.nocml.pojo;

import java.io.Serializable;
import java.util.ArrayList;


public class Line implements Serializable {
	Point s = new Point();
	Point e = new Point();
	//行顺序
	long order = -1;//改为时间

	//轨迹id？？
	String num = null;
	int classifiy = 0;
	int clusterId = -1;
	ArrayList<Integer> cluster = new ArrayList<Integer>();
	public Line() {
	}
	public Line(Point s , Point e) {
		this.s = s;
		this.e = e;
	}

	
	public int getClusterId() {
		return clusterId;
	}
	public void setClusterId(int clusterId) {
		this.clusterId = clusterId;
	}
	public long getOrder() {
		return order;
	}


	public void setOrder(long order) {
		this.order = order;
	}


	public int getClassifiy() {
		return classifiy;
	}


	public void setClassifiy(int classifiy) {
		this.classifiy = classifiy;
	}


	public ArrayList<Integer> getCluster() {
		return cluster;
	}


	public void setCluster(ArrayList<Integer> cluster) {
		this.cluster = cluster;
	}


	public Point getS() {
		return s;
	}

	public void setS(Point s) {
		this.s = s;
	}

	public Point getE() {
		return e;
	}

	public void setE(Point e) {
		this.e = e;
	}

	public void addCluster(ArrayList<Integer> cl){
		this.cluster.addAll(cl);
	}
	public void addCluster(int index){
		this.cluster.add(index);
	}
	
	
	public String getNum() {
		return num;
	}

	/**
	 * @description 设置读入时的原始行号
	 * @param num
	 */
	public void setNum(String num) {
		this.num = num;
	}


	@Override
	public String toString() {
		return order + "," + num + "," + s +","+ e;
	}


	
	
}
