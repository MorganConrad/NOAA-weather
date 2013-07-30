package com.flyingspaniel.net.weather;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.flyingspaniel.ranges.RangesPredicates;
import com.flyingspaniel.xml.UsesXPath;



/**
 * Implementation of IWeather using the NOAA National Weather Service
 * 
 * @author Morgan Conrad
 * @since Copyright(c) 2013  Morgan Conrad
 *
 * @see <a href="http://www.gnu.org/copyleft/lesser.html">This software is released under the LGPL</a><p>
 * @see <a href="http://graphical.weather.gov/xml/rest.php">NOAA REST Web Service</a>
 *
 */
public class NOAAWeather extends UsesXPath implements Weather.Interface<Double[]> {

   /**
    * Keys for the moreInfo map.
    */
   public enum MoreInfoKeys {
      DATE, 
      IN_URL,   // URL we send to NOAA 
      OUT_URL   // URL they return as a link in moreWeatherInformation
   }
   
   
   public static final String BASE_URL = "http://graphical.weather.gov/xml/sample_products/browser_interface/ndfdXMLclient.php";
   
   static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");  
   
  // NDFDs that are always done
   static final EnumSet<NDFD> STANDARD_NDFDS = EnumSet.of(NDFD.maxt, NDFD.mint, NDFD.temp, NDFD.icons);
   
   static final String TIME_LAYOUT = "time-layout";
   static final long ONE_HOUR = 1000L*60*60;
   
   protected Document document = null;
   protected Node mainNode = null;       // "dwml/data"
   
   protected List<Forecast> forecasts;
   protected List<Date> forecastPreferredDates;
   protected Map<Date, String> hazardMap;
   
   protected final Map<String, String> moreInfo = new HashMap<String, String>();
   protected final Map<NDFD, NDFDSeries> ndfdSeriesMap = new HashMap<NDFD, NDFDSeries>();

   protected int preferredHour = 11;

   // additional NDFDs the use may want
   protected EnumSet<NDFD> NDFDs = EnumSet.noneOf(NDFD.class);
   
   protected HashMap<String, TimeLayout> timeLayoutMap = new HashMap<String, TimeLayout>();
   protected TimeLayout timeLayout0;
   
   protected GregorianCalendar calendar = null;
   
   protected double maxHourDiff = 6.0;
   protected Date startDate;
   protected Date endDate;
   protected double latitude;
   protected double longitude;
   
   /**
    * Alphabetical for convenience of viewing, not required (they go into a Map)
    * @see <a href="http://w1.weather.gov/xml/current_obs/weather.php">Weather Conditions and Icons</a>
    */
   static final String[][] ICON_NAMES_TO_MEANINGS = { 
      { "bkn", "Mostly Cloudy" },
      { "blizzard", "Blizzard" },
      { "cold", "Cold" },
      { "du", "Dust or Sand" }, 
      { "dust", "Dust or Sand" }, 
      { "skc", "Clear" },
      { "few", "A Few Clouds" },
      { "fg", "Foggy" },
      { "fu", "Fire?" },
      { "fzra", "Freezing Rain" },
      { "fzrara", "Rain and Freezing Rain" },
      { "hazy", "Hazy" },
      { "hi_bkn", "High Clouds" },
      { "hi_few", "A Few High Clouds" },
      { "hi_shwrs", "Showers in Vicinity" },      
      { "hi_tsra", "Thunderstorms in Vicinity" },
      { "hot", "Hot" },
      { "ip", "Hail" },
      { "mix", "Freezing Rain and Snow" },
      { "nsvrtsra", "Possible Tornados" },      
      { "ovc", "Overcast" },
      { "ra", "Rain" },
      { "ra1", "Light Rain" },
      { "raip", "Rain and Hail" },
      { "rasn", "Rain and Snow" },
      { "sct", "Partly Cloudy" },
      { "scttsra", "Scattered Rain" },      
      { "shra", "Rain Showers" },
      { "smoke", "Smoke" },
      { "sn", "Snow" },
      { "tsra", "Rain and Thunderstorms" },
      { "wind", "Breezy" },
      
   };

