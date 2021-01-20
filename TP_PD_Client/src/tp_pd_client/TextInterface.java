package tp_pd_client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.Naming;
import java.util.Scanner;
import myComm.LogRegPack;
import myComm.ServerRMI;

public class TextInterface
{
    private final Scanner sc;
    
    private Socket sock = null;
    private ObjectOutputStream oos = null;
    private ObjectInputStream ios = null;
    private svState state = svState.UNNAUTHORIZED;
    
    private ServerRMI sv_rmi_interface = null;
    
    private enum svState
    {
        UNNAUTHORIZED,
        LOGIN,
        REGISTER,
        AUTHORIZED
    }
    
    public TextInterface(Socket s)
    {
        this.sock = s;
        this.sc = new Scanner(System.in);
    }
    
    public void run()
    {
        try
        {
            oos = new ObjectOutputStream(sock.getOutputStream());
            ios = new ObjectInputStream(sock.getInputStream());
        }
        catch(IOException e) {}
        
        //Get RMI interface
        
        try
        {
            String query = "RMI";
            oos.writeObject(query);
            oos.flush();
            int rmi_id = ios.readInt();
            String rmi_url = "rmi://localhost/TP_PD_Server_" + rmi_id;
            
            sv_rmi_interface = (ServerRMI) Naming.lookup(rmi_url);
        }
        catch(Exception e) { e.printStackTrace();}
        
        
        char option = 0;
        
        while(true)
        {
            printMenu();
            option = sc.nextLine().charAt(0);
            
            if(option == '0') return;
            
            procOption(option);
        }
    }
    
    private void procOption(char o)
    {
        switch(o)
        {
            case '1':
                state = svState.LOGIN;
                loginOrRegister(1);
                break;
            case '2':
                state = svState.REGISTER;
                loginOrRegister(2);
                break;
            default:
                break;
        }
    }
    
    private void loginOrRegister(int type)
    {
        //1 for login
        //2 for register
        //same code for both funtions, all that changes is how to communicate it with the server
        
        System.out.print("Username: ");
        String username = sc.nextLine();
        System.out.print("Password: ");
        String password = sc.nextLine();
        
        boolean res = false;
        /*myComm.LogRegPack pack = new LogRegPack(username, password, type);
        
        try
        {
            oos.writeObject(pack);
            oos.flush();
            res = ios.readBoolean();
        }
        catch(IOException e) {}*/
        
        try
        {
            res = (type == 1) ? sv_rmi_interface.login(username, password) : sv_rmi_interface.register(username, password);
        }
        catch(Exception e) { e.printStackTrace(); }
        
        if(type == 1 && res)
        {
            System.out.println("Login Successful!");
            return;
        }
        else if(type == 2 && res)
        {
            System.out.println("Registering completed");
            return;
        }
        else
            System.out.println("The operation failed.");
        
        this.state = svState.UNNAUTHORIZED;
    }
    
    private void printMenu()
    {
        switch(state)
        {
            case UNNAUTHORIZED:
                printMainMenu();
                break;
            case AUTHORIZED:
                break;
            default:
        }
    }
    
    private void printMainMenu()
    {
        System.out.println("1 - Login");
        System.out.println("2 - Register");
        System.out.println("0 - Exit");
    }
    
    private void printLogin()
    {
        System.out.println("Username: ");
    }
    
    private void printRegister()
    {
        System.out.println("Register a new account");
        
    }
}