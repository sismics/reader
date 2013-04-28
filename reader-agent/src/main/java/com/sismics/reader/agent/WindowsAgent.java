package com.sismics.reader.agent;

import java.awt.Desktop;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.sismics.reader.agent.deployer.DeploymentStatus;
import com.sismics.reader.agent.deployer.DeploymentStatusListener;
import com.sismics.reader.agent.deployer.ReaderDeployer;
import com.sismics.reader.agent.model.Setting;
import com.sismics.reader.agent.ui.AgentFrame;
import com.sismics.reader.agent.ui.TrayController;

/**
 * Windows Agent to configure and launch the Reader application.
 *
 * @author jtremeaux
 */
public class WindowsAgent {

    private final List<DeploymentStatusListener> listeners = new ArrayList<DeploymentStatusListener>();
    
    private final TrayController trayController;
    
    private AgentFrame frame;
    
    private boolean elevated;
    
    private final ReaderDeployer readerDeployer;

    private final Setting setting;
    
    /**
     * Constructor of WindowsAgent.
     */
    public WindowsAgent() {
        setting = new Setting();
        
        readerDeployer = new ReaderDeployer(this);
        setLookAndFeel();
        trayController = new TrayController(this);
    }

    public void notifyDeploymentInfo() {
        final DeploymentStatus status = readerDeployer.getDeploymentStatus();
        if (listeners != null) {
            for (DeploymentStatusListener listener : listeners) {
                listener.notifyDeploymentStatus(status);
            }
        }
    }
    
    private void setLookAndFeel() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.indexOf("win") >= 0) {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            }
        } catch (Throwable e) {
            System.err.println("Failed to set look-and-feel.\n" + e);
        }
    }

    /**
     * If necessary, restart agent with elevated rights.
     */
    public void checkElevation(String... args) {

        if (isElevationNeeded() && !isElevated()) {
            try {
                List<String> command = new ArrayList<String>();
                command.add("cmd");
                command.add("/c");
                command.add("reader-agent-elevated.exe");
                command.addAll(Arrays.asList(args));

                ProcessBuilder builder = new ProcessBuilder();
                builder.command(command);
                System.err.println("Executing: " + command + " with current dir: " + System.getProperty("user.dir"));
                builder.start();
                System.exit(0);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "Failed to elevate Reader Control Panel. " + e,
                        "Error",
                        JOptionPane.WARNING_MESSAGE);
                e.printStackTrace(System.err);
            }
        }
    }

    /**
     * Returns whether UAC elevation is necessary (to start/stop services etc).
     */
    private boolean isElevationNeeded() {

        String osVersion = System.getProperty("os.version");
        try {
            int majorVersion = Integer.parseInt(osVersion.substring(0, osVersion.indexOf(".")));

            // Elevation is necessary in Windows Vista (os.version=6.1) and later.
            return majorVersion >= 6;
        } catch (Exception x) {
            System.err.println("Failed to resolve OS version from '" + osVersion + "'\n" + x);
            return false;
        }
    }

    /**
     * Add a deployment status listener.
     * 
     * @param listener Listener
     */
    public void addListener(DeploymentStatusListener listener) {
        listeners.add(listener);
    }

    public void showStatusPanel() {
        frame.showStatusPanel();
    }

    public void showTrayIconMessage() {
        trayController.showMessage();
    }

    public void exit() {
        trayController.uninstallComponents();
        System.exit(0);
    }

    /**
     * Open Reader in a browser window.
     */
    public void openBrowser() {
        try {
            Desktop.getDesktop().browse(new URI(readerDeployer.getUrl()));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Start the agent.
     * 
     * @param args Arguments
     */
    private void start(List<String> args) {
        elevated = args.contains("-elevated");
        frame = new AgentFrame(this);

        if (args.contains("-balloon")) {
            showTrayIconMessage();
        }

        if (setting.isAutoStart()) {
            checkElevation("-start");
            readerDeployer.start();
        } else if (args.contains("-start")) {
            System.out.println("Starting service");
            readerDeployer.start();
            showStatusPanel();
        } else if (args.contains("-stop")) {
            System.out.println("Stopping service");
            readerDeployer.stop();
            showStatusPanel();
        } 
    }

    /**
     * Entry point of the Windows Agent.
     * 
     * @param args Arguments
     */
    public static void main(String[] args) {
        System.out.println("Starting up Windows agent");

        WindowsAgent agent = new WindowsAgent();
        agent.start(Arrays.asList(args));
    }

    /**
     * Getter of readerDeployer.
     *
     * @return readerDeployer
     */
    public ReaderDeployer getReaderDeployer() {
        return readerDeployer;
    }

    /**
     * Getter of elevated.
     * 
     * @return elevated
     */
    private boolean isElevated() {
        return elevated;
    }

    /**
     * Getter of setting.
     *
     * @return setting
     */
    public Setting getSetting() {
        return setting;
    }    
}
