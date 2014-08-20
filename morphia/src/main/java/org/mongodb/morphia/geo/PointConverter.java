package org.mongodb.morphia.geo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.mongodb.morphia.converters.SimpleValueConverter;
import org.mongodb.morphia.converters.TypeConverter;
import org.mongodb.morphia.mapping.MappedField;

import java.util.List;

/**
 * Converts the Point class from and to MongoDB-shaped DBObjects. This means the Point class can be a prettier, most usable object since all
 * the serialisation logic is here.
 */
public class PointConverter extends TypeConverter implements SimpleValueConverter {
    /**
     * Create a new converter.  Registers itself to convert Point classes.
     */
    public PointConverter() {
        super(Point.class);
    }

    @Override
    @SuppressWarnings("unchecked") // always going to have unchecked warnings when converting from/to the raw DBObject
    public Object encode(final Object value, final MappedField optionalExtraInfo) {
        double[] coordinates = CoordinateConverter.encode((Point) value);
        return new BasicDBObject("type", GeoJsonType.POINT.getType())
               .append("coordinates", getMapper().getConverters().encode(coordinates));
    }

    @Override
    @SuppressWarnings("unchecked") // always going to have unchecked warnings when converting from/to the raw DBObject
    public Object decode(final Class<?> targetClass, final Object fromDBObject, final MappedField optionalExtraInfo) {
        DBObject dbObject = (DBObject) fromDBObject;
        List<Double> coordinates = (List<Double>) dbObject.get("coordinates");
        return CoordinateConverter.decode(coordinates);
    }
}