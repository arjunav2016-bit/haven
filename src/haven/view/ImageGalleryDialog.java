package haven.view;

import haven.model.Accommodation;
import haven.model.PropertyImage;
import haven.model.User;
import haven.util.PropertyImageStorage;
import haven.repository.AccommodationRepository;
import haven.repository.ImageRepository;
import haven.repository.RepositoryProvider;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ImageGalleryDialog - Interactive photo gallery and carousel viewer for an Accommodation.
 * Supports:
 * - Prev / Next navigation across property photos stored on disk (images/<id>/)
 * - Zoom toggle (Fit to Window vs 135% Zoom)
 * - Thumbnail strip at the bottom for direct selection
 * - Owner / Admin "Upload Photo" action using JFileChooser + PropertyImageStorage
 */
public class ImageGalleryDialog extends JDialog {
    private static final Logger LOGGER = Logger.getLogger(ImageGalleryDialog.class.getName());
    private final Accommodation accommodation;
    private final User currentUser;
    private final Runnable onImagesUpdated;
    private final ImageRepository imageRepo;
    private final AccommodationRepository accRepo;

    private List<String> imagePaths = new ArrayList<>();
    private List<PropertyImage> imageRecords = new ArrayList<>();
    private int currentIndex = 0;
    private boolean zoomed = false;

    private final JLabel imageDisplayLabel;
    private final JLabel counterLabel;
    private final JLabel captionLabel;
    private final JPanel thumbnailStrip;
    private final JButton prevBtn;
    private final JButton nextBtn;
    private final JButton zoomBtn;

