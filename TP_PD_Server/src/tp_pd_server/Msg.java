package tp_pd_server;

import java.io.Serializable;

class Msg implements Serializable
{
    static final long serialVersionUID = 1L;

    protected String nickname;
    protected String msg;

    public Msg(String nickname, String msg)
    {
            this.nickname = nickname;
            this.msg = msg;
    }

    public String getNickname(){ return nickname;}

    public String getMsg(){ return msg;}	
        
}