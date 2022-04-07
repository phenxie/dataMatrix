package com.example.reflectcommon.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * @ClassName: WriteFile
 * @Description: 写文件操作

 *
 */
public class WriteFile {

//    private static String pathname = "src/com/adamjwh/jnp/ex14/out.txt";

    public static void write(StringBuffer sBuffer,String pathname) throws Exception {
        File file = new File(pathname);
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));

        bw.write(sBuffer.toString());
        bw.close();
    }

}
