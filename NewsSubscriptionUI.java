import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;

public class NewsSubscriptionUI extends JFrame {
    private JTextField emailField;
    private JCheckBox techNews;
    private JCheckBox businessNews;
    private JButton submitButton;

    public NewsSubscriptionUI() {
        setTitle("News Subscription");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());

        getContentPane().setBackground(new Color(25, 25, 25)); // Darker background

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        // Title Label
        JLabel titleLabel = createStyledLabel("Subscribe to News", 24);
        add(titleLabel, gbc);

        // Email Input
        gbc.gridy++;
        JLabel emailLabel = createStyledLabel("Enter your email:", 18);
        add(emailLabel, gbc);

        gbc.gridy++;
        emailField = new JTextField(20);
        emailField.setFont(new Font("Arial", Font.PLAIN, 16));
        emailField.setBackground(new Color(50, 50, 50));
        emailField.setForeground(Color.WHITE);
        emailField.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        add(emailField, gbc);

        // Checkboxes
        gbc.gridy++;
        gbc.gridwidth = 1;
        techNews = createStyledCheckBox("Tech News");
        businessNews = createStyledCheckBox("Business News");

        add(techNews, gbc);
        gbc.gridx = 1;
        add(businessNews, gbc);

        // Submit Button
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        submitButton = createStyledButton("Subscribe");
        submitButton.addActionListener(e -> handleSubscription());
        add(submitButton, gbc);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(0, 153, 255)); // Bright blue
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Padding
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(220, 50));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0, 119, 255)); // Darker blue on hover
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0, 153, 255)); // Original color
            }
        });

        return button;
    }

    private JLabel createStyledLabel(String text, int fontSize) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, fontSize));
        label.setPreferredSize(new Dimension(250, 30));

        return label;
    }

    private JCheckBox createStyledCheckBox(String text) {
        JCheckBox checkBox = new JCheckBox(text);
        checkBox.setForeground(Color.WHITE);
        checkBox.setBackground(new Color(25, 25, 25)); // Match background
        checkBox.setFont(new Font("Arial", Font.BOLD, 16));
        checkBox.setFocusPainted(false);

        return checkBox;
    }

    private void handleSubscription() {
        String email = emailField.getText();
        boolean wantsTech = techNews.isSelected();
        boolean wantsBusiness = businessNews.isSelected();

        if (email.isEmpty() || (!wantsTech && !wantsBusiness)) {
            JOptionPane.showMessageDialog(this, "Please enter an email and select at least one category.");
            return;
        }
        StringBuilder category = new StringBuilder();

       if (wantsTech){
        category.append("tech");
       }

       if(wantsBusiness){
        if (category.length() > 0){
            category.append(",");

        }
        category.append("business");
       }
        Path file = Paths.get("data/preferences.csv");
        try (PrintWriter printWriter =
        new PrintWriter(Files.newBufferedWriter(file, StandardCharsets.UTF_8,StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            printWriter.write(email + "," + category.toString() + "\n");
        
        
       } catch (Exception e) {
        
       }
      
        JOptionPane.showMessageDialog(this, "Subscription successful!");
    }

   
    public static void main(String[] args) {
        SwingUtilities.invokeLater(NewsSubscriptionUI::new);
    }
}