package netconf;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.rmi.registry.*;

public class NetConfElement extends UnicastRemoteObject implements OnConnectInterface{
	
	/*
	 * XML instances must always reside in xml_instances folder
	 * Instance file names must be in the format <instancename>_insta.xml e.g. toaster_insta.xml
	 * The class files or source code must always reside in netconf package
	 * The yang files must be named as <filename>.yang e.g sampleyang.yang
	 * 
	 * some characters like < , > etc must be removed/replaced with ( )
	 * 
	 * NE must already have yang files folder in it at the path specified by yangFolder variable
	 * It must also have the XML instances in the path specified by xmlFolder variable
	 * 
	 * pyang -f sample-xml-skeleton -o vyatta-firewall_insta.xml vyatta-firewall.yang --sample-xml-skeleton-defaults
	 * 
	 */
	
	private static ArrayList<String> nodeList = new ArrayList<String>();
	private static String xmlFolder = "F:\\xml_instances";
	private static String yangFolder = new String("F:\\yang_files");
	Vector<Vector<String>> modules = new Vector<Vector<String>>();
	private static String fileDownloaded;
	static Logger logger = Logger.getLogger("SERVER LOGGER");
	//private static final long serialVersionUID = -5451352025625549838L;
	
	public NetConfElement() throws RemoteException
	{
		File path = new File(xmlFolder+"\\logs");
		if(!path.exists())
			path.mkdirs();
		
		File logFile = new File(path.getAbsolutePath()+"\\Log.log");
		FileHandler fh;
		
		try{	
			fh = new FileHandler(logFile.getAbsolutePath());
			logger.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
			logger.setUseParentHandlers(false);
		} 
		catch (SecurityException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		logger.info("Server is instantiated.");
		System.out.println("Server is Instantiated");
	}

	public Vector<Vector<String>> getModules() throws RemoteException    
	{
		try {
			logger.info(getClientHost()+" has connected.");
			System.out.println("Connection established with "+getClientHost());
		} 
		catch(ServerNotActiveException e) {
			// TODO Auto-generated catch block
			logger.info("Server not up. Problem: "+e.getClass().getSimpleName());
		}
		
		Vector<String> mods = new Vector<String>();
		File f = new File(yangFolder);
		String[] files = f.list();
		Collections.addAll(mods, files);
		modules.clear();
		modules.addElement(mods);
		
		logger.info("Yang files sent.");
		
		return modules;
	}
	
	public String downloadModules(int index) throws RemoteException
	{
		StringBuilder sb = new StringBuilder();
		String yangfile = new String();
		try 
		{
			Scanner in = new Scanner(new FileReader(yangFolder+"\\"+modules.elementAt(0).elementAt(index)));
			
			fileDownloaded = modules.elementAt(0).elementAt(index);
			while(in.hasNextLine())
			{
				sb.append(in.nextLine() + System.lineSeparator());
			}
			in.close();
			
			yangfile = sb.toString();
			
			logger.info(modules.elementAt(0).elementAt(index)+" was downloaded by client "+getClientHost());
		}
		catch(IOException | ServerNotActiveException e)
		{
			logger.info("Exception in downloadModules: "+e.getClass().getSimpleName()+" : "+e.getMessage());
			e.getMessage();
			return null;
		}
		return yangfile;
	}
	
	public String rpcRequest(String op, String xml) throws Exception
	{
		logger.info(op+" request received");
		
		if(op.equals("get"))
			return getOperation(op, xml);
		if(op.equals("get-config"))
			return getConfigOperation(op, xml);
		if(op.equals("edit-config-root") && xml.equals(""))
			return editConfigRootOperation();
		if(op.equals("edit-config-root") && !xml.equals(""))
			return updateInsta(xml);
		if(op.equals("edit-config"))
			return editConfigOperation(op, xml);
		if(op.equals("copy-config"))
			return copyConfigOperation(xml);
		if(op.equals("delete-config"))
			return deleteConfigOperation(xml);
		if(op.equals("lock"))
			return lockConfigOperation(xml);
		if(op.equals("unlock"))
			return unlockConfigOperation(xml);
		return "";
	}
	
	static String unlockConfigOperation(String xml) throws Exception
	{
		String xmlFile = fileDownloaded.replace(".yang", "_insta.xml");
		File xmlInstance = new File(xmlFolder+"\\"+xmlFile);
		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document cmdXml = docBuilder.parse(new InputSource(new ByteArrayInputStream(xml.getBytes(Charset.defaultCharset()))));
		
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		
		String lockExp = "rpc/unlock/target/*";
		
		XPathExpression lockExpr = xpath.compile(lockExp);
		
		Node temp = (Node)lockExpr.evaluate(cmdXml, XPathConstants.NODE);
		String datastore = temp.getNodeName();
		
		
		String rpcExp = "rpc";
		
		XPathExpression rpcExpr = xpath.compile(rpcExp);
		
		Node rpcNode = (Node)rpcExpr.evaluate(cmdXml, XPathConstants.NODE);
		Node msgIdAttNode = rpcNode.getAttributes().getNamedItem("message-id");
		
		logger.info("Unlocked "+datastore+" datastore");
		System.out.println("Unlocked "+datastore+" datastore");
		
		xmlInstance.setWritable(true);
		
		return "<rpc-reply "+msgIdAttNode+">\n</ok>\n</rpc-reply>";
	}
	
	static String lockConfigOperation(String xml) throws Exception
	{
		String xmlFile = fileDownloaded.replace(".yang", "_insta.xml");
		File xmlInstance = new File(xmlFolder+"\\"+xmlFile);
		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document cmdXml = docBuilder.parse(new InputSource(new ByteArrayInputStream(xml.getBytes(Charset.defaultCharset()))));
		
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		
		String lockExp = "rpc/lock/target/*";
		
		XPathExpression lockExpr = xpath.compile(lockExp);
		
		Node temp = (Node)lockExpr.evaluate(cmdXml, XPathConstants.NODE);
		String datastore = temp.getNodeName();
		
		
		String rpcExp = "rpc";
		
		XPathExpression rpcExpr = xpath.compile(rpcExp);
		
		Node rpcNode = (Node)rpcExpr.evaluate(cmdXml, XPathConstants.NODE);
		Node msgIdAttNode = rpcNode.getAttributes().getNamedItem("message-id");
		
		System.out.println("Locked: "+datastore+" datastore");
		logger.info("Locked: "+datastore+" datastore");
		
		xmlInstance.setReadOnly();
		
		return "<rpc-reply "+msgIdAttNode+">\n</ok>\n</rpc-reply>";
	}
	
	static String deleteConfigOperation(String xml) throws Exception
	{
		String xmlFile = fileDownloaded.replace(".yang", "_insta.xml");
		File xmlInstance = new File(xmlFolder+"\\"+xmlFile);
		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document cmdXml = docBuilder.parse(new InputSource(new ByteArrayInputStream(xml.getBytes(Charset.defaultCharset()))));
		
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		
		String delExp = "rpc/delete-config/target/*";
		
		XPathExpression delExpr = xpath.compile(delExp);
		
		Node temp = (Node)delExpr.evaluate(cmdXml, XPathConstants.NODE);
		String delete = temp.getNodeName();
		
		
		String rpcExp = "rpc";
		
		XPathExpression rpcExpr = xpath.compile(rpcExp);
		
		Node rpcNode = (Node)rpcExpr.evaluate(cmdXml, XPathConstants.NODE);
		Node msgIdAttNode = rpcNode.getAttributes().getNamedItem("message-id");
		
		
		System.out.println("Deleted: "+delete+" datastore");
		logger.info("Deleted: "+delete+" datastore");
		
		xmlInstance.delete();
		
		return "<rpc-reply "+msgIdAttNode+">\n</ok>\n</rpc-reply>";
	}
	
	static String copyConfigOperation(String xml) throws Exception
	{
		String xmlFile = fileDownloaded.replace(".yang", "_insta.xml");
		File xmlInstance = new File(xmlFolder+"\\"+xmlFile);
		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document cmdXml = docBuilder.parse(new InputSource(new ByteArrayInputStream(xml.getBytes(Charset.defaultCharset()))));
		
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		
		String fromExp = "rpc/copy-config/from/*";
		String toExp = "rpc/copy-config/to/*";
		
		XPathExpression fromExpr = xpath.compile(fromExp);
		XPathExpression toExpr = xpath.compile(toExp);
		
		Node temp = (Node)fromExpr.evaluate(cmdXml, XPathConstants.NODE);
		String from = temp.getNodeName();
		
		temp = (Node)toExpr.evaluate(cmdXml, XPathConstants.NODE);
		String to = temp.getNodeName();
		
		byte contentBytes[] = Files.readAllBytes(Paths.get(xmlInstance.getAbsolutePath()));
		String content = new String(contentBytes, Charset.defaultCharset());
		
		String newDataStorePath = xmlFolder+"\\"+fileDownloaded.replace(".yang", "_"+to+".xml");
		System.out.println(newDataStorePath);
		FileWriter fw = new FileWriter(newDataStorePath);
		fw.write(content);
		fw.close();
		
		
		String rpcExp = "rpc";
		
		XPathExpression rpcExpr = xpath.compile(rpcExp);
		
		Node rpcNode = (Node)rpcExpr.evaluate(cmdXml, XPathConstants.NODE);
		Node msgIdAttNode = rpcNode.getAttributes().getNamedItem("message-id");
		
		
		System.out.println("Copied from: "+from+" datastore to "+to+" datastore");
		logger.info("Copied from: "+from+" datastore to "+to+" datastore");
		
		return "<rpc-reply "+msgIdAttNode+">\n</ok>\n</rpc-reply>";
	}
	
	static String updateInsta(String updatedXml) throws Exception
	{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document newXml = docBuilder.parse(new InputSource(new ByteArrayInputStream(updatedXml.getBytes(Charset.defaultCharset()))));
		
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		
		String dataExp = "data";
		
		XPathExpression dataExpr = xpath.compile(dataExp);
		
		Node rpcNode = (Node)dataExpr.evaluate(newXml, XPathConstants.NODE);
		Node xmlnsAttNode = rpcNode.getAttributes().getNamedItem("xmlns");
		
		String xmlFile = fileDownloaded.replace(".yang", "_insta.xml");
		File xmlInstance = new File(xmlFolder+"\\"+xmlFile);

		FileWriter fw = new FileWriter(xmlInstance);
		fw.write(updatedXml);
		fw.close();
		
		return "<rpc-reply "+xmlnsAttNode+">\n</ok>\n</rpc-reply>";
	}
	
	static String editConfigRootOperation() throws Exception
	{
		String xmlFile = fileDownloaded.replace(".yang", "_insta.xml");
		File xmlInstance = new File(xmlFolder+"\\"+xmlFile);
		
		byte contentBytes[] = Files.readAllBytes(Paths.get(xmlInstance.getAbsolutePath()));
		String temp = new String(contentBytes, Charset.defaultCharset());
		
		return temp;
	}
	
	private static String editConfigOperation(String op, String xml) throws Exception
	{
		String content = "";
		boolean rootSelected = false;
		
		String xmlFile = fileDownloaded.replace(".yang", "_insta.xml");
		File xmlInstance = new File(xmlFolder+"\\"+xmlFile);
		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document cmdXml = docBuilder.parse(new InputSource(new ByteArrayInputStream(xml.getBytes(Charset.defaultCharset()))));
		
//		System.out.println(doc.getElementsByTagName(operation).item(0).getChildNodes().getLength());
		
		Node startNode = null;
		for(int i = 1; i < cmdXml.getElementsByTagName(op).item(0).getChildNodes().getLength(); i++)
		{
			Node child = cmdXml.getElementsByTagName(op).item(0).getChildNodes().item(i);
			if(!child.getNodeName().equals("#text") && !child.getNodeName().equals("source"))
			{
				startNode= child;
			}
		}
		
		if(startNode == null)
		{
			rootSelected = true;
		}

		if(rootSelected)
		{
			byte contentBytes[] = Files.readAllBytes(Paths.get(xmlInstance.getAbsolutePath()));
			String temp = new String(contentBytes, Charset.defaultCharset());
			return temp;
//			content.append(temp);
		}
		else
		{
			nodeList.add(startNode.getNodeName());
			recurserForBottomMostNode(startNode, startNode.getChildNodes());
			
			/*for(int i = 0; i < nodeList.size(); i++)
				System.out.println(nodeList.get(i));*/
			
			Document insta = docBuilder.parse(xmlInstance);
			
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			String exp = "data";
			
			for(int i = 0; i < nodeList.size(); i++)
				exp = exp + "/" + nodeList.get(i);

//			System.out.println("Effective exp is "+exp);
			
			String queryExp = "rpc/edit-config";
			for(int k = 0; k < nodeList.size(); k++)
				queryExp = queryExp  + "/" + nodeList.get(k);
			
//			System.out.println("Effective query exp is "+queryExp);
			
			XPathExpression expr = xpath.compile(exp);
			XPathExpression queryExpr = xpath.compile(queryExp);
			XPathExpression msgIdExpr = xpath.compile("/rpc[@message-id]/@message-id");
			
			NodeList sibNodes = (NodeList)expr.evaluate(insta, XPathConstants.NODESET);
			String newVal = queryExpr.evaluate(cmdXml, XPathConstants.STRING).toString().trim();
			
//			System.out.println("Queried for: "+newVal);
			
			String msgId = msgIdExpr.evaluate(cmdXml, XPathConstants.STRING).toString();
			System.out.println("Received rpc request ith message id : "+msgId);
			
//			System.out.println("It has "+sibNodes.getLength());
//			System.out.println("No of children: "+sibNodes.item(0).getChildNodes().getLength());
			
			if(sibNodes.getLength() == 1 && sibNodes.item(0) != null && sibNodes.item(0).getChildNodes().getLength() == 1)
			{
				Node curEle = sibNodes.item(0);
//				System.out.println("Name: "+curEle.getNodeName()+" Value: "+curEle.getTextContent());
				curEle.setTextContent(newVal);

				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(insta);
				StreamResult result = new StreamResult(xmlInstance);
				transformer.transform(source, result);
				
				content = "<rpc-reply message-id = \""+msgId+"\">\n</ok>\n</rpc-reply>";
				
//				System.out.println("Parent of "+curEle.getNodeName()+" is "+curEle.getParentNode().getNodeName());
//				System.out.println(sibNodes.item(0).getNodeName()+" : From "+curEle.getTextContent()+" to "+sibNodes.item(0).getTextContent());
			}
		}
		
		nodeList.clear();
		return content;
	}
	
	public String getConfigOperation(String op, String xml) throws Exception
	{
		boolean rootSelected = false;
		
		String xmlFile = fileDownloaded.replace(".yang", "_insta.xml");
		File xmlInstance = new File(xmlFolder+"\\"+xmlFile);
		
		StringBuilder content = new StringBuilder("");
		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document cmdXml = docBuilder.parse(new InputSource(new ByteArrayInputStream(xml.getBytes(Charset.defaultCharset()))));
		
//		System.out.println(doc.getElementsByTagName(operation).item(0).getChildNodes().getLength());
		
		Node startNode = null;
		for(int i = 1; i < cmdXml.getElementsByTagName(op).item(0).getChildNodes().getLength(); i++)
		{
			Node child = cmdXml.getElementsByTagName(op).item(0).getChildNodes().item(i);
			if(!child.getNodeName().equals("#text") && !child.getNodeName().equals("source"))
			{
				startNode= child;
			}
		}
		
		if(startNode == null)
		{
			rootSelected = true;
		}
		
		if(rootSelected)
		{
			byte contentBytes[] = Files.readAllBytes(Paths.get(xmlInstance.getAbsolutePath()));
			String temp = new String(contentBytes, Charset.defaultCharset());
			content.append(temp);
		}
		else
		{
			nodeList.add(startNode.getNodeName());
			recurserForBottomMostNode(startNode, startNode.getChildNodes());
			
			/*for(int i = 0; i < nodeList.size(); i++)
				System.out.println(nodeList.get(i));*/
			
			Document insta = docBuilder.parse(xmlInstance);
			
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			String exp = "data";
			
			for(int i = 0; i < nodeList.size(); i++)
				exp = exp + "/" + nodeList.get(i);
			
//			System.out.println("Effective exp is "+exp);
			
			String queryExp = "rpc/get-config";
			for(int k = 0; k < nodeList.size(); k++)
				queryExp = queryExp  + "/" + nodeList.get(k);
			
//			System.out.println("Effective query exp is "+queryExp);
			
			XPathExpression expr = xpath.compile(exp);
			XPathExpression queryExpr = xpath.compile(queryExp);
			XPathExpression msgIdExpr = xpath.compile("/rpc[@message-id]/@message-id");
			
			NodeList sibNodes = (NodeList)expr.evaluate(insta, XPathConstants.NODESET);
			String queryVal = queryExpr.evaluate(cmdXml, XPathConstants.STRING).toString().trim();
//			System.out.println("Queried for: "+queryVal);
			String msgId = msgIdExpr.evaluate(cmdXml, XPathConstants.STRING).toString();
			
			logger.info("Received rpc request with message id : "+msgId);
			
			
			String dataExp = "rpc";
			
			XPathExpression dataExpr = xpath.compile(dataExp);
			
			Node rpcNode = (Node)dataExpr.evaluate(cmdXml, XPathConstants.NODE);
			Node xmlnsAttNode = rpcNode.getAttributes().getNamedItem("xmlns");
			
			content.append("<rpc-reply message-id = \""+msgId+"\" "+xmlnsAttNode+">");
			
			for(int i = 0; i < sibNodes.getLength(); i++)
			{
//				System.out.println(sibNodes.item(i).getNodeName());
				boolean canWrapperTag = sibNodes.item(i).getFirstChild().getNodeValue().trim().equals("");
				String wrapperTag = sibNodes.item(i).getNodeName();
				
				Node sibling = sibNodes.item(i);
				NodeList children = sibling.getChildNodes();
				
				for(int j = 0; j < children.getLength(); j++)
				{
					Node child = children.item(j);
					if(child.getNodeType() == 1 && child.getTextContent().equals(queryVal))
					{
//						System.out.println(j+"th child: "+child.getNodeName()+" val: "+child.getTextContent()+" Query: "+queryVal);
						if(canWrapperTag)
							content.append("\n<"+wrapperTag+">");
						buildReplyForGet(content, sibNodes.item(i));
						if(canWrapperTag)
							content.append("\n</"+wrapperTag+">");
					}
					else if(child.getTextContent().equals(queryVal))
					{
						if(canWrapperTag)
							content.append("\n<"+wrapperTag+">");
						buildReplyForGet(content, sibNodes.item(i));
						if(canWrapperTag)
							content.append("\n</"+wrapperTag+">");
					}
				}
			}
		}
		
		content.append("\n</rpc-reply>");	
		
		nodeList.clear();
		return content.toString().trim();
	}
	
	public String getOperation(String op, String xml) throws Exception
	{
		String xmlFile = fileDownloaded.replace(".yang", "_insta.xml");
		File xmlInstance = new File(xmlFolder+"\\"+xmlFile);
		
		
		boolean rootSelected = false;
		StringBuilder content = new StringBuilder("");
		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document cmdXml = docBuilder.parse(new InputSource(new ByteArrayInputStream(xml.getBytes(Charset.defaultCharset()))));
		
//		System.out.println(doc.getElementsByTagName("get").item(0).getChildNodes().getLength());
		
		Node startNode = null;
		for(int i = 1; i < cmdXml.getElementsByTagName(op).item(0).getChildNodes().getLength(); i++)
		{
			Node child = cmdXml.getElementsByTagName(op).item(0).getChildNodes().item(i);
			if(!child.getNodeName().equals("#text") && !child.getNodeName().equals("source"))
			{
				startNode= child;
			}
		}
		
		if(startNode == null)
			rootSelected = true;

		if(rootSelected)
		{
			byte contentBytes[] = Files.readAllBytes(Paths.get(xmlInstance.getAbsolutePath()));//scan.useDelimiter("\\A").next();
			String temp = new String(contentBytes, Charset.defaultCharset());
			content.append(temp);
		}
		else
		{
			nodeList.add(startNode.getNodeName());
			recurserForBottomMostNode(startNode, startNode.getChildNodes());
			
			/*for(int i = 0; i < nodeList.size(); i++)
				System.out.println(nodeList.get(i));*/
			
			Document insta = docBuilder.parse(xmlInstance);
			
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			String exp = "data";
			
			for(int i = 0; i < nodeList.size(); i++)
				exp = exp + "/" + nodeList.get(i);
			
//			System.out.println("Effective exp is "+exp);
			
			XPathExpression expr = xpath.compile(exp);
			XPathExpression msgIdExpr = xpath.compile("/rpc[@message-id]/@message-id");
			
			NodeList childNodes = (NodeList)expr.evaluate(insta, XPathConstants.NODESET);
			String msgId = msgIdExpr.evaluate(cmdXml, XPathConstants.STRING).toString();
			
			logger.info("Received rpc request with message id :  "+msgId);
			
			
			String dataExp = "rpc";
			
			XPathExpression dataExpr = xpath.compile(dataExp);
			
			Node rpcNode = (Node)dataExpr.evaluate(cmdXml, XPathConstants.NODE);
			Node xmlnsAttNode = rpcNode.getAttributes().getNamedItem("xmlns");
			
			content.append("<rpc-reply message-id = \""+msgId+"\" "+xmlnsAttNode+">");
			
			
			for(int i = 0; i < childNodes.getLength(); i++)
			{
				boolean canWrapperTag = childNodes.item(i).getFirstChild().getNodeValue().trim().equals("");
				String wrapperTag = childNodes.item(i).getNodeName();
				
//				System.out.println(canWrapperTag);
//				System.out.println("child name : "+childNodes.item(i).getFirstChild().getNodeValue());
				
				
				
				if(!childNodes.item(i).getNodeName().equals("#text"))
				{
					if(canWrapperTag)
							content.append("\n<"+wrapperTag+">");
					buildReplyForGet(content, childNodes.item(i));
					if(canWrapperTag)
							content.append("\n</"+wrapperTag+">");
				}
			}
		}
		content.append("\n</rpc-reply>");
		
		nodeList.clear();
		return content.toString().trim();
	}
	
	public static void buildReplyForGet(StringBuilder content, Node node)
	{
		if(node.hasChildNodes())
		{
			for(int i = 0; i < node.getChildNodes().getLength(); i++)
			{
				String parentName = null;
				Node n = node.removeChild(node.getFirstChild());
				String childVal = n.getNodeValue();
				parentName = node.getNodeName();
//				System.out.println(node.getNodeName()+" child val is "+childVal);
				
				if(childVal != null && !n.getNodeValue().trim().equals(""))
				{
					content.append("\n<"+parentName+">"+childVal+"</"+parentName+">");
					buildReplyForGet(content, node);
				}
				else if(childVal == null)
				{
					buildReplyForGet(content, n);
				}
				else
				{
//					System.out.println("Fn called with "+node.getNodeName());
					buildReplyForGet(content, node);
				}
			}
		}
		else if(node.getNodeValue() != null && !node.getNodeValue().trim().equals(""))
			content.append("\n"+node.getNodeValue());
	}
	
	public static void recurserForBottomMostNode(Node node, NodeList nl)
	{
		for(int i = 0; i < nl.getLength(); i++)
		{
			Node n = nl.item(i);
			if(!n.getNodeName().equals("#text"))
			{
				nodeList.add(n.getNodeName());
				recurserForBottomMostNode(n, n.getChildNodes());
			}
		}
	}
	
	public static void main(String arg[])
	{
		try
		{
			NetConfElement p = new NetConfElement();
			Registry reg = LocateRegistry.createRegistry(3232);
			//System.setProperty("java.rmi.server.hostname","192.168.206.1");
			//Naming.rebind("Cal",p);
			reg.rebind("Cal",p);
		}
		catch(Exception e)
		{
			logger.info("Problem at server : "+e.getClass().getSimpleName()+" : "+e.getMessage());
			System.out.println("Exception occurred : "+e.getMessage());
		}
	}
}