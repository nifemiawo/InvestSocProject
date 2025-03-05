import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
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


public class Application {
    public static void main(String[] args) {
        String apiKey = System.getenv("NEWSAPIKEY");
        String senderEmail = System.getenv("EMAIL");
        String senderPassword = System.getenv("EMAIL_PASSWORD");

        String newsContent = getNews(apiKey);

        ArrayList<String> mailingList = new ArrayList<>();
        mailingList.add("munsif.shameem@gmail.com");

        sendEmail(mailingList, senderEmail, senderPassword, newsContent);
    }

    private static String getNews(String key) {

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
            for (int i = 0; i < 10; i++) {
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

    /**
     * Method responsible for sending the news to all members of the mailing list
     * 
     * @param mailingList - the mailing list (stored as an arraylist)
     * @param senderEmail - the senders email
     * @param senderPassword - the password for the senders email
     * @param newsContent - the news content to email
     */
    public static void sendEmail(ArrayList<String> mailingList, String senderEmail, String senderPassword, String newsContent) {
        // SMTP Configuration for Gmail
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

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
                message.setText(newsContent);

                // Send email
                Transport.send(message);
                System.out.println("Email sent to: " + recipient);
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
