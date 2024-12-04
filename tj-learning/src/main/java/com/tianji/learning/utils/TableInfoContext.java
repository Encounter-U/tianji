package com.tianji.learning.utils;

/**
 * @author Encounter
 * @date 2024/12/04 12:36<br/>
 */
public class TableInfoContext
    {
        private static final ThreadLocal<String> TL = new ThreadLocal<>();
        
        public static String getInfo()
            {
                return TL.get();
            }
        
        public static void setInfo(String info)
            {
                TL.set(info);
            }
        
        public static void remove()
            {
                TL.remove();
            }
    }
