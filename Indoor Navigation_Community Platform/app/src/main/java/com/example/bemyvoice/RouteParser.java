package com.example.bemyvoice;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class RouteParser {

    public static List<Route> parseRoutes(InputStream inputStream) throws XmlPullParserException, IOException {
        List<Route> routes = new ArrayList<>();
        XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
        XmlPullParser parser = xmlPullParserFactory.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(inputStream, null);

        int eventType = parser.getEventType();
        Route currentRoute = null;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String tagName = parser.getName();

            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if ("route".equals(tagName)) {
                        currentRoute = new Route();
                        currentRoute.setId(Integer.parseInt(parser.getAttributeValue(null, "id")));
                    } else if (currentRoute != null) {
                        switch (tagName) {
                            case "startLocation":
                                currentRoute.setStartLatitude(Double.parseDouble(parser.nextText()));
                                break;
                            case "endLocation":
                                currentRoute.setEndLatitude(Double.parseDouble(parser.nextText()));
                                break;
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if ("route".equals(tagName) && currentRoute != null) {
                        routes.add(currentRoute);
                        currentRoute = null;
                    }
                    break;
            }

            eventType = parser.next();
        }

        return routes;
    }
}
