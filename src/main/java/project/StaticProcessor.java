package project;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class StaticProcessor implements Processor {

	private String folder;

	public StaticProcessor(String folder) {
		this.folder = folder;
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		String path = exchange.getIn().getHeader(Exchange.HTTP_PATH, String.class);
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		if (Files.exists(Paths.get(folder, path))) {

			String contentType = getContentType(Paths.get(folder, path));
			exchange.getIn().setHeader(Exchange.CONTENT_TYPE, contentType);


			exchange.getIn().setBody(Files.readString(Paths.get(folder, path)));
		}
	}

	private String getContentType(Path filePath) {
		String fileName = filePath.getFileName().toString();
		int dotIndex = fileName.lastIndexOf('.');
		String fileExtension = dotIndex > 0 ? fileName.substring(dotIndex + 1) : "";

		switch (fileExtension.toLowerCase()) {
			case "html":
				return "text/html";
			case "css":
				return "text/css";
			case "js":
				return "application/javascript";
			default:
				return "application/octet-stream";
		}
	}
}