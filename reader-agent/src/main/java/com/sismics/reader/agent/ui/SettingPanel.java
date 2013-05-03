package com.sismics.reader.agent.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.sismics.reader.agent.ReaderAgent;
import com.sismics.reader.agent.model.Setting;
import com.sismics.util.MessageUtil;

/**
 * Settings panel.
 *
 * @author jtremeaux
 */
public class SettingPanel extends JPanel {

    /**
     * Serial UID.
     */
    private static final long serialVersionUID = 1L;

    private static final Format INTEGER_FORMAT = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.US));

    private JFormattedTextField portTextField;
    
    private JComboBox contextPathComboBox;
    
    private JCheckBox autoStartCheckBox;
    
    private JButton defaultButton;
    
    private JButton saveButton;
    
    private ReaderAgent readerAgent;
    
    /**
     * Constructor of SettingPanel.
     * 
     * @param readerAgent Reader agent
     */
    public SettingPanel(ReaderAgent readerAgent) {
        this.readerAgent = readerAgent;
        
        initComponent();
        readSetting();
    }
    
    /**
     * Read the settings from the properties file, and initialize the panel fields.
     */
    public void readSetting() {
        final Setting setting = readerAgent.getSetting();
        setting.read();
        portTextField.setValue(setting.getPort());
        contextPathComboBox.setSelectedItem(setting.getContextPath());
        autoStartCheckBox.setSelected(setting.isAutoStart());
    }

    /**
     * Save the settings from the panel to the properties file.
     * 
     * @throws Exception
     */
    public void saveSetting() throws Exception {
        final Setting setting = readerAgent.getSetting();
        setting.setPort(getPort());
        setting.setContextPath(getContextPath());
        setting.setAutoStart(autoStartCheckBox.isSelected());
        setting.save();

        JOptionPane.showMessageDialog(this,
                MessageUtil.getMessage("agent.setting.save.ok.message"),
                MessageUtil.getMessage("agent.setting.save.ok.title"),
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Initialize UI components.
     */
    private void initComponent() {
        portTextField = new JFormattedTextField(INTEGER_FORMAT);
        
        contextPathComboBox = new JComboBox();
        contextPathComboBox.setEditable(true);
        contextPathComboBox.addItem("/");
        contextPathComboBox.addItem("/reader");

        defaultButton = new JButton(MessageUtil.getMessage("agent.setting.default"));
        defaultButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                portTextField.setValue(Setting.DEFAULT_PORT);
                contextPathComboBox.setSelectedItem(Setting.DEFAULT_CONTEXT_PATH);
                autoStartCheckBox.setSelected(Setting.DEFAULT_AUTO_START);
            }
        });
        
        autoStartCheckBox = new JCheckBox();

        saveButton = new JButton(MessageUtil.getMessage("agent.setting.save"));
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                try {
                    saveSetting();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(SettingPanel.this,
                            e.getMessage(),
                            MessageUtil.getMessage("agent.setting.save.error.title"),
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        FormLayout layout = new FormLayout("d, 6dlu, d, max(d;30dlu):grow");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.append(MessageUtil.getMessage("agent.setting.port"), portTextField);
        builder.append(MessageUtil.getMessage("agent.setting.context_path"), contextPathComboBox);
        builder.append(MessageUtil.getMessage("agent.setting.auto_start"), autoStartCheckBox);

        setBorder(Borders.DIALOG_BORDER);

        setLayout(new BorderLayout(12, 12));
        add(builder.getPanel(), BorderLayout.CENTER);
        add(ButtonBarFactory.buildCenteredBar(defaultButton, saveButton), BorderLayout.SOUTH);
    }

    /**
     * Validate and get the context path.
     * 
     * @return Context path
     * @throws Exception
     */
    private String getContextPath() throws Exception {
        String contextPath = ((String) contextPathComboBox.getSelectedItem()).trim();
        if (contextPath.contains(" ") || !contextPath.startsWith("/")) {
            throw new Exception(MessageUtil.getMessage("agent.setting.save.error.context_path"));
        }
        return contextPath;
    }

    /**
     * Validate and get the port number.
     * 
     * @return Port number
     * @throws Exception
     */
    private int getPort() throws Exception {
        int port;
        try {
            port = ((Number) portTextField.getValue()).intValue();
            if (port < 1 || port > 65535) {
                throw new Exception(MessageUtil.getMessage("agent.setting.save.error.port"));
            }
        } catch (Exception e) {
            throw new Exception(MessageUtil.getMessage("agent.setting.save.error.port"), e);
        }
        return port;
    }
}
