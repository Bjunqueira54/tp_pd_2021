package tp_pd_server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.sql.*;

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
    private ServerSocket tcp_socket = null;
    
    private final SendMulticastInfo sendInfo;
    
    public static class pdSQLConnectionManager
    {
        private static final int TIMEOUT = 10000; //10 seconds
        private static final int TABLE_ENTRY_TIMEOUT = 60000; //60 seconds

        private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
        private static final String GET_WORKERS_QUERY = "SELECT * FROM pi_workers;";
        
        private static Connection conn = null;
        private static Statement stmt = null, stmt2;
        private static ResultSet rs = null;

        public static void connectToDatabase(int port)
        {
            String dbName = "TP_PD_" + port;
            
            System.out.println("DB Name is: " + dbName);
            
            System.out.println("Trying to connect to DB");
            try
            {
                Class.forName(JDBC_DRIVER);
                System.out.println("Trying to connect to DB...");
                String dbConnectionString = "jdbc:mysql://localhost/" + dbName + "?user=root&password=123456";
                conn = DriverManager.getConnection(dbConnectionString);
                System.out.println("Connection success!");
            }
            catch(ClassNotFoundException e)
            {
                System.out.println("ClassNotFoundException!");
            }
            catch(SQLException e)
            {
                System.out.println("SQLException!");
                try
                {
                    conn = DriverManager.getConnection("jdbc:mysql://localhost/?user=root&password=123456");
                    stmt = conn.createStatement();
                    stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
                }
                catch(SQLException e1) 
                {
                    System.out.println("2nd SQLException. Are you sure the server is online?");
                }
            }
            finally
            {
                try
                {
                    conn = DriverManager.getConnection("jdbc:mysql://localhost/" + dbName + "?user=root&password=123456");
                }
                catch(SQLException e)
                {
                    System.out.println("SQLException inside finally{}... wtf are you doing?");
                }
            }
            
            checkTables();
        }
        
        private static void checkTables()
        {
            try
            {
                stmt  = conn.createStatement();
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS clients(\n" +
                                        "username VARCHAR(6) PRIMARY KEY,\n" +
                                        "lastHost VARCHAR(30),\n" +
                                        "port int(6),\n" +
                                        "isConnected BOOLEAN)");
            }
            catch(SQLException e)
            {
                System.out.println("SQLException on checkTables");
            }
        }
    }

    public Server(SendMulticastInfo si, ServerSocket ss)
    {
        this.sendInfo = si;
        this.tcp_socket = ss;
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
        
        {
            System.out.println("Exception opening tcp socket... Server.run() methods exceptio");
        }
        
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
        
        SV_ID = SV_PORT - 5000;
        
        
        while(mcThread.idExists(SV_ID))
        {
            SV_ID++;
        }
        
        MulticastInformation info = null;
        SendMulticastInfo sendInfo = null;
        
        try
        {
            info = new MulticastInformation(SV_ID, SV_PORT, InetAddress.getLocalHost().getCanonicalHostName(), nClients);
            sendInfo = mcThread.startSendInfo();  //Handle para a thread interior.
            sendInfo.getMyInfo(info);
        }
        catch(UnknownHostException e) {}
        
        pdSQLConnectionManager.connectToDatabase(SV_PORT);
        
        while(true)
        {
            try
            {
                udp_pkt = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);

                udp_socket.receive(udp_pkt);

                String cl_message = new String(udp_pkt.getData(), 0, udp_pkt.getLength());

                if(cl_message.compareTo(INIT_CONNECTION) == 0 && !(mcThread.getLeastBusyServer() < (int) (nClients / 2.0)))
                {
                        udp_pkt = new DatagramPacket(CONNECTION_ACCEPTED.getBytes(), CONNECTION_ACCEPTED.length(), udp_pkt.getAddress(), udp_pkt.getPort());
                        udp_socket.send(udp_pkt);
                        
                        ServerSocket sock = null;
                        
                        try
                        {
                            sock = new ServerSocket(0);
                        }
                        catch(IOException e)
                        {
                            System.out.println("Exception in creating a TCP socket.");
                        }
                        
                        String tcp_Port = Integer.toString(sock.getLocalPort());
                        
                        udp_pkt = new DatagramPacket(tcp_Port.getBytes(), tcp_Port.getBytes().length, udp_pkt.getAddress(), udp_pkt.getPort());
                        udp_socket.send(udp_pkt);
                        
                        Server s = new Server(sendInfo, sock);
                        s.start();
                        client_thread_list.add(s);
                }
                else
                {
                    //just refuse and send a list
                    udp_pkt = new DatagramPacket(CONNECTION_REFUSED.getBytes(), CONNECTION_REFUSED.length(), udp_pkt.getAddress(), udp_pkt.getPort());
                    udp_socket.send(udp_pkt);
                    
                    try
                    {
                        Thread.sleep(100);
                    }
                    catch(InterruptedException e) {}
                    
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