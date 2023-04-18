package com.minigit.old;

import com.minigit.util.FileUtils;
import com.minigit.util.GitUtils;
import lombok.Data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Data
public class Index {
    private List<IndexEntry> entries; // 记录文件的修改状态，分别是添加、修改和删除

    public Index() {
        this.entries = new ArrayList<>();
    }

    public void addEntry(IndexEntry indexEntry) {
        entries.add(indexEntry);
    }

    // 将content的内容写入index文件
    public void write() throws IOException {
        String content = getIndexContent();
        FileUtils.writeFile(GitUtils.indexPath, content);
    }

    // 获取index文件中的内容，并保存到content字符串中
    public String getIndexContent() {
        StringBuilder sb = new StringBuilder();
        for (IndexEntry entry : entries) {
            sb.append(entry.toString()).append("\n");
        }
        return sb.toString();
    }


    // 将index文件中的内容读取到index对象
    public Index read() throws IOException {
        String indexPath = GitUtils.indexPath;
        String content = FileUtils.readFile(indexPath);
        if (content == null || content.isEmpty()) {
            return null;
        }

        String[] lines = content.split("\\r?\\n"); // 匹配回车或者换行

        int currentIndex = 0;

        Index index = new Index();

        int entryNum = lines.length;

        List<IndexEntry> entries = new ArrayList<>(entryNum);
        for (int i = 0; i < entryNum; i++) {
            String[] line =lines[currentIndex++].split(" ");
            entries.add(new IndexEntry(line[0],line[1]));
        }
        index.setEntries(entries);

        return index;
    }
}