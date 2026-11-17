package dev.portfolio.incident;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="incidents",indexes=@Index(name="idx_incident_site",columnList="siteId"))
public class Incident {
 @Id public String id;
 @Column(nullable=false,unique=true,length=80) public String eventId;
 @Column(nullable=false,length=80) public String siteId;
 @Column(nullable=false,length=80) public String cameraId;
 @Column(nullable=false,length=60) public String eventType;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) public Status status;
 @Column(nullable=false) public Instant createdAt;
 @Version public long version;
 protected Incident() {}
 Incident(CameraEvent event){id=java.util.UUID.randomUUID().toString();eventId=event.eventId;siteId=event.siteId;cameraId=event.cameraId;eventType=event.eventType;status=Status.OPEN;createdAt=Instant.now();}
 public enum Status {OPEN,REVIEWING,CLOSED}
}
