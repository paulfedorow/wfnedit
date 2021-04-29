package org.wfnedit.persistence;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wfnedit.model.Id;
import org.wfnedit.model.Net;
import org.wfnedit.model.Position;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Deserializes pnml-files into {@link Net} instances.
 *
 * @see Serializer
 * @see Net
 */
public class Deserializer {
    /**
     * Deserializes pnml-files into {@link Net} instances.
     *
     * @param xmlFile the pnml-file to deserialize
     * @return the {@link Net} instance
     * @throws DeserializationException if the parsing fails
     */
    public Net deserialize(File xmlFile) throws DeserializationException {
        Net net = new Net();

        Document document;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(xmlFile);
        } catch (Exception e) {
            throw new DeserializationException();
        }

        toList(document.getElementsByTagName("place")).forEach(node -> collectNode(node, net::addPlace));
        toList(document.getElementsByTagName("transition")).forEach(node -> collectNode(node, net::addTransition));
        toList(document.getElementsByTagName("arc")).forEach(edge -> collectEdge(edge, net::connect));

        return net;
    }

    /**
     * Takes a place xml node, parses it and collects it with the given collector.
     *
     * @param node      the place xml node
     * @param collector the place collector
     */
    private void collectEdge(Node node, EdgeCollector collector) {
        NamedNodeMap attributes = node.getAttributes();
        Node sourceNode = attributes.getNamedItem("source");
        Node targetNode = attributes.getNamedItem("target");
        if (!sourceNode.getNodeValue().isEmpty() && !targetNode.getNodeValue().isEmpty()) {
            collector.collect(new Id(sourceNode.getNodeValue()), new Id(targetNode.getNodeValue()));
        }
    }

    /**
     * Takes a workflow-node xml-node, parses it and collects it with the given collector.
     *
     * @param node      the workflow-node xml-node
     * @param collector the workflow-node collector
     */
    private void collectNode(Node node, NodeCollector collector) {
        Optional<Id> id = parseId(node);
        Optional<String> name = parseName(node);
        Optional<Position> position = parsePosition(node);

        if (id.isPresent() && position.isPresent()) {
            collector.collect(id.get(), name.orElse(""), position.get());
        }
    }

    /**
     * Takes a position xml node, parses it and returns a position instance.
     *
     * @param node the position xml node
     * @return the position instance
     */
    private Optional<Position> parsePosition(Node node) {
        List<Node> children = toList(node.getChildNodes());

        for (Node child : children) {
            if (child.getNodeName().equals("graphics")) {
                List<Node> graphicsChildren = toList(child.getChildNodes());
                for (Node graphicsChild : graphicsChildren) {
                    if (graphicsChild.getNodeName().equals("position")) {
                        NamedNodeMap positionAttributes = graphicsChild.getAttributes();
                        try {
                            return Optional.of(new Position(
                                    Double.parseDouble(positionAttributes.getNamedItem("x").getNodeValue()),
                                    Double.parseDouble(positionAttributes.getNamedItem("y").getNodeValue())
                            ));
                        } catch (Position.IllegalCoordinate | NumberFormatException e) {
                            return Optional.empty();
                        }
                    }
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Takes an id xml node, parses it and returns an id instance.
     *
     * @param node the id xml node
     * @return the id instance
     */
    private Optional<Id> parseId(Node node) {
        if (!node.getAttributes().getNamedItem("id").getNodeValue().isEmpty()) {
            return Optional.of(new Id(node.getAttributes().getNamedItem("id").getNodeValue()));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Takes a workflow-node xml-node, searches a name in it und returns the name.
     *
     * @param node the workflow-node xml-node
     * @return the name
     */
    private Optional<String> parseName(Node node) {
        List<Node> children = toList(node.getChildNodes());

        for (Node child : children) {
            if (child.getNodeName().equals("name")) {
                List<Node> nameChildren = toList(child.getChildNodes());
                for (Node nameChild : nameChildren) {
                    if (nameChild.getNodeName().equals("value")) {
                        return Optional.of(nameChild.getTextContent().trim());
                    }
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Converts a {@code NodeList} into a {@code List<Node>}.
     *
     * @param nodeList the xml node list to convert
     * @return the converted list of nodes
     */
    private List<Node> toList(NodeList nodeList) {
        List<Node> nodes = new ArrayList<>();

        for (int i = 0; i < nodeList.getLength(); i++) {
            nodes.add(nodeList.item(i));
        }

        return nodes;
    }

    /**
     * Is thrown when the parsing of a pnml-file fails.
     */
    public static class DeserializationException extends Exception {}

    /**
     * Represents a node collector. A helper class to decouple parsing and net construction.
     */
    private interface NodeCollector {
        /**
         * Collects node parsing information.
         *
         * @param id       the id of the node
         * @param name     the name of the node
         * @param position the position of the node
         */
        void collect(Id id, String name, Position position);
    }

    /**
     * Represents an edge collector. A helper class to decouple parsing and net construction.
     */
    private interface EdgeCollector {
        /**
         * Collects edge parsing information.
         *
         * @param fromNodeId the from-node id
         * @param toNodeId   the to-node id
         */
        void collect(Id fromNodeId, Id toNodeId);
    }
}
