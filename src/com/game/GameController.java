package com.game;

import com.api.*;
import com.game.model.Agent;
import com.game.model.Pokemon;
import com.impl.DirectedWeightedGraphAlgorithmsImpl;
import com.impl.DirectedWeightedGraphImpl;
import com.json.JsonUtilities;
import com.json.game.*;
import gui.GamePanel;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameController implements Runnable {
    private final GamePanel mUI;
    private final Client mClient = new Client();
    private final PriorityQueue<Pokemon> mPq = new PriorityQueue<>(Comparator.comparingDouble(Pokemon::getDistance));
    private DirectedWeightedGraph mGraph;
    private final List<Agent> mAgents = new CopyOnWriteArrayList<>();
    private final List<Pokemon> mPokemons = new CopyOnWriteArrayList<>();
    private final DirectedWeightedGraphAlgorithms mAlgo = new DirectedWeightedGraphAlgorithmsImpl();
    private final HashMap<Integer, Integer> mAgentsDestantion = new HashMap<>();
    private final Object lock = new Object();

    public GameController(GamePanel gamePanel) {
        this.mUI = gamePanel;
    }

    public void initalizeGame() throws IOException {
        mClient.startConnection("127.0.0.1", 6666);
        mGraph = new DirectedWeightedGraphImpl(mClient.getGraph(), true);
        mAlgo.init(mGraph);
        addAgents();
        convertJsonAgentsFromStringAndSetList(mClient.getAgents());
        convertJsonPokemonsFromStringAndSetList(mClient.getPokemons());
    }

    private void addAgents() {
        JsonInfo jsonInfo = JsonUtilities.getJsonInfo(mClient.getInfo());
        for (int i = 0; i < jsonInfo.GameServer.agents; i++) {
            mClient.addAgent("{\"id\":" + i + "}");
            mAgentsDestantion.put(i, -1);
        }
    }


    public void convertJsonAgentsFromStringAndSetList(String json) {
        mAgents.clear();
        JsonAgents jsonAgents = JsonUtilities.getJsonAgents(json);
        for (JsonAgent agent : jsonAgents.Agents) {
            Agent newAgentToAdd = new Agent(agent.Agent.id, agent.Agent.src, agent.Agent.dest, agent.Agent.pos, agent.Agent.speed, agent.Agent.value);
            mAgents.add(newAgentToAdd);
        }
    }


    public void convertJsonPokemonsFromStringAndSetList(String json) {
        mPokemons.clear();
        JsonPokemons jsonPokemons = JsonUtilities.getJsonPokemons(json);
        for (JsonPokemon pokemon : jsonPokemons.Pokemons) {
            Pokemon pokemonToAdd = new Pokemon(pokemon.Pokemon.value, pokemon.Pokemon.type, pokemon.Pokemon.pos);
            setEdgeToPokemon(pokemonToAdd);
            mPokemons.add(pokemonToAdd);
        }
    }


    public List<Pokemon> getPokemons() {
        return mPokemons;
    }

    public List<Agent> getAgents() {
        return mAgents;
    }


    public DirectedWeightedGraph getGraph() {
        return mGraph;
    }

    public boolean isGameRunning() {
        synchronized (lock) {
            return mClient.isRunning().equals("true");
        }
    }

    public void updatePlayers() {
        synchronized (lock) {
            convertJsonAgentsFromStringAndSetList(mClient.getAgents());
            convertJsonPokemonsFromStringAndSetList(mClient.getPokemons());
        }
    }

    public void move() {
        synchronized (lock) {
            mClient.move();
        }
    }


    public boolean isPokemonOnEdge(double edgeDistance, double pokemonDestFromSrcPlusDest) {
        return edgeDistance > pokemonDestFromSrcPlusDest - 0.000001;
    }

    public void setEdgeToPokemon(Pokemon pokemon) {
        Iterator<EdgeData> edgeDataIterator = mGraph.edgeIter();
        GeoLocation pokemonPosition = pokemon.getPosition();
        while (edgeDataIterator.hasNext()) {
            EdgeData edgeData = edgeDataIterator.next();
            if (pokemon.getType() > 0 && edgeData.getDest() > edgeData.getSrc() || pokemon.getType() < 0 && edgeData.getSrc() >= edgeData.getDest()) {
                GeoLocation srcNodePosition = mGraph.getNode(edgeData.getSrc()).getLocation();
                GeoLocation destNodePosition = mGraph.getNode(edgeData.getDest()).getLocation();
                double edgeDistance = srcNodePosition.distance(destNodePosition);
                double pokemonDestFromSrcPlusDest = srcNodePosition.distance(pokemonPosition) + pokemonPosition.distance(destNodePosition);
                if (isPokemonOnEdge(edgeDistance, pokemonDestFromSrcPlusDest)) {
                    pokemon.setEdge(edgeData);
                    break;
                }
            }
        }
    }

    @Override
    public void run() {
        mClient.start();
        while (isGameRunning()) {
            updatePlayers();
            moveAgentToCatchPokemon();

            mUI.repaint();
            try {

                move();
                Thread.sleep(100);
            } catch (Exception e) {

            }

        }
        synchronized (lock) {
            mUI.setResults(getInfo());
        }
    }


    private boolean isAgentOnNode(Agent agent) {
        return agent.getDest() == -1;
    }


    private void moveAgentToCatchPokemon() {
        for (Agent agent : mAgents) {
            if (isAgentOnNode(agent)) {
                if (agent.getSrc() == mAgentsDestantion.get(agent.getId())) {
                    mAgentsDestantion.put(agent.getId(), -1);
                }
                int nextNode = calculateNextNode(agent);
                moveAgentToAnotherNodeThroughEdge(agent, nextNode);
            }
        }
    }

    public void moveAgentToAnotherNodeThroughEdge(Agent agent, int nextNode) {
        synchronized (lock) {
            mClient.chooseNextEdge("{\"agent_id\":" + agent.getId() + ", \"next_node_id\":" + nextNode + "}");
        }
    }


    private int calculateNextNode(Agent agent) {
        mPq.clear();
        for (Pokemon p : getPokemons()) {
            try {
                if (!mAgentsDestantion.containsValue(p.getCurrentEdgePlace().getDest()) || mAgentsDestantion.get(agent.getId()) == p.getCurrentEdgePlace().getDest()) {
                    p.setDistanceFromAgentToPokemon(mAlgo.shortestPathDist(agent.getId(), p.getCurrentEdgePlace().getDest()));
                    mPq.add(p);
                }
            } catch (Exception e) {

            }
        }
        ArrayList<NodeData> lst = null;
        if (!mPq.isEmpty()) {
            Pokemon pk = mPq.poll();
            mAgentsDestantion.put(agent.getId(), pk.getCurrentEdgePlace().getDest());
            if (agent.getSrc() == pk.getCurrentEdgePlace().getDest()) {
                return pk.getCurrentEdgePlace().getSrc();
            } else {
                lst = new ArrayList<>(mAlgo.shortestPath(agent.getSrc(), pk.getCurrentEdgePlace().getDest()));
            }

        }
        //Get the nextone
        if (lst == null || lst.isEmpty()) {
            List<EdgeData> list = new ArrayList<>();
            mGraph.edgeIter(agent.getSrc()).forEachRemaining(list::add);
            LinkedList<EdgeData> ed = new LinkedList<>(list);
            return ed.getFirst().getDest();
        } else {
            return lst.get(1).getKey();
        }


    }


    public void stopGame() {
        mClient.stop();
    }

    public String getInfo() {
        synchronized (lock) {
            String info = mClient.getInfo();
            final JsonInfo jsonInfo = JsonUtilities.getJsonInfo(info);
            if (jsonInfo != null && jsonInfo.GameServer != null) {
                lastInfo = String.format("Grade: %6d ,Moves: %6d , EndTime: %10s", jsonInfo.GameServer.grade, jsonInfo.GameServer.moves, mClient.timeToEnd());
            }
            return lastInfo;
        }

    }

    private String lastInfo;
}
