package dev.portfolio.incident;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Instant;
import java.util.UUID;
@SpringBootTest @AutoConfigureMockMvc class IncidentIntegrationTest {
 @Autowired MockMvc mvc; @Autowired ObjectMapper json; @Autowired IncidentRepository incidents;
 String event(String id,String camera){return "{\"eventId\":\""+id+"\",\"siteId\":\"site-1\",\"cameraId\":\""+camera+"\",\"eventType\":\"MOTION\",\"observedAt\":\"2026-11-17T12:00:00Z\"}";}
 @Test void duplicateEventReturnsSameIncident() throws Exception {
  String key=UUID.randomUUID().toString(); var first=mvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(event(key,"cam-1"))).andExpect(status().isCreated()).andReturn();
  var next=mvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(event(key,"cam-1"))).andExpect(status().isOk()).andReturn();
  assertThat(json.readTree(first.getResponse().getContentAsString()).get("id").asText()).isEqualTo(json.readTree(next.getResponse().getContentAsString()).get("id").asText());
  assertThat(incidents.findByEventId(key)).isPresent();
 }
 @Test void sameEventIdWithDifferentBodyRejected() throws Exception {
  String key=UUID.randomUUID().toString();mvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(event(key,"cam-a"))).andExpect(status().isCreated());
  mvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(event(key,"cam-b"))).andExpect(status().isConflict());
 }
 @Test void staleVersionRejected() throws Exception {
  String key=UUID.randomUUID().toString(); var created=mvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(event(key,"cam-1"))).andReturn();
  var node=json.readTree(created.getResponse().getContentAsString());String id=node.get("id").asText();long version=node.get("version").asLong();
  mvc.perform(patch("/api/incidents/"+id+"/status").contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"REVIEWING\",\"expectedVersion\":"+version+"}")).andExpect(status().isOk());
  mvc.perform(patch("/api/incidents/"+id+"/status").contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CLOSED\",\"expectedVersion\":"+version+"}")).andExpect(status().isConflict());
 }
}
