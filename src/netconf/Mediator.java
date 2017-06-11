package netconf;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Mediator {

	String ipHost;
	int portNum;
	
	String sendClicked(String operation, String commandXml)
	{
		String rpcReply = null;
		Registry reg;
		try 
		{
			reg = LocateRegistry.getRegistry(this.ipHost,this.portNum);
			OnConnectInterface p = (OnConnectInterface)reg.lookup("Cal");
			
			NetConfBrowser.logger.info("Command XML sent to "+ipHost);
			
			rpcReply = p.rpcRequest(operation, commandXml);
			
			NetConfBrowser.logger.info("Reply sent by "+ipHost);
			
//			if(rpcReply.isEmpty())
//				NetConfBrowser.raiseError("Operation cannot be performed on that node.", "Select another node");
		} 
		catch(RemoteException re) {
			NetConfBrowser.logger.info("Could not connect to "+ipHost);
			NetConfBrowser.raiseError("Please check your network connectivity!", re.getClass().getSimpleName());
		}
		
		catch (Exception e) {
			// TODO Auto-generated catch block
			NetConfBrowser.raiseError(e.getMessage(), e.getClass().getSimpleName());
//			e.printStackTrace();
		}
		return rpcReply;
	}
}