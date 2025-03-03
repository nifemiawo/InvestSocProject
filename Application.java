import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Application {
    // just an initial thing gonna make betteer and more modular later
    public static void main(String[] args) {
        String apiKey = System.getenv("ENV");
        // body of message
        var body = """
            {
              "model": "gpt-4o",
              "messages": [
                {
                  "role": "user",
                  "content": "get the latest financial news for me"
                }
              ]
            }
            """;
            
                
        // constructing request
      HttpRequest request=  HttpRequest.newBuilder()
        .uri(URI.create("https://api.openai.com/v1/chat/completions"))
        .header("Content-Type", "application/json")
        .header("Authorization", "Bearer "+ apiKey)
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build();


        HttpClient client = HttpClient.newHttpClient();
       HttpResponse<String> response = null;
    try {
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
    } catch (IOException | InterruptedException e) {
        e.printStackTrace();
    }

                
    }

}