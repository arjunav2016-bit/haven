package haven.view;

import haven.model.Accommodation;
import haven.model.ChatMessage;
import haven.model.Enquiry;
import haven.model.User;
import haven.event.ChatEvent;
import haven.event.HavenEventBus;
import haven.event.HavenEventListener;
import haven.repository.MessageRepository;
import haven.repository.RepositoryProvider;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

/**
 * ChatDialog - Real-time / Reactive In-App Messaging Window between Students and Property Owners.
 * Supports:
 * - Enquiry-linked message threads (with enquiryId badge and context)
 * - Standalone pre-booking conversations on any accommodation
 * - Reactive updates via HavenEventBus (ChatEvent)
 */
public class ChatDialog extends JDialog {
    private final User currentUser;
    private final String peerUsername;
    private final String accommodationId;
    private final String accommodationName;
    private final String enquiryId;
    private final MessageRepository messageRepo;

    private final JPanel messagesContainer;
    private final JScrollPane scrollPane;
    private final JTextField inputField;
    private final HavenEventListener<ChatEvent> chatListener;

    /**
     * Opens a standalone or pre-booking chat for an Accommodation.
     */
    public ChatDialog(Window parent, User currentUser, Accommodation accommodation) {
        this(parent,
             currentUser,
             resolvePeerForAccommodation(currentUser, accommodation),
             accommodation.getId(),
             accommodation.getName(),
             "");
    }

    /**
     * Opens an enquiry-linked chat thread for a specific Enquiry.
     */
    public ChatDialog(Window parent, User currentUser, Enquiry enquiry) {
        this(parent,
             currentUser,
             resolvePeerForEnquiry(currentUser, enquiry),
             enquiry.getAccommodationId(),
             enquiry.getAccommodationName(),
             enquiry.getEnquiryId());
    }

