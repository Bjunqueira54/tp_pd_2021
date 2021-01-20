package myComm;

import java.io.Serializable;

public class LogRegPack implements Serializable
{
    static final long serialVersionUID = 1L;
    
    private final String username;
    private final String password;
    private final int type; //1 login, 2 register
    
    public LogRegPack(String u, String p, int t)
    {
        this.username = u;
        this.password = p;
        this.type = t;
    }
    
    public String getUsername() { return this.username; }
    public String getPassword() { return this.password; }
    public int getType() { return this.type; }
}