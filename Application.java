import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonArray;
import javax.json.*;

public class Application {
    
    public static void main(String[] args) {
        
     
        
            String apiKey = System.getenv("NEWSAPIKEY");
            
            if (apiKey != null) {
                System.out.println("hi");
            } else {
                System.out.println("bye");
            }
            System.out.println(getNews(apiKey));
            
        }
        

       
                
    


    private static String getNews(String key){

        String url = "https://newsapi.org/v2/top-headlines?category=business&apiKey=" + key;
        StringBuilder newsContent = new StringBuilder();


        HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .header("Content-Type", "application/json")
        .build();


        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response;

        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonReader jsonReader = Json.createReader(new StringReader(response.body()));
            JsonObject jsonObject = jsonReader.readObject();
            jsonReader.close();


            JsonArray articles = jsonObject.getJsonArray("articles");
            // get 10 articles related to business
            for (int i =0; i < 10; i++){
                JsonObject article = articles.getJsonObject(i);
                String title = article.getString("title");
                String newsUrl = article.getString("url");

                newsContent.append("Title: ").append(title).append("\n");
                newsContent.append("URL: ").append(newsUrl).append("\n\n");
            }

            
        } catch (Exception e) {
          System.out.println(e.getMessage());
          e.printStackTrace();
        }
        return newsContent.toString();
    }

}