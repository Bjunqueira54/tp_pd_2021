package tp_pd_server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class MulticastWrapper extends Thread
{
    private static final int MULTI_PORT = 5432;
    private static final int BUFFER_SIZE = 4096;
    
    private MulticastSocket mcSocket = null;
    private InetAddress mcGroup = null;
    private DatagramPacket mcPkt = null;
    private boolean running = true;
    
    private ByteArrayOutputStream bout = null;
    private ByteArrayInputStream bin = null;
    
    private ObjectOutputStream out = null;
    private ObjectInputStream in = null;
    
    private List<MulticastInformation> list = null;
    
    @Override
    public synchronized void start()
    {
        try
        {
            mcSocket = new MulticastSocket(MULTI_PORT);
            mcGroup = InetAddress.getByName("224.0.0.3");
            mcSocket.setNetworkInterface(NetworkInterface.getByInetAddress(InetAddress.getLocalHost()));
            
            list = new ArrayList();
            bout = new ByteArrayOutputStream();
            out = new ObjectOutputStream(bout);
        }
        catch(UnknownHostException e)
        {
            System.out.println("Can't connect to Multicast Group");
        }
        catch(IOException e) {}
        
        super.start();
    }
    
    public SendMulticastInfo startSendInfo()
    {
        SendMulticastInfo s = new SendMulticastInfo(mcSocket, mcGroup, MULTI_PORT);
        s.start();
        return s;
    }

    @Override
    public void run()
    {
        //write your thread code here
        try
        {
            mcSocket.joinGroup(mcGroup);
        }
        catch(IOException e) {}
        
        mcPkt = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
        
        while(running)
        {
            System.out.println("A espera de um packet em MulticastWrapper");
            try
            {
                mcSocket.receive(mcPkt);
                System.out.println("Recebi um packet em MulticastWrapper");
                bin = new ByteArrayInputStream(mcPkt.getData());
                in = new ObjectInputStream(bin);
                
                Object obj = in.readObject();
                
                if(obj instanceof MulticastInformation)
                    catchServerUpdate((MulticastInformation) obj);
            }
            catch(IOException | ClassNotFoundException e) {}
            
            clearTimeouts();
            
            System.out.println("Atualmente tenho " + list.size() + " servidores na lista");
        }
        
        try
        {
            mcSocket.leaveGroup(mcGroup);
            mcSocket.close();
        }
        catch(IOException e) {}
    }

    public void terminate() { this.running = false; }
    
    public boolean idExists(int myId)
    {
        for(MulticastInformation it: list)
            if(it.getServerId() == myId)
                return true;
        
        return false;
    }
    
    private void clearTimeouts()
    {
        Date currentTime = GregorianCalendar.getInstance().getTime();
            
            for(int i = 0; i < list.size(); i++)
            {
                if(currentTime.getTime() - list.get(i).getLastPing() > (10 * 1000))
                    list.remove(i);
            }
    }
    
    private void catchServerUpdate(MulticastInformation info)
    {
        boolean found = false;
        
        info.setLastPing(GregorianCalendar.getInstance().getTime().getTime());

        for(int i = 0; i < list.size(); i++)
        {
            if(list.get(i).getServerId() == info.getServerId())
            {
                list.set(i, info);  //Just update the info
                found = true;
                break;
            }
        }

        if(!found) list.add(info);  //If not found, add new server to list
    }
    
    public int getLeastBusyServer()
    {
        int min = 9999999;
        
        for(MulticastInformation it: list)
            if(it.getnClients() < min)
                min = it.getnClients();
        
        return min;
    }
    
    public byte[] getServerList()
    {
        byte[] sv_list = new byte[list.size()];
        
        for(int i = 0; i < list.size(); i++)
            sv_list[i] = (byte) list.get(i).getPortUDP();
        
        return sv_list;
    }

    public boolean getIsRunning()
    {
        return this.running;
    }
}