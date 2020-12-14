package tp_pd_server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class SendMulticastInfo extends Thread
{
    private MulticastSocket mcSocket = null;
    private InetAddress mcGroup = null;
    private int mcPort = 0;
    private boolean running = true;
    
    private ObjectOutputStream out = null;
    private ByteArrayOutputStream bout = null;
    
    private MulticastInformation myInfo = null;
    
    public SendMulticastInfo(MulticastSocket s, InetAddress g, int p)
    {
        this.mcSocket = s;
        this.mcGroup = g;
        this.mcPort = p;    
    }
    
    @Override
    public void run()
    {
        try
        {
            mcSocket.joinGroup(mcGroup);
        }
        catch(IOException e) {}
        
        try
        {
            bout = new ByteArrayOutputStream();
            out = new ObjectOutputStream(bout);
        }
        catch(IOException e) {}
        
        MulticastInformation lastInfo = null;
        
        while(running)
        {
            if(myInfo == null)
            {
                try
                {
                    sleep(1*1000);  //sleep for a sec, try again
                }
                catch(InterruptedException e)  {}
                
                continue;
            }
            try
            {
                if(lastInfo == myInfo)  //No updated information to send
                    Thread.sleep(5 * 1000); //sleep 5s before ping
                else
                    lastInfo = myInfo;
                
                out.writeObject(myInfo);
                int len = bout.toByteArray().length;
                DatagramPacket mcPkt = new DatagramPacket(bout.toByteArray(),len, mcGroup, mcPort);
                mcSocket.send(mcPkt);
                System.out.println("Sending packet from SendMulticastInfo");
            }
            catch(IOException | InterruptedException e) {}
        }
    }
    
    public void getMyInfo(MulticastInformation i) { this.myInfo = i; }
    
    public void terminate() { running = false; }
}
