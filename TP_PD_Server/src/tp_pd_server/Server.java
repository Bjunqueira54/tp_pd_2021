package tp_pd_server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Server extends Thread
{
    private static final int BUFFER_SIZE = 4096;   //4kb should be more than enough... right?
    
    public static void main(String[] args)
    {
        DatagramSocket udp_socket;
        DatagramPacket udp_pkt;
        byte[] buffer = new byte[BUFFER_SIZE];
        
        try
        {
            udp_socket = new DatagramSocket(1337);
            udp_pkt = new DatagramPacket(buffer, BUFFER_SIZE);
            
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