    public ChatDialog(Window parent, User currentUser, String peerUsername,
                      String accommodationId, String accommodationName, String enquiryId) {
        super(parent, "HAVEN Direct Chat — " + accommodationName, ModalityType.APPLICATION_MODAL);
        this.currentUser = currentUser;
        this.peerUsername = (peerUsername != null && !peerUsername.trim().isEmpty()) ? peerUsername.trim() : "owner";
        this.accommodationId = accommodationId != null ? accommodationId : "";
        this.accommodationName = accommodationName != null ? accommodationName : "Accommodation";
        this.enquiryId = enquiryId != null ? enquiryId : "";
        this.messageRepo = RepositoryProvider.getMessageRepository();

        setSize(560, 540);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(HavenTheme.BG_CANVAS);

        // Header Bar
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(HavenTheme.SURFACE_WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, HavenTheme.BORDER_LIGHT),
                new EmptyBorder(12, 18, 12, 18)
        ));

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBox.setOpaque(false);
        JLabel titleLbl = new JLabel(this.accommodationName);
        titleLbl.setFont(HavenTheme.FONT_SUBTITLE);
        titleLbl.setForeground(HavenTheme.TEXT_PRIMARY);

        String roleLabel = "Conversation with @" + this.peerUsername;
        if (this.currentUser != null && this.currentUser.isOwner()) {
            roleLabel = "Conversation with Student @" + this.peerUsername;
        } else if (this.currentUser != null && this.currentUser.isStudent()) {
            roleLabel = "Conversation with Property Owner (@" + this.peerUsername + ")";
        }
        String subText = roleLabel;
        if (!this.enquiryId.isEmpty()) {
            subText += " • Linked Enquiry: " + this.enquiryId;
        } else {
            subText += " • Pre-Booking Direct Chat";
        }
        JLabel subLbl = new JLabel(subText);
        subLbl.setFont(HavenTheme.FONT_CAPTION);
        subLbl.setForeground(HavenTheme.TEXT_SECONDARY);

        titleBox.add(titleLbl);
        titleBox.add(subLbl);
        header.add(titleBox, BorderLayout.WEST);

        JLabel badge = HavenTheme.createBadge(
                !this.enquiryId.isEmpty() ? "ENQUIRY THREAD" : "DIRECT MESSAGE",
                HavenTheme.PRIMARY, HavenTheme.PRIMARY_LIGHT, HavenTheme.PRIMARY_BORDER
        );
        JPanel badgeWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        badgeWrap.setOpaque(false);
        badgeWrap.add(badge);
        header.add(badgeWrap, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Messages Scroll Area
        messagesContainer = new JPanel();
        messagesContainer.setLayout(new BoxLayout(messagesContainer, BoxLayout.Y_AXIS));
        messagesContainer.setBackground(HavenTheme.BG_CANVAS);
        messagesContainer.setBorder(new EmptyBorder(14, 16, 14, 16));

        scrollPane = new JScrollPane(messagesContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        add(scrollPane, BorderLayout.CENTER);

        // Input Composer Bar
        JPanel composer = new JPanel(new BorderLayout(8, 0));
        composer.setBackground(HavenTheme.SURFACE_WHITE);
        composer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, HavenTheme.BORDER_LIGHT),
                new EmptyBorder(12, 16, 12, 16)
        ));

        inputField = new JTextField();
        inputField.setFont(HavenTheme.FONT_BODY);
        inputField.setBorder(HavenTheme.createInputPadding());
        inputField.setToolTipText("Type a message and press Enter or click Send");
        inputField.addActionListener(e -> sendMessage());

        JButton sendBtn = ModernButton.primary("Send Message");
        sendBtn.addActionListener(e -> sendMessage());

        composer.add(inputField, BorderLayout.CENTER);
        composer.add(sendBtn, BorderLayout.EAST);
        add(composer, BorderLayout.SOUTH);

        // Subscribe to ChatEvent for live updates
        chatListener = event -> SwingUtilities.invokeLater(this::refreshMessages);
        HavenEventBus.getInstance().subscribe(ChatEvent.class, chatListener);

        // Mark conversation read on open
        if (currentUser != null) {
            messageRepo.markConversationAsRead(this.accommodationId, this.peerUsername, currentUser.getUsername());
        }
        refreshMessages();
    }

    private static String resolvePeerForAccommodation(User currentUser, Accommodation acc) {
        if (currentUser != null && currentUser.isOwner()) {
            if (acc != null && acc.getId() != null) {
                List<ChatMessage> msgs = RepositoryProvider.getMessageRepository().findConversation(acc.getId(), null, null);
                for (int i = msgs.size() - 1; i >= 0; i--) {
                    ChatMessage m = msgs.get(i);
                    if (!currentUser.getUsername().equalsIgnoreCase(m.getSenderUsername()) && m.getSenderUsername() != null && !m.getSenderUsername().isEmpty()) {
                        return m.getSenderUsername();
                    }
                }
            }
            return "student";
        }
        String ownerId = (acc != null) ? acc.getOwnerId() : null;
        if (ownerId != null && !ownerId.trim().isEmpty() && RepositoryProvider.getUserRepository().userExists(ownerId.trim())) {
            return ownerId.trim();
        }
        return "owner";
    }

    private static String resolvePeerForEnquiry(User currentUser, Enquiry eq) {
        if (currentUser != null && currentUser.getUsername().equalsIgnoreCase(eq.getStudentUsername())) {
            String ownerId = eq.getOwnerId();
            if (ownerId != null && !ownerId.trim().isEmpty() && RepositoryProvider.getUserRepository().userExists(ownerId.trim())) {
                return ownerId.trim();
            }
            return "owner";
        }
        return (eq != null && eq.getStudentUsername() != null && !eq.getStudentUsername().isEmpty())
                ? eq.getStudentUsername()
                : "student";
    }

    private void refreshMessages() {
        messagesContainer.removeAll();
        String myUsername = (currentUser != null) ? currentUser.getUsername() : "student";

        List<ChatMessage> messages = messageRepo.findConversation(accommodationId, myUsername, peerUsername);
        if (messages.isEmpty() && !enquiryId.isEmpty()) {
            messages = messageRepo.findByEnquiryId(enquiryId);
        }

        if (messages.isEmpty()) {
            JPanel emptyBox = new JPanel(new BorderLayout());
            emptyBox.setOpaque(false);
            emptyBox.setBorder(new EmptyBorder(60, 20, 60, 20));
            JLabel emptyLbl = new JLabel("No messages yet. Start the conversation with @" + peerUsername + " below!", SwingConstants.CENTER);
            emptyLbl.setFont(HavenTheme.FONT_BODY);
            emptyLbl.setForeground(HavenTheme.TEXT_SECONDARY);
            emptyBox.add(emptyLbl, BorderLayout.CENTER);
            messagesContainer.add(emptyBox);
        } else {
            for (ChatMessage msg : messages) {
                boolean isMine = msg.getSenderUsername().equalsIgnoreCase(myUsername);
                messagesContainer.add(createBubbleRow(msg, isMine));
                messagesContainer.add(Box.createVerticalStrut(8));
            }
        }

        messagesContainer.revalidate();
        messagesContainer.repaint();
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = scrollPane.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    private JPanel createBubbleRow(ChatMessage msg, boolean isMine) {
        JPanel row = new JPanel(new FlowLayout(isMine ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);

        JPanel bubble = new JPanel(new BorderLayout(0, 4));
        bubble.setBackground(isMine ? HavenTheme.PRIMARY : HavenTheme.SURFACE_WHITE);
        bubble.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(isMine ? HavenTheme.PRIMARY_HOVER : HavenTheme.BORDER_LIGHT, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        String metaText = "@" + msg.getSenderUsername() + " • " + msg.getFormattedTime();
        if (msg.isEnquiryLinked()) {
            metaText += " [" + msg.getEnquiryId() + "]";
        }
        JLabel metaLbl = new JLabel(metaText);
        metaLbl.setFont(HavenTheme.FONT_BADGE);
        metaLbl.setForeground(isMine ? new Color(224, 231, 255) : HavenTheme.TEXT_SECONDARY);

        JTextArea contentArea = new JTextArea(msg.getContent());
        contentArea.setFont(HavenTheme.FONT_BODY);
        contentArea.setForeground(isMine ? Color.WHITE : HavenTheme.TEXT_PRIMARY);
        contentArea.setOpaque(false);
        contentArea.setEditable(false);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        int width = Math.min(360, Math.max(180, msg.getContent().length() * 7));
        contentArea.setSize(new Dimension(width, Short.MAX_VALUE));
        Dimension pref = contentArea.getPreferredSize();
        contentArea.setPreferredSize(new Dimension(width, pref.height));

        bubble.add(metaLbl, BorderLayout.NORTH);
        bubble.add(contentArea, BorderLayout.CENTER);
        row.add(bubble);
        return row;
    }

    private void sendMessage() {
        String text = inputField.getText() != null ? inputField.getText().trim() : "";
        if (text.isEmpty()) return;

        String sender = (currentUser != null) ? currentUser.getUsername() : "student";
        ChatMessage msg = new ChatMessage(
                sender,
                peerUsername,
                accommodationId,
                accommodationName,
                enquiryId,
                text
        );
        messageRepo.save(msg);
        inputField.setText("");
        refreshMessages();
    }

    @Override
    public void dispose() {
        if (chatListener != null) {
            HavenEventBus.getInstance().unsubscribe(ChatEvent.class, chatListener);
        }
        super.dispose();
    }
}
