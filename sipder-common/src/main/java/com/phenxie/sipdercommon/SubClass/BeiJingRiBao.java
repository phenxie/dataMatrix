package com.phenxie.sipdercommon.SubClass;

import com.phenxie.sipdercommon.Base.NewsPages;
import com.phenxie.sipdercommon.utils.DateUtlis;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.*;

public class BeiJingRiBao extends NewsPages {

    @Override
    public void pageParse() {
//        System.out.println("北京日报");
        this.chinsesName="北京日报";
        Calendar c=this.calendar;
        String path="https://bjrbdzb.bjd.com.cn/fzxb/mobile/"+c.get(Calendar.YEAR)+"/"+DateUtlis.getDateString2(c)+"/"+DateUtlis.getDateString2(c)+"_m.html?v=1642139670898#page0";
        driver.get(path);
        List<WebElement> list= driver.findElements(By.className("nav-list-group"));
        Map<String,String> suburl=new HashMap<String,String>();
        logger.info(driver.getPageSource());
        list.forEach(x-> x.findElements(By.tagName("a")).forEach(y->{
                    suburl.put(y.getAttribute("innerText"),"https://bjrbdzb.bjd.com.cn/fzxb/mobile/"
                            +c.get(Calendar.YEAR)+"/"
                            +DateUtlis.getDateString2(c)
                            +y.getAttribute("data-href").substring(1));
            //https://bjrbdzb.bjd.com.cn/fzxb/mobile/2022/20220401/20220401_001/content_20220401_001_1.htm#page0
                }));

        logger.info("找到链接数量："+suburl.size());
        for(String s:suburl.keySet()){
            try {
                boolean issucess=false;
                int retryCount=10;
                while (!issucess) {
                    path = suburl.get(s);
                    Thread.sleep(1000 * 10);
                    driver.get(path);

                    if (!driver.getTitle().equals("403 Forbidden")) {
                        pushData(s, driver.getPageSource());
                        issucess=true;
                        logger.info(s + " 下载成功！");
                    } else {
                        logger.info(s + " 拒绝访问！300秒后重试！");
                        Thread.sleep(1000*300);
                    }
                    retryCount--;
                    if(retryCount==0){
                        logger.info("重试次数达到最大值，不再重试！");
                    }
                }
            } catch (Exception ex) {
                logger.error(ex.toString());
                continue;
            }
        }
    }

    public static void main(String[] args) {
        new BeiJingRiBao().exec();
    }
}
