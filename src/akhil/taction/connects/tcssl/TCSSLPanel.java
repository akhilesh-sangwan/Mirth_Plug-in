package akhil.taction.connects.tcssl;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.HashSet;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import com.mirth.connect.client.ui.AbstractConnectorPropertiesPanel;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthButton;
import com.mirth.connect.client.ui.components.MirthCheckBox;
import com.mirth.connect.client.ui.components.MirthTextField;
import com.mirth.connect.donkey.model.channel.ConnectorPluginProperties;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.model.Connector.Mode;

import net.miginfocom.swing.MigLayout;

public class TCSSLPanel extends AbstractConnectorPropertiesPanel {

    private MirthCheckBox enabledBox;

    private JLabel certPathLabel;
    private MirthTextField certPathField;

    private JLabel keyPathLabel;
    private MirthTextField keyPathField;

    private MirthCheckBox trustAllCertsBox;

    private JLabel trustedCertPathsLabel;
    private DefaultListModel<String> trustedCertPathsModel;
    private JList<String> trustedCertPathsList;

    private MirthButton newButton;
    private MirthButton deleteButton;

    public TCSSLPanel() {
        initComponents();
        initToolTips();
        initLayout();
    }

    @Override
    public ConnectorPluginProperties getProperties() {
        TCSSLPluginProperties properties = new TCSSLPluginProperties();

        properties.setEnabled(enabledBox.isSelected());

        properties.setCertPath(certPathField.getText());
        properties.setKeyPath(keyPathField.getText());

        properties.setTrustAllCerts(trustAllCertsBox.isSelected());
        properties.setTrustedCertPaths(new HashSet<>(Collections.list(trustedCertPathsModel.elements())));

        return properties;
    }

    @Override
    public void setProperties(ConnectorProperties connectorProperties, ConnectorPluginProperties properties, Mode mode,
            String transportName) {
        TCSSLPluginProperties sslProperties = (TCSSLPluginProperties) properties;

        enabledBox.setSelected(sslProperties.isEnabled());

        certPathField.setText(sslProperties.getCertPath());
        keyPathField.setText(sslProperties.getKeyPath());

        trustAllCertsBox.setSelected(sslProperties.doTrustAllCerts());

        trustedCertPathsModel.clear();
        for (String path : sslProperties.getTrustedCertPaths()) {
            trustedCertPathsModel.addElement(path);
        }

        updateActivations();
    }

    @Override
    public ConnectorPluginProperties getDefaults() {
        return new TCSSLPluginProperties();
    }

    @Override
    public boolean checkProperties(ConnectorProperties connectorProperties, ConnectorPluginProperties properties,
            Mode mode, String transportName, boolean highlight) {
        // Implement validation logic if needed
        return true;
    }

    @Override
    public void resetInvalidProperties() {
        // Implement resetting logic if needed
    }

    @Override
    public Component[][] getLayoutComponents() {
        // Implement layout components if needed
        return null;
    }

    @Override
    public void setLayoutComponentsEnabled(boolean enabled) {
        // Implement setting layout components enabled state if needed
    }

    private void initComponents() {
        setBackground(UIConstants.BACKGROUND_COLOR);

        enabledBox = new MirthCheckBox("Enabled");
        enabledBox.setBackground(getBackground());
        enabledBox.addItemListener(e -> updateActivations());

        certPathLabel = new JLabel("Certificate Path:");
        certPathField = new MirthTextField();
        certPathField.setBackground(getBackground());

        keyPathLabel = new JLabel("Key Path:");
        keyPathField = new MirthTextField();
        keyPathField.setBackground(getBackground());

        trustAllCertsBox = new MirthCheckBox("Trust All Certificates");
        trustAllCertsBox.setBackground(getBackground());
        trustAllCertsBox.addItemListener(e -> updateActivations());

        trustedCertPathsLabel = new JLabel("Trusted Certificate Paths:");
        trustedCertPathsModel = new DefaultListModel<>();
        trustedCertPathsList = new JList<>(trustedCertPathsModel);
        trustedCertPathsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        newButton = new MirthButton("New...");
        newButton.addActionListener(e -> newTrustedCertClicked());

        deleteButton = new MirthButton("Delete");
        deleteButton.addActionListener(e -> deleteTrustedCertClicked());
    }

    private void initToolTips() {
        enabledBox.setToolTipText("Enable or disable SSL.");
        certPathField.setToolTipText(
                "Path to connector X509 certificate PEM file. Required for server mode, optional for client mode.");
        keyPathField.setToolTipText("Path to connector PKCS8 private key PEM file. Required for certificates.");
        trustedCertPathsList.setToolTipText("Paths to trusted X509 certificate PEM files.");
        newButton.setToolTipText("Add new path to trusted certificate list.");
        deleteButton.setToolTipText("Remove selected path from trusted certificate list.");
    }

    private void initLayout() {
        JScrollPane scrollPane = new JScrollPane(trustedCertPathsList);
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
        pane.add(newButton);
        pane.add(deleteButton);

        setLayout(new MigLayout());
        add(enabledBox, "span");
        add(certPathLabel, "newline, right");
        add(certPathField, "w 400");
        add(keyPathLabel, "newline, right");
        add(keyPathField, "w 400");
        add(trustAllCertsBox, "newline, span");
        add(trustedCertPathsLabel, "newline, span");
        add(scrollPane, "newline, span, h 100, w 400");
        add(pane, "newline");
    }

    private void updateActivations() {
        boolean enabled = enabledBox.isSelected();
        certPathLabel.setEnabled(enabled);
        certPathField.setEnabled(enabled);
        keyPathLabel.setEnabled(enabled);
        keyPathField.setEnabled(enabled);
        trustAllCertsBox.setEnabled(enabled);
        trustedCertPathsList.setEnabled(enabled && !trustAllCertsBox.isSelected());
        newButton.setEnabled(enabled && !trustAllCertsBox.isSelected());
        deleteButton.setEnabled(enabled && !trustAllCertsBox.isSelected());
    }

    private void newTrustedCertClicked() {
        String path = (String) JOptionPane.showInputDialog(this, "Trusted Cert:", "New Trusted Cert",
                JOptionPane.PLAIN_MESSAGE, null, null, "");
        if (path != null && trustedCertPathsModel.indexOf(path) < 0) {
            trustedCertPathsModel.addElement(path);
            trustedCertPathsList.setSelectedIndex(trustedCertPathsModel.getSize() - 1);
        }
    }

    private void deleteTrustedCertClicked() {
        int index = trustedCertPathsList.getSelectedIndex();
        if (index >= 0) {
            trustedCertPathsModel.remove(index);
        }
    }
}
