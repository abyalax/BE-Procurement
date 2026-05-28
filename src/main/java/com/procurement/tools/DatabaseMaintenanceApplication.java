package com.procurement.tools;

import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DatabaseMaintenanceApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(DatabaseMaintenanceApplication.class)
      .web(WebApplicationType.NONE)
      .run(args);
  }

  @Bean
  @ConditionalOnProperty(name = "maintenance.command")
  CommandLineRunner maintenanceCommand(DataSource dataSource, ApplicationArguments arguments) {
    return args -> {
      String command = firstArgument(arguments, "maintenance.command");
      if (command == null) {
        throw new IllegalArgumentException(
          "Missing maintenance.command. Use migrate, seed, or all."
        );
      }

      switch (command) {
        case "migrate" -> runLiquibase(
          dataSource,
          "classpath:db/changelog/db.changelog-schema.yml"
        );
        case "seed" -> runLiquibase(dataSource, "classpath:db/changelog/db.changelog-seed.yml");
        case "all" -> {
          runLiquibase(dataSource, "classpath:db/changelog/db.changelog-schema.yml");
          runLiquibase(dataSource, "classpath:db/changelog/db.changelog-seed.yml");
        }
        default -> throw new IllegalArgumentException(
          "Unsupported maintenance.command: " + command + ". Use migrate, seed, or all."
        );
      }
    };
  }

  private String firstArgument(ApplicationArguments arguments, String name) {
    if (!arguments.containsOption(name)) {
      return null;
    }
    return arguments.getOptionValues(name).stream().findFirst().orElse(null);
  }

  private void runLiquibase(DataSource dataSource, String changeLog) throws Exception {
    SpringLiquibase liquibase = new SpringLiquibase();
    liquibase.setDataSource(dataSource);
    liquibase.setChangeLog(changeLog);
    liquibase.afterPropertiesSet();
  }
}
