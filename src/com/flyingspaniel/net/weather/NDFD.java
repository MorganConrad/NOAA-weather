package com.flyingspaniel.net.weather;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.flyingspaniel.xml.UsesXPath;


/**
 * Represents (some) of the possible National Digital Forecast Database Element Names
 * 
 * @author Morgan Conrad
 * @since Copyright(c) 2013  Morgan Conrad
 *
 * @see <a href="http://www.gnu.org/copyleft/lesser.html">This software is released under the LGPL</a><p>
 * @see <a href="http://graphical.weather.gov/xml/docs/elementInputNames.php">Full list of National Digital Forecast Database Element Names</a>
 *
 */
public enum NDFD {
   maxt("High", "parameters/temperature[@type='maximum']"),
   mint("Low", "parameters/temperature[@type='minimum']"),
   temp("Temperature", "parameters/temperature[@type='hourly']"),
   dew("Dewpoint", "parameters/temperature[@type='dew point']"),
   icons("Icons", "parameters/conditions-icon", "icon-link"),
   interpretation("Overall Condition", null, null),
   qpf("Precipitation", "parameters/precipitation[@type='liquid']"),
   rh("Relative Humidity", "parameters/humidity[@type='relative']"),
   sky("Cloud Cover", "parameters/cloud-amount"),
   snow("Snowfall", "parameters/precipitation[@type='snow']"),
   waveh("Wave Height", "parameters/water-state/waves"),
   wdir("Wind Direction", "parameters/direction[@type='wind']"),
   wgust("Wind Gusts(knots)", "parameters/wind-speed[@type='gust']"),
   wspd("Wind Speed", "parameters/wind-speed[@type='sustained']"),
   wwa("Watches, Warnings, and Advisories", "parameters/hazards", "hazard-conditions") {
      @Override protected String parseValue(Node valueNode) { return parseWWAContent(valueNode); }
   },
   wx("Weather", "parameters/weather", "weather-conditions");
   
   static final EnumSet<NDFD> STRING_NDFDS = EnumSet.of(NDFD.icons, NDFD.interpretation, NDFD.sky, NDFD.wwa, NDFD.wx);
   
   static final HashMap<String, NDFD> sNDFDsByUIName = new HashMap<String, NDFD>();
   
   static {
      for (NDFD ndfd : NDFD.values())
         sNDFDsByUIName.put(ndfd.nameForUI, ndfd);
   }
   
   public final String nameForUI;
   public final String xPathToNode;    // relative to the main node, "dwml/data"  usually starts with "parameters/"
   public final String listTag;        // usually "value"
   
   NDFD(String nameForUI, String xPathToNodeList, String listTag) {
      this.nameForUI = nameForUI;
      this.xPathToNode = xPathToNodeList;  
      this.listTag = listTag;
   }
   
   NDFD(String nameForUI, String xPathToNodeList) {
      this(nameForUI, xPathToNodeList, "value");
   }
   

   /**
    * Like valueOf, but pass in the more UI friendly name
    * @param nameForUI  e.g. "Wind Speed"
    * @return  null of no NDFD matches that nameForUI
    */
   public static NDFD valueOfUIName(String nameForUI) {
      return sNDFDsByUIName.get(nameForUI);
   }
   
   
   /**
    * Obtain an NDFD, trying both valueOfUIName and valueOf
    * @param uiNameOrEnumName
    * @return  null if no NDFD matches
    */
   public static NDFD lookup(String uiNameOrEnumName) {
      NDFD ndfd = valueOfUIName(uiNameOrEnumName);
      if (ndfd == null)
         try {
            ndfd = NDFD.valueOf(uiNameOrEnumName);
         }
         catch (IllegalArgumentException iae) {
            ndfd = null;
         }
      
      return ndfd;
   }
   
   
   /**
    * Whether this measurement is numeric (could be converted to a Float)
    */
   public boolean isNumeric() {
      return !STRING_NDFDS.contains(this);
   }
   
   
   /**
    * Parse a NodeList
    *   (which would typically be found at xPathToNode + "/" + listTag)
    * 
    * @param valueNodes
    * @return never null, seldom empty, but may contain "" if no value was present
    */
   public List<String> parseValues(NodeList valueNodes) {
      ArrayList<String> valueList = new ArrayList<String>(valueNodes.getLength());
      for (int n=0; n<valueNodes.getLength(); n++) {
         String content = parseValue(valueNodes.item(n));
         valueList.add(content);
      }
      
      return valueList;
   }
   
   
   /**
    * Standard implementation to parse a single value
    * @param valueNode
    * @return String
    */
   protected String parseValue(Node valueNode) {
      return valueNode.getTextContent();
   }
   
   
   
   
   // used by NDFD.wwa to parse hazards
   protected String parseWWAContent(Node valueNode) {
      UsesXPath uxp = UsesXPath.getInstance();

      try {
         NodeList hazards = uxp.getNodeListFromXPath(valueNode, "hazard ");

         // note that hazards will often have a length of 0
         if (hazards.getLength() == 0)
            return "";

         StringBuilder sb = new StringBuilder();
         for (int hn = 0; hn < hazards.getLength(); hn++) {
            Node hazard = hazards.item(hn);
            sb.append("  " + UsesXPath.getAttribute(hazard, "phenomena"));
            sb.append(" " + UsesXPath.getAttribute(hazard, "significance"));
            Node url = uxp.getNodeFromXPath(hazard, "hazardTextURL");
            sb.append(" @link:" + url.getTextContent());
         }
         return sb.toString();
      }

      catch (XPathExpressionException xpe) {
         return xpe.getMessage();
      }
   }
}
