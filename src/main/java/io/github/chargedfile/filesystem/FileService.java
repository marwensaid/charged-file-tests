package io.github.chargedfile.filesystem;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FileService {
    private final Map<String, File> files = new HashMap<>();

    public String uploadFile(String name, byte[] content) {
        String id = UUID.randomUUID().toString();
        files.put(id, new File(id, name, content));
        return id;
    }

    public File getFile(String id) {
        return files.get(id);
    }

    public List<File> listFiles() {
        return new ArrayList<>(files.values());
    }

    public boolean deleteFile(String id) {
        return files.remove(id) != null;
    }
}