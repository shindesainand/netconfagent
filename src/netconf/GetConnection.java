package netconf;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Vector;
import java.util.logging.Logger;

public class GetConnection {
	
	Vector<Vector<String>> vec;
	String ipHost;
	int portNum;
	Registry reg;
	Logger logger = Logger.getLogger("LOGGER");
	
	/*void disConnect()
	{
		try {
			this.ipHost = null;
			this.portNum = 0;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			NetConfBrowser.raiseError("Could not disconnect from Network Element.", e.getClass().getSimpleName());
			e.printStackTrace();
		}
	}*/
	
	void connectToNE(String ipHost, int portNum)
	{
		// TODO Auto-generated method stub
		this.ipHost = ipHost;
		this.portNum = portNum;
		try
		{
			logger.info("Trying to connect to "+ipHost+":"+portNum+"...");

			reg = LocateRegistry.getRegistry(ipHost,portNum);
			OnConnectInterface p = (OnConnectInterface)reg.lookup("Cal");
			
			logger.info("Connected to "+ipHost);
			
			vec = new Vector<Vector<String>>(p.getModules());
			
			//System.out.println("Modules are : \n"+vec+"\nSize is "+vec.elementAt(0).size()+"\n2 is "+vec.elementAt(0).elementAt(2));

			for(int i = 0; i < vec.elementAt(0).size(); i++)
			{
				Vector<String> temp = new Vector<String>();
				temp.addElement(vec.elementAt(0).elementAt(i));
				NetConfBrowser.rowData.add(i, temp);
				logger.info(NetConfBrowser.rowData.elementAt(i).firstElement()+" was addded"+System.lineSeparator());
			}
		}
		catch(Exception e)
		{
			logger.info("Connection refused to "+ipHost);
			NetConfBrowser.raiseError(e.getMessage(), e.getClass().getSimpleName());
			System.out.println("Exception occurred in GetConnection"+e.getMessage());
		}
	}
	
	public String downloadClicked(int index)
	{
		String yangMod = new String();
		Registry reg;
		try {
			reg = LocateRegistry.getRegistry(this.ipHost,this.portNum);
			OnConnectInterface p = (OnConnectInterface)reg.lookup("Cal");
			yangMod = p.downloadModules(index);
			//System.out.println("Yang file displayed: \n"+yang);
		} catch (RemoteException | NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return(yangMod);
	}
}