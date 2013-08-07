package com.p1.mobile.p1android.net.withclause;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class WithParam implements Param {

    private Set<String> withStrings = new LinkedHashSet<String>();
    private static final String SEPARATOR = ";";
    private static final String START = "with=";

    @Override
    public boolean isEmpty() {
        return withStrings.isEmpty();
    }

    @Override
    public void addParam(String param) {
        if (isEmpty()) {
            withStrings.add(START);
        }
        withStrings.add(param);
    }

    @Override
    public String getParamString() {
        StringBuilder builder = new StringBuilder();
        Iterator<String> iterator = withStrings.iterator();
        if(iterator.hasNext()) {
            builder.append(iterator.next());
        }
        
        while (iterator.hasNext()) {
            builder.append(iterator.next());

            if (iterator.hasNext()) {
                builder.append(SEPARATOR);
            }
        }

        return builder.toString();
    }

}
