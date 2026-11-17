package dev.portfolio.incident;
import static dev.portfolio.incident.ApiModels.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.annotation.Transactional;
@Service public class IncidentService {
 private final EventRepository events; private final IncidentRepository incidents; private final TransactionTemplate transactions;
 public IncidentService(EventRepository events,IncidentRepository incidents,org.springframework.transaction.PlatformTransactionManager manager){this.events=events;this.incidents=incidents;this.transactions=new TransactionTemplate(manager);}
 private String fingerprint(EventRequest r){try {String canonical=String.join("\n",r.eventId(),r.siteId(),r.cameraId(),r.eventType(),r.observedAt().toString());byte[] bytes=MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8));return HexFormat.of().formatHex(bytes);}catch(Exception e){throw new IllegalStateException(e);}}
 public IngestResult ingest(EventRequest request){
  String hash=fingerprint(request);
  try {
   return transactions.execute(s -> {
    var existing=events.findById(request.eventId());
    if(existing.isPresent())return replay(request.eventId(),hash,existing.get());
    CameraEvent event=new CameraEvent(request.eventId(),request.siteId(),request.cameraId(),request.eventType(),request.observedAt(),hash);
    events.saveAndFlush(event);
    Incident incident=incidents.saveAndFlush(new Incident(event));
    return new IngestResult(IncidentResponse.from(incident),true);
   });
  } catch(DataIntegrityViolationException conflict){
   // A concurrent writer may have inserted the same eventId. Transaction has ended (rolled back) before this read.
   return transactions.execute(s -> events.findById(request.eventId())
    .map(existing -> replay(request.eventId(),hash,existing))
    .orElseThrow(() -> conflict));
  }
 }
 private IngestResult replay(String eventId,String hash,CameraEvent prior){
  if(!prior.payloadHash.equals(hash))throw new ApiException(HttpStatus.CONFLICT,"Event ID reused with different event data");
  Incident incident=incidents.findByEventId(eventId).orElseThrow(() -> new IllegalStateException("Event without incident"));
  return new IngestResult(IncidentResponse.from(incident),false);
 }
 public record IngestResult(IncidentResponse response,boolean created){}
 @Transactional(readOnly=true) public IncidentResponse get(String id){return IncidentResponse.from(incidents.findById(id).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Incident not found")));}
 @Transactional public IncidentResponse update(String id,StatusRequest request){
  Incident incident=incidents.findById(id).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Incident not found"));
  if(incident.version!=request.expectedVersion())throw new ApiException(HttpStatus.CONFLICT,"Stale version; refresh incident and try again");
  incident.status=request.status(); incidents.flush(); // @Version protects competing writes on commit/flush
  return IncidentResponse.from(incident);
 }
}
