package com.sismics.reader.agent.ui;

import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.sismics.reader.agent.ReaderAgent;
import com.sismics.reader.agent.deployer.DeploymentStatusListener;
import com.sismics.reader.agent.deployer.DeploymentStatus;
import com.sismics.reader.agent.deployer.DeploymentStatus.ServerState;
import com.sismics.util.MessageUtil;

/**
 * Controls the Reader tray icon.
 *
 * @author jtremeaux
 */
public class TrayController implements DeploymentStatusListener {

    private final ReaderAgent readerAgent;
    
    private TrayIcon trayIcon;

    private Action openAction;
    
    private Action controlPanelAction;
    
    private Action hideAction;
    
    private Image startedImage;
    
    private Image stoppedImage;

    /**
     * Constructor of TrayController.
     * 
     * @param readerAgent Reader agent
     */
    @SuppressWarnings("serial")
    public TrayController(ReaderAgent readerAgent) {
        this.readerAgent = readerAgent;
        try {
            openAction = new AbstractAction(MessageUtil.getMessage("agent.systray.open")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    TrayController.this.readerAgent.openBrowser();
                }
            };

            controlPanelAction = new AbstractAction(MessageUtil.getMessage("agent.systray.frame")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    TrayController.this.readerAgent.showStatusPanel();
                }
            };


            hideAction = new AbstractAction(MessageUtil.getMessage("agent.systray.quit")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //TODO Confirm if reader is running
                    TrayController.this.readerAgent.exit();
                }
            };
            startedImage = createImage("/images/reader-started-16.png");
            stoppedImage = createImage("/images/reader-stopped-16.png");

            PopupMenu menu = new PopupMenu();
            menu.add(createMenuItem(openAction));
            menu.add(createMenuItem(controlPanelAction));
            menu.addSeparator();
            menu.add(createMenuItem(hideAction));
            
            // Install the systray icon
            trayIcon = new TrayIcon(stoppedImage, MessageUtil.getMessage("agent.systray.title"), menu);
            trayIcon.addActionListener(controlPanelAction);
            SystemTray.getSystemTray().add(trayIcon);
            
            readerAgent.addListener(this);
        } catch (Throwable e) {
            System.err.println("Disabling tray support.");
        }
    }

    public void showMessage() {
        trayIcon.displayMessage("Reader", "Reader is now running. Click this balloon to get started.",
                TrayIcon.MessageType.INFO);
    }

    private Image createImage(String resourceName) {
        URL url = getClass().getResource(resourceName);
        return Toolkit.getDefaultToolkit().createImage(url);
    }

    private MenuItem createMenuItem(Action action) {
        MenuItem menuItem = new MenuItem((String) action.getValue(Action.NAME));
        menuItem.addActionListener(action);
        return menuItem;
    }

    public void uninstallComponents() {
        try {
            SystemTray.getSystemTray().remove(trayIcon);
        } catch (Throwable e) {
            System.err.println("Disabling tray support.");
        }
    }

    private void setTrayImage(Image image) {
        if (trayIcon.getImage() != image) {
            trayIcon.setImage(image);
        }
    }

    @Override
    public void notifyDeploymentStatus(DeploymentStatus deploymentStatus) {
        final ServerState serverState = deploymentStatus.getServerState();
        setTrayImage(serverState == ServerState.STARTED ? startedImage : stoppedImage);
    }
}
