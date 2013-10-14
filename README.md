NOAA-weather
============

Java / Android code for collecting, parsing, and organizing weather data collected from [NOAA](http://www.noaa.gov/), 
specifically, the [NOAA REST API](http://graphical.weather.gov/xml/rest.php)

Usage
=====

[JavaDocs are here](http://morganconrad.github.com/NOAA-weather/gh-pages/index-all.html)

Getting and parsing the data
----------------------------

1. Create a NOAAWeather
		NOAAWeather weather = new NOAAWeather();
2. Set the dates of interest using one form of setDates
		weather.setDates(new Date(), 10);      // next 10 days
3. Set the location
		weather.setLocation(39.096, -94.595);  // Kansas City here I come
4. By default, this will obtain data for min and maximum temperature, temperature, and general conditions.
   If you want more, e.g. wind direction and wind gusts, call addNDFDParameters.
		weather.addNDFDParameters("wdir", "wgust");
5. You are ready to call().  This may take a few seconds so you might want to use an Executor etc.
		weather.call();

Using the results
-----------------

1. There is some "metadata" in a Map<String, String>.  Current keys are enumerated in <NOAAWeather.MoreInfoKeys>
		<String theirURL = weather.getMoreInfo().get(NOAAWeather.MoreInfoKeys.OUT_URL.name());>
2. You can obtain all data for the time period, for a certain measurement, as an NDFDSeries
		<NDFDSeries allMaxTs = weather.getNDFDSeries(NDFD.maxt);>
3. More to come...
		
		
Developed By
============

* Morgan Conrad - <flyingspaniel@gmail.com>

License
=======

    Copyright 2013 Morgan Conrad

    Licensed under the LGPL (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.gnu.org/copyleft/lesser.html

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
