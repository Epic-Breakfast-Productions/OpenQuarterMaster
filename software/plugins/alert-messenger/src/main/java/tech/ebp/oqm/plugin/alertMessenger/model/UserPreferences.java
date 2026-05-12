package tech.ebp.oqm.plugin.alertMessenger.model;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;

// Represents user preferences for notifications (email and Slack webhook).
@Entity
@Table(name = "user_preferences")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserPreferences {

    @Id
    private UUID id; // Matches the User ID

    @Column(name = "email_notifications")
    private String emailNotifications;

    @Column(name = "slack_webhook")
    private String slackWebhook;
}