    public ImageGalleryDialog(Window parent, Accommodation accommodation, User currentUser, Runnable onImagesUpdated) {
        super(parent, "Photo Gallery - " + accommodation.getName(), ModalityType.APPLICATION_MODAL);
        this.accommodation = accommodation;
        this.currentUser = currentUser;
        this.onImagesUpdated = onImagesUpdated;
        this.imageRepo = RepositoryProvider.getImageRepository();
        this.accRepo = RepositoryProvider.getAccommodationRepository();

        setSize(820, 620);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(HavenTheme.BG_CANVAS);

        // Header Bar
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(HavenTheme.SURFACE_WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, HavenTheme.BORDER_LIGHT),
                new EmptyBorder(12, 20, 12, 20)
        ));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setOpaque(false);
        JLabel titleLbl = new JLabel(accommodation.getName() + " — Property Photo Gallery");
        titleLbl.setFont(HavenTheme.FONT_TITLE);
        titleLbl.setForeground(HavenTheme.TEXT_PRIMARY);

        captionLabel = new JLabel("Exterior & Room Views");
        captionLabel.setFont(HavenTheme.FONT_CAPTION);
        captionLabel.setForeground(HavenTheme.TEXT_SECONDARY);

        titlePanel.add(titleLbl);
        titlePanel.add(captionLabel);
        header.add(titlePanel, BorderLayout.WEST);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerRight.setOpaque(false);

        counterLabel = HavenTheme.createBadge("Photo 1 of 1", HavenTheme.PRIMARY, HavenTheme.PRIMARY_LIGHT, HavenTheme.PRIMARY_BORDER);
        headerRight.add(counterLabel);

        zoomBtn = ModernButton.subtle("Zoom: 100%");
        zoomBtn.setFont(HavenTheme.FONT_CAPTION_BOLD);
        zoomBtn.addActionListener(e -> {
            zoomed = !zoomed;
            zoomBtn.setText(zoomed ? "Zoom: 135%" : "Zoom: Fit");
            renderCurrentImage();
        });
        headerRight.add(zoomBtn);

        boolean canUpload = currentUser != null && (currentUser.isOwner() || currentUser.isAdmin());
        if (canUpload) {
            JButton uploadBtn = ModernButton.primary("+ Upload Photo");
            uploadBtn.setFont(HavenTheme.FONT_CAPTION_BOLD);
            uploadBtn.addActionListener(e -> handleUploadPhoto());
            headerRight.add(uploadBtn);
        }

        header.add(headerRight, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Center Image Viewport + Left/Right Controls
        JPanel centerPanel = new JPanel(new BorderLayout(10, 0));
        centerPanel.setBackground(HavenTheme.SURFACE_DARK);
        centerPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        prevBtn = ModernButton.secondary("< Prev");
        prevBtn.setPreferredSize(new Dimension(85, 40));
        prevBtn.addActionListener(e -> {
            if (!imagePaths.isEmpty()) {
                currentIndex = (currentIndex - 1 + imagePaths.size()) % imagePaths.size();
                renderCurrentImage();
            }
        });

        nextBtn = ModernButton.secondary("Next >");
        nextBtn.setPreferredSize(new Dimension(85, 40));
        nextBtn.addActionListener(e -> {
            if (!imagePaths.isEmpty()) {
                currentIndex = (currentIndex + 1) % imagePaths.size();
                renderCurrentImage();
            }
        });

        JPanel westWrap = new JPanel(new GridBagLayout());
        westWrap.setOpaque(false);
        westWrap.add(prevBtn);

        JPanel eastWrap = new JPanel(new GridBagLayout());
        eastWrap.setOpaque(false);
        eastWrap.add(nextBtn);

        imageDisplayLabel = new JLabel("", SwingConstants.CENTER);
        imageDisplayLabel.setOpaque(false);

        JScrollPane imageScroll = new JScrollPane(imageDisplayLabel);
        imageScroll.setBorder(null);
        imageScroll.getViewport().setBackground(HavenTheme.SURFACE_DARK);

        centerPanel.add(westWrap, BorderLayout.WEST);
        centerPanel.add(imageScroll, BorderLayout.CENTER);
        centerPanel.add(eastWrap, BorderLayout.EAST);
        add(centerPanel, BorderLayout.CENTER);

        // Bottom Thumbnail Strip + Close Button
        JPanel footer = new JPanel(new BorderLayout(12, 0));
        footer.setBackground(HavenTheme.SURFACE_WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, HavenTheme.BORDER_LIGHT),
                new EmptyBorder(10, 20, 10, 20)
        ));

        thumbnailStrip = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        thumbnailStrip.setOpaque(false);
        JScrollPane thumbScroll = new JScrollPane(thumbnailStrip);
        thumbScroll.setBorder(null);
        thumbScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        thumbScroll.setPreferredSize(new Dimension(560, 64));

        JPanel footerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        footerRight.setOpaque(false);
        if (canUpload) {
            JButton uploadFooterBtn = ModernButton.secondary("Add Another Photo");
            uploadFooterBtn.addActionListener(e -> handleUploadPhoto());
            footerRight.add(uploadFooterBtn);
        }
        JButton closeBtn = ModernButton.subtle("Close");
        closeBtn.addActionListener(e -> dispose());
        footerRight.add(closeBtn);

        footer.add(thumbScroll, BorderLayout.CENTER);
        footer.add(footerRight, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);

        reloadImages();
    }

    private void reloadImages() {
        PropertyImageStorage.ensureDefaultImagesForAccommodation(accommodation);
        imageRecords = imageRepo.findByAccommodationId(accommodation.getId());
        imagePaths = new ArrayList<>(accommodation.getImagePaths());
        for (PropertyImage rec : imageRecords) {
            if (rec.getFilePath() != null && !imagePaths.contains(rec.getFilePath())) {
                imagePaths.add(rec.getFilePath());
            }
        }
        if (currentIndex >= imagePaths.size()) {
            currentIndex = Math.max(0, imagePaths.size() - 1);
        }
        rebuildThumbnails();
        renderCurrentImage();
    }

    private void rebuildThumbnails() {
        thumbnailStrip.removeAll();
        for (int i = 0; i < imagePaths.size(); i++) {
            final int idx = i;
            String path = imagePaths.get(i);
            ImageIcon icon = PropertyImageStorage.loadScaledIcon(path, 76, 52);
            JButton thumbBtn = new JButton();
            if (icon != null) {
                thumbBtn.setIcon(icon);
            } else {
                thumbBtn.setText("#" + (i + 1));
            }
            thumbBtn.setPreferredSize(new Dimension(82, 56));
            thumbBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            thumbBtn.setBorder(new LineBorder(idx == currentIndex ? HavenTheme.PRIMARY : HavenTheme.BORDER_LIGHT, idx == currentIndex ? 2 : 1));
            thumbBtn.addActionListener(e -> {
                currentIndex = idx;
                rebuildThumbnails();
                renderCurrentImage();
            });
            thumbnailStrip.add(thumbBtn);
        }
        thumbnailStrip.revalidate();
        thumbnailStrip.repaint();
    }

    private void renderCurrentImage() {
        if (imagePaths.isEmpty()) {
            imageDisplayLabel.setIcon(null);
            imageDisplayLabel.setText("No images available.");
            imageDisplayLabel.setForeground(Color.WHITE);
            counterLabel.setText("0 of 0");
            prevBtn.setEnabled(false);
            nextBtn.setEnabled(false);
            return;
        }

        prevBtn.setEnabled(imagePaths.size() > 1);
        nextBtn.setEnabled(imagePaths.size() > 1);
        counterLabel.setText("Photo " + (currentIndex + 1) + " of " + imagePaths.size());

        String path = imagePaths.get(currentIndex);
        String caption = (currentIndex == 0)
                ? accommodation.getName() + " — Architectural Exterior View"
                : (currentIndex == 1)
                ? accommodation.getName() + " — Furnished Student Room Interior"
                : accommodation.getName() + " — Uploaded Property Photo #" + (currentIndex + 1);

        for (PropertyImage rec : imageRecords) {
            if (path.equals(rec.getFilePath()) && rec.getCaption() != null && !rec.getCaption().isEmpty()) {
                caption = rec.getCaption() + " (Uploaded by @" + rec.getUploadedBy() + ")";
                break;
            }
        }
        captionLabel.setText(caption);

        int targetW = zoomed ? 780 : 560;
        int targetH = zoomed ? 500 : 360;
        ImageIcon icon = PropertyImageStorage.loadScaledIcon(path, targetW, targetH);
        if (icon != null) {
            imageDisplayLabel.setText("");
            imageDisplayLabel.setIcon(icon);
        } else {
            BufferedImage fallback = PropertyImageStorage.createPlaceholderImage(accommodation.getName(), targetW, targetH);
            imageDisplayLabel.setText("");
            imageDisplayLabel.setIcon(new ImageIcon(fallback));
        }
    }

    private void handleUploadPhoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Property Photograph (PNG / JPG)");
        chooser.setFileFilter(new FileNameExtensionFilter("Image Files (PNG, JPG, JPEG, GIF)", "png", "jpg", "jpeg", "gif"));
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            if (selectedFile != null && selectedFile.exists()) {
                String caption = JOptionPane.showInputDialog(this,
                        "Enter a caption for this photograph:",
                        accommodation.getName() + " - Room / Facility View");
                if (caption == null) caption = accommodation.getName() + " - Photo";

                try {
                    String savedRelPath = PropertyImageStorage.copyImageToStorage(accommodation.getId(), selectedFile);
                    if (savedRelPath != null) {
                        accommodation.addImagePath(savedRelPath);
                        accRepo.save(accommodation);
                        String uploader = (currentUser != null) ? currentUser.getUsername() : "owner";
                        PropertyImage imgModel = new PropertyImage(accommodation.getId(), savedRelPath, caption, uploader);
                        imageRepo.save(imgModel);

                        reloadImages();
                        currentIndex = imagePaths.size() - 1;
                        rebuildThumbnails();
                        renderCurrentImage();

                        if (onImagesUpdated != null) {
                            onImagesUpdated.run();
                        }
                        JOptionPane.showMessageDialog(this,
                                "Photograph uploaded and saved to " + savedRelPath,
                                "Upload Complete",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, "Failed to upload image file: " + selectedFile.getName(), ex);
                    JOptionPane.showMessageDialog(this,
                            "Failed to upload image: " + ex.getMessage(),
                            "Upload Error",
                            JOptionPane.ERROR_MESSAGE);
                } catch (RuntimeException ex) {
                    LOGGER.log(Level.SEVERE, "Unexpected error uploading image: " + selectedFile.getName(), ex);
                    JOptionPane.showMessageDialog(this,
                            "Failed to upload image: " + ex.getMessage(),
                            "Upload Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
