import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteMailBox extends Remote {
    public void putCommand(String cmd) throws RemoteException, InterruptedException;
    public String getCommand() throws RemoteException, InterruptedException;
    public void putResponse(String res) throws RemoteException, InterruptedException;
    public String getResponse() throws RemoteException, InterruptedException;
}
