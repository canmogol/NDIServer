package com.fererlab.action;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;

/**
 * acm | 2/27/13
 */
public class FileContentHandler {

    private String contentPath;
    private String filePath;
    private String fileExtension = "";

    public byte[] getContent(String contentPath, String filePath) throws FileNotFoundException {
        return getContent(contentPath + filePath);
    }

    public byte[] getContent(String filePath) throws FileNotFoundException {
        byte[] bytes = new byte[0];
        fileExtension = filePath.substring(filePath.lastIndexOf(".") + 1).trim();
        RandomAccessFile f = null;
        try {
            f = new RandomAccessFile(filePath, "r");
            bytes = new byte[(int) f.length()];
            f.read(bytes);
        } catch (FileNotFoundException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (f != null) {
                    f.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bytes;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public String getContentPath() {
        if (contentPath == null) {
            URL currentPath = Thread.currentThread().getContextClassLoader().getResource("");
            if (currentPath != null) {
                contentPath = currentPath.getPath();
            }
        }
        return contentPath;
    }

}
