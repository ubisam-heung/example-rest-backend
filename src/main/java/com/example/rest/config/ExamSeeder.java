package com.example.rest.config;

import java.sql.Connection;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;

@Configuration
public class ExamSeeder {
  private static final Logger logger = LoggerFactory.getLogger(ExamSeeder.class);
  private static final String SEED_RESOURCE = "db/exam_seed_20.sql";

  private final JdbcTemplate jdbcTemplate;
  private final DataSource dataSource;

  public ExamSeeder(JdbcTemplate jdbcTemplate, DataSource dataSource) {
    this.jdbcTemplate = jdbcTemplate;
    this.dataSource = dataSource;
  }

  @Bean
  public CommandLineRunner seedExamData() {
    return args -> {
      if (!tableExists("exam")) {
        logger.info("Exam table not found. Skipping seed.");
        return;
      }

      Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM exam", Long.class);
      if (count != null && count > 0) {
        logger.info("Exam table already has data ({} rows). Skipping seed.", count);
        return;
      }

      Resource resource = new ClassPathResource(SEED_RESOURCE);
      try (Connection connection = dataSource.getConnection()) {
        ScriptUtils.executeSqlScript(connection, resource);
        logger.info("Seeded exam data from {}", SEED_RESOURCE);
      }
    };
  }

  private boolean tableExists(String tableName) {
    Integer count = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
        Integer.class,
        tableName
    );
    return count != null && count > 0;
  }

}
