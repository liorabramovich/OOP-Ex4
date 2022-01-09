package com.impl;

import com.api.DirectedWeightedGraph;
import com.api.EdgeData;
import com.api.NodeData;
import com.json.JsonEdgeData;
import com.json.JsonModel;
import com.json.JsonNodeData;
import com.json.JsonUtilities;

import java.io.FileNotFoundException;
import java.util.*;

import static com.json.JsonUtilities.getJsonModelFromString;

public class DirectedWeightedGraphImpl implements DirectedWeightedGraph {

    private final HashMap<Integer, HashMap<Integer, EdgeData>> mEdges = new HashMap<>();
    private final HashMap<Integer, NodeData> mNodes = new HashMap<>();
    private int mMc = 0;
    private int mEdgeSize = 0;

    private Integer mLastNodeListMc = null;
    private CustomList<NodeData> mNodeDataList = new CustomList<>();

    private Integer mLastEdgeListMc;
    private CustomList<EdgeData> mEdgeDataList = new CustomList<>();

    public DirectedWeightedGraphImpl(String fileName) throws FileNotFoundException {
        JsonModel jsonModel = JsonUtilities.getJsonModelFromFile(fileName);
        for (JsonNodeData nodeData : jsonModel.Nodes) {
            addNode(new NodeDataImpl(nodeData.id, nodeData.pos));
        }
        for (JsonEdgeData edgeData : jsonModel.Edges) {
            connect(edgeData.src, edgeData.dest, edgeData.w);
        }
    }

    public DirectedWeightedGraphImpl() {
    }

    public DirectedWeightedGraphImpl(String data, boolean fromGui) {
        JsonModel jsonModel = getJsonModelFromString(data);
        for (JsonNodeData nodeData : jsonModel.Nodes) {
            addNode(new NodeDataImpl(nodeData.id, nodeData.pos));
        }
        for (JsonEdgeData edgeData : jsonModel.Edges) {
            connect(edgeData.src, edgeData.dest, edgeData.w);
        }
    }

    @Override
    public NodeData getNode(int key) {
        if (mNodes.containsKey(key)) {
            return mNodes.get(key);
        }
        return null;
    }

    @Override
    public EdgeData getEdge(int src, int dest) {
        if (mNodes.containsKey(src) && mNodes.containsKey(dest) && src != dest && mEdges.get(src).get(dest) != null) {
            return mEdges.get(src).get(dest);
        }
        return null;
    }

    @Override
    public void addNode(NodeData n) {
        if (n != null && !mNodes.containsKey(n.getKey())) {
            mNodes.put(n.getKey(), n);
            mEdges.put(n.getKey(), new HashMap<>());
            mMc++;
        }
    }

    @Override
    public void connect(int src, int dest, double w) {
        if (src != dest && w >= 0 && mNodes.containsKey(src) && mNodes.containsKey(dest)) {
            if (getEdge(src, dest) == null) {
                mEdgeSize++;
                mMc++;
                mEdges.get(src).put(dest, new EdgeDataImpl(src, dest, w));
            }
        }
    }

    @Override
    public Iterator<NodeData> nodeIter() {
        if (mLastNodeListMc == null || mLastNodeListMc != getMC()) {
            mNodeDataList = new CustomList<>();
            for (NodeData nodeData : mNodes.values()) {
                mNodeDataList.add(nodeData);
            }
        }
        return mNodeDataList.iterator();
    }

    @Override
    public Iterator<EdgeData> edgeIter() {
        if (mLastEdgeListMc == null || mLastEdgeListMc != getMC()) {
            mEdgeDataList = new CustomList<>();
            for (HashMap<Integer, EdgeData> edges : mEdges.values()) {
                for (EdgeData edgeData : edges.values()) {
                    mEdgeDataList.add(edgeData);
                }
            }
        }
        return mEdgeDataList.iterator();
    }

    @Override
    public Iterator<EdgeData> edgeIter(int node_id) {
        CustomList<EdgeData> edgeDataList = new CustomList<>();
        if (mNodes.containsKey(node_id) && mEdges.containsKey(node_id)) {
            for (EdgeData edgeData : mEdges.get(node_id).values()) {
                edgeDataList.add(edgeData);
            }
        }
        return edgeDataList.iterator();
    }

    @Override
    public NodeData removeNode(int key) {
        if (!mNodes.containsKey(key)) {
            return null;
        }
        if (edgeIter(key) != null) {
            Iterator<NodeData> nodeDataIterator = nodeIter();
            while (nodeDataIterator.hasNext()) {
                NodeData nodeData = nodeDataIterator.next();
                if (mEdges.get(nodeData.getKey()) != null && mEdges.get(nodeData.getKey()).containsKey(key)) {
                    mEdges.get(nodeData.getKey()).remove(key);
                    mEdgeSize--;
                }

            }
        }
        int count = mEdges.get(key).size();
        mEdges.remove(key);
        mEdgeSize -= count;
        return mNodes.remove(key);
    }

    @Override
    public EdgeData removeEdge(int src, int dest) {
        if (mNodes.containsKey(src) && mNodes.containsKey(dest) && getEdge(src, dest) != null) {
            mEdgeSize--;
            mMc++;
            return mEdges.get(src).remove(dest);
        }
        return null;
    }

    @Override
    public int nodeSize() {
        return mNodes.size();
    }

    @Override
    public int edgeSize() {
        return mEdgeSize;
    }

    @Override
    public int getMC() {
        return mMc;
    }


    public class CustomList<T> implements Iterable<T> {
        Node<T> head, tail;


        // add new Element at tail of the linked list in O(1)
        public synchronized void add(T data) {
            Node<T> node = new Node<>(data, null);
            if (head == null)
                tail = head = node;
            else {
                tail.setNext(node);
                tail = node;
            }
        }

        // return Head
        public Node<T> getHead() {
            return head;
        }


        // return Tail
        public Node<T> getTail() {
            return tail;
        }

        // return Iterator instance
        public Iterator<T> iterator() {
            return new ListIterator<T>(this, mMc);
        }

    }

    class ListIterator<T> implements Iterator<T> {
        Node<T> current;
        private final int mLastMc;

        // initialize pointer to head of the list for iteration
        public ListIterator(CustomList<T> list, int lstMc) {
            current = list.getHead();
            mLastMc = lstMc;
        }

        // returns false if next element does not exist
        public boolean hasNext() {
            return current != null;
        }

        // return current data and update pointer
        public T next() {
            if (mLastMc != getMC()) {
                throw new RuntimeException("Graph was changed");
            }
            T data = current.getData();
            current = current.getNext();
            return data;
        }

        // implement if needed
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    // Constituent Node of Linked List
    class Node<T> {
        T data;
        Node<T> next;

        public Node(T data, Node<T> next) {
            this.data = data;
            this.next = next;
        }

        // Setter getter methods for Data and Next Pointer
        public void setData(T data) {
            this.data = data;
        }

        public void setNext(Node<T> next) {
            this.next = next;
        }

        public T getData() {
            return data;
        }

        public Node<T> getNext() {
            return next;
        }
    }
}
