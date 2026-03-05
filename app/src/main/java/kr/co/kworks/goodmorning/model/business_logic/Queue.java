package kr.co.kworks.goodmorning.model.business_logic;

public class Queue {
    private Node<Float> front;
    private Node<Float> rear;
    private int count;
    private int max_size;
    private float sum;
    private Tune tune;

    public interface Tune {
        float logic(float value);
    }

    public Queue() {
        front = rear = null;
        count = 0;
        max_size = -1;
        sum = 0f;
    }

    public Queue(int size) {
        front = rear = null;
        count = 0;
        sum = 0f;
        setSize(size);
    }

    public boolean isQueueEmpty() {
        return (front==null && rear==null) || count==0;
    }

    public void push(float value) {
        Node<Float> node = new Node<>();
        node.setValue(value);

        if(isQueueEmpty()) {
            front = node;
            rear = node;
            count++;
            sum += (float) value;
            return ;
        }

        // tuning for value that be pushed
        if(max_size != -1 && getCount() == max_size) {
            if(tune != null) {
                value = tune.logic(value);
            }
        }

        if(max_size != -1 && max_size == count) {
            pop();
        }

        node.setValue(value);
        rear.setNextNode(node);
        rear = node;
        sum += (float) value;
        count++;
    }

    public void setSize(int size) {
        this.max_size = size;
    }

    public int getCount() {
        return count;
    }

    public Node<Float> pop() {
        Node<Float> popNode = front;
        if(popNode == null) {
            front = null;
            rear = null;
            count = 0;
            sum = 0f;
            return null;
        }
        front = front.getNextNode();
        count--;
        sum -= (float) popNode.getValue();
        return popNode;
    }

    public float getSum() {
        return sum;
    }

    public float getAverage() {
        return sum/count;
    }

    public void setTune(Tune tune) {
        this.tune = tune;
    }

    public Node<Float> getRear() {
        return rear;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        StringBuilder sb = new StringBuilder();
        sb.append("max_size: ");
        if(max_size==-1) sb.append("∞");
        else sb.append(max_size);
        sb.append("\n");
        sb.append("count: ");
        sb.append(count);
        sb.append("\n");
        sb.append("[queue]");

        Node<Float> node = front;
        if(node == null) System.out.println("node == null");
        while(true) {
            if(node == null) break;
            sb.append(node.getValue() + " ");
            node = node.getNextNode();
        }

        return sb.toString();
    }
}
