package tp_pd_server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Server extends Thread
{
    private static final int BUFFER_SIZE = 4096;   //4kb should be more than enough... right?
    private static int SV_PORT = 5001;
    
    public static void main(String[] args)
    {
        DatagramSocket udp_socket = null;
        DatagramPacket udp_pkt;
        
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
        
        SendMulticastInfo sendInfo = mcThread.startSendInfo();  //Handle para a thread interior.
        
        try
        {
            udp_pkt = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
            
            udp_socket.receive(udp_pkt);
            
            String cl_message = new String(udp_pkt.getData());
            
            System.out.println(cl_message);
            
            String reply = "Hello client";
            
            udp_pkt = new DatagramPacket(reply.getBytes(), reply.length(), udp_pkt.getAddress(), udp_pkt.getPort());
            
            udp_socket.send(udp_pkt);
        }
        catch(SocketException e)
        {
            System.out.println("SocketException! Possible error binding to port!");
        }
        catch(IOException e) {}
    }
}