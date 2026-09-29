

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.*;




public class OwnerUI{
    private JButton createButton(String text) {

    JButton button = new JButton(text) {

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );

            // Normal / hover color
            if (getModel().isRollover()) {
                g2.setColor(new Color(95, 95, 95));
            } else {
                g2.setColor(new Color(75, 75, 75));
            }

            // Rounded background
            g2.fillRoundRect(
                0, 0,
                getWidth(),
                getHeight(),
                12, 12
            );

            // Center text
            g2.setColor(Color.WHITE);
            g2.setFont(getFont());

            FontMetrics metrics = g2.getFontMetrics();

            int x = (getWidth() - metrics.stringWidth(getText())) / 2;
            int y = (getHeight() - metrics.getHeight()) / 2
                    + metrics.getAscent();

            g2.drawString(getText(), x, y);

            g2.dispose();
        }
    };

    button.setFont(new Font("Segoe UI", Font.PLAIN, 14));

    button.setPreferredSize(new Dimension(160, 42));
    button.setMinimumSize(new Dimension(160, 42));
    button.setMaximumSize(new Dimension(160, 42));

    button.setFocusPainted(false);
    button.setBorderPainted(false);
    button.setContentAreaFilled(false);

    return button;
}
    




    
    
        public void main(String[] args){
        JFrame frame=new JFrame();
        frame.setTitle("Owner Interface");
        frame.setVisible(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);


        
        
        //navigationbar panel
        JPanel naviPanel  = new JPanel();
        frame.add(naviPanel, BorderLayout.WEST);
        naviPanel.setLayout(new BoxLayout(naviPanel, BoxLayout.Y_AXIS));

        JPanel contentPanel = new JPanel(new CardLayout());
        frame.add(contentPanel, BorderLayout.CENTER);
        

        //Home panel
        HomePanel homePanel = new HomePanel();
        naviPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(homePanel,"HOME");
        JButton homeButton = createButton("Home");
        naviPanel.add(homeButton);
        homeButton.setBorder(
            BorderFactory.createEmptyBorder(20,40,10,20)
        );
        CardLayout cardLayout = (CardLayout) contentPanel.getLayout();
        homeButton.addActionListener(e->{ cardLayout.show(contentPanel, "HOME");});



        //NewListingPanel 
        NewListingPanel newListingPanel = new NewListingPanel();
        contentPanel.add(newListingPanel,"NEW_LISTING");
        JButton newListingButton = createButton("New Listing");
        naviPanel.add(newListingButton);
        newListingButton.setBorder(
            BorderFactory.createEmptyBorder(20,20,10,20)
        );
        newListingButton.setContentAreaFilled(false);
        newListingButton.addActionListener(e ->{ cardLayout.show(contentPanel, "NEW_LISTING");});


        //Update Listing Panel
        UpdateListingPanel updateListingPanel = new UpdateListingPanel();
        contentPanel.add(updateListingPanel,"UPDATE_LISTING");
        JButton updateListingButton = createButton("Update Listing");
        naviPanel.add(updateListingButton);
        newListingButton.setBorder(
            BorderFactory.createEmptyBorder(20,20,10,20)
        );
        updateListingButton.setContentAreaFilled(false);
        updateListingButton.addActionListener(e ->{ cardLayout.show(contentPanel, "UPDATE_LISTING");});


        //Message Listing Panel
        MessagePanel messagePanel = new MessagePanel();
        contentPanel.add(messagePanel,"MESSAGES");
        JButton messageButton = createButton("Messages");
        naviPanel.add(messageButton);
        messageButton.setBorder(
            BorderFactory.createEmptyBorder(20,20,10,20)
        );
        messageButton.setContentAreaFilled(false);
        messageButton.addActionListener(e ->{ cardLayout.show(contentPanel, "MESSAGES");});




        //Switch
        SwitchPanel switchPanel = new SwitchPanel();
        contentPanel.add(switchPanel,"SWITCH");
        JButton switchButton = createButton("Switch");
        naviPanel.add(switchButton);
        switchButton.setBorder(
            BorderFactory.createEmptyBorder(20,20,10,20)
        );
        switchButton.setContentAreaFilled(false);
        switchButton.addActionListener(e ->{ cardLayout.show(contentPanel, "SWITCH");});




        

        
        



        naviPanel.setBackground(Color.DARK_GRAY);
        naviPanel.setPreferredSize(new Dimension(200,200));
        
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);




        
    }
    
}


