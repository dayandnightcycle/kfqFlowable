package com.xazktx.flowable.model;

import lombok.Data;

import java.io.File;
import java.util.List;

@Data
public class FileTreeNode {
    /**
     * 文件名称
     */
    private String name;

    /**
     * 文件内容
     */
    private File file;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 当前文件所属层级
     */
    private int level;

    /**
     * 类型  1:文件夹  2:文件
     */
    private int type;

    /**
     * 下级文件
     */
    private List<FileTreeNode> nextNodes;

    /**
     * 是否有下级
     */
    public boolean hasNext() {
        return nextNodes != null && nextNodes.size() > 0;
    }

    /**
     * 是不是文件夹
     */
    public boolean isDirectory() {
        return type == 1;
    }

    /**
     * 是不是文件
     */
    public boolean isFile() {
        return type == 2;
    }
}
