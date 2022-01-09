package com.impl;

import com.api.*;
import com.google.gson.Gson;
import com.json.JsonEdgeData;
import com.json.JsonModel;
import com.json.JsonNodeData;

import java.io.FileWriter;
import java.util.*;

import static com.impl.NodeDataImpl.*;

public class DirectedWeightedGraphAlgorithmsImpl implements DirectedWeightedGraphAlgorithms {

    private DirectedWeightedGraph mGraph;
    //private WarshalAlgo mFloydWarshal;

    @Override
    public void init(DirectedWeightedGraph g) {
        mGraph = g;
//        mFloydWarshal = new WarshalAlgo(g);
    }

    @Override
    public DirectedWeightedGraph getGraph() {
        return mGraph;
    }

    @Override
    public DirectedWeightedGraph copy() {
        DirectedWeightedGraph deepCopyGraph = new DirectedWeightedGraphImpl();
        Iterator<NodeData> nodeDataIterator = mGraph.nodeIter();
        while (nodeDataIterator.hasNext()) {
            NodeData nodeData = nodeDataIterator.next();
            GeoLocation geoLocation = nodeData.getLocation();
            deepCopyGraph.addNode(new NodeDataImpl(nodeData.getKey(), nodeData.getTag(), nodeData.getInfo(), nodeData.getWeight(), new GeoLocationImpl(geoLocation.x(), geoLocation.y(), geoLocation.z())));
        }
        Iterator<EdgeData> edgeDataIterator = mGraph.edgeIter();
        while (edgeDataIterator.hasNext()) {
            EdgeData edgeData = edgeDataIterator.next();
            deepCopyGraph.connect(edgeData.getSrc(), edgeData.getDest(), edgeData.getWeight());
        }
        return deepCopyGraph;
    }

    @Override
    public boolean isConnected() {
        if (mGraph == null || mGraph.nodeSize() <= 1) {
            return true;
        }
        Iterator<NodeData> nodeDataIterator = mGraph.nodeIter();
        while (nodeDataIterator.hasNext()) {
            NodeData node = nodeDataIterator.next();
            if (mGraph.edgeIter(node.getKey()) == null || getCounterIterator(mGraph.edgeIter(node.getKey())) == 0) {
                return false;
            }
        }
        nodeDataIterator = mGraph.nodeIter();
        bfs(mGraph.getNode(nodeDataIterator.next().getKey()));
        nodeDataIterator = mGraph.nodeIter();
        while (nodeDataIterator.hasNext()) {
            NodeData node = nodeDataIterator.next();
            if (!node.getInfo().equals(BLACK_COLOR)) {
                return false;
            }
        }
        return true;
    }

    private int getCounterIterator(Iterator data) {
        int counter = 0;
        Iterator it = data;
        while (data.hasNext()) {
            Object i = it.next();
            counter++;
        }
        return counter;
    }

    @Override
    public double shortestPathDist(int src, int dest) {

//        mFloydWarshal.performFloydWarshalAlgoIfNeeded();
//        double distWeight = mFloydWarshal.getWeight(new SrcDest(src, dest));
//        return distWeight != Double.MAX_VALUE ? distWeight : -1;
        NodeData source = mGraph.getNode(src);
        NodeData destination = mGraph.getNode(dest);
        if (source != null && destination != null) {
            Dijkstra(source);
            if (destination.getWeight() < Double.POSITIVE_INFINITY) {
                return destination.getWeight();
            }
        }
        return -1;
    }

    @Override
    public List<NodeData> shortestPath(int src, int dest) {
        List<NodeData> path = new ArrayList<NodeData>();
        //Return the source if both of the src and the dest are equals.
        if (src == dest) {
            path.add(mGraph.getNode(src));
            return path;
        }

        if (shortestPathDist(src, dest) > -1) {
            NodeData destination = mGraph.getNode(dest);
            String str = destination.getInfo();
            String arr[] = str.split("-");
            for (String temp : arr) {
                if (isNumeric(temp)) {
                    int key = Integer.valueOf(temp);
                    path.add(mGraph.getNode(key));
                }
            }
            path.add(destination);
            return path;
        }
        return null;
    }

    private static boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


//        mFloydWarshal.performFloydWarshalAlgoIfNeeded();
//        return mFloydWarshal.getShortethPathList(new SrcDest(src, dest));


    @Override
    public NodeData center() {
        if (!isConnected()) {
            return null;
        }
        double shortest_dist = Integer.MAX_VALUE;
        NodeData ans = null;
        Iterator<NodeData> iter = mGraph.nodeIter();
        while (iter.hasNext()) {
            NodeData n = iter.next();
            Dijkstra(n);
            Iterator<NodeData> iter2 = mGraph.nodeIter();
            double max_dist = -1;
            while (iter2.hasNext()) {
                NodeData v = iter2.next();
                if (v.getWeight() > max_dist) {
                    max_dist = v.getWeight();
                }
            }
            if (max_dist < shortest_dist) {
                shortest_dist = max_dist;
                ans = n;
            }
        }
        return ans;
    }




    private HashSet<NodeData> visited = new HashSet<>();


    Comparator<NodeData> comparator = (n1, n2) -> (int) (n1.getWeight() - n2.getWeight());

