package netconf;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import java.awt.*;

import net.miginfocom.swing.MigLayout;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.awt.event.ActionEvent;

public class NetConfBrowser {
	
	/*
	 * The source code or class files must always reside in netconf package
	 */

	private JFrame frame;
	static String ip;
	static int port;
	static String operation;
	private static JTable table;
	public static Vector<Vector<String>> rowData = new Vector<Vector<String>>();
	private static DefaultTableModel dataModel = new DefaultTableModel();
	static String downloadFolder = "F:\\dowloaded_files\\";
	static Logger logger = Logger.getLogger("LOGGER");
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		FileHandler fh;
		try
		{
			fh = new FileHandler("."+"\\Log.log");
			logger.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
			logger.setUseParentHandlers(false);
		}
		catch (SecurityException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
				
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException ex) {
                } catch (InstantiationException ex) {
                } catch (IllegalAccessException ex) {
                } catch (UnsupportedLookAndFeelException ex) {
                }
				try {
					NetConfBrowser window = new NetConfBrowser();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * @throws Exception 
	 */
	public NetConfBrowser() {
		try {
			initialize();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			raiseError(e.getMessage(), e.getLocalizedMessage());
		}
	}

	/**
	 * Initialize the contents of the frame.
	 * @throws Exception 
	 */
	private void initialize() throws Exception {
		frame = new JFrame();
		frame.setBounds(100, 100, 750, 500);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new CardLayout(0, 0));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frame.getContentPane().add(tabbedPane, "name_43583469675882");
		
		GetConnection getConnect = new GetConnection(); //GetConnection object created for further use in the program
		
		JPanel connect = new JPanel();
		tabbedPane.addTab("Connect", null, connect, null);
		SpringLayout sl_connect = new SpringLayout();
		connect.setLayout(sl_connect);
		
		JPanel panel_1 = new JPanel();
		sl_connect.putConstraint(SpringLayout.NORTH, panel_1, 10, SpringLayout.NORTH, connect);
		sl_connect.putConstraint(SpringLayout.WEST, panel_1, frame.getWidth()/3, SpringLayout.WEST, connect);
		sl_connect.putConstraint(SpringLayout.SOUTH, panel_1, 110, SpringLayout.NORTH, connect);
		sl_connect.putConstraint(SpringLayout.EAST, panel_1, -frame.getWidth()/3, SpringLayout.EAST, connect);
		connect.add(panel_1);
		
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{40, 0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		JLabel lblHostIp = new JLabel("Host IP");
		GridBagConstraints gbc_lblHostIp = new GridBagConstraints();
		gbc_lblHostIp.insets = new Insets(0, 0, 5, 5);
		gbc_lblHostIp.gridx = 0;
		gbc_lblHostIp.gridy = 0;
		panel_1.add(lblHostIp, gbc_lblHostIp);
		
		MaskFormatter formatterIP = new MaskFormatter();
		try {
			formatterIP = new MaskFormatter("***************");
			formatterIP.setValidCharacters("0123456789. ");
		} catch (java.text.ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		JFormattedTextField frmtdtxtfldIpaddress = new JFormattedTextField(formatterIP);
		GridBagConstraints gbc_frmtdtxtfldIpaddress = new GridBagConstraints();
		gbc_frmtdtxtfldIpaddress.gridwidth = 2;
		gbc_frmtdtxtfldIpaddress.insets = new Insets(0, 0, 5, 0);
		gbc_frmtdtxtfldIpaddress.fill = GridBagConstraints.HORIZONTAL;
		gbc_frmtdtxtfldIpaddress.gridx = 1;
		gbc_frmtdtxtfldIpaddress.gridy = 0;
		panel_1.add(frmtdtxtfldIpaddress, gbc_frmtdtxtfldIpaddress);
		
		JLabel lblPort = new JLabel("Port");
		GridBagConstraints gbc_lblPort = new GridBagConstraints();
		gbc_lblPort.insets = new Insets(0, 0, 5, 5);
		gbc_lblPort.gridx = 0;
		gbc_lblPort.gridy = 1;
		panel_1.add(lblPort, gbc_lblPort);
		
		MaskFormatter formatterPort = new MaskFormatter();
		try {
			formatterPort = new MaskFormatter("*****");
			formatterPort.setValidCharacters("0123456789 ");
		} catch (java.text.ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		JFormattedTextField frmtdtxtfldPortno = new JFormattedTextField(formatterPort);
		frmtdtxtfldPortno.setText("");
		GridBagConstraints gbc_frmtdtxtfldPortno = new GridBagConstraints();
		gbc_frmtdtxtfldPortno.gridwidth = 2;
		gbc_frmtdtxtfldPortno.insets = new Insets(0, 0, 5, 0);
		gbc_frmtdtxtfldPortno.fill = GridBagConstraints.HORIZONTAL;
		gbc_frmtdtxtfldPortno.gridx = 1;
		gbc_frmtdtxtfldPortno.gridy = 1;
		panel_1.add(frmtdtxtfldPortno, gbc_frmtdtxtfldPortno);
		
		JButton btnConnect = new JButton("Connect");
		
		GridBagConstraints gbc_btnConnect = new GridBagConstraints();
		gbc_btnConnect.gridwidth = 2;
		gbc_btnConnect.gridx = 1;
		gbc_btnConnect.gridy = 2;
		panel_1.add(btnConnect, gbc_btnConnect);
		
		JScrollPane scrollPane_2 = new JScrollPane();
		sl_connect.putConstraint(SpringLayout.NORTH, scrollPane_2, 10, SpringLayout.SOUTH, panel_1);
		sl_connect.putConstraint(SpringLayout.WEST, scrollPane_2, 20, SpringLayout.WEST, connect);
		sl_connect.putConstraint(SpringLayout.SOUTH, scrollPane_2, -35, SpringLayout.SOUTH, connect);
		sl_connect.putConstraint(SpringLayout.EAST, scrollPane_2, -20, SpringLayout.EAST, connect);
		connect.add(scrollPane_2);
		
		JPanel yangmodule = new JPanel();
		tabbedPane.addTab("yang", null, yangmodule, null);
		SpringLayout sl_yangmodule = new SpringLayout();
		yangmodule.setLayout(sl_yangmodule);
		
		JScrollPane scrollPane_3 = new JScrollPane();
		sl_yangmodule.putConstraint(SpringLayout.NORTH, scrollPane_3, 0, SpringLayout.NORTH, yangmodule);
		sl_yangmodule.putConstraint(SpringLayout.WEST, scrollPane_3, 0, SpringLayout.WEST, yangmodule);
		sl_yangmodule.putConstraint(SpringLayout.SOUTH, scrollPane_3, 0, SpringLayout.SOUTH, yangmodule);
		sl_yangmodule.putConstraint(SpringLayout.EAST, scrollPane_3, 0, SpringLayout.EAST, yangmodule);
		yangmodule.add(scrollPane_3);
		
		JTextArea txtrYangfile = new JTextArea();
		txtrYangfile.setEditable(false);
		scrollPane_3.setViewportView(txtrYangfile);
		
		JButton btnDownload = new JButton("Download");
		
		sl_connect.putConstraint(SpringLayout.NORTH, btnDownload, 5, SpringLayout.SOUTH, scrollPane_2);
		sl_connect.putConstraint(SpringLayout.EAST, btnDownload, -frame.getWidth()/3, SpringLayout.EAST, connect);
		sl_connect.putConstraint(SpringLayout.WEST, btnDownload, frame.getWidth()/3, SpringLayout.WEST, connect);
		connect.add(btnDownload);
		
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				String ipRegPattern = "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
				
				if(frmtdtxtfldIpaddress.getText().trim().isEmpty())
				{
					JOptionPane noIPPane = new JOptionPane();
					JOptionPane.showMessageDialog(noIPPane, "Please enter the Network Element IP address!", "Error", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				if(!frmtdtxtfldIpaddress.getText().trim().matches(ipRegPattern))
				{
					JOptionPane invalidIPPane = new JOptionPane();
					JOptionPane.showMessageDialog(invalidIPPane, "Please enter proper IP address!", "Error", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				if(frmtdtxtfldPortno.getText().trim().isEmpty())
				{
					JOptionPane noIPPane = new JOptionPane();
					JOptionPane.showMessageDialog(noIPPane, "Please enter the Network Element Port number!", "Error", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				ip = frmtdtxtfldIpaddress.getText().trim();
				port = Integer.parseInt(frmtdtxtfldPortno.getText().trim());
				
				txtrYangfile.setText("");
				
				Vector<String> colName = new Vector<String>();
				colName.addElement("Yang Modules:");
				resetTable();
				getConnect.connectToNE(ip, port);
				
				displayTable(rowData, colName, scrollPane_2);
			}});
		
		JPanel browse = new JPanel();
		tabbedPane.addTab("Browse", null, browse, null);
		browse.setLayout(new MigLayout("", "[225.00px,grow][0][grow][grow]", "[350.00px,grow]"));
		
		JScrollPane scrollPane_4 = new JScrollPane();
		browse.add(scrollPane_4, "cell 0 0,grow");
		
		JTree tree = new JTree();
		scrollPane_4.setViewportView(tree);
		
		JPanel panel = new JPanel();
		browse.add(panel, "cell 2 0 2 1,grow");
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{57, 36, 2, 155, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridheight = 2;
		gbc_scrollPane.gridwidth = 6;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		panel.add(scrollPane, gbc_scrollPane);
		
		JTextArea txtrCmdXml = new JTextArea();
		scrollPane.setViewportView(txtrCmdXml);
		
		btnDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(table.getSelectedRow() == -1)
				{
					JOptionPane noFileSelectedPane = new JOptionPane();
					JOptionPane.showMessageDialog(noFileSelectedPane, "Please select a yang module to download!", "Error", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				txtrCmdXml.setText("");
				
				String yangFileName = dataModel.getValueAt(table.getSelectedRow(), table.getSelectedColumn()).toString();
				String yangContent = getConnect.downloadClicked(table.getSelectedRow());
				txtrYangfile.setText(yangContent);
				try {
					TreeMaker tm = new TreeMaker();
					tm.txtrCommandxml = txtrCmdXml;
					tm.tree = tree;
					tm.initialize(createYangFile(yangFileName, yangContent).getAbsolutePath());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					raiseError(e.getMessage(), e.getClass().getSimpleName());
				}
			}
		});
		
		
		
		JLabel lblCommandXml = new JLabel("Command XML");
		GridBagConstraints gbc_lblCommandXml = new GridBagConstraints();
		gbc_lblCommandXml.insets = new Insets(0, 0, 5, 5);
		gbc_lblCommandXml.gridx = 0;
		gbc_lblCommandXml.gridy = 0;
		panel.add(lblCommandXml, gbc_lblCommandXml);
		
		JButton btnClearCmdXml = new JButton("Clear XML");
		btnClearCmdXml.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtrCmdXml.setText("");
				logger.info("Command XML text area cleared.");
			}
		});
		GridBagConstraints gbc_btnClearCmdXml = new GridBagConstraints();
		gbc_btnClearCmdXml.insets = new Insets(0, 0, 5, 0);
		gbc_btnClearCmdXml.gridx = 5;
		gbc_btnClearCmdXml.gridy = 0;
		btnClearCmdXml.setPreferredSize(new Dimension(90, 20));
		panel.add(btnClearCmdXml, gbc_btnClearCmdXml);
		
		JButton btnCloseSession = new JButton("Close Session");
		btnCloseSession.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logger.info("Close session performed.");
//				getConnect.disConnect();
				System.exit(0);
			}
		});
		GridBagConstraints gbc_btnCloseSession = new GridBagConstraints();
		gbc_btnCloseSession.insets = new Insets(0, 0, 5, 5);
		gbc_btnCloseSession.gridx = 2;
		gbc_btnCloseSession.gridy = 3;
		btnCloseSession.setPreferredSize(new Dimension(100, 25));
		panel.add(btnCloseSession, gbc_btnCloseSession);
		
		JButton btnKillSession = new JButton("Kill Session");
		btnKillSession.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane confirmationPane = new JOptionPane();
				if(JOptionPane.showConfirmDialog(confirmationPane, "Are you sure you want to KILL this session ?", "Confirmation", JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
				{
					logger.info("Kill session performed. Yes clicked.");
					System.exit(1);
//					getConnect.disConnect();
				}
				else
					logger.info("Kill session performed. No clicked.");
			}
		});
		GridBagConstraints gbc_btnKillSession = new GridBagConstraints();
		gbc_btnKillSession.insets = new Insets(0, 0, 5, 5);
		gbc_btnKillSession.gridx = 3;
		gbc_btnKillSession.gridy = 3;
		btnKillSession.setPreferredSize(new Dimension(100, 25));
		panel.add(btnKillSession, gbc_btnKillSession);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.gridheight = 2;
		gbc_scrollPane_1.gridwidth = 6;
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = 5;
		panel.add(scrollPane_1, gbc_scrollPane_1);
		
