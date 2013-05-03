package com.sismics.reader.agent.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.sismics.reader.agent.ReaderAgent;
import com.sismics.reader.agent.deployer.DeploymentStatus;
import com.sismics.reader.agent.deployer.DeploymentStatus.ServerState;
import com.sismics.reader.agent.deployer.DeploymentStatusListener;
import com.sismics.util.MessageUtil;

/**
 * Panel displaying the status of the Reader service.
 *
 * @author jtremeaux
 */
public class StatusPanel extends JPanel implements DeploymentStatusListener {

    /**
     * Serial UID.
     */
    private static final long serialVersionUID = 1L;

    private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

    private final ReaderAgent readerAgent;

    private JTextField statusTextField;
    
    private JTextField startedTextField;
    
    private JTextField memoryTextField;
    
    private JTextArea errorTextField;
    
    private JButton startButton;
    
    private JButton stopButton;
    
    private JButton urlButton;

    /**
     * Constructor of StatusPanel.
     * 
     * @param readerAgent Windows agent
     */
    public StatusPanel(ReaderAgent readerAgent) {
        this.readerAgent = readerAgent;
        initComponent();
        readerAgent.addListener(this);
    }

    /**
     * Initialize UI components.
     */
    private void initComponent() {
        statusTextField = new JTextField();
        statusTextField.setEditable(false);

        startedTextField = new JTextField();
        startedTextField.setEditable(false);

        memoryTextField = new JTextField();
        memoryTextField.setEditable(false);

        errorTextField = new JTextArea(3, 24);
        errorTextField.setEditable(false);
        errorTextField.setLineWrap(true);
        errorTextField.setBorder(startedTextField.getBorder());

        startButton = new JButton(MessageUtil.getMessage("agent.status.start"));
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                readerAgent.checkElevation("-start");
                readerAgent.getReaderDeployer().start();
            }
        });
        
        stopButton = new JButton(MessageUtil.getMessage("agent.status.stop"));
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                readerAgent.checkElevation("-stop");
                readerAgent.getReaderDeployer().stop();
            }
        });
        
        urlButton = new JButton();
        urlButton.setBorderPainted(false);
        urlButton.setContentAreaFilled(false);
        urlButton.setForeground(Color.BLUE.darker());
        urlButton.setHorizontalAlignment(SwingConstants.LEFT);
        urlButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                readerAgent.openBrowser();
            }
        });

        JPanel buttons = ButtonBarFactory.buildRightAlignedBar(startButton, stopButton);

        FormLayout layout = new FormLayout("right:d, 6dlu, max(d;30dlu):grow");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout, this);
        builder.append(MessageUtil.getMessage("agent.status.state"), statusTextField);
        builder.append("", buttons);
        builder.appendParagraphGapRow();
        builder.nextRow();
        builder.append(MessageUtil.getMessage("agent.status.start_time"), startedTextField);
        builder.append(MessageUtil.getMessage("agent.status.memory"), memoryTextField);
        builder.append(MessageUtil.getMessage("agent.status.error_message"), errorTextField);
        builder.append(MessageUtil.getMessage("agent.status.server_address"), urlButton);

        setBorder(Borders.DIALOG_BORDER);
    }

    @Override
    public void notifyDeploymentStatus(DeploymentStatus status) {
        String statusText = null;
        final ServerState state = status.getServerState();
        switch (state) {
        case STOPPED:
            statusText = MessageUtil.getMessage("agent.status.state.stopped");
            break;
        case STARTING:
            statusText = MessageUtil.getMessage("agent.status.state.starting");
            break;
        case STARTED:
            statusText = MessageUtil.getMessage("agent.status.state.started");
            break;
        case STOPPING:
            statusText = MessageUtil.getMessage("agent.status.state.stopping");
            break;
        }
        statusTextField.setText(statusText);
        startedTextField.setText(state == ServerState.STARTED ? DATE_FORMAT.format(status.getStartTime()) : null);
        memoryTextField.setText(state == ServerState.STARTED ? MessageUtil.getMessage("agent.status.memory.value", status.getMemoryUsed()) : null);
        errorTextField.setText(state == ServerState.STOPPED ? status.getErrorMessage() : null);
        urlButton.setText(state == ServerState.STARTED ? status.getUrl() : null);
    }
}
