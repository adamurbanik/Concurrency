import java.util.Vector;

public class MailingList extends Vector<MailBox> {
    public MailBox get(String ip) {
        for(MailBox m : this)
            if(m.toString().equals(ip))
                return m;

        return null;
    }

    public boolean add(MailBox mailBox) {
        return !contains(mailBox) && super.add(mailBox);
    }

    public MailBox remove(String ip) {
        MailBox mb = get(ip);

        if(mb != null)
            remove(mb);

        return mb;
    }
}
