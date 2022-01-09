package gui;

import com.api.*;
import com.game.GameController;
import com.game.model.Agent;
import com.game.model.Pokemon;
import com.impl.DirectedWeightedGraphAlgorithmsImpl;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class GamePanel extends JPanel {
    private final DirectedWeightedGraphAlgorithms mGraphAlgo = new DirectedWeightedGraphAlgorithmsImpl();
    private DirectedWeightedGraph mGraph;
    private double minYOffset = Double.MAX_VALUE;
    private double minXOffset = Double.MAX_VALUE;
    private double scaleXWIDTH = 0;
    private double scaleHeight = 0;
    private final int HEIGHT_OFFSET = 80;
    private final JLabel mInfoLabel = new JLabel("");
    private final JButton mStopButton = new JButton("Stop");
    private final GameController mController = new GameController(this);
    private final BufferedImage mBackgroundImage;
    private final BufferedImage mPokemonImage;
    private final BufferedImage mAgentImage;


    public GamePanel() throws IOException {
        initUiElements();
        mController.initalizeGame();
        mBackgroundImage = ImageIO.read(new File("./background.jpeg"));
        mPokemonImage = resize(ImageIO.read(new File("./pikachu.png")), 30, 30);
        mAgentImage = resize(ImageIO.read(new File("./ash2.png")), 50, 50);
        new Thread(mController).start();
    }

    private void initUiElements() {
        mInfoLabel.setHorizontalAlignment(JLabel.RIGHT);
        add(mInfoLabel, BorderLayout.NORTH);
        add(mStopButton);
        mStopButton.addActionListener(e -> mController.stopGame());
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D grapic = (Graphics2D) g.create();
        grapic.drawImage(mBackgroundImage, 0, 0, getWidth(), getHeight(), null);
        drawGraph(grapic);
        mInfoLabel.setText(mController.getInfo());
    }


    private void drawGraph(Graphics2D graphics2D) {
        mGraph = mController.getGraph();
        mGraphAlgo.init(mGraph);
        setScaleMinMax();
        paintGraph(graphics2D);
    }

    public void setResults(String results) {
        mInfoLabel.setText(results);
    }


    public void setScaleMinMax() {
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;
        Iterator<NodeData> NodeI = mGraph.nodeIter();
        while (NodeI.hasNext()) {
            NodeData currNode = NodeI.next();
            GeoLocation geoLocation = currNode.getLocation();
            if (currNode.getLocation().x() < minXOffset) {
                minXOffset = geoLocation.x();
            }
            if (currNode.getLocation().x() > maxX) {
                maxX = geoLocation.x();
            }
            if (currNode.getLocation().y() < minYOffset) {
                minYOffset = geoLocation.y();
            }
            if (currNode.getLocation().y() > maxY) {
                maxY = geoLocation.y();
            }
        }
        scaleXWIDTH = Math.abs(maxX - minXOffset);
        scaleHeight = Math.abs(maxY - minYOffset);

    }

    private void paintNodes(Graphics g) {
        Iterator<NodeData> NodeI = mGraph.nodeIter();
        g.setColor(Color.red);
        while (NodeI.hasNext()) {
            NodeData currNode = NodeI.next();
            double x = (currNode.getLocation().x() - minXOffset) * getXScale() + HEIGHT_OFFSET;
            double y = (currNode.getLocation().y() - minYOffset) * getYScale() + HEIGHT_OFFSET;
            g.fillOval((int) x - 5, (int) y - 5, 12, 12);
            g.drawString(String.format("%s", currNode.getKey()), (int) x - 20, (int) y - 7);
        }
    }

    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }

    private void paintEdges(Graphics g) {
        Iterator<EdgeData> EdgeI = mGraph.edgeIter();
        while (EdgeI.hasNext()) {
            EdgeData currEdge = EdgeI.next();
            g.setColor(Color.BLACK);
            drawEdge(g, currEdge);
        }
    }

    private void drawEdge(Graphics g, EdgeData edgeData) {
        double srcX = (mGraph.getNode(edgeData.getSrc()).getLocation().x() - minXOffset) * getXScale() + HEIGHT_OFFSET;
        double srcY = (mGraph.getNode(edgeData.getSrc()).getLocation().y() - minYOffset) * getYScale() + HEIGHT_OFFSET;
        double destX = (mGraph.getNode(edgeData.getDest()).getLocation().x() - minXOffset) * getXScale() + HEIGHT_OFFSET;
        double destY = (mGraph.getNode(edgeData.getDest()).getLocation().y() - minYOffset) * getYScale() + HEIGHT_OFFSET;
        drawArrowLine(g, (int) srcX, (int) srcY, (int) destX, (int) destY, 15, 3);
    }

    private double getXScale() {
        return getWidth() / scaleXWIDTH * 0.8;
    }

    private double getYScale() {
        return getHeight() / scaleHeight * 0.8;
    }

    private void paintGraph(Graphics2D graphics2D) {
        paintNodes(graphics2D);
        paintEdges(graphics2D);
        paintPokemons(graphics2D);
        paintAgents(graphics2D);
    }

    private void paintAgents(Graphics g) {
        for (Agent agent : mController.getAgents()) {
//            g.setColor(Color.MAGENTA);
            double x = (agent.getPosition().x() - minXOffset) * getXScale() + HEIGHT_OFFSET;
            double y = (agent.getPosition().y() - minYOffset) * getYScale() + HEIGHT_OFFSET;
            g.drawImage(mAgentImage, (int) x, (int) y, null, this);
//            g.fillOval((int) x - 5, (int) y - 5, 12, 12);
//            g.drawString(String.format("%s", agent.getId()), (int) x - 20, (int) y - 7);
        }
    }

    private void paintPokemons(Graphics g) {
        for (Pokemon pokemon : mController.getPokemons()) {
//            g.setColor(Color.blue);
            double x = (pokemon.getPosition().x() - minXOffset) * getXScale() + HEIGHT_OFFSET;
            double y = (pokemon.getPosition().y() - minYOffset) * getYScale() + HEIGHT_OFFSET;
            g.drawImage(mPokemonImage, (int) x-10, (int) y-10,null, this);
//            g.drawString(String.format("%s - %s ", pokemon.getValue(), pokemon.getType()), (int) x - 20, (int) y - 7);
        }
    }

    private void drawArrowLine(Graphics g, int x1, int y1, int x2, int y2, int d, int h) {
        int dx = x2 - x1, dy = y2 - y1;
        double D = Math.sqrt(dx * dx + dy * dy);
        double xm = D - d, xn = xm, ym = h, yn = -h, x;
        double sin = dy / D, cos = dx / D;

        x = xm * cos - ym * sin + x1;
        ym = xm * sin + ym * cos + y1;
        xm = x;

        x = xn * cos - yn * sin + x1;
        yn = xn * sin + yn * cos + y1;
        xn = x;

        int[] xpoints = {x2, (int) xm, (int) xn};
        int[] ypoints = {y2, (int) ym, (int) yn};

        g.drawLine(x1, y1, x2, y2);
        g.fillPolygon(xpoints, ypoints, 3);
    }
}
