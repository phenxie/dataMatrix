//package com.phenxie.sipdercommon.SubClass;
//
//import com.phenxie.sipdercommon.Base.NewsPages;
//import com.phenxie.sipdercommon.utils.DateUtlis;
//import java.util.Calendar;
//public class GuangMingRiBao  extends NewsPages {
//
//    @Override
//    public void pageParse() {
//        System.out.println("光明日报");
//        this.chinsesName="光明日报";
//        Calendar c=this.calendar;
//        // 与浏览器同步非常重要，必须等待浏览器加载完毕
////        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
//
//        String path="";
//        for(int j=1;j<21;j++) {
//            for (int m = 1; m < 21; m++) {
//                path="https://epaper.gmw.cn/gmrb/html/"+DateUtlis.getDateString1(c)+"/nw.D110000gmrb_"+DateUtlis.getDateString2(c)+"_" + m + "-"+(j>=10?""+j:"0"+j)+".htm";
//                //https://epaper.gmw.cn/gmrb/html/2010-01/01/nw.D110000gmrb_20100101_1-01.htm
////            urls.add("https://epaper.gmw.cn/gmrb/html/2021-12/27/nw.D110000gmrb_20211227_2-01.htm");
////            urls.add("https://epaper.gmw.cn/gmrb/html/2021-12/27/nw.D110000gmrb_20211227_3-01.htm");
////            urls.add("https://epaper.gmw.cn/gmrb/html/2021-12/27/nw.D110000gmrb_20211227_4-01.htm");
////            urls.add("https://epaper.gmw.cn/gmrb/html/2021-12/27/nw.D110000gmrb_20211227_5-01.htm");
////            urls.add("https://epaper.gmw.cn/gmrb/html/2021-11/02/nw.D110000gmrb_20211102_1-16.htm");
////            urls.add("https://epaper.gmw.cn/gmrb/html/2021-11/02/nw.D110000gmrb_20211102_2-16.htm");
////            urls.add("https://epaper.gmw.cn/gmrb/html/2021-11/02/nw.D110000gmrb_20211102_3-16.htm");
//                try {
//                    driver.get(path);
//                    // 与浏览器同步非常重要，必须等待浏览器加载完毕
//                    if(!driver.getTitle().equals("404错误")) {
//                        pushData(driver.getTitle(),driver.getPageSource());
//                    }else {
//                        break;
//                    }
//                    Thread.sleep(3000);
//                } catch (Exception e) {
//                    e.getStackTrace();
//                    System.out.println(e.toString());
//                    continue;
//                }
//            }
//        }
//
//    }
//
//}
