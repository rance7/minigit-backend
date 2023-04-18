package com.minigit.old;

import lombok.Data;

@Data
public class IndexEntry {
    private String hash;
    private String path;
    public IndexEntry(String hash, String path) {
        this.hash = hash;
        this.path = path;
    }
    public String toFile(){
        return hash + " " + path + "\n";
    }
}

