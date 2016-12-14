public class TMsg {
    public static final int TYPE_LISTEN = 0;
    public static final int TYPE_SHUTDOWN = 1;
    public static final int TYPE_MESSAGE = 2;
    public static final int TYPE_CONNECT = 3;
    public static final int TYPE_DISCONNECT = 4;
    public static final int TYPE_RESPONSE = 5;

    private int _type;
    private Object _content;

    public TMsg(int type) {
        this(type, null);
    }
    public TMsg(int type, Object content) {
        _type = type;
        _content = content;
    }

    public int getType() {
        return _type;
    }
    public Object getContent() {
        return _content;
    }
}
