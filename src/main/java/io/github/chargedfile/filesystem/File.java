package io.github.chargedfile.filesystem;

public class File {
    private String id;
    private String name;
    private byte[] content;

    public File(String id, String name, byte[] content) {
        this.id = id;
        this.name = name;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public byte[] getContent() {
        return content;
    }
}