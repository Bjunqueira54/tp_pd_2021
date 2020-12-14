package tp_pd_server;

import java.io.Serializable;
import java.util.Date;

public class MulticastInformation implements Serializable
{
    static final long serialVersionUID = 1L;
    
    protected int serverId;
    
    //port UDP do server
    protected int portUDP;
    
    //IP do server
    protected String INET_ADDR;
    
    //nº de clientes no server
    protected int nClients;
    
    //mensagem a enviar a um cliente
    protected Msg msg;
    protected String ficheiroEnv;
    
    //se uma Msg foi enviada corretamente e segura
    protected boolean isSafe;
    protected myClient cliente;
    
    private long lastPing = 0;
    
    public MulticastInformation(int serverId,int portUDP, String INET_ADDR, int nClients)
    {
        this.serverId = serverId;
        this.INET_ADDR = INET_ADDR;
        this.portUDP = portUDP;
        this.nClients = nClients;
    }

    public MulticastInformation() {}
    
    public int getServerId() { return serverId; }
    public void setServerId(int serverId) { this.serverId = serverId; }
    public int getPortUDP() { return portUDP; }
    public void setPortUDP(int portUDP) { this.portUDP = portUDP; }
    public String getINET_ADDR() { return INET_ADDR; }
    public void setINET_ADDR(String INET_ADDR) { this.INET_ADDR = INET_ADDR; }
    public int getnClients() { return nClients; }
    public void setnClients(int nClients) { this.nClients = nClients; }
    public Msg getMsg() { return msg; }
    public void setMsg(Msg msg) { this.msg = msg; }
    public String getFicheiroEnv() { return ficheiroEnv; }
    public void setFicheiroEnv(String ficheiroEnv) { this.ficheiroEnv = ficheiroEnv; }
    public boolean isIsSafe() { return isSafe; }
    public void setIsSafe(boolean isSafe) { this.isSafe = isSafe; }
    public myClient getClient() { return cliente; }
    public void setClient(myClient cliente) { this.cliente = cliente; }
    public void setLastPing(long d) { this.lastPing = d; }
    public long getLastPing() { return this.lastPing; }
}