package dev.portfolio.incident;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.transaction.PlatformTransactionManager;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.*;
@SpringBootTest @Testcontainers class MysqlContainerIT {
 @Container static MySQLContainer<?> mysql=new MySQLContainer<>("mysql:8.4").withDatabaseName("incidenttest");
 @DynamicPropertySource static void properties(DynamicPropertyRegistry registry){registry.add("spring.datasource.url",mysql::getJdbcUrl);registry.add("spring.datasource.username",mysql::getUsername);registry.add("spring.datasource.password",mysql::getPassword);registry.add("spring.datasource.driver-class-name",()->"com.mysql.cj.jdbc.Driver");registry.add("spring.jpa.hibernate.ddl-auto",()->"create-drop");}
 @Autowired IncidentService service; @Autowired IncidentRepository repo;
 @Test void duplicateDeliveryOneIncident() throws Exception {
  String key=UUID.randomUUID().toString();var event=new ApiModels.EventRequest(key,"site-1","cam-1","MOTION",Instant.parse("2026-11-17T12:00:00Z"));
  var first=service.ingest(event);var second=service.ingest(event);assertThat(first.created()).isTrue();assertThat(second.created()).isFalse();assertThat(first.response().id()).isEqualTo(second.response().id());
 }
}
