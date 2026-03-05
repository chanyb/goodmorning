package kr.co.kworks.goodmorning.model.business_logic;

public class Node<T> {

    private T value;

    public void setValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    private Node<T> nextNode;

    public Node() {
    }

    public Node<T> nextItem() {
        return nextNode;
    }

    public void setNextNode(Node<T> nextNode) {
        this.nextNode = nextNode;
    }

    public Node<T> getNextNode() {
        return nextNode;
    }
}
