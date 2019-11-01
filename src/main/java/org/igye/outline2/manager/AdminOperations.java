package org.igye.outline2.manager;

import org.igye.outline2.common.OutlineUtils;
import org.igye.outline2.exceptions.OutlineException;
import org.igye.outline2.rpc.RpcMethod;
import org.igye.outline2.rpc.RpcMethodsCollection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RpcMethodsCollection
@Component
public class AdminOperations {
    @Value("${backup.dir}")
    private String backupDirPath;
    @Value("${images.location}")
    private String imagesDirPath;
    @Value("${h2.version}")
    private String h2Version;
    @Value("${app.version}")
    private String appVersion;

    @PersistenceContext
    private EntityManager entityManager;

    @RpcMethod
    public void doBackup() throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String time = ZonedDateTime.now().format(formatter);
        File backupDir = new File(backupDirPath);
        final String backupZipPath = backupDir.getAbsolutePath()
                + "/" + time + "-outline-db--" + appVersion + "--" + h2Version + ".zip";
        OutlineUtils.getCurrentSession(entityManager).doWork(connection ->
                connection.prepareStatement("BACKUP TO '" + backupZipPath + "'").executeUpdate()
        );
        addDirToZip(new File(imagesDirPath).getAbsoluteFile().toPath(), Paths.get(backupZipPath));
    }

    private void addDirToZip(Path srcDir, Path zipPath) throws IOException {
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        try (FileSystem zipfs = FileSystems.newFileSystem(zipPath, this.getClass().getClassLoader())) {
            Files.walk(srcDir).forEach(externalFilePath -> {
                try {
                    Path pathInZipfile = zipfs.getPath(
                            srcDir.toFile().getName() + "/" + srcDir.relativize(externalFilePath)
                    );
                    if (externalFilePath.toFile().isDirectory()) {
                        Files.createDirectories(pathInZipfile);
                    } else {
                        Files.copy(externalFilePath, pathInZipfile, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException ex) {
                    throw new OutlineException(ex);
                }
            });
        }
    }
}
