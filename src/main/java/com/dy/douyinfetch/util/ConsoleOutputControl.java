package com.dy.douyinfetch.util;

/**
 * Created by chengxia on 2022/8/19. */  
public class ConsoleOutputControl {  
    /**  
     * 通过\033特殊转义字符实现输出格式控制     * @param content, 待格式化的内容  
     * @param fontColor, 字体颜色：30黑 31红 32绿 33黄 34蓝 35紫 36深绿 37白  
     * @param fontType, 字体格式：0重置 1加粗 2减弱 3斜体 4下划线 5慢速闪烁 6快速闪烁  
     * @param backgroundColor, 字背景颜色：40黑 41红 42绿 43黄 44蓝 45紫 46深绿 47白  
     * */    public static String getFormatOutputString(String content, int fontColor, int fontType, int backgroundColor){  
        return String.format("\033[%d;%d;%dm%s\033[0m", fontColor, fontType, backgroundColor, content);  
    }  
    /**  
     * 通过\033特殊转义字符实现输出格式控制，获得带颜色的字体输出     * @param content, 待格式化的内容  
     * @param fontColor, 字体颜色：30黑 31红 32绿 33黄 34蓝 35紫 36深绿 37白  
     * */    public static String getColoredOutputString(String content, int fontColor){  
        return String.format("\033[%dm%s\033[0m", fontColor, content);  
    }  
    /**  
     * 通过\033特殊转义字符实现输出格式控制，获得带背景颜色的字体输出     * @param content, 待格式化的内容  
     * @param backgroundColor, 字背景颜色：40黑 41红 42绿 43黄 44蓝 45紫 46深绿 47白  
     * */    public static String getBackgroundColoredOutputString(String content, int backgroundColor){  
        return String.format("\033[%dm%s\033[0m", backgroundColor, content);  
    }  
  
    /**  
     * 能接受一个顺序标识，按顺序产生带颜色的输出字符串     * */    public static String orderedColorString(String content, int i){  
        int tmpColor = 31 + (i % 7);  
        return String.format("\033[%dm%s\033[0m", tmpColor, content);  
    }  
  
    public static void main(String []args){  
        System.out.println(getFormatOutputString("字体颜色为红色，背景色为黄色，带下划线", 31, 4, 43));  
        //按顺序输出各个颜色代码的字符串  
        for(int i = 0; i < 7; i++){  
            System.out.println(getColoredOutputString(String.format("color code: %d", 31 + i), 31 + i));  
        }  
        //按顺序输出各个背景颜色代码的字符串  
        for(int i = 0; i < 7; i++){  
            System.out.println(getBackgroundColoredOutputString(String.format("background color code: %d", 41 + i), 41 + i));  
        }  
        //按顺序输出各个颜色代码的字符串  
        for(int i = 0; i < 7; i++){  
            System.out.println(orderedColorString(String.format("font color code: %d", 31 + i), i));  
        }  
    }  
}