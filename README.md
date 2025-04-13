# charged-file-tests

### TD : Tests de Charge et Tests aux Limites d'une API avec JMeter et Java

---

### **Introduction**
Les tests de charge permettent de valider les performances d'une API lorsqu'elle est soumise à une forte charge, tandis que les tests aux limites permettent de vérifier comment l'API réagit à des scénarios extrêmes (comme un grand nombre de requêtes, des données volumineuses ou des scénarios d'erreur). Dans ce TD, vous allez utiliser **JMeter** pour effectuer ces tests sur une API que vous allez implémenter en Java avec Spring Boot.

---

### **Objectifs**

1. Développer une API en Java Spring Boot avec les endpoints nécessaires.
2. Installer et configurer JMeter pour effectuer des tests de charge et des tests aux limites.
3. Simuler plusieurs scénarios dans JMeter, notamment :
   - **Tests de charge** : Nombre élevé de requêtes simultanées.
   - **Tests aux limites** : Envoi de données volumineuses ou invalides.
   - **Tests de montée en charge** : Augmentation progressive du trafic.
   - **Tests de stress** : Déterminer le point de rupture de l'API.
4. Collecter et analyser les métriques de performance (temps de réponse, taux d'erreur, etc.).

---

### **Prérequis**

- **Java 11** ou plus récent installé.
- **Spring Boot** configuré.
- **Maven** ou **Gradle** installé.
- **Apache JMeter** installé.
- Une machine capable d'exécuter des tests de charge (CPU et mémoire suffisants).

---

### **Consignes**

1. **Étape 1 : Développer l'API**
   Implémentez une API REST simple pour gérer un système de gestion de fichiers. L'API doit inclure les fonctionnalités suivantes :
   - **Upload de fichier** (`POST /files/upload`) : Permet de télécharger un fichier.
   - **Téléchargement de fichier** (`GET /files/download/{id}`) : Permet de récupérer un fichier par son ID.
   - **Liste des fichiers** (`GET /files`) : Retourne la liste des fichiers disponibles.
   - **Suppression de fichier** (`DELETE /files/{id}`) : Supprime un fichier par son ID.

2. **Étape 2 : Installer et configurer JMeter**
   - Installez Apache JMeter ([lien de téléchargement officiel](https://jmeter.apache.org/)).
   - Créez un plan de test JMeter pour simuler les différents scénarios de charge et de tests aux limites.

3. **Étape 3 : Configurer les scénarios de tests**
   - **Test de charge** :
     - Simulez 100 utilisateurs simultanés envoyant 1000 requêtes au total.
   - **Test aux limites** :
     - Envoi d'un fichier volumineux de 50 Mo.
     - Envoi de requêtes avec des données invalides (par exemple, des fichiers non supportés).
   - **Test de montée en charge** :
     - Augmentez progressivement le nombre d'utilisateurs de 10 à 500.
   - **Test de stress** :
     - Continuez à augmenter la charge jusqu'à ce que l'API atteigne son point de rupture.

4. **Étape 4 : Collecter et analyser les résultats**
   - Mesurez les temps de réponse moyens et maximums, les taux d'erreur et le nombre total de requêtes traitées.
   - Identifiez les goulots d'étranglement et proposez des solutions pour améliorer les performances.

---

### **Code de l'API**

#### Classe `File`
````java name=src/main/java/com/example/filesystem/File.java
package com.example.filesystem;

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
````

---

#### Service `FileService`
````java name=src/main/java/com/example/filesystem/FileService.java
package com.example.filesystem;

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
````

---

#### Contrôleur `FileController`
````java name=src/main/java/com/example/filesystem/FileController.java
package com.example.filesystem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String id = fileService.uploadFile(file.getOriginalFilename(), file.getBytes());
            return ResponseEntity.ok(id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading file");
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String id) {
        File file = fileService.getFile(id);
        if (file != null) {
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"")
                    .body(file.getContent());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @GetMapping
    public List<String> listFiles() {
        return fileService.listFiles().stream()
                .map(File::getName)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFile(@PathVariable String id) {
        if (fileService.deleteFile(id)) {
            return ResponseEntity.ok("File deleted");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
    }
}
````

---

#### Configuration `application.properties`
````properties name=src/main/resources/application.properties
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB
server.tomcat.max-threads=200
````

---

### **Tests de Charge et Limites avec JMeter**

#### Scénarios dans JMeter

1. **Test de charge simple (GET /files)** :
   - Ajoutez un **Thread Group** avec 100 utilisateurs simultanés.
   - Ajoutez un **HTTP Request Sampler** pour appeler `GET /files`.
   - Ajoutez un **View Results Tree** pour surveiller les réponses.

2. **Test aux limites (POST /files/upload)** :
   - Ajoutez un **HTTP Request Sampler** pour simuler l'upload d'un fichier de 50 Mo.
   - Ajoutez un **Response Assertion** pour vérifier que le serveur répond avec un code HTTP 200.

3. **Test de montée en charge** :
   - Configurez le **Thread Group** pour augmenter progressivement le nombre d'utilisateurs (de 10 à 500 sur 1 minute).
   - Ajoutez des requêtes pour chaque endpoint (`GET /files`, `POST /files/upload`, `DELETE /files/{id}`).

4. **Test de stress** :
   - Augmentez progressivement la charge jusqu'à ce que l'API commence à renvoyer des erreurs (code HTTP 500 ou temps de réponse > 10s).

---

### **Livrables**

1. **Code source complet** de l'API.
2. **Plan de test JMeter** (fichier `.jmx`).
3. **Rapport d'exécution des tests** :
   - Temps de réponse moyen et maximum.
   - Taux d'erreur.
   - Courbes de montée en charge et de stress.
4. Propositions d'amélioration pour les performances.

---

### **Ressources utiles**

- [Documentation officielle de JMeter](https://jmeter.apache.org/)
- [Documentation Spring Boot](https://spring.io/projects/spring-boot)
- [Tutoriel JMeter pour les tests de charge](https://www.blazemeter.com/jmeter-tutorial)

---