		JTextArea txtrReply = new JTextArea();
		scrollPane_1.setViewportView(txtrReply);
		
		JButton btnClearReply = new JButton("Clear Reply");
		btnClearReply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtrReply.setText("");
				logger.info("Reply XML text area cleared.");
			}
		});
		GridBagConstraints gbc_btnClearReply = new GridBagConstraints();
		gbc_btnClearReply.insets = new Insets(0, 0, 5, 0);
		gbc_btnClearReply.gridx = 5;
		gbc_btnClearReply.gridy = 4;
		btnClearReply.setPreferredSize(new Dimension(90, 20));
		panel.add(btnClearReply, gbc_btnClearReply);
		
		JButton btnSend = new JButton("Send");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				logger.info("Command XML is being sent to "+ip);
				
				String command = txtrCmdXml.getText();
				Mediator med = new Mediator();
				med.ipHost = ip;
				med.portNum = port;
				
				txtrReply.setText(med.sendClicked(operation, command));
				if(txtrReply.getText().isEmpty())
					txtrReply.setText("<rpc-error message-id = \"101\" xmlns=\""+NetConfProtocol.namespaceVal+"\">\n</rpc-error>");
			}
		});
		btnSend.setPreferredSize(new Dimension(70, 25));
		GridBagConstraints gbc_btnSend = new GridBagConstraints();
		gbc_btnSend.insets = new Insets(0, 0, 5, 5);
		gbc_btnSend.gridx = 0;
		gbc_btnSend.gridy = 3;
		panel.add(btnSend, gbc_btnSend);
		
		JLabel lblRpcReply = new JLabel("RPC Reply");
		GridBagConstraints gbc_lblRpcReply = new GridBagConstraints();
		gbc_lblRpcReply.insets = new Insets(0, 0, 5, 5);
		gbc_lblRpcReply.gridx = 0;
		gbc_lblRpcReply.gridy = 4;
		panel.add(lblRpcReply, gbc_lblRpcReply);
		
		/*JPanel capabilities = new JPanel();
		tabbedPane.addTab("Capabilities", null, capabilities, null);*/
	}
	
	public static File createYangFile(String yangName, String content) throws Exception
	{
		String dirPath = downloadFolder+ip+"\\";
		File file = new File(dirPath);
		if(!file.exists())
			file.mkdirs();
		
		file = new File(dirPath+yangName);//+".yang");
		FileWriter fw = new FileWriter(file);
		fw.write(content);
		fw.close();
		
		logger.info("Downloaded "+file.getName()+" file");
		
		return file;
	}
	
	public void displayTable(Vector<Vector<String>> row, Vector<String> col, JScrollPane jsp)
	{	
		dataModel = new DefaultTableModel(row,col);
		table = new JTable(){
			//private static final long serialVersionUID = 1L;
			public boolean isCellEditable(int row, int column) { //int row, int column
		        return false;
		    }
		};
		table.setModel(dataModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jsp.setViewportView(table);
	}
	
	public void resetTable()
	{
		while (dataModel.getRowCount() > 0){
	        for (int i = 0; i < dataModel.getRowCount(); ++i){
	            dataModel.removeRow(i);
	        }
	    }
	}
	
	public static void raiseError(String msg, String errorName)
	{
		JOptionPane noIPPane = new JOptionPane();
		JOptionPane.showMessageDialog(noIPPane, msg, errorName, JOptionPane.ERROR_MESSAGE);
		return;
	}
}