import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TClient extends Thread {
    private RemoteTServer _srv;
    private RemoteMailBox _mailbox;
    private boolean _connected;
    private String _ip;

    public TClient(String ip, int port) {
        _srv = null;
        _connected = false;

        try
        {
            _ip = InetAddress.getLocalHost().getHostAddress();
        }
        catch (Exception e)
        {
            _ip = null;
            log(e.getMessage());
        }

        if(_ip != null)
            if(connect(ip, port))
            {
                log("Connected to " + ip + ":" + port);
                _connected = true;
            }
            else
            {
                //disconnect();
                log("Connection to " + ip + ":" + port + " failed!");
            }
    }

    private boolean connect(String ip, int port) {
        boolean success;

        try
        {
            Registry r = LocateRegistry.getRegistry(ip, port);
            _srv = (RemoteTServer)r.lookup("TServer");
            success = (_mailbox = _srv.connect(_ip)) != null;
        }
        catch (Exception e)
        {
            success = false;
            log(e.getMessage());
        }

        return success;
    }
    private void disconnect() {
        if(_srv != null)
        {
            try
            {
                _srv.disconnect(_ip);
            }
            catch (Exception e)
            {
                log(e.getMessage());
            }
        }

        _srv = null;
        _connected = false;
        _mailbox = null;
    }

    private boolean processCommand(String cmd) {
        boolean done = false;
        String response;

        log("Processing command - '" + cmd + "'");

        if(cmd.equalsIgnoreCase("terminate"))
        {
            response = "Command processed successfully";
            done = true;
        }
        else
        {
            try
            {
                Process p = Runtime.getRuntime().exec("cmd /c " + cmd);
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                StringBuilder sb = new StringBuilder();
                String s;

                while ((s = stdInput.readLine()) != null)
                    sb.append(s);

                while ((s = stdError.readLine()) != null)
                    sb.append(s);

                response = sb.toString();
            }
            catch (Exception e)
            {
                response = e.getMessage();
                log(e.getMessage());
            }
        }

        try
        {
            if(!response.equals(""))
            {
                log(response);
                _mailbox.putResponse(response);
            }
        }
        catch (Exception e)
        {
            log(e.getMessage());
        }

        return done;
    }

    public void run() {
        if(_connected)
        {
            log("Running...");

            while (_connected)
            {
                try
                {
                    log("Waiting for command");
                    if(processCommand(_mailbox.getCommand()))
                        break;
                }
                catch (Exception e)
                {
                    log(e.getMessage());
                    break;
                }
            }

            log("Disconnecting...");
            disconnect();
        }
    }

    private void log(String s) {
        System.out.println("Client: " + s);
    }

    public static void main(String[] args) {
        try
        {
            new TClient(args[0], Integer.parseInt(args[1])).start();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}
