package dev.portfolio.incident;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="camera_events")
public class CameraEvent {
 @Id @Column(length=80) public String eventId;
 @Column(nullable=false,length=80) public String siteId;
 @Column(nullable=false,length=80) public String cameraId;
 @Column(nullable=false,length=60) public String eventType;
 @Column(nullable=false) public Instant observedAt;
 @Column(nullable=false,length=64) public String payloadHash;
 protected CameraEvent() {}
 CameraEvent(String eventId,String siteId,String cameraId,String eventType,Instant observedAt,String payloadHash){this.eventId=eventId;this.siteId=siteId;this.cameraId=cameraId;this.eventType=eventType;this.observedAt=observedAt;this.payloadHash=payloadHash;}
}
