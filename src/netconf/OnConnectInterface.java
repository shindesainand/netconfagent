package netconf;

import java.rmi.*;
import java.util.Vector;

public interface OnConnectInterface extends Remote{
	
	public Vector<Vector<String>> getModules() throws RemoteException;
	public String downloadModules(int index) throws RemoteException;
	public String rpcRequest(String operation, String command) throws Exception;
}