    private void Dijkstra(NodeData source) {
        visited.clear();
        Iterator<NodeData> nodeDataIter = mGraph.nodeIter();
        while (nodeDataIter.hasNext()) {
            NodeData temp = nodeDataIter.next();
            temp.setInfo(null);
            temp.setWeight(Double.POSITIVE_INFINITY);
        }
        source.setWeight(0);
        PriorityQueue<NodeData> pq = new PriorityQueue<>(mGraph.nodeSize(), comparator);
        pq.add(source);
        while (!pq.isEmpty()) {
            NodeData current = pq.poll();
            if (!visited.contains(current)) {
                visited.add(current);
                Iterator<EdgeData> edgeData = mGraph.edgeIter(current.getKey());
                while (edgeData.hasNext()) {
                    NodeData temp = mGraph.getNode(edgeData.next().getDest());
                    if (!visited.contains(temp)) {
                        double distance = current.getWeight() + mGraph.getEdge(current.getKey(), temp.getKey()).getWeight();
                        if (distance < temp.getWeight()) {
                            temp.setWeight(distance);
                            temp.setInfo(current.getInfo() + "-" + current.getKey() + "-");
                            pq.add(temp);
                        }
                    }
                }

            }
        }
    }


    @Override
    public List<NodeData> tsp(List<NodeData> cities) {
        if (!isConnected()) {
            return null;
        }
        List<NodeData> result = new ArrayList<>();
        List<NodeData> tmpList = new ArrayList<>();
        double currentDist = Double.MAX_VALUE;
        int dest = 0;
        int from = runFirstIteration(cities);
        int i = 0;
        while (!cities.isEmpty()) {
            double minDist = Double.MAX_VALUE;
            for (NodeData city : cities) {
                if (!result.isEmpty()) {
                    from = result.get(result.size() - 1).getKey();
                } else {
                    if (i > 0) {
                        from = cities.get(i).getKey();
                    }
                }
                currentDist = shortestPathDist(from, city.getKey());
                if (currentDist < minDist && currentDist != 0) {
                    minDist = currentDist;
                    tmpList = shortestPath(from, city.getKey());
                    dest = city.getKey();
                }
            }
            if (i == 0) {
                cities = removeDestFromList(cities, from);

            }
            i++;
            result = copyArrToOtherList(result, tmpList);
            cities = removeDestFromList(cities, dest);
        }
        return result;
    }

    public List<NodeData> copyArrToOtherList(List<NodeData> res, List<NodeData> tmp) {
        for (int i = 0; i < tmp.size(); i++) {
            if (res.isEmpty() || i != 0) {
                res.add(tmp.get(i));
            }
        }
        return res;
    }

    public List<NodeData> removeDestFromList(List<NodeData> targets, int dest) {
        for (int i = 0; i < targets.size(); i++) {
            if (targets.get(i).getKey() == dest) {
                targets.remove(i);
            }
        }
        return targets;
    }

    private int runFirstIteration(List<NodeData> targets) {
        double currentDist = Double.MAX_VALUE;
        double minPath = Double.MAX_VALUE;
        int vertex = 0;
        for (int i = 0; i < targets.size(); i++) {
            for (int j = 0; j < targets.size(); j++) {
                currentDist = shortestPathDist(targets.get(i).getKey(), targets.get(j).getKey());
                if (currentDist < minPath && currentDist != 0) {
                    minPath = currentDist;
                    vertex = targets.get(i).getKey();
                }
            }
        }
        return vertex;
    }

    @Override
    public boolean save(String file) {
        try {
            FileWriter f = new FileWriter(file);
            Gson gson = new Gson();
            JsonModel jsonModel = new JsonModel();
            List<JsonEdgeData> edgesJson = new ArrayList<>();
            List<JsonNodeData> nodeJson = new ArrayList<>();
            Iterator<EdgeData> edgeDataIterator = mGraph.edgeIter();
            while (edgeDataIterator.hasNext()) {
                EdgeData edgeData = edgeDataIterator.next();
                edgesJson.add(new JsonEdgeData(edgeData.getSrc(), edgeData.getDest(), edgeData.getWeight()));
            }
            Iterator<NodeData> nodeDataIterator = mGraph.nodeIter();
            while (nodeDataIterator.hasNext()) {
                NodeData nodeData = nodeDataIterator.next();
                nodeJson.add(new JsonNodeData(nodeData.getKey(), String.format("%f,%f,%f", nodeData.getLocation().x(), nodeData.getLocation().y(), nodeData.getLocation().z())));
            }
            jsonModel.Edges = edgesJson;
            jsonModel.Nodes = nodeJson;
            f.flush();
            gson.toJson(jsonModel, f);
            f.close();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean load(String file) {
        try {
            mGraph = new DirectedWeightedGraphImpl(file);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void paintWhiteColorToAllNodes() {
        Iterator<NodeData> nodeDataIterator = mGraph.nodeIter();
        while (nodeDataIterator.hasNext()) {
            NodeData nodeData = nodeDataIterator.next();
            if (nodeData != null) {
                nodeData.setInfo(WHITE_COLOR);
            }

        }
    }

    private void bfs(NodeData nodeData) {
        if (mGraph != null && nodeData != null) {
            paintWhiteColorToAllNodes();
            mGraph.getNode(nodeData.getKey()).setInfo(RED_COLOR);
            Queue<NodeData> queue = new LinkedList<>();
            queue.add(nodeData);
            while (!queue.isEmpty()) {
                NodeData u = queue.remove();
                if (!u.getInfo().equals(BLACK_COLOR)) {
                    u.setInfo(RED_COLOR);
                }
                Iterator<EdgeData> i = mGraph.edgeIter(u.getKey());
                while (i.hasNext()) {
                    EdgeData index = i.next();
                    if (mGraph.getNode(index.getDest()).getInfo().equals(WHITE_COLOR)) {
                        mGraph.getNode(index.getDest()).setInfo(BLACK_COLOR);
                        queue.add(mGraph.getNode(index.getDest()));
                    } else if (mGraph.getNode(index.getDest()).getInfo().equals(RED_COLOR)) {
                        mGraph.getNode(index.getDest()).setInfo(BLACK_COLOR);
                    }
                }
            }
        }
    }


}
