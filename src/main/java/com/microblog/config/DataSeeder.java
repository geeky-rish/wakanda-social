package com.microblog.config;

import com.microblog.entity.User;
import com.microblog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2) // Run after DatabaseConfig
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Force database connection and table creation
        try {
            long userCount = userRepository.count();
            log.info("🔍 Current user count in database: {}", userCount);

            if (userCount == 0) {
                log.info("🌱 Seeding Wakanda database with initial citizens...");

                User user1 = new User("alice", "alice@wakanda.com", passwordEncoder.encode("password123"));
                User user2 = new User("bob", "bob@wakanda.com", passwordEncoder.encode("password123"));
                User user3 = new User("charlie", "charlie@wakanda.com", passwordEncoder.encode("password123"));
                User user4 = new User("diana", "diana@wakanda.com", passwordEncoder.encode("password123"));
                User user5 = new User("erik", "erik@wakanda.com", passwordEncoder.encode("password123"));

                user1.setDisplayName("Alice Johnson");
                user1.setBio("Guardian of Wakanda's wisdom and knowledge 📚✨");

                user2.setDisplayName("Bob Smith");
                user2.setBio("Tech innovator brewing vibranium solutions ☕⚡");

                user3.setDisplayName("Charlie Brown");
                user3.setBio("Spreading positivity across the kingdom 🌟😊");

                user4.setDisplayName("Diana Prince");
                user4.setBio("Warrior princess defending justice and truth 🛡️👑");

                user5.setDisplayName("Erik Stevens");
                user5.setBio("Revolutionary thinker challenging the status quo 🔥💭");

                // Save users one by one and flush to ensure persistence
                userRepository.saveAndFlush(user1);
                userRepository.saveAndFlush(user2);
                userRepository.saveAndFlush(user3);
                userRepository.saveAndFlush(user4);
                userRepository.saveAndFlush(user5);

                // Verify the save
                long finalCount = userRepository.count();
                log.info("✅ Wakanda database seeded successfully with {} citizens", finalCount);
                log.info("🔐 Demo login credentials:");
                log.info("   👤 alice / password123");
                log.info("   👤 bob / password123");
                log.info("   👤 charlie / password123");
                log.info("   👤 diana / password123");
                log.info("   👤 erik / password123");

            } else {
                log.info("🏛️ Wakanda database already contains {} citizens, skipping seeding", userCount);
                log.info("📊 Database location: ./wakanda-data/wakanda-social-db.mv.db");
            }
        } catch (Exception e) {
            log.error("❌ Error during database seeding", e);
            throw e;
        }
    }
}
