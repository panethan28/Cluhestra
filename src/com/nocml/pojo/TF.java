package com.nocml.pojo;

import org.apache.kafka.common.protocol.types.Field;

import java.io.Serializable;

public class TF implements Serializable {
    public Point SC;
    public double SA;
    public Point BL;
    public Point TR;
    public Long N;
    public Long T;
    public String UID;
    public TF(){

    }

    public TF(TF t){
        this.SA = t.getSA();
        this.SC = t.getSC();
        this.BL = t.getBL();
        this.TR = t.getTR();
        this.N = t.getN();
        this.T = t.getT();
        this.UID = t.getUID();
    }

    public String getUID(){return UID;}
    public Point getSC() {
        return SC;
    }
    public double getSA() {
        return SA;
    }
    public Point getBL() {
        return BL;
    }
    public Point getTR() {
        return TR;
    }
    public Long getN() { return N; }
    public Long getT() { return T; }

    public TF(Point SC , double SA, Point BL, Point TR, Long N, Long T, String UID){
        this.SC = SC;
        this.SA = SA;
        this.BL = BL;
        this.TR = TR;
        this.N = N;
        this.T = T;
        this.UID = UID;
    }

    public void setUID(String UID) { this.UID = UID; }
    public void setBL(Point p){
        this.BL = p;
    }

    public void setTR(Point p){
        this.TR = p;
    }

    @Override
    public String toString() {
        return "(" + SC + "," + SA + "," + BL + "," + TR + "," + N + "," + T + ")"+"UID:"+UID;
    }
}
