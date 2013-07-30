package com.flyingspaniel.xml;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * Base class for classes that use XPath to navigate a DOM
 * 
 * Contains useful utilities for XPath navigation, plus utilities for loading DOMs.<P> 
 * 
 * The base class, UsesXPath, shares a single static DocumentBuilderFactory and XPathFactory
 * for all instances.  Since neither DocumentBuilderFactory nor XPathFactory are thread safe,
 * their calls, {@link #loadDOM(File)}, {@link #loadDOM(String)} and {@link #compile(String)}, 
 * are explicitly synchronized.<p>
 * 
 * If you are using this class on a high-load application, use {@link UsesXPath.HighLoad} instead,
 * because in that class each instance has it's own factory.
 * 
 * @author Morgan Conrad
 * @since Copyright(c) 2013  Morgan Conrad
 *
 * @see <a href="http://www.gnu.org/copyleft/lesser.html">This software is released under the LGPL</a>
 */
public class UsesXPath {

   /**
    * Initialization On Demand Holder idiom, see Josh Bloch Effective Java 2nd ed. page 283 or
    * @see <a href="http://www.cs.umd.edu/~pugh/java/memoryModel/jsr-133-faq.html#dcl>More info</a>
    * @see <a href=http://blog.crazybob.org/2007/01/lazy-loading-singletons.html>More info</a>
    */
   static class SingletonFactoryHolder {
      protected static final DocumentBuilderFactory sDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
      protected static final XPathFactory sXPathfactory = XPathFactory.newInstance();
   }
   
   
   /**
    * Another Initialization On Demand Holder idiom
    */
   static class SingletonInstanceHolder {
      protected static final UsesXPath instance = new UsesXPath();
   }
   
   
   /**
    * If you just want a shared instance (you don't want to extend this class) use this
    * @return UsesXPath
    */
   public static synchronized UsesXPath getInstance() {
      return SingletonInstanceHolder.instance;
   }
   
   /**
    * Compile the String to an XPathExpression
    * This (default) implementation uses synchronized access to a single static instance
    * 
    * @see HighLoad   for alternative implementation, one factory per instance
    * @param xpath
    * @return XPathExpression
    * @throws XPathExpressionException
    */
   public XPathExpression compile(String xpath) throws XPathExpressionException {
      synchronized(SingletonFactoryHolder.sXPathfactory) {
         return SingletonFactoryHolder.sXPathfactory.newXPath().compile(xpath);
      }
   }
   
   
   /**
    * Return the attribute of a Node
    * @param node  the non-null Node
    * @param name  the attribute name
    * @return null if name is not an attribute
    */
   public static String getAttribute(Node node, String name) {
      Node attrNode = node.getAttributes().getNamedItem(name);
      return attrNode != null ? attrNode.getNodeValue() : null;
   }
   
   
   /**
    * Evaluates the xpath and returns a Boolean
    * @param node
    * @param xpath
    * @return Boolean
    * @throws XPathExpressionException
    */
   public Boolean getBooleanFromXPath(Object node, String xpath) throws XPathExpressionException {
      return (Boolean) compile(xpath).evaluate(node, XPathConstants.BOOLEAN);
   }

   /**
    * Evaluates the xpath and returns a Double
    * @param node
    * @param xpath
    * @return Double
    * @throws XPathExpressionException
    */
   public Double getDoubleFromXPath(Object node, String xpath) throws XPathExpressionException {
      return (Double) compile(xpath).evaluate(node, XPathConstants.NUMBER);
   }
   
   /**
    * Evaluates the xpath and returns a Node
    * @param node
    * @param xpath
    * @return Node
    * @throws XPathExpressionException
    */
   public Node getNodeFromXPath(Object node, String xpath) throws XPathExpressionException {
      return (Node) compile(xpath).evaluate(node, XPathConstants.NODE);
   }
   
   /**
    * Evaluates the xpath and returns a NodeList
    * @param node
    * @param xpath
    * @return NodeList
    * @throws XPathExpressionException
    */
   public NodeList getNodeListFromXPath(Object node, String xpath) throws XPathExpressionException {
      return (NodeList) compile(xpath).evaluate(node, XPathConstants.NODESET);
   }

   
   /**
    * Evaluates the xpath and returns a String
    * @param node
    * @param xpath
    * @return String
    * @throws XPathExpressionException
    */
   public String getStringFromXPath(Object node, String xpath) throws XPathExpressionException {
      return (String) compile(xpath).evaluate(node, XPathConstants.STRING);
   }

   
   /**
    * Obtain a DocumentBuilderFactory
    * This (default) implementation uses synchronized access to a single static instance
    * 
    * @return DocumentBuilderFactory
    */
   public DocumentBuilderFactory getDocumentBuilderFactory() {
      return SingletonFactoryHolder.sDocumentBuilderFactory;
   }
   
   
   /**
    * Loads a DOM from a URL
    * This (default) implementation uses synchronized access to a single static instance
    * 
    * @param url
    * @return XML Document
    * @throws SAXException
    * @throws IOException
    * @throws ParserConfigurationException
    */
   public Document loadDOM(String url) throws SAXException, IOException, ParserConfigurationException {   
      DocumentBuilder db;
      synchronized(SingletonFactoryHolder.sXPathfactory) {
         db = getDocumentBuilderFactory().newDocumentBuilder();
      }
      Document doc = db.parse(url);
      return doc;
   }
 
   
   /**
    * Loads a DOM from a File
    * This (default) implementation synchronized access to a single static instance
    * 
    * @param file
    * @return XML Document
    * @throws SAXException
    * @throws IOException
    * @throws ParserConfigurationException
    */
   public Document loadDOM(File file) throws SAXException, IOException, ParserConfigurationException {   
      DocumentBuilder db;
      synchronized(SingletonFactoryHolder.sXPathfactory) {
         db = getDocumentBuilderFactory().newDocumentBuilder();
      }
      Document doc = db.parse(file);
      return doc;
   }

   
   
   /**
    * Static inner class for high load situations where you don't want to synchronize and share the one global XPathFactory 
    */
   public static class HighLoad extends UsesXPath {
      protected final XPathFactory localPathFactory = XPathFactory.newInstance();
      protected final DocumentBuilderFactory localDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
      
      @Override
      public XPathExpression compile(String xpath) throws XPathExpressionException {
         return localPathFactory.newXPath().compile(xpath);
      }
       
      @Override
      public DocumentBuilderFactory getDocumentBuilderFactory() {
         return localDocumentBuilderFactory;
      }

      @Override
      public Document loadDOM(String url) throws SAXException, IOException, ParserConfigurationException {
         return localDocumentBuilderFactory.newDocumentBuilder().parse(url);
      }
      
      @Override
      public Document loadDOM(File file) throws SAXException, IOException, ParserConfigurationException {
         return localDocumentBuilderFactory.newDocumentBuilder().parse(file);
      }
   }
   
}
