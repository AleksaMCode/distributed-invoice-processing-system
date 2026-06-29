package com.dips.parser.xml;

import com.dips.validator.model.Client;
import com.dips.validator.model.Invoice;
import com.dips.validator.model.Item;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class InvoiceXmlParser {
  public Invoice parse(byte[] xmlBytes) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    factory.setExpandEntityReferences(false);
    factory.setXIncludeAware(false);

    Document document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(xmlBytes));
    document.getDocumentElement().normalize();
    Element root = document.getDocumentElement();

    String id = text(root, "id");
    Element clientNode = first(root, "client");
    Client client =
        new Client(text(clientNode, "name"), text(clientNode, "ein"), text(clientNode, "email"));

    LocalDate date = LocalDate.parse(text(root, "date"));
    String currency = text(root, "currency");

    List<Item> items = new ArrayList<>();
    Element itemsNode = first(root, "items");
    if (itemsNode != null) {
      NodeList itemNodes = itemsNode.getElementsByTagName("item");
      for (int i = 0; i < itemNodes.getLength(); i++) {
        Element itemNode = (Element) itemNodes.item(i);
        String description = text(itemNode, "description");
        Integer quantity = Integer.parseInt(text(itemNode, "quantity"));
        BigDecimal unitPrice = new BigDecimal(text(itemNode, "unitPrice"));
        items.add(new Item(description, quantity, unitPrice));
      }
    }

    return new Invoice(id, client, date, currency, items);
  }

  public String extractInvoiceId(byte[] xmlBytes) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    factory.setExpandEntityReferences(false);
    factory.setXIncludeAware(false);
    Document document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(xmlBytes));
    document.getDocumentElement().normalize();
    return text(document.getDocumentElement(), "id");
  }

  private Element first(Element parent, String tag) {
    NodeList list = parent.getElementsByTagName(tag);
    if (list.getLength() == 0) {
      return null;
    }
    return (Element) list.item(0);
  }

  private String text(Element parent, String tag) {
    Element node = first(parent, tag);
    if (node == null || node.getTextContent() == null) {
      return null;
    }
    return node.getTextContent().trim();
  }
}
