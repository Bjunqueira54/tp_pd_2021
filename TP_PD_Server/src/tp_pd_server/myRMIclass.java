package tp_pd_server;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class myRMIclass extends UnicastRemoteObject implements myComm.ServerRMI
{
    private int rmi_id;
    
    public myRMIclass(int id) throws RemoteException
    {
        this.rmi_id = id;
    }
    
    public void registerRMI()
    {
        try
        {
            try
            {
                LocateRegistry.createRegistry(Registry.REGISTRY_PORT);               
            }
            catch(RemoteException e) { e.printStackTrace(); }            
            
            Naming.bind("rmi://localhost/TP_PD_Server_" + rmi_id, this);
        }
        catch(Exception e) { e.printStackTrace(); }
    }
    
    public void unregisterRMI()
    {
        try
        {
            UnicastRemoteObject.unexportObject(this, true);
        }
        catch(Exception e) { e.printStackTrace(); }
    }
    
    @Override
    public boolean login(String u, String p) throws RemoteException
    {
        return Server.pdSQLConnectionManager.login(u, p);
    }

    @Override
    public boolean register(String u, String p) throws RemoteException
    {
        return Server.pdSQLConnectionManager.register(u, p);
    }
    
    @Override
    public byte[] getFile(String filename) throws RemoteException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}