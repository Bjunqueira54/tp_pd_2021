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
import java.util.List;

public class MulticastWrapper extends Thread
{
    private static final int MULTI_PORT = 1337;
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
            mcGroup = InetAddress.getByName("localhost");
            mcSocket.setNetworkInterface(NetworkInterface.getByInetAddress(mcGroup));
            
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
            try
            {
                mcSocket.receive(mcPkt);
                
                bin = new ByteArrayInputStream(mcPkt.getData());
                in = new ObjectInputStream(bin);
                
                MulticastInformation info = (MulticastInformation) in.readObject();
                boolean found = false;
                
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
            catch(IOException | ClassNotFoundException e) {}
        }
        
        try
        {
            mcSocket.leaveGroup(mcGroup);
            mcSocket.close();
        }
        catch(IOException e) {}
    }

    public void terminate() { this.running = false; }
}