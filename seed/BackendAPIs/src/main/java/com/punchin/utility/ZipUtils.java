package com.punchin.utility;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class ZipUtils {

    private List <String> fileList;
    public ZipUtils() {
        fileList = new ArrayList < String > ();
    }

    public void zipIt(String zipFile, String sourceFolder) {
        byte[] buffer = new byte[1024];
        String source = new File(sourceFolder).getName();
        log.info("zipIt source {}", source);
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
            fos = new FileOutputStream(zipFile);
            zos = new ZipOutputStream(fos);

            log.info("Output to Zip : " + zipFile);

            for (String file: this.fileList) {
                log.info("File Added : " + file);
                ZipEntry ze = new ZipEntry(file);
                zos.putNextEntry(ze);
                try (FileInputStream in = new FileInputStream(sourceFolder + File.separator + file)){
                    log.info("FILE read : file {}", sourceFolder + File.separator + file);
                    int len;
                    while ((len = in .read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                } finally {
                    log.info("FileInputStream closing");
                    log.info("FileInputStream closed");
                }
            }

            zos.closeEntry();
            log.info("Folder successfully compressed");

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                zos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void generateFileList(File node, String source) {
        // add file only
        if (node.isFile()) {
            fileList.add(generateZipEntry(node.toString(), source));
        }

        if (node.isDirectory()) {
            String[] subNote = node.list();
            for (String filename: subNote) {
                generateFileList(new File(node, filename), source);
            }
        }
    }

    private String generateZipEntry(String file, String source) {
        return file.substring(source.length() + 1, file.length());
    }


}
