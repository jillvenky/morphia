package org.mongodb.morphia.geo;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents either a simple polygon enclosing an area, or a more complex polygon that contains both an exterior boundary and 
 * interior boundaries (holes) within it.  
 */
public class Polygon {
    private final String type = "Polygon";
    private final List<List<List<Double>>> coordinates = new ArrayList<List<List<Double>>>();

    @SuppressWarnings("UnusedDeclaration") // used by Morphia
    Polygon() {
    }

    Polygon(final LineString exteriorBoundary, final List<LineString> interiorBoundaries) {
        this.coordinates.add(exteriorBoundary.getCoordinates());
        for (final LineString interiorBoundary : interiorBoundaries) {
            this.coordinates.add(interiorBoundary.getCoordinates());
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Polygon that = (Polygon) o;

        if (!coordinates.equals(that.coordinates)) {
            return false;
        }
        if (!type.equals(that.type)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + coordinates.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "MultiRingPolygon{"
               + "type='" + type + '\''
               + ", coordinates=" + coordinates
               + '}';
    }

    List<List<List<Double>>> getCoordinates() {
        return coordinates;
    }
}
