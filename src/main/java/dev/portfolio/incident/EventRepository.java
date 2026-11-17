package dev.portfolio.incident;
import org.springframework.data.jpa.repository.JpaRepository;
public interface EventRepository extends JpaRepository<CameraEvent,String> {}
