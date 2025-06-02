package com.microblog.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/database")
@CrossOrigin(origins = "*")
@Slf4j
public class DatabaseInfoController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/info")
    public Map<String, Object> getDatabaseInfo() {
        Map<String, Object> info = new HashMap<>();

        try {
            // Database file information
            File dbFile = new File("wakanda-data/wakanda-social-db.mv.db");
            info.put("databaseExists", dbFile.exists());
            info.put("databasePath", dbFile.getAbsolutePath());
            info.put("databaseSize", dbFile.exists() ? dbFile.length() : 0);
            info.put("databaseSizeKB", dbFile.exists() ? dbFile.length() / 1024 : 0);

            // Database connection information
            try (Connection connection = dataSource.getConnection()) {
                DatabaseMetaData metaData = connection.getMetaData();
                info.put("databaseProductName", metaData.getDatabaseProductName());
                info.put("databaseProductVersion", metaData.getDatabaseProductVersion());
                info.put("driverName", metaData.getDriverName());
                info.put("driverVersion", metaData.getDriverVersion());
                info.put("url", metaData.getURL());
                info.put("userName", metaData.getUserName());
            }

            // Directory information
            File dataDir = new File("wakanda-data");
            info.put("dataDirectory", dataDir.getAbsolutePath());
            info.put("dataDirExists", dataDir.exists());

            if (dataDir.exists()) {
                File[] files = dataDir.listFiles();
                info.put("filesInDataDir", files != null ? files.length : 0);
                if (files != null) {
                    Map<String, Long> fileInfo = new HashMap<>();
                    for (File file : files) {
                        fileInfo.put(file.getName(), file.length());
                    }
                    info.put("fileDetails", fileInfo);
                }
            }

            // Additional info
            info.put("status", "✅ Wakanda Database Online");
            info.put("consoleUrl", "http://localhost:8080/h2-console");
            info.put("jdbcUrl", "jdbc:h2:file:./wakanda-data/wakanda-social-db");
            info.put("username", "wakanda_admin");

        } catch (Exception e) {
            log.error("Error getting database info", e);
            info.put("error", e.getMessage());
            info.put("status", "❌ Database Error");
        }

        return info;
    }
}
