package com.wangjubao.dolphin.codegenerate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class WriteFile {
    public void write(String filePackage, String fileName, String fileExtentionName, String content) {
        String path = "src/test/java/" + filePackage.replace(".", "/") + "/" + fileName + "."
                + fileExtentionName;
        try {
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }

            f.getParentFile().mkdirs();

            if (f.createNewFile()) {
            } else {
                throw new RuntimeException("create file failed : " + path);
            }

            BufferedWriter utput = new BufferedWriter(new FileWriter(f));
            utput.write(content);
            utput.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