   static final HashMap<String, String> sIconMeanings = new HashMap<String, String>();
   
   static {
      for (int i=0; i<ICON_NAMES_TO_MEANINGS.length; i++)
         sIconMeanings.put(ICON_NAMES_TO_MEANINGS[i][0], ICON_NAMES_TO_MEANINGS[i][1]);
   }
   
   
   
   @Override
   public void setDates(Date startDate, Date endDate) {
      this.startDate = startDate;
      this.endDate = endDate;   
      document = null;
   }
   
   @Override
   public void setDates(Date startDate, long days) {
      if (startDate == null)
         startDate = new Date();
      this.startDate = startDate;
      long ms = startDate.getTime() + (days * 24 * TimeInterval.HOURL);
      this.endDate = new Date(ms);
   }
   
   @Override
   public void setLocation(Double...latLong) {
      latitude = latLong[0];
      longitude = latLong[1];
      document = null;
   }
   
   @Override
   public NOAAWeather call() throws IOException {

      try {
         NDFDs.addAll(STANDARD_NDFDS);
         if (document == null)
            document = loadDocFromNOAA();

         mainNode = getNodeFromXPath(document, "dwml/data");

         NodeList timeLayouts = getNodeListFromXPath(mainNode, TIME_LAYOUT);
         for (int n=0; n<timeLayouts.getLength(); n++) {
            TimeLayout timeLayout = TimeLayout.parseXML(timeLayouts.item(n));
            this.timeLayoutMap.put(timeLayout.layoutKey, timeLayout);
            
            if (n == 0)
               timeLayout0 = timeLayout;
         }
         
        //  NDFDs.addAll(STANDARD_NDFDS);
         for (NDFD ndfd : NDFDs) {
            NDFDSeries series = parseNDFDSeries(ndfd);
            ndfdSeriesMap.put(series.ndfd, series);
         }
         
         // icons get computed afterwards...
         if (NDFDs.contains(NDFD.icons)) {
            NDFDSeries series = computeIconInterpretations(ndfdSeriesMap.get(NDFD.icons));
            ndfdSeriesMap.put(series.ndfd, series);
         }
         
         computeMoreInfo(document);
         
         forecasts = computeDailyForecasts();

      } catch (IOException ioe) {
         throw ioe;
      } catch (Exception ex) {
         throw new Weather.Exception(ex);
      }
      
      return this;
   }
   
  
   /**
    * Loads the doc from a previously stored file.  (generally for unit tests)
    * @param file
    * @return XML Document
    * @throws IOException
    */
   protected Document loadDocFromFile(File file) throws IOException {
      try {
         return loadDOM(file);
      } catch (IOException ioe) {
         throw ioe;
      } catch (Exception ex) {
         throw new Weather.Exception(ex);
      }
   }
   
   
   /**
    * Loads the XML Document from the NOAA REST service
    * @return XML Document
    * @throws IOException
    */
   protected Document loadDocFromNOAA() throws IOException {
      String begin;
      String end;
      
      synchronized(DATE_TIME_FORMAT) {
         begin = startDate != null ? DATE_TIME_FORMAT.format(startDate) : "";
         end = startDate != null ? DATE_TIME_FORMAT.format(startDate) : "";
      }
      
      StringBuilder url = new StringBuilder();
      url.append(BASE_URL);
      url.append("?lat=" + latitude);
      url.append("&lon=" + longitude);
      url.append("&product=time-series");
      url.append("&begin=" + begin);
      url.append("&end=" + end);
      for (NDFD ndfd : NDFDs) {
         String name = ndfd.name();
         url.append("&" + name + "=" + name);
      }    
      
      moreInfo.put(MoreInfoKeys.IN_URL.name(), url.toString());
      
      try {
         return loadDOM(url.toString());
      }
      catch (IOException ioe) {
         throw ioe;
      }
      catch (Exception ex) {
         throw new Weather.Exception(ex);
      }
   }
   
   
   public Forecast getConditionAtTime(Date date) {
      if (date == null)
         date = new Date();
      TimeInterval now = new TimeInterval(date);
      @SuppressWarnings("unchecked")
      List<Forecast> shouldBeOnlyOne = RangesPredicates.listAcceptsExemplar((Collection)forecasts, now, RangesPredicates.Contains);
       
      if (shouldBeOnlyOne.size() == 1)
         return shouldBeOnlyOne.get(0);
      
      List<Forecast> nearbyForecasts = shouldBeOnlyOne.isEmpty() ? forecasts : shouldBeOnlyOne;
      int closestIndex =  TimeInterval.findClosestTime(nearbyForecasts, date, 24);
      
      return (closestIndex < 0) ? null : nearbyForecasts.get(closestIndex);
   }


