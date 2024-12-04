// package tech.ebp.oqm.plugin.alertMessenger;

// import com.fasterxml.jackson.databind.node.ObjectNode;
// import jakarta.annotation.PostConstruct;
// import jakarta.enterprise.context.ApplicationScoped;
// import jakarta.inject.Inject;
// import lombok.extern.slf4j.Slf4j;
// import org.eclipse.microprofile.reactive.messaging.Incoming;
// import org.eclipse.microprofile.reactive.messaging.Message;
// import tech.ebp.oqm.plugin.alertMessenger.utils.EmailUtils;

// import java.util.concurrent.CompletionStage;

// @Slf4j
// @ApplicationScoped
// public class AlertConsumer {

//     @Inject
//  	EmailUtils email;

//     @PostConstruct
//     public void init() {
//         log.info("Starting AlertConsumer");
//     }

//     /**
//      * Receives messages from the Kafka topic "alert-messages".
//      *
//      * @param message the incoming Kafka message wrapped in a Message object.
//      * @return a CompletionStage that acknowledges the message.
//      */
//     @Incoming("oqm-core-all-events")
//     public CompletionStage<Void> receive(Message<ObjectNode> message) {
//         try {
//             // Log the message payload
//             ObjectNode payload = message.getPayload();
//             log.info("Received alert message: {}", payload);

//             // Process the message
//             processMessage(payload);

//             // Acknowledge the message
//             return message.ack();
//         } catch (Exception e) {
//             log.error("Error processing message: {}", e.getMessage(), e);
//             // Nack the message to handle the failure strategy
//             return message.nack(e);
//         }
//     }

//     /**
//      * Processes the payload of the incoming Kafka message.
//      *
//      * @param payload the JSON payload of the Kafka message.
//      */
//     private void processMessage(ObjectNode payload) {

//         // Example processing logic - customize as needed
//         String alertType = payload.get("type").asText();
//         String alertDetails = payload.get("details").asText();
//         log.info("Processing alert of type: {} with details: {}", alertType, alertDetails);

//         // Business logic for handling alerts
//         // Ask the user whether they want email alerts first to get this data and store it in both database and JWT
//         if (EMAIL_IS_CONFIGURED) {
//             log.info("Email is configured. Sending email message.");
            
//             // Get recipient info.
//             // This is a filler. I need to find a way to get my email from the JWT.
//             email.sendEmail("andrewsbrendanp@gmail.com", alertType, alertDetails);

//         } else {
//             log.warn("Email is not configured.");
//         }
        
//         // // I need to find a way to see if slack is configured, and it involves a database.
//         // if (SLACK_IS_CONFIGURED) {

//         //     log.info("Slack is configured. Sending Slack message.");

//         //     // SEND_SLACK_MESSAGE
//         //     // I need to find a way to include slack webhook url in the credentials from the user.

//         // } else {
//         //     log.warn("Slack is not configured.");
//         // }
//     }
// }