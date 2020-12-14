package tp_pd_client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client
{
    private static final int BUFFER_SIZE = 4096;   //4kb should be more than enough... right?
    private static final int SV_PORT = 5001;    //There will always be a server here
    
    public static void main(String[] args)
    {
        Scanner sc = new Scanner(System.in);
        DatagramSocket sv_socket_udp;
        DatagramPacket sv_pkt_udp;
        
        ////////////////////////////////////
        
        System.out.print("Server IP: ");
        String sv_ip = sc.next();
        
        if(sv_ip.length() < 7)
        {
            System.out.println("Invalid IP format?");
            return;
        }
        
        try
        {
            sv_socket_udp = new DatagramSocket();
            
            String msg = "Hello server!";
            int msg_len = msg.length();
            sv_pkt_udp = new DatagramPacket(msg.getBytes(), msg_len, InetAddress.getByName(sv_ip), SV_PORT);
            sv_socket_udp.send(sv_pkt_udp);

            sv_pkt_udp = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
            
            //Recycle the packet
            sv_socket_udp.receive(sv_pkt_udp);
            
            String reply = new String(sv_pkt_udp.getData());
            
            System.out.println(reply);
        }
        catch(SocketException e)
        {
            System.out.println("SocketException! Couldn't connect to socket. Check server port and try again!");
        }
        catch(UnknownHostException e)
        {
            System.out.println("UnknownHostException! Check the IP and try again!");
        }
        catch(IOException e) {}
    }
}