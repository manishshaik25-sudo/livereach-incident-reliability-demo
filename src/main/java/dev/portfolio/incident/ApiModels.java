package dev.portfolio.incident;
import jakarta.validation.constraints.*;
import java.time.Instant;
public final class ApiModels {
 private ApiModels() {}
 public record EventRequest(@NotBlank String eventId,@NotBlank String siteId,@NotBlank String cameraId,@NotBlank String eventType,@NotNull Instant observedAt) {}
 public record IncidentResponse(String id,String eventId,String siteId,String cameraId,String eventType,Incident.Status status,long version) {
  static IncidentResponse from(Incident i){return new IncidentResponse(i.id,i.eventId,i.siteId,i.cameraId,i.eventType,i.status,i.version);}
 }
 public record StatusRequest(@NotNull Incident.Status status,@NotNull Long expectedVersion) {}
}
