import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteTServer extends Remote {
    public RemoteMailBox connect(String ip) throws RemoteException;
    public void disconnect(String ip) throws RemoteException;
}
