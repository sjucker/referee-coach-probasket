package ch.refereecoach.probasket.util;

import lombok.NoArgsConstructor;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class XmlUtil {

    public static Optional<String> getAttributeInElement(Document doc, String tagName, String attributeName) {
        var elements = doc.getDocumentElement().getElementsByTagName(tagName);
        if (elements.getLength() == 1) {
            return Optional.ofNullable(elements.item(0).getAttributes().getNamedItem(attributeName)).map(Node::getNodeValue);
        }
        return Optional.empty();
    }

    public static Optional<String> getAttributeValue(Node parentNode, String name) {
        var node = parentNode.getAttributes().getNamedItem(name);
        return node != null ? Optional.ofNullable(node.getNodeValue()) : Optional.empty();
    }

}