   @Override
   public List<Forecast> getForecasts() {
      return forecasts;
   }


   @Override
   public  Map<String, String> getMoreInfo() {
      return moreInfo;
   }
   
   
   public Map<Date, String> getHazardMap() {
      return hazardMap;
   }
   
   
   public List<String> getAdvisoriesBetween(Date d1, Date d2) {
      long d1ms = (d1 != null) ? d1.getTime() : 0L;
      long d2ms = (d2 != null) ? d2.getTime() : Long.MAX_VALUE;
      
      ArrayList<String> advisories = new ArrayList<String>();
      for (Date d : hazardMap.keySet()) {
         long ms = d.getTime();
         if ((ms >= d1ms) && (ms <= d2ms))
            advisories.add(hazardMap.get(d));
      }
      
      return advisories;
   }
   
   
 
   public Map<NDFD, List<Condition>> getAllConditionsFor(TimeInterval ti) {
      
      Map<NDFD, List<Condition>> map = new EnumMap<NDFD, List<Condition>>(NDFD.class);
      
      for ( NDFDSeries series :ndfdSeriesMap.values()) {
         // TODO skip series that aren't conditions...
         
         List<Condition> conditions = new ArrayList<Condition>();
         List<TimeInterval> intervals = series.timeLayout.intervals;
         
         int[] indices = RangesPredicates.exemplarAcceptsIndices(ti, intervals, RangesPredicates.Contains);
         for (int idx : indices)
            conditions.add(new Condition(intervals.get(idx), series.ndfd, series.getValue(idx)));
         
         map.put(series.ndfd, conditions);
      }
      
      return map;
   }
   
   
   /**
    * Add optional NDFDs by their name
    * @param names
    * @return how many were added (usually ignored)
    */
   public int addNDFDParameters(String...names) {
      int initialSize = NDFDs.size();
      if (names != null)
         for (String s : names)
            NDFDs.add(NDFD.valueOf(s));
      
      return NDFDs.size() - initialSize;
   }

   
   /**
    * Add optional NDFDs as NDFDs
    * @param ndfds
    * @return how many were added (usually ignored)
    */
   public int addNDFDParameters(NDFD...ndfds) {
      int initialSize = NDFDs.size();
      if (ndfds != null)
         for (NDFD ndfd: ndfds)
            NDFDs.add(ndfd);
      
      return NDFDs.size() - initialSize;
   }
   
   
   /**
    * Add a collection of optional NDFDs
    * @param addThese  if null nothing happens
    * @return how many were added (usually ignored)
    */
   public int addNDFDParameters(Collection<NDFD> addThese) {
      int initialSize = NDFDs.size();
      if (addThese != null)
         NDFDs.addAll(addThese);
      
      return NDFDs.size() - initialSize;
   }
   
   
   /**
    * Clear the list of optional NDFDs
    */
   public void clearNDFDParameters() {
      NDFDs.clear();
   }

   
   /**
    * Provides user-friendly interpretation of the icon provided by NOAA
    * 
    * @param iconName     may or may not have the full path (contain a /) or end with .jpg
    * @return never null
    */
   public String interpretIconName(String iconName) {
     
      String addendum = "";
      String meaning = null;
      try {
         int lastSlash = iconName.lastIndexOf('/');
         int lastDot = iconName.lastIndexOf('.');
         if (lastDot < 0)
            lastDot = iconName.length();
         
         String shortName = iconName.substring(lastSlash + 1, lastDot);
         
         if (shortName.charAt(0) == 'n') // in theory we don't have the "night" forecast, but just in case
            shortName = shortName.substring(1);
         shortName = shortName.replace("_n", "_");   // replace any possible _n as well
         
         int slen = shortName.length();
         if (shortName.charAt(slen-1) == '0') {  // is there a percentage included?
            int cutoff = shortName.charAt(slen-2) == '0' ? slen-3 : slen-2;
            String percent = " (" + shortName.substring(cutoff) + "%)";
            addendum = addendum + percent;
            shortName = shortName.substring(0, cutoff);
         }
         
         
         meaning = sIconMeanings.get(shortName);
         
         if ((meaning == null) && shortName.startsWith("hi_")) {
            meaning = sIconMeanings.get(shortName.substring(3));  // try again without the "hi_"
            addendum = addendum + " (high)";
         }
      }
      catch (IndexOutOfBoundsException ioob) {  // catch most of the errors...
         meaning = null;
      }
      
      return (meaning != null) ? meaning + addendum : "Cannot interpret " + iconName;     
   }
   
