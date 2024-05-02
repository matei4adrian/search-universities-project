package project;

import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class University {

    @JsonProperty
    private String name;

    @JsonProperty
    private String country;

    @JsonProperty
    private String webPage;


    private University(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getWebPage() {
        return webPage;
    }

    public void setWebPage(String webPage) {
        this.webPage = webPage;
    }

    public void setDetails(Map<String, Object> details) {
        var fields = Arrays.asList(getClass().getDeclaredFields())
                .stream()
                .collect(toMap(field -> field.getName(), field -> field));
        for (var entry : fields.entrySet()) {
            if (details.containsKey(entry.getKey())) {
                entry.getValue().setAccessible(true);
                try {
                    entry.getValue().set(this, details.get(entry.getKey()));
                } catch (IllegalArgumentException | IllegalAccessException e) {
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static List<University> extractUniversities(Exchange exchange) {
        List<University> universities = new ArrayList<University>();
        List body = exchange.getIn().getBody(List.class);
        for (Object univ : body) {
            Map<String, Object> universityMap = (Map<String, Object>) univ;
            Object nameValue = universityMap.get("name");
            universities.add(new University(nameValue.toString()));
        }
        return universities;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> extractUniversitiesDetails(Exchange exchange) {
        Map<String, Object> details = new HashMap<String, Object>();
        List body = exchange.getIn().getBody(List.class);
        Map<String, Object> university = (Map<String, Object>) body.get(0);
        var webPages = (ArrayList<String>) university.get("web_pages");
        var webPage = webPages.get(0);
        var country = (String) university.get("country");

        details.put("webPage", webPage);
        details.put("country", country);
        return details;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return super.toString();
        }
    }

}
