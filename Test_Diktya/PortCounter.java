public class PortCounter {
    private int port;
    private int counter;

    public PortCounter(int port, int counter) {
        this.port = port;
        this.counter = counter;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public void incrementCounter() {
        this.counter++;
    }

    @Override
    public String toString() {
        return "Port: " + port + ", Counter: " + counter;
    }
}