   /**
    * Set the preferred hour (0-23) for our forecasts
    * May be called before or after doing the forecasts
    * 
    * @param hour
    */
   public void setPreferredHour(int hour) {
      preferredHour = hour;
      
      if (mainNode != null) {  // recalculate
         try {
            call();
         } catch (Exception xpee) {
            throw new Weather.Exception(xpee);
         }
      }
   }
   
   
   /**
    * Generate a Date for the given date and hour in the local time zone of the location
    * 
    * @param date if null current date will be used
    * @param hour
    * @return Date
    */
   public Date getDateAtHour(Date date, int hour) {
      calendar.setTime(date != null ? date : new Date());
      calendar.set(Calendar.HOUR_OF_DAY, hour);
      return calendar.getTime();
   }
   


   
   protected List<Forecast> computeDailyForecasts() throws XPathExpressionException, ParseException {      
      List<TimeInterval> dailyDates = timeLayout0.intervals;
      
      NDFDSeries maxtSeries = ndfdSeriesMap.get(NDFD.maxt);
      NDFDSeries mintSeries = ndfdSeriesMap.get(NDFD.mint);
      NDFDSeries iconSeries =  ndfdSeriesMap.get(NDFD.icons);
      NDFDSeries iconInterpretationSeries =  ndfdSeriesMap.get(NDFD.interpretation);
      
      boolean inCelsius = maxtSeries.units.toLowerCase().startsWith("fahr");
      
      int forecastsLength = Math.min(dailyDates.size(), maxtSeries.size());
      forecastsLength = Math.min(forecastsLength, mintSeries.size());
       
      List<Forecast> forecasts = new ArrayList<Forecast>(forecastsLength);
      for (int f=0; f<forecastsLength; f++) {
         float high = maxtSeries.getFloatValue(f);
         float low = mintSeries.getFloatValue(f);
         
         Forecast forecast = new Forecast(new Date(dailyDates.get(f).startMS), low, high, inCelsius, 
               iconInterpretationSeries.getValue(f), iconSeries.getValue(f));
         forecasts.add(forecast);
      }
      
      return forecasts;
   }
   
   
   
