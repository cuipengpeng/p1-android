package com.p1.mobile.p1android.util;
import java.util.regex.Matcher;  
import java.util.regex.Pattern;  
public class ChineseRegexUtil {  
   private static String mChineseRegEx = "[\u4e00-\u9fa5]";   
   private static Pattern mPattern = Pattern.compile(mChineseRegEx);  
   public static boolean isContainsChinese(String str)     
    {    
        Matcher matcher = mPattern.matcher(str);     
        boolean flg = false;  
        if (matcher.find())    {    
            flg = true;   
        }     
        return flg;     
    }  
 
}  