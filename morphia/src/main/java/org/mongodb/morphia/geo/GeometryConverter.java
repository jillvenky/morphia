package org.mongodb.morphia.geo;

import com.mongodb.DBObject;
import org.mongodb.morphia.converters.SimpleValueConverter;
import org.mongodb.morphia.converters.TypeConverter;
import org.mongodb.morphia.mapping.MappedField;

import java.util.List;

import static org.mongodb.morphia.geo.GeoJsonType.LINE_STRING;
import static org.mongodb.morphia.geo.GeoJsonType.MULTI_LINE_STRING;
import static org.mongodb.morphia.geo.GeoJsonType.MULTI_POINT;
import static org.mongodb.morphia.geo.GeoJsonType.MULTI_POLYGON;
import static org.mongodb.morphia.geo.GeoJsonType.POINT;
import static org.mongodb.morphia.geo.GeoJsonType.POLYGON;

/**
 * A Morphia TypeConverter that knows how to turn things that are labelled with the Geometry interface into the correct concrete class,
 * based on the GeoJSON type.
 * <p/>
 * Only implements the decode method as the concrete classes can encode themselves without needing a converter. It's when they come out of
 * the database that there's not enough information for Morphia to automatically create Geometry instances.
 */
public class GeometryConverter extends TypeConverter implements SimpleValueConverter {
    /**
     * Sets up this converter to work with
     */
    public GeometryConverter() {
        super(Geometry.class);
    }

    @Override
    @SuppressWarnings("unchecked") // always going to have to cast getting raw objects from the database
    public Object decode(final Class<?> targetClass, final Object fromDBObject, final MappedField optionalExtraInfo) {
        DBObject dbObject = (DBObject) fromDBObject;
        String type = (String) dbObject.get("type");
        if (type.equals(POINT.getType())) {
            List<Double> coordinates = (List<Double>) dbObject.get("coordinates");
            return new Point(coordinates);
        } else if (type.equals(LINE_STRING.getType())) {
            List<List<Double>> coordinates = (List<List<Double>>) dbObject.get("coordinates");
            return new LineString(coordinates);
        } else if (type.equals(POLYGON.getType())) {
            List<List<List<Double>>> coordinates = (List<List<List<Double>>>) dbObject.get("coordinates");
            return new Polygon(coordinates);
        } else if (type.equals(MULTI_POINT.getType())) {
            List<List<Double>> coordinates = (List<List<Double>>) dbObject.get("coordinates");
            return new MultiPoint(coordinates);
        } else if (type.equals(MULTI_LINE_STRING.getType())) {
            List<List<List<Double>>> coordinates = (List<List<List<Double>>>) dbObject.get("coordinates");
            return new MultiLineString(coordinates);
        } else if (type.equals(MULTI_POLYGON.getType())) {
            List<List<List<List<Double>>>> coordinates = (List<List<List<List<Double>>>>) dbObject.get("coordinates");
            return new MultiPolygon(coordinates);
        }
        return null;
    }
}