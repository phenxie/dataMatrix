package com.phenxie.sipdercommon.sipders;

import com.phenxie.sipdercommon.utils.DateUtlis;
import org.assertj.core.util.DateUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class gmw2007 {
    public static void main(String[] args) {
        Start();
    }

    public static void Start(){
      Calendar start=  DateUtlis.CreateCalendar(2004);
      Calendar end2002=DateUtlis.CreateCalendar(2002);
        Calendar end2005=DateUtlis.CreateCalendar(2005);
        Calendar end2008=DateUtlis.CreateCalendar(2008);
        start.set(Calendar.MONTH,1);
        start.set(Calendar.DATE,28);
      while(start.compareTo(end2005)<0){
//          if(start.compareTo(end2002)<0) {
//              gmw98to07(start, 1);
//          }else{
//              if(start.compareTo(end2005)<0){
//                  gmw98to07(start,2);
//              }else{
//                  gmw98to07(start,3);
//              }
//          }

          gmw98to07(start,3);
          start.add(Calendar.DAY_OF_YEAR,1);
      }
    }

    public static String getUrl(Calendar c, int type){
        //https://www.gmw.cn/01gmrb/2003-02/01/2003-01-01-Homepage.htm
        switch (type){
            case 1:
                return "https://www.gmw.cn/01gmrb/"+DateUtlis.getDateString1(c)+"/GB/DEFAULT.HTM";
            case 2:
                return "https://www.gmw.cn/01gmrb/"+DateUtlis.getDateString1(c)+"/"+DateUtlis.getDateString3(c)+"-Homepage.htm";
            case 3:
                return "https://www.gmw.cn/01gmrb/"+DateUtlis.getDateString1(c)+"/default.htm";
            case 4:
                return "https://www.gmw.cn/01gmrb/"+DateUtlis.getDateString1(c)+"/GB/default.htm";
                default:
                return "";
        }
    }

    public static  void gmw98to07(Calendar c, int type){
        String url=getUrl(c,type);
        //http://chromedriver.storage.googleapis.com/index.html
        //chromedirver  下载页面

        //不显示浏览器
        System.out.println("--------------------"+ DateUtlis.getDateString2(c)+"---------------------");
        ChromeOptions chromeOptions=new ChromeOptions();
        chromeOptions.addArguments("-headless");
//        chromeOptions.addArguments("blink-settings=imagesEnabled=false");
//        chromeOptions.addArguments("--disable-dev-shm-usage");
//        chromeOptions.addArguments("--disable-gpu");
        chromeOptions.addArguments("--no-sandbox");
        WebDriver driver = new ChromeDriver(chromeOptions);
//        driver.get("/Users/phenxie/gitee/java.test.springboot.demo/1.html");

//        driver.get("file:///Users/phenxie/gitee/java.test.springboot.demo/1.html");
//        driver.manage().window().maximize();
//        driver.manage().deleteAllCookies();
        // 与浏览器同步非常重要，必须等待浏览器加载完毕
//        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

//        WebElement img =driver.findElement(By.className("main"));
        long ll=new Date().getTime();
        System.out.println("当前时间："+ DateUtil.formatAsDatetime(new Date()));
//        List urls= new List();
//        String path="";
        driver.get(url);
        List<WebElement> list= driver.findElements(By.className("channel-newsTitle"));
        List<String> suburl=new ArrayList<>();
        list.forEach(x->suburl.add( x.findElement(By.tagName("a")).getAttribute("href")));
       for(int i=0;i<suburl.size();i++){
            try {
                String path = "";
                path = suburl.get(i);

                driver.get(path);
//                driver.manage().window().maximize();
//                driver.manage().deleteAllCookies();
//                driver.manage().window().setSize(new Dimension(1920,1080));
//                driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
//                driver.manage().timeouts().pageLoadTimeout(10,TimeUnit.SECONDS);
                // 与浏览器同步非常重要，必须等待浏览器加载完毕
                driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
//                WebElement img =driver.findElement(By.id("app"));
                File f = new File("html/" + DateUtlis.getDateString2(c));
                if (!f.exists()) {
                    f.mkdir();
                }

                File html = new File("html/" + DateUtlis.getDateString2(c) + "/" + path.substring(  path.lastIndexOf("/")+1));
                BufferedWriter output = new BufferedWriter(new FileWriter(html));
                output.write(driver.getPageSource());
                output.close();
                long ll2 = new Date().getTime();
                System.out.println(+(ll2 - ll) + "-->" + path);


                Thread.sleep(3000);
            }catch (Exception ex){
                ex.getStackTrace();
                System.out.println(ex.toString());
            }
        }

        long ll2=new Date().getTime();
        System.out.println("总耗时："+(ll2-ll));
        driver.close();
        driver.quit();
    }
}
