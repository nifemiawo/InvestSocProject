import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonArray;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;


import java.time.LocalDate;
public class Application {
    public static void main(String[] args) {

        
        String apiKey = System.getenv("NEWSAPIKEY");
        String senderEmail = System.getenv("EMAIL");
        String senderPassword = System.getenv("EMAIL_PASSWORD");


        
        String newsContent = getNews(apiKey);
        // getting current date
        LocalDate date = LocalDate.now();

        // set to store mailing list with
        HashSet<String> mailingList = new HashSet<String>();
        
        //dummy email
        mailingList.add("footyworldinsta@gmail.com");
    

        sendEmail(mailingList, senderEmail, senderPassword, newsContent, date);
    }

    /**
     * Method responsible for getting news from API
     * @param key - API key
     * @return the news
     */
    private static String getNews(String key) {

        
        String url = "https://newsapi.org/v2/top-headlines?category=business&apiKey=" + key;
        StringBuilder newsContent = new StringBuilder();

        // build a request
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
            // get  articles related to business
            for (int i = 0; i <= 4; i++) {
                JsonObject article = articles.getJsonObject(i);
                String title = article.getString("title");
                String newsUrl = article.getString("url");

                // make title heading h3 size and urls all say "read more"
                newsContent.append("<h3>").append(title).append("</h3>");
                newsContent.append("<p><a href='").append(newsUrl).append("'>Read more</a></p>");

            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return newsContent.toString();
    }

    /**
     * Method responsible for sending the news to all members of the mailing list
     * 
     * @param mailingList - the mailing list (stored as a set)
     * @param senderEmail - the senders email
     * @param senderPassword - the password for the senders email
     * @param newsContent - the news content to email
     */
    public static void sendEmail(HashSet<String> mailingList, String senderEmail, String senderPassword, String newsContent, LocalDate date) {
        // SMTP Configuration for Gmail
        Properties props = new Properties();
       props.put("mail.smtp.auth", "true");
props.put("mail.smtp.ssl.enable", "true");  
props.put("mail.smtp.host", "smtp.gmail.com");
props.put("mail.smtp.port", "465"); 
props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        // Authenticate sender
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            for (String recipient : mailingList) {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(senderEmail));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
               message.setSubject("Daily Business News");
                message.setContent(
              "<h1>Finance and Business news on " + date +  "</h1>" + newsContent.toString() ,  "text/html");
              message.saveChanges();
              
              
           //  message.saveChanges();
              //  message.setText(newsContent);

                // Send email
                Transport.send(message);
                System.out.println("Email sent to: " + recipient);
            }
        } catch (MessagingException e) {
           System.out.println("Invalid email address " );
        }
    }
}
