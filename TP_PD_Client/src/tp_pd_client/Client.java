package tp_pd_client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client
{
    private static final int BUFFER_SIZE = 4096;   //4kb should be more than enough... right?
    private static int SV_PORT = 5001;    //There will always be a server here
    private static final String INIT_CONNECTION = "HELLO";
    private static final String CONNECTION_REFUSED = "REFUSED";
    private static final String CONNECTION_ACCEPTED = "ACCEPTED";
    
    public static void main(String[] args)
    {
        Scanner sc = new Scanner(System.in);
        DatagramSocket sv_socket_udp;
        DatagramPacket sv_pkt_udp;
        
        Socket sv_socket_tcp = null;
        
        ////////////////////////////////////
        
        System.out.print("Server IP: ");
        String sv_ip = sc.next();
        
        if(sv_ip.length() < 7)
        {
            System.out.println("Invalid IP format?");
            return;
        }

        int msg_len = INIT_CONNECTION.length();

        while(true)
        {
            try
            {
                sv_socket_udp = new DatagramSocket();
                sv_pkt_udp = new DatagramPacket(INIT_CONNECTION.getBytes(), msg_len, InetAddress.getByName(sv_ip), SV_PORT);
                sv_socket_udp.send(sv_pkt_udp);
                break;
            }
            catch(IOException e)
            {
                SV_PORT++;
            }
        }
        
        while(true)
        {
            sv_pkt_udp = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);

            try
            {
                //Recycle the packet
                sv_socket_udp.receive(sv_pkt_udp);

                String reply = new String(sv_pkt_udp.getData());

                if(reply.compareToIgnoreCase(CONNECTION_ACCEPTED) == 0)
                {
                    System.out.println("Connection Accepted");
                    sv_pkt_udp = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
                    sv_socket_udp.receive(sv_pkt_udp);
                    
                    String tcp_port = new String(sv_pkt_udp.getData());
                    
                    sv_socket_tcp = new Socket(InetAddress.getByName(sv_ip), Integer.parseUnsignedInt(tcp_port));
                    break;
                }
                else if(reply.compareToIgnoreCase(CONNECTION_REFUSED) == 0)
                {
                    System.out.println("Connection Refused");
                    sv_pkt_udp = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
                    sv_socket_udp.receive(sv_pkt_udp);
                }
            }
            catch(IOException e) {}
        }
        
        //Do Server <-> Client stuff here
        try
        {
            sv_socket_tcp.close();
        }
        catch(IOException e) {}
    }
}