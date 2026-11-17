package dev.portfolio.incident;
import static dev.portfolio.incident.ApiModels.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api") public class IncidentController {
 private final IncidentService service;
 IncidentController(IncidentService service){this.service=service;}
 @PostMapping("/events") public ResponseEntity<IncidentResponse> ingest(@Valid @RequestBody EventRequest req){var result=service.ingest(req);return ResponseEntity.status(result.created()?HttpStatus.CREATED:HttpStatus.OK).body(result.response());}
 @GetMapping("/incidents/{id}") public IncidentResponse get(@PathVariable String id){return service.get(id);}
 @PatchMapping("/incidents/{id}/status") public IncidentResponse status(@PathVariable String id,@Valid @RequestBody StatusRequest req){return service.update(id,req);}
}
