package com.nocml.pojo;

import java.io.Serializable;

public class LF implements Serializable {
    public String KeyID;
    public double SA;
    public Line L;
    public Long N;
    public Long T;
    public String UID;
    public LF(){

    }

    public LF(LF t){
        this.KeyID = t.KeyID;
        this.SA = t.getSA();
        this.L = t.getL();
        this.N = t.getN();
        this.T = t.getT();
        this.UID = t.getUID();
    }

    public String getUID() {return UID;}
    public String getKeyID() {
        return KeyID;
    }
    public double getSA() {
        return SA;
    }
    public Line getL() { return L; }
    public Long getN() { return N; }
    public Long getT() { return T; }

    public LF(String KeyID , double SA, Line L, Long N, Long T,String UID){
        this.KeyID = KeyID;
        this.SA = SA;
        this.L = L;
        this.N = N;
        this.T = T;
        this.UID = UID;
    }

    @Override
    public String toString() {
        return "(" + KeyID + "," + SA + "," + L + "," + N + "," + T + ")"+"UID:"+UID;
    }
}
