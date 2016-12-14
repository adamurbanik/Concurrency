import java.rmi.RemoteException;

public class MailBox implements RemoteMailBox {
    private String _ip;
    private String _cmd;
    private String _res;

    private boolean _cmdLock, _resLock;

    public MailBox(String ip) {
        _resLock = _cmdLock = false;
        _ip = ip;
    }

    synchronized public void putCommand(String cmd) throws RemoteException, InterruptedException {
        while (_cmd != null || _cmdLock) wait();
        _cmdLock = true;
        _cmd = cmd;
        _cmdLock = false;
        notifyAll();
    }
    synchronized public String getCommand() throws RemoteException, InterruptedException {
        while (_cmd == null || _cmdLock) wait();
        _cmdLock = true;
        String cmd = _cmd;
        _cmd = null;
        _cmdLock = false;
        notifyAll();
        return cmd;
    }
    synchronized public void putResponse(String res) throws RemoteException, InterruptedException {
        while (_res != null || _resLock) wait();
        _resLock = true;
        _res = res;
        _resLock = false;
        notifyAll();
    }
    synchronized public String getResponse() throws RemoteException, InterruptedException {
        while (_res == null || _resLock) wait();
        _resLock = true;
        String res = _res;
        _res = null;
        _resLock = false;
        notifyAll();
        return res;
    }

    public String toString() {
        return _ip;
    }
    public boolean equals(Object o) {
        return o instanceof MailBox && ((MailBox)o)._ip.equals(_ip);
    }
}
