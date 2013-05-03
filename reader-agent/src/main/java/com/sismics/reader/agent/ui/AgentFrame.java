package com.sismics.reader.agent.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.sismics.reader.agent.ReaderAgent;
import com.sismics.reader.agent.deployer.DeploymentStatusListener;
import com.sismics.reader.agent.deployer.DeploymentStatus;
import com.sismics.util.MessageUtil;

/**
 * Agent frame containing the status and settings panels.
 *
 * @author jtremeaux
 */
public class AgentFrame extends JFrame implements DeploymentStatusListener {

    /**
     * Serial UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * ServerState panel.
     */
    private final StatusPanel statusPanel;
    
    /**
     * Settings panel.
     */
    private final SettingPanel settingPanel;
    
    private JTabbedPane tabbedPane;
    
    private JButton closeButton;

    private Image startedImage;
    
    private Image stoppedImage;

    /**
     * Constructor of AgentFrame.
     * 
     * @param readerAgent Reader agent
     */
    public AgentFrame(ReaderAgent readerAgent) {
        super(MessageUtil.getMessage("agent.frame.title"));
        
        settingPanel = new SettingPanel(readerAgent);
        statusPanel = new StatusPanel(readerAgent);
        
        initComponent();
        
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        startedImage = toolkit.createImage(getClass().getResource("/images/reader-started-16.png"));
        stoppedImage = toolkit.createImage(getClass().getResource("/images/reader-stopped-16.png"));
        
        setIcon(stoppedImage);

        pack();
        centerComponent();
        
        readerAgent.addListener(this);
    }

    /**
     * Center the frame inside the window.
     */
    public void centerComponent() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width / 2 - getWidth() / 2,
                    screenSize.height / 2 - getHeight() / 2);
    }

    /**
     * Initialize UI components.
     */
    private void initComponent() {
        tabbedPane = new JTabbedPane();
        closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        tabbedPane.add(MessageUtil.getMessage("agent.frame.pane.status"), statusPanel);
        tabbedPane.add(MessageUtil.getMessage("agent.frame.pane.setting"), settingPanel);

        JPanel pane = (JPanel) getContentPane();
        pane.setLayout(new BorderLayout(10, 10));
        pane.add(tabbedPane, BorderLayout.CENTER);
        pane.add(ButtonBarFactory.buildCloseBar(closeButton), BorderLayout.SOUTH);

        pane.setBorder(Borders.TABBED_DIALOG_BORDER);
    }

    /**
     * Show the status panel.
     */
    public void showStatusPanel() {
        settingPanel.readSetting();
        tabbedPane.setSelectedComponent(statusPanel);
        pack();
        setVisible(true);
        toFront();
    }

    /**
     * Show the settings panel.
     */
    public void showSettingPanel() {
        settingPanel.readSetting();
        tabbedPane.setSelectedComponent(settingPanel);
        pack();
        setVisible(true);
        toFront();
    }

    /**
     * Set the frame icon image.
     * 
     * @param image Icon image
     */
    private void setIcon(Image image) {
        if (getIconImage() != image) {
            setIconImage(image);
        }
    }

    @Override
    public void notifyDeploymentStatus(DeploymentStatus deploymentStatus) {
        setIconImage(deploymentStatus == null ? stoppedImage : startedImage);
    }
}
