package tp_pd_server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Server extends Thread
{
    private static final int BUFFER_SIZE = 4096;   //4kb should be more than enough... right?
    private static final String INIT_CONNECTION = "HELLO";
    private static final String CONNECTION_REFUSED = "REFUSED";
    private static final String CONNECTION_ACCEPTED = "ACCEPTED";
    
    private static int SV_PORT = 5001;
    private static int SV_ID;
    private static int nClients = 0;
    
    private boolean running = true;
    
    private final SendMulticastInfo sendInfo;

    public Server(SendMulticastInfo si)
    {
        this.sendInfo = si;
    }
    
    @Override
    public void run()
    {
        //Whenever new client connects.
        //Write code here
        
        if(this.sendInfo == null)
            return;
        
        try
        {
            MulticastInformation info = new MulticastInformation(SV_ID, SV_PORT, InetAddress.getLocalHost().getCanonicalHostName(), nClients);
            sendInfo.getMyInfo(info);
        }
        catch(UnknownHostException e) {}
        
        while(running)
        {
            //Do Server <-> Client stuff here   
        }
    }
    
    private void terminate() { this.running = false; }
    
    public static void main(String[] args)
    {
        DatagramSocket udp_socket = null;
        DatagramPacket udp_pkt;
        List<Thread> client_thread_list = new ArrayList();
        
        while(true)
        {
          try
          {
            udp_socket = new DatagramSocket(SV_PORT);
            break;
          }
          catch(SocketException e) { SV_PORT++; }
        }
        
        if(udp_socket == null)
            return;
        
        MulticastWrapper mcThread = new MulticastWrapper(); //Handle para a Thread.
        mcThread.start();
        
        do
        {
            SV_ID++;
        }
        while(mcThread.idExists(SV_ID));
        
        MulticastInformation info = null;
        SendMulticastInfo sendInfo = null;
        
        try
        {
            info = new MulticastInformation(SV_ID, SV_PORT, InetAddress.getLocalHost().getCanonicalHostName(), nClients);
            sendInfo = mcThread.startSendInfo();  //Handle para a thread interior.
            sendInfo.getMyInfo(info);
        }
        catch(UnknownHostException e) {}
        
        while(true)
        {
            try
            {
                udp_pkt = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);

                udp_socket.receive(udp_pkt);

                String cl_message = new String(udp_pkt.getData());

                if(cl_message.compareTo(INIT_CONNECTION) == 0 && !(mcThread.getLeastBusyServer() < (int) (nClients / 2.0)))
                {
                        udp_pkt = new DatagramPacket(CONNECTION_ACCEPTED.getBytes(), CONNECTION_ACCEPTED.length(), udp_pkt.getAddress(), udp_pkt.getPort());
                        udp_socket.send(udp_pkt);
                        
                        Server s = new Server(sendInfo);
                        s.start();
                        client_thread_list.add(s);
                }
                else
                {
                    //just refuse and send a list
                    udp_pkt = new DatagramPacket(CONNECTION_REFUSED.getBytes(), CONNECTION_REFUSED.length(), udp_pkt.getAddress(), udp_pkt.getPort());
                    udp_socket.send(udp_pkt);
                    udp_pkt = new DatagramPacket(mcThread.getServerList(), mcThread.getServerList().length, udp_pkt.getAddress(), udp_pkt.getPort());
                    udp_socket.send(udp_pkt);
                }
            }
            catch(SocketException e)
            {
                System.out.println("SocketException! Possible error binding to port!");
            }
            catch(IOException e) {}
            
            if(!mcThread.getIsRunning())
                break;
        }
        
        for(Iterator<Thread> it = client_thread_list.listIterator(); it.hasNext(); it.next())
        {
            Server temp = (Server) it.next();
            
            temp.terminate();
            
            it.remove();
        }
        
        mcThread.terminate();
        sendInfo.terminate();
    }
}