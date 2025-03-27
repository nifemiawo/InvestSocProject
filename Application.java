import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.Properties;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

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

        if (apiKey == null || senderEmail == null || senderPassword == null) {
            System.err.println("Error: Missing environment variables.");
            return;
        }

        String csvFile = "data/preferences.csv";
        readDataAndSend(csvFile, apiKey, senderEmail, senderPassword);
    }

    private static String getBusinessNews(String key) {
        return fetchNews(key, "business");
    }

    private static String getTechNews(String key) {
        return fetchNews(key, "technology");
    }

    private static String fetchNews(String key, String category) {
        String url = "https://newsapi.org/v2/top-headlines?category=" + category + "&apiKey=" + key;
        StringBuilder newsContent = new StringBuilder();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .build();

        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonReader jsonReader = Json.createReader(new StringReader(response.body()));
            JsonObject jsonObject = jsonReader.readObject();
            jsonReader.close();

            JsonArray articles = jsonObject.getJsonArray("articles");
            for (int i = 0; i <= 4; i++) {
                JsonObject article = articles.getJsonObject(i);
                newsContent.append("<h3>").append(article.getString("title")).append("</h3>")
                          .append("<p><a href='").append(article.getString("url")).append("'>Read more</a></p>");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newsContent.toString();
    }

    private static void readDataAndSend(String fileName, String apiKey, String senderEmail, String senderPassword) {
        Map<String, List<String>> userPreferences = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length < 2) continue;

                String email = data[0].trim();
                List<String> prefs = new ArrayList<>();
                for (int i = 1; i < data.length; i++) {
                    String pref = data[i].trim().toLowerCase();
                    if (pref.equals("business") || pref.equals("tech")) prefs.add(pref);
                }
                if (!prefs.isEmpty()) userPreferences.put(email, prefs);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        LocalDate date = LocalDate.now();
        String businessNews = getBusinessNews(apiKey);
        String techNews = getTechNews(apiKey);

        userPreferences.forEach((email, prefs) -> {
            StringBuilder userContent = new StringBuilder();
            StringJoiner sectorsJoiner = new StringJoiner(" & ");

            prefs.forEach(pref -> {
                if (pref.equals("business")) {
                    userContent.append("<h2>Business News</h2>").append(businessNews);
                    sectorsJoiner.add("Business");
                } else if (pref.equals("tech")) {
                    userContent.append("<h2>Tech News</h2>").append(techNews);
                    sectorsJoiner.add("Tech");
                }
            });

            String sectors = sectorsJoiner.toString();
            String subject = "Daily " + sectors + " News";
            String emailContent = createHTML(userContent.toString(), date, sectors);
            sendEmail(email, senderEmail, senderPassword, emailContent, subject);
        });
    }

    private static void sendEmail(String recipientEmail, String senderEmail, String senderPassword, 
                                  String emailContent, String subject) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setContent(emailContent, "text/html");
            Transport.send(message);
            System.out.println("Email sent to: " + recipientEmail);
        } catch (MessagingException e) {
            System.out.println("Failed to send to " + recipientEmail + ": " + e.getMessage());
        }
    }

    private static String createHTML(String newsContent, LocalDate date, String sector) {
        return "<html><head><style>" +
               "body { font-family: Arial, sans-serif; }" +
               ".container { max-width: 600px; margin: auto; padding: 20px; }" +
               "h1 { color: #0056b3; }" +
               ".footer { text-align: center; margin-top: 20px; }" +
               "</style></head>" +
               "<body><div class='container'>" +
               "<h1>" + sector + " News - " + date + "</h1>" +
               newsContent +
               "<div class='footer'>Sent by St Andrews Investment Society</div>" +
               "</div></body></html>";
    }
}