import java.net.InetAddress;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.Observable;

public class TServer extends Observable implements RemoteTServer {
    private Registry _r;
    private RemoteTServer _stub;

    private MailingList _list;

    public TServer() {
        _list = new MailingList();
    }

    public boolean listen(String port) {
        boolean success;

        try
        {
            int p = Integer.parseInt(port);
            _stub = (RemoteTServer) UnicastRemoteObject.exportObject(this, 0);
            _r = LocateRegistry.createRegistry(p);
            _r.rebind("TServer", _stub);
            log("Listening on " + InetAddress.getLocalHost().getHostAddress() + ":" + p);
            _list.clear();
            setChanged();
            notifyObservers(new TMsg(TMsg.TYPE_LISTEN));
            success = true;
        }
        catch (Exception e)
        {
            success = false;
            shutdown();
            log("Error occurred! Not listening!");
            log(e.getMessage());
        }

        return success;
    }
    public void shutdown() {
        try
        {
            if(_stub != null)
                UnicastRemoteObject.unexportObject(this, true);

            if(_r != null)
            {
                _r.unbind("TServer");
                UnicastRemoteObject.unexportObject(_r, true);
                log("Stopped listening.");
            }

            setChanged();
            notifyObservers(new TMsg(TMsg.TYPE_SHUTDOWN));
        }
        catch (Exception e)
        {
            log(e.getMessage());
        }
    }

    public RemoteMailBox connect(String ip) {
        final MailBox m = new MailBox(ip);

        if(_list.add(m))
        {
            log("Connection from " + ip + " accepted");
            setChanged();
            notifyObservers(new TMsg(TMsg.TYPE_CONNECT, ip));

            new Thread(new Runnable() {
                public void run() {
                    while(_list.contains(m))
                    {
                        try
                        {
                            String r = m + " >> " + m.getResponse();
                            setChanged();
                            notifyObservers(new TMsg(TMsg.TYPE_RESPONSE, r));
                        }
                        catch (Exception e)
                        {
                            log(e.getMessage());
                        }
                    }
                }
            }).start();

            RemoteMailBox rmb = null;

            try
            {
                rmb = (RemoteMailBox)UnicastRemoteObject.exportObject(m, 0);
            }
            catch (Exception e)
            {
                log(e.getMessage());
            }

            return rmb;
        }

        log("Connection from " + ip + " rejected");
        return null;
    }
    public void disconnect(String ip) {
        log("Disconnecting client from " + ip);
        setChanged();
        notifyObservers(new TMsg(TMsg.TYPE_DISCONNECT, ip));

        MailBox m = _list.remove(ip);

        if(m != null)
        {
            try
            {
                UnicastRemoteObject.unexportObject(m, true);
            }
            catch (Exception e)
            {
                log(e.getMessage());
            }
        }
    }

    public void putCmd(String ip, String cmd) {
        MailBox m = _list.get(ip);

        if(m != null)
        {
            try
            {
                m.putCommand(cmd);
                setChanged();
                notifyObservers(new TMsg(TMsg.TYPE_RESPONSE, m + " << " + cmd));
            }
            catch (Exception e)
            {
                log(e.getMessage());
            }
        }
    }

    protected void log(String s) {
        System.out.println("Server: " + s);
        setChanged();
        notifyObservers(new TMsg(TMsg.TYPE_MESSAGE, s));
    }

    public static void main(String[] args) {
        try
        {
            new TServer().listen(args[0]);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}