   protected NDFDSeries parseNDFDSeries(NDFD ndfd) throws XPathExpressionException {  
     if (ndfd.xPathToNode != null) {  // a few special cases get parsed specially later...
         Node node = getNodeFromXPath(mainNode, ndfd.xPathToNode);
         if (node == null)
            System.out.println("foo");
         String timeLayoutName = getAttribute(node, NOAAWeather.TIME_LAYOUT);
         String units = getAttribute(node, "units");
         NodeList nodeList = getNodeListFromXPath(node, ndfd.listTag);
         List<String> values = ndfd.parseValues(nodeList);
         NDFDSeries ndfdSeries = new NDFDSeries(ndfd, timeLayoutMap.get(timeLayoutName), units, values);
         return ndfdSeries;         
      }
     else
        return new NDFDSeries(ndfd);
   }
     
   
   
   
   
   protected NDFDSeries computeIconInterpretations(NDFDSeries iconSeries) {
      ArrayList<String> interpretations = new ArrayList<String>(iconSeries.values.size());
      for (String iconName : iconSeries.values) {
         interpretations.add(interpretIconName(iconName));
      }
      
      return new NDFDSeries(NDFD.interpretation, iconSeries.timeLayout, "", interpretations);
   }
   

  
   protected List<Date> computePreferredHoursEachDay(List<Forecast> forecasts, int preferredHour) {
      
      List<Date> preferredHoursEachDay = new ArrayList<Date>(forecasts.size());
      for (Forecast f : forecasts) {
         calendar.setTime(f.getDate());
         calendar.set(Calendar.HOUR_OF_DAY, preferredHour);
         preferredHoursEachDay.add(calendar.getTime());
      }
      
      return preferredHoursEachDay;
   }
   
   
   protected void computeMoreInfo(Document document) {
      try {
         String moreInfoURL = getNodeFromXPath(mainNode, "moreWeatherInformation").getTextContent();
         moreInfo.put(MoreInfoKeys.OUT_URL.name(), moreInfoURL); 
         
         if (document != null) {
            String moreInfoDate = getStringFromXPath(document, "dwml/head/product/creation-date");
            moreInfo.put(MoreInfoKeys.DATE.name(), moreInfoDate);
         }
         
      }
      catch (XPathExpressionException xpe) {
         throw new Weather.Exception(xpe);
      }
   }

   
   
   
   /**
    * Parse an RFC 3339 timestamp
    * 
    * @param  dateString
    * @return Date
    * @throws ParseException
    * @see <a href="http://www.ietf.org/rfc/rfc3339.txt">IETF RFC Document</a>
    */
   public static synchronized Date parseRFC3339(String dateString) throws ParseException {
      int len = dateString.length();
      String removeLastColon = dateString.substring(0, len-3) + dateString.substring(len-2);
      return DATE_TIME_FORMAT.parse(removeLastColon);
   }

   /**
    * Generate an RFC 3339 timestamp
    * 
    * @param  inDate
    * @return String representation
    * @throws ParseException
    * @see <a href="http://www.ietf.org/rfc/rfc3339.txt">IETF RFC Document</a>
    */
   public static synchronized String formatRFC3339(Date inDate) throws ParseException {
      String withoutColon = DATE_TIME_FORMAT.format(inDate);
      int len = withoutColon.length();
      return withoutColon.substring(0, len-2) + ":" + withoutColon.substring(len-2);
   }

   
   protected Date adjustDateToPreferredHour(Date inDate) {
      calendar.setTime(inDate);
      calendar.set(Calendar.HOUR_OF_DAY, preferredHour);
      return calendar.getTime();
   }

   @Override
   public List<Condition> getConditions() {
      // TODO Auto-generated method stub
      return null;
   }
   
   
   
   /**
    * Retrieve all conditions for a specific measurement (e.g. max temp) through the forecast
    * @param ndfd
    * @return  null if that measurement was not requested.
    */
   public NDFDSeries getNDFDSeries(NDFD ndfd) {
      return ndfdSeriesMap.get(ndfd);
   }
   
   
}
