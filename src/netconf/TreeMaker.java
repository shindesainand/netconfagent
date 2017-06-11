package netconf;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.logging.Logger;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class TreeMaker {
	
		JTextArea txtrCommandxml;
		static boolean parserCreated = false;
		JTree tree;
		static DefaultTreeModel treeModel = new DefaultTreeModel(null);
		static String jTreeRootName;
		Logger logger = Logger.getLogger("LOGGER");
		
		void initialize(String filePath) throws Exception{
			
			//String filePath = "C:\\Users\\sainand\\ProjectNetConf\\NetConfAgent\\res\\IF-MIB.yang";
			File destFile = new File(filePath.replace(".yang", ".xml"));
			String command[] = {"java","jyang","-o",destFile.getAbsolutePath(),"-f","yin",filePath};
			File curDir = new File(destFile.getParentFile().getAbsolutePath());
			
			File file = new File(filePath);
			FileInputStream yangfile = new FileInputStream(file);
			if(parserCreated == false)
			{
				new jyang.parser.yang(yangfile);
				parserCreated = true;
			}
			else
				jyang.parser.yang.ReInit(yangfile);
			jyang.parser.YANG_Specification spec;
			spec = jyang.parser.yang.Start();
			jTreeRootName = spec.getName();
			
			/*Command to create yin file from the yang file is 
			 * 
			 * java jyang -o vyatta-firewall.xml -f yin C:\\users\sainand\\ProjectNetConf\\NetConfAgent\\res\\vyatta-firewall.yang
			 * 
			 */		

			ProcessBuilder pb = new ProcessBuilder(command);
			pb.directory(curDir);
			pb.redirectErrorStream(true);
			pb.start();
			Thread.sleep(500);
			logger.info("Yang file parsed and Yin file created.");
			
			editLeafList(destFile);
			
			logger.info("Yin file repaired.");
			
			String filepath = destFile.getAbsolutePath();
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(filepath);
			
			Node module = getModule(doc);
			
			logger.info("Creating "+spec.getName()+" module tree.");
			
			tree.setModel(treeModel);
			iterate(module, setTreeRoot(jTreeRootName));

			logger.info("Created JTree representing the "+spec.getName()+" module.");
		
			tree.addMouseListener(new MouseAdapter()
			{
				boolean leaf;
				TreePath treePath;
				DefaultMutableTreeNode temp;
				
				ActionListener menuListener = new ActionListener()
				{
					public void actionPerformed(ActionEvent event)
					{
						NetConfProtocol ncp = new NetConfProtocol();
						
						logger.info(event.getActionCommand() + " selected.");
						
						if(temp == null)
						{
							NetConfBrowser.raiseError("Make sure you select the tree node correctly.","Click Again!");
							return;
						}
						if(!temp.equals(treePath.getLastPathComponent()))
						{
							NetConfBrowser.raiseError("Make sure you select the tree node correctly.","Click Again!");
							return;
						}
						else if(event.getActionCommand().equals("get"))
						{
							NetConfBrowser.operation = "get";
							txtrCommandxml.setText(ncp.get(treePath.toString(), destFile, leaf));
						}
						else if(event.getActionCommand().equals("get-config"))
						{
							NetConfBrowser.operation = "get-config";
							txtrCommandxml.setText(ncp.get_config(treePath.toString(), destFile, leaf));
						}
						else if(event.getActionCommand().equals("copy-config"))
						{
							NetConfBrowser.operation = "copy-config";
							txtrCommandxml.setText(ncp.copy_config());
						}
						else if(event.getActionCommand().equals("edit-config"))
						{
							if(treePath.getPathCount() == 1)
								try {
									NetConfBrowser.operation = "edit-config-root";
									Mediator med = new Mediator();
									med.ipHost = NetConfBrowser.ip;
									med.portNum = NetConfBrowser.port;
									txtrCommandxml.setText(med.sendClicked("edit-config-root", ""));
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.getMessage();
								}
							else
							{
								NetConfBrowser.operation = "edit-config";
								txtrCommandxml.setText(ncp.edit_config(treePath.toString(), destFile, leaf));
							}
						}
						else if(event.getActionCommand().equals("delete-config"))
						{
							NetConfBrowser.operation = "delete-config";
							txtrCommandxml.setText(ncp.delete_config());
						}
						else if(event.getActionCommand().equals("lock"))
						{
							NetConfBrowser.operation = "lock";
							txtrCommandxml.setText(ncp.lock());
						}
						else if(event.getActionCommand().equals("unlock"))
						{
							NetConfBrowser.operation = "unlock";
							txtrCommandxml.setText(ncp.unlock());
						}
					}
				};

			public void mouseClicked(MouseEvent me)
			{
				temp = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
				if(temp != null)
					leaf = temp.isLeaf() ? true : false;
				TreePath tp = tree.getPathForLocation(me.getX(), me.getY());
				if (tp != null)
				{
//					System.out.println(tp.toString());
					treePath = tp;
//					System.out.println("Clicked on "+temp);
				}
			}
			
			 public void mousePressed (MouseEvent e)
			 {
				 if(SwingUtilities.isRightMouseButton(e))
				 {
					 TreePath path = tree.getPathForLocation(e.getX(), e.getY());
					 Rectangle pathBounds = tree.getUI().getPathBounds(tree, path);
					 if(pathBounds != null && pathBounds.contains(e.getX(), e.getY()))
					 {
						 JPopupMenu menu = new JPopupMenu();
						 menu.add(new JMenuItem("get")).addActionListener(menuListener);
						 menu.add(new JMenuItem("get-config")).addActionListener(menuListener);
						 menu.add(new JMenuItem("copy-config")).addActionListener(menuListener);
						 menu.add(new JMenuItem("edit-config")).addActionListener(menuListener);
						 menu.add(new JMenuItem("delete-config")).addActionListener(menuListener);
						 menu.add(new JMenuItem("lock")).addActionListener(menuListener);
						 menu.add(new JMenuItem("unlock")).addActionListener(menuListener);
						 menu.show(tree, pathBounds.x, pathBounds.y+pathBounds.height);
					 }
				 }
			 }
			});
		}
		
		public static DefaultMutableTreeNode setTreeRoot(String modName)
		{
			DefaultMutableTreeNode root = new DefaultMutableTreeNode(modName);
			treeModel.setRoot(root);
			return (DefaultMutableTreeNode)treeModel.getRoot();
		}
		
		public static void addChild(String parent, String child)
		{
			DefaultMutableTreeNode parentNode = new DefaultMutableTreeNode(parent);
			DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);
			
			if(child.equals("#text"))
				return;
			
			if(parent.equals("module"))
			{
				parentNode = (DefaultMutableTreeNode)treeModel.getRoot();
				treeModel.insertNodeInto(childNode, parentNode, parentNode.getChildCount());
				return;
			}
			else
			{
				DefaultMutableTreeNode root = (DefaultMutableTreeNode)treeModel.getRoot();
				Enumeration<DefaultMutableTreeNode> children = root.children();
				while(children.hasMoreElements())
				{
					parentNode = (DefaultMutableTreeNode) children.nextElement();
					if(parentNode.toString().equals(parent))
					{
						treeModel.insertNodeInto(childNode, parentNode, parentNode.getChildCount());
					}
				}
			}
		}
		
		public static void editLeafList(File f) throws Exception
		{
			byte contentBytes[] = Files.readAllBytes(Paths.get(f.getAbsolutePath()));
			String content = new String(contentBytes, Charset.defaultCharset());
			content = content.replaceAll("</leaflist>", "").replaceAll("</uses>", "");
			//content = content.concat("</module>");
			new PrintWriter(f).close();
			FileWriter fileWriter = new FileWriter(f);
			fileWriter.write(content);
			fileWriter.close();
		}
		
		public static Node getModule(Document page)
		{
			Node mod = page.getElementsByTagName("module").item(0); //possible bug because of email id or any other value containing <>
			return mod;
		}
		
		public static void iterate(Node parent, DefaultMutableTreeNode root)
		{
			NodeList nodeList = parent.getChildNodes();
			for(int i = 0;i < nodeList.getLength(); i++)
			{
				Node node = nodeList.item(i);
				Node temp = node.hasAttributes() ? node.getAttributes().getNamedItem("name") : null;			
				if(!node.getNodeName().equals("#text") && temp != null && node.hasAttributes())
				{
					Element hello = (Element)node;
					String attName = hello.getAttribute("name");
					DefaultMutableTreeNode child = new DefaultMutableTreeNode(attName);//node.getNodeName()); if u want nodename itself as the tree node name rather than the node's name attribute
					treeModel.insertNodeInto(child, root, root.getChildCount());
		            if (node.hasChildNodes()) {
		                iterate(node, child);
		            }
				}
			}
		}
}
