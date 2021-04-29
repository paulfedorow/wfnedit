package org.wfnedit.persistence;

import org.wfnedit.model.*;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Serializes a {@link Net} instance into a pnml-file.
 *
 * @see Deserializer
 * @see Net
 */
public class Serializer {
    /**
     * Serializes a {@link Net} instance into a pnml-file.
     *
     * @param net  the net to serialize
     * @param file the file to serialize to
     * @throws FileNotFoundException if there was a problem with the creation of the file
     * @throws XMLStreamException if there is an unexpected error while writing the pnml xml
     */
    public void serialize(Net net, File file) throws FileNotFoundException, XMLStreamException {
        FileOutputStream fos = new FileOutputStream(file);
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        final XMLStreamWriter writer = factory.createXMLStreamWriter(fos, "UTF-8");

        writer.writeStartDocument("UTF-8", "1.0");
        writer.writeStartElement("pnml");
        writer.writeStartElement("net");

        net.getNodes().forEach(node -> node.accept(new Node.NodeVisitor() {
            @Override public void visit(Transition transition) {
                try {
                    Serializer.this.writeTransition(writer, transition);
                } catch (XMLStreamException e) {
                    e.printStackTrace();
                }
            }
            @Override public void visit(Place place) {
                try {
                    Serializer.this.writePlace(writer, place);
                } catch (XMLStreamException e) {
                    e.printStackTrace();
                }
            }
        }));

        net.getEdges().forEach(edge -> {
            try {
                writeEdge(writer, edge);
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        });

        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.close();
    }

    /**
     * Writes the pnml xml for the given transition.
     *
     * @param writer     the xml writer
     * @param transition the transition
     * @throws XMLStreamException if there is an unexpected error while writing the pnml xml
     */
    private void writeTransition(XMLStreamWriter writer, Transition transition) throws XMLStreamException {
        writer.writeStartElement("", "transition", "");
        writer.writeAttribute("id", transition.getId().toString());

        writer.writeStartElement("", "name", "");
        writer.writeStartElement("", "value", "");
        writer.writeCharacters(transition.getName());
        writer.writeEndElement();
        writer.writeEndElement();

        writer.writeStartElement("", "graphics", "");
        writer.writeStartElement("", "position", "");
        writer.writeAttribute("x", String.format("%.0f" , transition.getX()));
        writer.writeAttribute("y", String.format("%.0f", transition.getY()));
        writer.writeEndElement();
        writer.writeEndElement();

        writer.writeEndElement();
    }

    /**
     * Writes the pnml xml for the given place.
     *
     * @param writer the xml writer
     * @param place  the place
     * @throws XMLStreamException if there is an unexpected error while writing the pnml xml
     */
    private void writePlace(XMLStreamWriter writer, Place place) throws XMLStreamException {
        writer.writeStartElement("", "place", "");
        writer.writeAttribute("id", place.getId().toString());

        writer.writeStartElement("", "name", "");
        writer.writeStartElement("", "value", "");
        writer.writeCharacters(place.getName());
        writer.writeEndElement();
        writer.writeEndElement();

        writer.writeStartElement("", "initialMarking", "");
        writer.writeStartElement("", "token", "");
        writer.writeStartElement("", "value", "");
        writer.writeCharacters("0");
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();

        writer.writeStartElement("", "graphics", "");
        writer.writeStartElement("", "position", "");
        writer.writeAttribute("x", String.format("%.0f" , place.getX()));
        writer.writeAttribute("y", String.format("%.0f", place.getY()));
        writer.writeEndElement();
        writer.writeEndElement();

        writer.writeEndElement();
    }

    /**
     * Writes the pnml xml for the given edge.
     *
     * @param writer the xml writer
     * @param edge   the edge
     * @throws XMLStreamException if there is an unexpected error while writing the pnml xml
     */
    private void writeEdge(XMLStreamWriter writer, Edge edge) throws XMLStreamException {
        writer.writeStartElement("", "arc", "");
        writer.writeAttribute("id", edge.getId().toString());
        writer.writeAttribute("source", edge.getFromId().toString());
        writer.writeAttribute("target", edge.getToId().toString());
        writer.writeEndElement();
    }
}
