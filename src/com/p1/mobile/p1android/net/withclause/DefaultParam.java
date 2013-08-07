package com.p1.mobile.p1android.net.withclause;

public class DefaultParam implements Param{

    private String param;
    
    @Override
    public String getParamString() {
        return param;
    }

    @Override
    public void addParam(String param) {
        this.param = param;
    }

    @Override
    public boolean isEmpty() {
        return param.isEmpty();
    }

}
