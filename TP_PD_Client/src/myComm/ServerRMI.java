package myComm;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerRMI extends Remote
{
    public boolean login(String u, String p) throws RemoteException;
    public boolean register(String u, String p) throws RemoteException;
    public byte[] getFile(String filename) throws RemoteException;
}