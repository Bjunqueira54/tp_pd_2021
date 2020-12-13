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
    
    public static void main(String[] args)
    {
        Scanner sc = new Scanner(System.in);
        DatagramSocket sv_socket_udp;
        DatagramPacket sv_pkt_udp;
        
        ////////////////////////////////////
        
        System.out.print("Server IP: ");
        String sv_ip = sc.next();
        System.out.print("Server Port: ");
        int sv_port = sc.nextInt();
        
        if(sv_ip.length() < 7 || sv_port <= 0)
        {
            System.out.println("Invalid IP or Port.");
            return;
        }
        
        try
        {
            sv_socket_udp = new DatagramSocket();
            
            String msg = "Hello server!";
            int msg_len = msg.length();
            sv_pkt_udp = new DatagramPacket(msg.getBytes(), msg_len, InetAddress.getByName(sv_ip), sv_port);
            sv_socket_udp.send(sv_pkt_udp);
            
            byte[] buffer = new byte[BUFFER_SIZE];

            sv_pkt_udp = new DatagramPacket(buffer, BUFFER_SIZE);
            
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