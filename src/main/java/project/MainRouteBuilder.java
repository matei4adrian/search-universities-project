package project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

public class MainRouteBuilder extends RouteBuilder {
	
	private int port;
	private String folders;

	public MainRouteBuilder(int port, String folders) {
		this.port = port;
		this.folders = folders;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void configure() throws Exception {
		List<String> foldersArray = Arrays.asList(folders.split(","));
		foldersArray.forEach((folder) -> fromF("jetty:http://127.0.0.1:%d/"+folder+"?matchOnUriPrefix=true", port)
				.process(new StaticProcessor(folder)));
		restConfiguration()
			.host("127.0.0.1")
			.port(port)
			.component("jetty")
			.contextPath("/api");
		rest("/universities")
			.produces("application/json")
			.get("/{university}")
			.to("direct:searchUniversities");

		from("direct:searchUniversities")
			.removeHeaders("CamelHttp*")
			.setHeader("Accept").constant("application/json")
			.setHeader("Accept-Encoding").constant("deflate")
			.setHeader(Exchange.HTTP_QUERY).simple("name=${header.university}")
			.setBody(constant(""))
			.to("http://universities.hipolabs.com/search")
			.convertBodyTo(String.class)
			.unmarshal().json(JsonLibrary.Jackson)
			.bean(University.class, "extractUniversities")
			.setHeader("size").simple("${body.size}")
			.split().body()
			.enrich("direct:searchUniversityDetails", (oldExchange, newExchange) -> {
				oldExchange.getIn()
					.getBody(University.class)
					.setDetails((Map<String, Object>) newExchange.getIn().getBody(Map.class));
				return oldExchange;
			})
			.aggregate(constant(true), (oldExchange, newExchange) -> {
				University university = newExchange.getIn().getBody(University.class);
				if (oldExchange == null) {
					List<University> universities = new ArrayList<University>();
					universities.add(university);
					newExchange.getIn().setBody(universities);
					return newExchange;
				} else {
					List<University> universities = oldExchange.getIn().getBody(List.class);
					universities.add(university);
					return oldExchange;
				}
			})
			.completionSize(simple("${header.size}"))
			.marshal().json(JsonLibrary.Jackson);
		from("direct:searchUniversityDetails")
			.removeHeaders("CamelHttp*")
			.setHeader("Accept").constant("application/json")
			.setHeader("Accept-Encoding").constant("deflate")
			.setHeader(Exchange.HTTP_QUERY).simple("name=${body.name}")
			.setBody(constant(""))
			.to("http://universities.hipolabs.com/search")
			.convertBodyTo(String.class)
			.unmarshal().json(JsonLibrary.Jackson)
			.bean(University.class, "extractUniversitiesDetails");
	}

}
