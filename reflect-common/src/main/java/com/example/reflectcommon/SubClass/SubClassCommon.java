package com.example.reflectcommon.SubClass;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Set;
import com.phenxie.sipdercommon.Base.NewsPages;
public class SubClassCommon  {

    public static void main(String[] args)  {

        start("com/phenxie/sipdercommon/SubClass");


    }
    public static void start(String basePackageString){
        try {
            ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
            provider.addIncludeFilter(new AssignableTypeFilter(NewsPages.class));
            Set<BeanDefinition> components = provider.findCandidateComponents(basePackageString);

            for (BeanDefinition component : components) {
                for(int i=1;i<2;i++) {
                    Class cls = Class.forName(component.getBeanClassName());
                    Object objectCopy = cls.getConstructor(new Class[]{}).newInstance(new Class[]{});

                    Method execMethod = NewsPages.class.getMethod(NewsPages.ExecMethod, new Class[]{});
//                Method calendarMethod = NewsPages.class.getMethod(NewsPages.CalendarMethod, new Class[]{});
                    Calendar c = Calendar.getInstance();
                    c.add(Calendar.DATE, -1*i);
//                calendarMethod.invoke(objectCopy,c);
                    NewsPages p = (NewsPages) objectCopy;
                    p.calendar(c);
                    execMethod.invoke(p);
                }
            }
        }catch (Exception ex){
            System.out.println(ex.toString());
        }
    }


}
