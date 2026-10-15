package haven.repository;

import haven.model.ChatMessage;
import java.util.List;

/**
 * Domain-specific contract for in-app Student-Owner chat messages.
 * Extends the generic {@link Repository} interface.
 */
public interface MessageRepository extends Repository<ChatMessage, String> {

    /**
     * Finds all messages linked to a specific enquiry ID, ordered chronologically.
     *
     * @param enquiryId enquiry identifier
     * @return ordered list of chat messages
     */
    List<ChatMessage> findByEnquiryId(String enquiryId);

    /**
     * Finds all messages for a specific accommodation between two users (or all messages for an accommodation).
     *
     * @param accommodationId accommodation identifier
     * @param userA first participant username
     * @param userB second participant username
     * @return ordered list of chat messages
     */
    List<ChatMessage> findConversation(String accommodationId, String userA, String userB);

    /**
     * Finds all messages where the given user is either sender or receiver.
     *
     * @param username username
     * @return list of chat messages
     */
    List<ChatMessage> findByParticipant(String username);

    /**
     * Marks all unread messages addressed to {@code receiverUsername} in a conversation as read.
     *
     * @param accommodationId accommodation ID
     * @param senderUsername sender username
     * @param receiverUsername receiver username
     * @return number of messages marked read
     */
    int markConversationAsRead(String accommodationId, String senderUsername, String receiverUsername);
}
