package com.microblog.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@Slf4j
public class DatabaseConfig {

    @Bean
    @Order(1) // Ensure this runs first
    public CommandLineRunner createDatabaseDirectory() {
        return args -> {
            try {
                // Create the wakanda-data directory if it doesn't exist
                Path dataDir = Paths.get("wakanda-data");
                if (!Files.exists(dataDir)) {
                    Files.createDirectories(dataDir);
                    log.info("🏛️ Created Wakanda data directory: {}", dataDir.toAbsolutePath());
                } else {
                    log.info("🏛️ Wakanda data directory exists: {}", dataDir.toAbsolutePath());
                }

                // Log database file location
                File dbFile = new File("wakanda-data/wakanda-social-db.mv.db");
                if (dbFile.exists()) {
                    log.info("📊 Wakanda database found: {} (Size: {} KB)",
                            dbFile.getAbsolutePath(),
                            dbFile.length() / 1024);
                } else {
                    log.info("📊 New Wakanda database will be created at: {}",
                            new File("wakanda-data/wakanda-social-db.mv.db").getAbsolutePath());
                }

                // Create a README file in the data directory
                createReadmeFile(dataDir);

                log.info("🔗 Database Console: http://localhost:8080/h2-console");
                log.info("🔑 JDBC URL: jdbc:h2:file:./wakanda-data/wakanda-social-db");
                log.info("👤 Username: wakanda_admin");
                log.info("🔐 Password: vibranium_secure_2024");

            } catch (Exception e) {
                log.error("❌ Failed to create Wakanda data directory", e);
            }
        };
    }

    private void createReadmeFile(Path dataDir) {
        try {
            Path readmePath = dataDir.resolve("README.md");
            if (!Files.exists(readmePath)) {
                String readmeContent = """
                # 🌟 Wakanda Social Database
                
                This folder contains all your Wakanda Social application data.
                
                ## 📁 Files:
                - `wakanda-social-db.mv.db` - Main database file (contains all user data, posts, comments, etc.)
                - `wakanda-social-db.trace.db` - Database trace file (for debugging)
                
                ## 🔐 Database Access:
                - **JDBC URL**: `jdbc:h2:file:./wakanda-data/wakanda-social-db`
                - **Username**: `wakanda_admin`
                - **Password**: `vibranium_secure_2024`
                - **Web Console**: `http://localhost:8080/h2-console`
                
                ## 🛡️ Security:
                - All user passwords are encrypted using BCrypt
                - JWT tokens are used for secure authentication
                - Database is file-based and persistent
                
                ## 📊 Data Persistence:
                - All data is automatically saved to this folder
                - Data persists between application restarts
                - Backup this folder to preserve all user data
                
                ## 🚀 Demo Users:
                - alice / password123 (Guardian of Knowledge)
                - bob / password123 (Tech Innovator)
                - charlie / password123 (Positivity Spreader)
                - diana / password123 (Warrior Princess)
                - erik / password123 (Revolutionary Thinker)
                
                ## 🚀 Created by Wakanda Social v1.0
                """;

                Files.write(readmePath, readmeContent.getBytes());
                log.info("📝 Created README file in Wakanda data directory");
            }
        } catch (Exception e) {
            log.warn("⚠️ Could not create README file", e);
        }
    }
}
