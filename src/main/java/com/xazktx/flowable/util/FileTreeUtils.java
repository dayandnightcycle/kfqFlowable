package com.xazktx.flowable.util;

import com.xazktx.flowable.model.FileTreeNode;
import org.apache.commons.compress.utils.Lists;
import org.springframework.util.CollectionUtils;

import java.io.File;

public class FileTreeUtils {
    /**
     * 加载这个文件下的文件树  包括文件夹和文件
     *
     * @param file         要加载的文件
     * @param readMaxLevel 最大读多少层   该值小于1则表示有多少层读取多少层
     * @return
     */
    public static FileTreeNode readZipFile(File file, int readMaxLevel) {
        return readZipFile(file, 0, readMaxLevel);
    }

    private static FileTreeNode readZipFile(File file, int fileLevel, int readMaxLevel) {
        if (readMaxLevel > 0 && fileLevel > readMaxLevel) {
            return null;
        }

        // 取得文件目录中所有文件的File对象数组
        File[] files = file.listFiles();
        if (files == null || files.length < 1) {
            return null;
        }

        FileTreeNode fileTreeNode = new FileTreeNode();
        fileTreeNode.setFileSize(file.length());
        file.getName().lastIndexOf(".");

        fileTreeNode.setName(file.getName());
        fileTreeNode.setFile(file);
        fileTreeNode.setLevel(fileLevel);
        if (file.isDirectory()) {
            //文件夹
            fileTreeNode.setType(1);
        } else {
            //文件
            fileTreeNode.setType(2);
        }

        if (CollectionUtils.isEmpty(fileTreeNode.getNextNodes())) {
            fileTreeNode.setNextNodes(Lists.newArrayList());
        }

        fileLevel++;
        // 遍历file数组
        for (File nextFile : files) {
            FileTreeNode nextTreeNode = new FileTreeNode();
            nextFile.getName().lastIndexOf(".");

            nextTreeNode.setName(nextFile.getName());
            nextTreeNode.setFile(nextFile);
            nextTreeNode.setLevel(fileLevel);
            nextTreeNode.setFileSize(nextFile.length());
            if (nextFile.isDirectory()) {
                nextTreeNode.setType(1);
                // 递归子目录
                FileTreeNode fileTreeNode1 = readZipFile(nextFile, fileLevel, readMaxLevel);
                if (fileTreeNode1 != null) {
                    fileTreeNode.getNextNodes().add(fileTreeNode1);
                }
            } else {
                nextTreeNode.setType(2);
                fileTreeNode.getNextNodes().add(nextTreeNode);
            }
        }
        return fileTreeNode;
    }
}
