import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

public class GUI extends JFrame implements Observer {
    private TServer _srv;
    private DefaultListModel _clients;

    private JTextArea _console;
    private JTextArea _response;

    public GUI(TServer srv) {
        // Attach itself to listen for events
        _srv = srv;
        _srv.addObserver(this);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Client list
        _clients = new DefaultListModel();
        final JList clients = new JList(_clients);
        clients.setPreferredSize(new Dimension(120, 250));
        clients.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        getContentPane().add(clients, BorderLayout.WEST);

        // Console output
        _console = new JTextArea("", 6, 40);
        _console.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        _console.setEditable(false);
        ((DefaultCaret)_console.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane scrollConsole = new JScrollPane(_console);
        scrollConsole.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        getContentPane().add(scrollConsole, BorderLayout.NORTH);

        // Server controls
        JPanel srvMan = new JPanel();
        final JTextField port = new JTextField();
        port.setPreferredSize(new Dimension(100, 20));
        final JButton btnStart = new JButton("Start Server");
        final JButton btnStop = new JButton("Stop Server");
        btnStop.setEnabled(false);
        btnStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(_srv.listen(port.getText()))
                {
                    port.setEnabled(false);
                    btnStop.setEnabled(true);
                    btnStart.setEnabled(false);
                }
            }
        });
        btnStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _srv.shutdown();
                port.setEnabled(true);
                btnStop.setEnabled(false);
                btnStart.setEnabled(true);
            }
        });
        srvMan.add(port);
        srvMan.add(btnStart);
        srvMan.add(btnStop);
        getContentPane().add(srvMan, BorderLayout.CENTER);

        // Commands
        JPanel cmds = new JPanel();
        cmds.setLayout(new BoxLayout(cmds, BoxLayout.Y_AXIS));
        _response = new JTextArea("", 6, 40);
        _response.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        _response.setEditable(false);
        ((DefaultCaret)_console.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        scrollConsole = new JScrollPane(_response);
        scrollConsole.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        cmds.add(scrollConsole);
        JPanel c = new JPanel();
        final JTextField cmd = new JTextField();
        cmd.setPreferredSize(new Dimension(550, 20));
        JButton submit = new JButton("Send");
        submit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String command = cmd.getText();
                Object ip = clients.getSelectedValue();

                if(!command.equals("") && ip != null)
                {
                    _srv.putCmd(ip.toString(), command);
                    cmd.setText("");
                }
            }
        });
        c.add(cmd);
        c.add(submit);
        cmds.add(c);
        getContentPane().add(cmds, BorderLayout.SOUTH);

        pack();
        setVisible(true);
    }

    public void update(Observable o, Object arg) {
        TMsg msg = (TMsg)arg;
        switch (msg.getType())
        {
            case TMsg.TYPE_CONNECT:
                _clients.addElement(msg.getContent());
                break;

            case TMsg.TYPE_DISCONNECT:
                _clients.removeElement(msg.getContent());
                break;

            case TMsg.TYPE_MESSAGE:
                _console.append(msg.getContent() + "\n");
                _console.setCaretPosition(_console.getDocument().getLength());
                break;

            case TMsg.TYPE_SHUTDOWN:
                _clients.clear();
                break;

            case TMsg.TYPE_RESPONSE:
                _response.append(msg.getContent() + "\n");
                _response.setCaretPosition(_response.getDocument().getLength());
                break;
        }
    }

    public static void main(String[] args) {
        new GUI(new TServer());
    }
}
