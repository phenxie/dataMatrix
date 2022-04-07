package com.phenxie.sipdercommon.Base;


import org.apache.log4j.Logger;
import com.phenxie.sipdercommon.utils.DateUtlis;
import org.assertj.core.util.DateUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;
import java.io.File;
import java.util.concurrent.TimeUnit;

public class NewsPages {
    public static final String ExecMethod="exec";
    public static final String CalendarMethod="calendar";
    public String saveFloder="/Users/phenxie/103/newspaper";
    protected Map<String,String> map=new HashMap<>();
    protected WebDriver driver;
    public Calendar calendar;
    public  String chinsesName="报纸";
    public Logger logger=Logger.getLogger(this.getClass());
    public NewsPages(){
        //http://chromedriver.storage.googleapis.com/index.html
        //不显示浏览器



//        # 此步骤很重要，设置为开发者模式，防止被各大网站识别出来使用了Selenium
//        #chrome_options.add_experimental_option('excludeSwitches', ['enable-logging'])#禁止打印日志
//        chrome_options.add_experimental_option('excludeSwitches', ['enable-automation'])#实现了规避监测
//        #chrome_options.add_experimental_option("excludeSwitches",['enable-automation','enable-logging'])#上面两个可以同时设置
//        chrome_options.add_argument('--headless') # 无头模式
//        chrome_options.add_argument('--disable-gpu')  # 上面代码就是为了将Chrome不弹出界面
//        chrome_options.add_argument('--start-maximized')#最大化
//        chrome_options.add_argument('--incognito')#无痕隐身模式
//        chrome_options.add_argument("disable-cache")#禁用缓存
//        chrome_options.add_argument('disable-infobars')
//        chrome_options.add_argument('log-level=3')#INFO = 0 WARNING = 1 LOG_ERROR = 2 LOG_FATAL = 3 default is 0

        ChromeOptions chromeOptions=new ChromeOptions();
        chromeOptions.addArguments("--headless");
//        chromeOptions.addArguments("blink-settings=imagesEnabled=false");
//        chromeOptions.addArguments("--disable-dev-shm-usage");
//        chromeOptions.addArguments("--disable-gpu");
        chromeOptions.addArguments("--no-sandbox");
//        chromeOptions.addArguments("--log-level=3");
//        chromeOptions.addArguments("--incognito");
        driver = new ChromeDriver(chromeOptions);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        calendar=Calendar.getInstance();
    }

    public void exec(){
        logger.info("文件 "+this.chinsesName+" 保存地址："+saveFloder);
        pageParse();
        driver.close();
        driver.quit();

        File fileFolder=new File(saveFloder);
        if (fileFolder.isDirectory()){
            if(!fileFolder.exists()){
                fileFolder.mkdirs();
            }
        }
        String path=saveFloder
                +File.pathSeparator+this.getClass().getSimpleName();
        path=saveFloder+"/"+this.chinsesName;
        fileFolder=new File(path);
        if(!fileFolder.exists()){
            fileFolder.mkdirs();
        }
        path=saveFloder
                +File.pathSeparator+this.chinsesName
                +File.pathSeparator+DateUtlis.yyyyMMdd(calendar);
        path=saveFloder+"/"+this.chinsesName+"/"+DateUtlis.yyyyMMdd(calendar);
        fileFolder=new File(path);
        if(!fileFolder.exists()){
            fileFolder.mkdirs();
        }
        for (String filename:map.keySet()){

            String tagertFile=saveFloder
                    +File.pathSeparator+this.getClass().getSimpleName()
                    +File.pathSeparator+ DateUtlis.yyyyMMdd(calendar)
                    +File.pathSeparator+filename+".html";
            tagertFile=saveFloder
                    +"/"+this.chinsesName
                    +"/"+ DateUtlis.yyyyMMdd(calendar)
                    +"/"+filename+".html";
            try {
                BufferedWriter output = new BufferedWriter(new FileWriter(tagertFile));
                output.write(map.get(filename));
                output.close();
            }catch (Exception ex){

            }
        }
        logger.info(this.chinsesName+"保存结束");
    }

    public NewsPages pushData(String filename,String content){
        this.map.put(filename,content);
        return this;

    }
    public void calendar(Calendar c){
        this.calendar=c;
//        return this;
    }
    public NewsPages saveFloder(String path){
        this.saveFloder=path;
        return this;
    }
    public void pageParse(){

    }

    public static void main(String[] args) {
        new NewsPages().exec();
    }
}
