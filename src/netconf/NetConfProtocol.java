package netconf;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

class NetConfProtocol {

	DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	static String namespaceVal;
	
	public String get(String jtreePath, File xmlFile, boolean leaf)
	{
		int cut = 1;
		StringBuilder content = new StringBuilder("<rpc message-id = \"101\" xmlns = \"");
		DocumentBuilder docBuilder;
		Document doc = null;
		try {
			docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.parse(xmlFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		namespaceVal = getAttributeValue(doc, "module", "xmlns");
//		System.out.println(namespaceVal);
		content.append(namespaceVal+"\">\n");
		content.append("<get>\n\t<source>\n\t\t<running/>\n\t</source>\n");
		
		if(leaf)	cut = 2;
		String path = jtreePath.substring(1, jtreePath.length() - 1);
		String tagNames[] = path.split(", ");
		StringBuilder temp = new StringBuilder("");
		for(int i = 1; i <= tagNames.length - cut; i++)
		{
			temp.append("<"+tagNames[i]+">\n");
		}
		//temp.append("Enter any value from the XML instance for which you want the config\n");
		for(int i = tagNames.length - cut; i > 0; i--)
		{
			temp.append("</"+tagNames[i]+">\n");
		}
		
		content.append(temp);
		content.append("</get>\n</rpc>");
//		System.out.println(content);
		return content.toString();
	}
	
	public String get_config(String jtreePath, File xmlFile, boolean leaf)
	{
		int cut = 1;
		StringBuilder content = new StringBuilder("<rpc message-id = \"101\" xmlns=\"");
		DocumentBuilder docBuilder;
		Document doc = null;
		try {
			docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.parse(xmlFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		content.append(getAttributeValue(doc, "module", "xmlns")+"\">\n");
		content.append("<get-config>\n\t<source>\n\t\t<running/>\n\t</source>\n");
		
		if(leaf)	cut = 2;
		String path = jtreePath.substring(1, jtreePath.length() - 1);
		String tagNames[] = path.split(", ");
		StringBuilder temp = new StringBuilder("");
		for(int i = 1; i <= tagNames.length - cut; i++)
		{
			temp.append("<"+tagNames[i]+">\n");
		}
		temp.append("Enter any value from the XML instance for which you want the config\n");
		for(int i = tagNames.length - cut; i > 0; i--)
		{
			temp.append("</"+tagNames[i]+">\n");
		}
		
		content.append(temp);
		content.append("</get-config>\n</rpc>");
//		System.out.println(content);
		return content.toString();
	}
	
	public String copy_config()
	{
		StringBuilder content = new StringBuilder("<rpc message-id = \"101\">\n");
		content.append("<copy-config>\n\t<from>\n\t   <startup/>\n\t</from>\n\t<to>\n\t   <running/>\n\t</to>\n</copy-config>\n</rpc>");
		
		return content.toString();
	}
	
	public String edit_config(String jtreePath, File xmlFile, boolean leaf)
	{
		/*Sends a get-config request to the server and displays the resultant instance XML so that the user
		can edit that XML and send the modified config back to the server*/
		
		int cut = 1;
		StringBuilder content = new StringBuilder("<rpc message-id = \"101\" xmlns=\"");
		DocumentBuilder docBuilder;
		Document doc = null;
		try {
			docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.parse(xmlFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		content.append(getAttributeValue(doc, "module", "xmlns")+"\">\n");
		content.append("<edit-config>\n\t<source>\n\t\t<running/>\n\t</source>\n");
		
		if(leaf)	cut = 2;
		String path = jtreePath.substring(1, jtreePath.length() - 1);
		String tagNames[] = path.split(", ");
		StringBuilder temp = new StringBuilder("");
		for(int i = 1; i <= tagNames.length - cut; i++)
		{
			temp.append("<"+tagNames[i]+">\n");
		}
		temp.append("Enter the new value here\n");
		for(int i = tagNames.length - cut; i > 0; i--)
		{
			temp.append("</"+tagNames[i]+">\n");
		}
		
		content.append(temp);
		content.append("</edit-config>\n</rpc>");
//		System.out.println(content);
		return content.toString();
	}
	
	public String delete_config()
	{
		StringBuilder content = new StringBuilder("<rpc message-id = \"101\">\n");
		content.append("<delete-config>\n\t<target>\n\t\t<running/>\n\t</target>\n</delete-config>\n</rpc>");
		
		return content.toString();
	}
	
	public String lock()
	{
		StringBuilder content = new StringBuilder("<rpc message-id = \"101\">\n");
		content.append("<lock>\n\t<target>\n\t\t<running/>\n\t</target>\n</lock>\n</rpc>");
		
		return content.toString();
	}
	
	public String unlock()
	{
		StringBuilder content = new StringBuilder("<rpc message-id = \"101\">\n");
		content.append("<unlock>\n\t<target>\n\t\t<running/>\n\t</target>\n</unlock>\n</rpc>");
		
		return content.toString();
	}
	
	static String getAttributeValue(Document doc, String nodeName, String attName)
	{
		String attVal = null;
		Node module = doc.getElementsByTagName(nodeName).item(0);
		Node temp = module.hasAttributes() ? module.getAttributes().getNamedItem(attName) : null;
		if(temp != null)
		{
			Element hello = (Element)module;
			String xmlns = hello.getAttribute(attName);
			attVal = xmlns;
		}
		return attVal;
	}
}