package org.mongodb.morphia.geo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.junit.Test;
import org.mongodb.morphia.TestBase;
import org.mongodb.morphia.testutil.JSONMatcher;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mongodb.morphia.geo.GeoJson.lineString;
import static org.mongodb.morphia.geo.GeoJson.point;

/**
 * Test driving features for Issue 643 - add support for saving entities with GeoJSON.
 */
public class GeoEntitiesTest extends TestBase {
    @Test
    public void shouldSaveAnEntityWithALocationStoredAsAPoint() {
        // given
        City city = new City("New City", GeoJson.pointBuilder().latitude(3.0).longitude(7.0).build());

        // when
        getDs().save(city);

        // then use the underlying driver to ensure it was persisted correctly to the database
        DBObject storedCity = getDs().getCollection(City.class).findOne(new BasicDBObject("name", "New City"),
                                                                        new BasicDBObject("_id", 0).append("className", 0));
        assertThat(storedCity, is(notNullValue()));
        assertThat(storedCity.toString(), JSONMatcher.jsonEqual("  {"
                                                                + " name: 'New City',"
                                                                + " location:  "
                                                                + " {"
                                                                + "  type: 'Point', "
                                                                + "  coordinates: [7.0, 3.0]"
                                                                + " }"
                                                                + "}"));
    }

    @Test
    public void shouldConvertPointCorrectlyToDBObject() {
        // given
        City city = new City("New City", point(3.0, 7.0));

        // when
        DBObject dbObject = getMorphia().toDBObject(city);

        assertThat(dbObject, is(notNullValue()));
        assertThat(dbObject.toString(), JSONMatcher.jsonEqual("  {"
                                                              + " name: 'New City',"
                                                              + " className: 'org.mongodb.morphia.geo.City',"
                                                              + " location:  "
                                                              + " {"
                                                              + "  type: 'Point', "
                                                              + "  coordinates: [7.0, 3.0]"
                                                              + " }"
                                                              + "}"));
    }

    @Test
    public void shouldRetrieveGeoJsonPoint() {
        // given
        City city = new City("New City", point(3.0, 7.0));
        getDs().save(city);

        // when
        City found = getDs().find(City.class).field("name").equal("New City").get();

        // then
        assertThat(found, is(notNullValue()));
        assertThat(found, is(city));
    }

    @Test
    public void shouldSaveAnEntityWithALineStringGeoJsonType() {
        // given
        Route route = new Route("My Route", lineString(point(1, 2), point(3, 5), point(19, 13)));

        // when
        getDs().save(route);

        // then use the underlying driver to ensure it was persisted correctly to the database
        DBObject storedRoute = getDs().getCollection(Route.class).findOne(new BasicDBObject("name", "My Route"),
                                                                          new BasicDBObject("_id", 0).append("className", 0));
        assertThat(storedRoute, is(notNullValue()));
        // lat/long is always long/lat on the server
        assertThat(storedRoute.toString(), JSONMatcher.jsonEqual("  {"
                                                                 + " name: 'My Route',"
                                                                 + " route:"
                                                                 + " {"
                                                                 + "  type: 'LineString', "
                                                                 + "  coordinates: [ [ 2.0,  1.0],"
                                                                 + "                 [ 5.0,  3.0],"
                                                                 + "                 [13.0, 19.0] ]"
                                                                 + " }"
                                                                 + "}"));
    }

    @Test
    public void shouldRetrieveGeoJsonLineString() {
        // given
        Route route = new Route("My Route", lineString(point(1, 2), point(3, 5), point(19, 13)));
        getDs().save(route);

        // when
        Route found = getDs().find(Route.class).field("name").equal("My Route").get();

        // then
        assertThat(found, is(notNullValue()));
        assertThat(found, is(route));
    }

    @Test
    public void shouldSaveAnEntityWithAPolygonGeoJsonType() {
        // given
        Area area = new Area("The Area", GeoJson.polygon(point(1.1, 2.0), point(2.3, 3.5), point(3.7, 1.0), point(1.1, 2.0)));

        // when
        getDs().save(area);

        // then use the underlying driver to ensure it was persisted correctly to the database
        DBObject storedArea = getDs().getCollection(Area.class).findOne(new BasicDBObject("name", "The Area"),
                                                                        new BasicDBObject("_id", 0)
                                                                        .append("className", 0)
                                                                        .append("area.className", 0));
        assertThat(storedArea, is(notNullValue()));
        assertThat(storedArea.toString(), JSONMatcher.jsonEqual("  {"
                                                                + " name: 'The Area',"
                                                                + " area:  "
                                                                + " {"
                                                                + "  type: 'Polygon', "
                                                                + "  coordinates: [ [ [ 2.0, 1.1],"
                                                                + "                   [ 3.5, 2.3],"
                                                                + "                   [ 1.0, 3.7],"
                                                                + "                   [ 2.0, 1.1] ] ]"
                                                                + " }"
                                                                + "}"));
    }

    @Test
    public void shouldRetrieveGeoJsonPolygon() {
        // given
        Area area = new Area("The Area", GeoJson.polygon(point(1.1, 2.0), point(2.3, 3.5), point(3.7, 1.0), point(1.1, 2.0)));
        getDs().save(area);

        // when
        Area found = getDs().find(Area.class).field("name").equal("The Area").get();

        // then
        assertThat(found, is(notNullValue()));
        assertThat(found, is(area));
    }

    @Test
    public void shouldSaveAnEntityWithAPolygonContainingInteriorRings() {
        // given
        String polygonName = "A polygon with holes";
        Polygon polygonWithHoles = GeoJson.polygonBuilder(point(1.1, 2.0), point(2.3, 3.5), point(3.7, 1.0), point(1.1, 2.0))
                                          .interiorRing(point(1.5, 2.0), point(1.9, 2.0), point(1.9, 1.8), point(1.5, 2.0))
                                          .interiorRing(point(2.2, 2.1), point(2.4, 1.9), point(2.4, 1.7), point(2.1, 1.8),
                                                        point(2.2, 2.1))
                                          .build();
        Area area = new Area(polygonName, polygonWithHoles);

        // when
        getDs().save(area);

        // then use the underlying driver to ensure it was persisted correctly to the database
        DBObject storedArea = getDs().getCollection(Area.class).findOne(new BasicDBObject("name", polygonName),
                                                                        new BasicDBObject("_id", 0)
                                                                        .append("className", 0)
                                                                        .append("area.className", 0));
        assertThat(storedArea, is(notNullValue()));
        assertThat(storedArea.toString(), JSONMatcher.jsonEqual("  {"
                                                                + " name: " + polygonName + ","
                                                                + " area:  "
                                                                + " {"
                                                                + "  type: 'Polygon', "
                                                                + "  coordinates: "
                                                                + "    [ [ [ 2.0, 1.1],"
                                                                + "        [ 3.5, 2.3],"
                                                                + "        [ 1.0, 3.7],"
                                                                + "        [ 2.0, 1.1] "
                                                                + "      ],"
                                                                + "      [ [ 2.0, 1.5],"
                                                                + "        [ 2.0, 1.9],"
                                                                + "        [ 1.8, 1.9],"
                                                                + "        [ 2.0, 1.5] "
                                                                + "      ],"
                                                                + "      [ [ 2.1, 2.2],"
                                                                + "        [ 1.9, 2.4],"
                                                                + "        [ 1.7, 2.4],"
                                                                + "        [ 1.8, 2.1],"
                                                                + "        [ 2.1, 2.2] "
                                                                + "      ]"
                                                                + "    ]"
                                                                + " }"
                                                                + "}"));
    }

    @Test
    public void shouldRetrieveGeoJsonMultiRingPolygon() {
        // given
        String polygonName = "A polygon with holes";
        Polygon polygonWithHoles = GeoJson.polygonBuilder(point(1.1, 2.0), point(2.3, 3.5), point(3.7, 1.0), point(1.1, 2.0))
                                          .interiorRing(point(1.5, 2.0), point(1.9, 2.0), point(1.9, 1.8), point(1.5, 2.0))
                                          .interiorRing(point(2.2, 2.1), point(2.4, 1.9), point(2.4, 1.7), point(2.1, 1.8),
                                                        point(2.2, 2.1))
                                          .build();
        Area area = new Area(polygonName, polygonWithHoles);
        getDs().save(area);

        // when
        Area found = getDs().find(Area.class).field("name").equal(polygonName).get();

        // then
        assertThat(found, is(notNullValue()));
        assertThat(found, is(area));
    }

    @Test
    public void shouldSaveAnEntityWithALocationStoredAsAMultiPoint() {
        // given
        String name = "My stores";
        Stores stores = new Stores(name, GeoJson.multiPoint(point(1, 2), point(3, 5), point(19, 13)));

        // when
        getDs().save(stores);

        // then use the underlying driver to ensure it was persisted correctly to the database
        DBObject storedObject = getDs().getCollection(Stores.class).findOne(new BasicDBObject("name", name),
                                                                            new BasicDBObject("_id", 0).append("className", 0));
        assertThat(storedObject, is(notNullValue()));
        assertThat(storedObject.toString(), JSONMatcher.jsonEqual("  {"
                                                                  + " name: " + name + ","
                                                                  + " locations:  "
                                                                  + " {"
                                                                  + "  type: 'MultiPoint', "
                                                                  + "  coordinates: [ [ 2.0,  1.0],"
                                                                  + "                 [ 5.0,  3.0],"
                                                                  + "                 [13.0, 19.0] ]"
                                                                  + " }"
                                                                  + "}"));
    }

    @Test
    public void shouldRetrieveGeoJsonMultiPoint() {
        // given
        String name = "My stores";
        Stores stores = new Stores(name, GeoJson.multiPoint(point(1, 2), point(3, 5), point(19, 13)));
        getDs().save(stores);

        // when
        Stores found = getDs().find(Stores.class).field("name").equal(name).get();

        // then
        assertThat(found, is(notNullValue()));
        assertThat(found, is(stores));
    }

    @Test
    public void shouldSaveAnEntityWithAMultiLineStringGeoJsonType() {
        // given
        String name = "Many Paths";
        Paths paths = new Paths(name, GeoJson.multiLineString(lineString(point(1, 2), point(3, 5), point(19, 13)),
                                                              lineString(point(1.5, 2.0),
                                                                         point(1.9, 2.0),
                                                                         point(1.9, 1.8),
                                                                         point(1.5, 2.0))));

        // when
        getDs().save(paths);

        // then use the underlying driver to ensure it was persisted correctly to the database
        DBObject storedPaths = getDs().getCollection(Paths.class).findOne(new BasicDBObject("name", name),
                                                                          new BasicDBObject("_id", 0).append("className", 0));
        assertThat(storedPaths, is(notNullValue()));
        // lat/long is always long/lat on the server
        assertThat(storedPaths.toString(), JSONMatcher.jsonEqual("  {"
                                                                 + " name: '" + name + "',"
                                                                 + " paths:"
                                                                 + " {"
                                                                 + "  type: 'MultiLineString', "
                                                                 + "  coordinates: "
                                                                 + "     [ [ [ 2.0,  1.0],"
                                                                 + "         [ 5.0,  3.0],"
                                                                 + "         [13.0, 19.0] "
                                                                 + "       ], "
                                                                 + "       [ [ 2.0, 1.5],"
                                                                 + "         [ 2.0, 1.9],"
                                                                 + "         [ 1.8, 1.9],"
                                                                 + "         [ 2.0, 1.5] "
                                                                 + "       ]"
                                                                 + "     ]"
                                                                 + " }"
                                                                 + "}"));
    }

    @Test
    public void shouldRetrieveGeoJsonMultiLineString() {
        // given
        String name = "Many Paths";
        Paths paths = new Paths(name, GeoJson.multiLineString(lineString(point(1, 2), point(3, 5), point(19, 13)),
                                                              lineString(point(1.5, 2.0),
                                                                         point(1.9, 2.0),
                                                                         point(1.9, 1.8),
                                                                         point(1.5, 2.0))));
        getDs().save(paths);

        // when
        Paths found = getDs().find(Paths.class).field("name").equal(name).get();

        // then
        assertThat(found, is(notNullValue()));
        assertThat(found, is(paths));
    }

    @Test
    public void shouldSaveAnEntityWithAMultiPolygonGeoJsonType() {
        // given
        String name = "All these shapes";
        Polygon polygonWithHoles = GeoJson.polygonBuilder(point(1.1, 2.0), point(2.3, 3.5), point(3.7, 1.0), point(1.1, 2.0))
                                          .interiorRing(point(1.5, 2.0), point(1.9, 2.0), point(1.9, 1.8), point(1.5, 2.0))
                                          .interiorRing(point(2.2, 2.1), point(2.4, 1.9), point(2.4, 1.7), point(2.1, 1.8),
                                                        point(2.2, 2.1))
                                          .build();
        Regions regions = new Regions(name, GeoJson.multiPolygon(GeoJson.polygon(point(1.1, 2.0),
                                                                                 point(2.3, 3.5),
                                                                                 point(3.7, 1.0),
                                                                                 point(1.1, 2.0)),
                                                                 polygonWithHoles));

        // when
        getDs().save(regions);

        // then use the underlying driver to ensure it was persisted correctly to the database
        DBObject storedRegions = getDs().getCollection(Regions.class).findOne(new BasicDBObject("name", name),
                                                                              new BasicDBObject("_id", 0)
                                                                              .append("className", 0));
        assertThat(storedRegions, is(notNullValue()));
        assertThat(storedRegions.toString(), JSONMatcher.jsonEqual("  {"
                                                                   + " name: '" + name + "',"
                                                                   + " regions:  "
                                                                   + " {"
                                                                   + "  type: 'MultiPolygon', "
                                                                   + "  coordinates: [ [ [ [ 2.0, 1.1],"
                                                                   + "                     [ 3.5, 2.3],"
                                                                   + "                     [ 1.0, 3.7],"
                                                                   + "                     [ 2.0, 1.1],"
                                                                   + "                   ]"
                                                                   + "                 ],"
                                                                   + "                 [ [ [ 2.0, 1.1],"
                                                                   + "                     [ 3.5, 2.3],"
                                                                   + "                     [ 1.0, 3.7],"
                                                                   + "                     [ 2.0, 1.1] "
                                                                   + "                   ],"
                                                                   + "                   [ [ 2.0, 1.5],"
                                                                   + "                     [ 2.0, 1.9],"
                                                                   + "                     [ 1.8, 1.9],"
                                                                   + "                     [ 2.0, 1.5] "
                                                                   + "                   ],"
                                                                   + "                   [ [ 2.1, 2.2],"
                                                                   + "                     [ 1.9, 2.4],"
                                                                   + "                     [ 1.7, 2.4],"
                                                                   + "                     [ 1.8, 2.1],"
                                                                   + "                     [ 2.1, 2.2] "
                                                                   + "                   ]"
                                                                   + "                 ]"
                                                                   + "               ]"
                                                                   + " }"
                                                                   + "}"));
    }

    @Test
    public void shouldRetrieveGeoJsonMultiPolygon() {
        // given
        String name = "All these shapes";
        Polygon polygonWithHoles = GeoJson.polygonBuilder(point(1.1, 2.0), point(2.3, 3.5), point(3.7, 1.0), point(1.1, 2.0))
                                          .interiorRing(point(1.5, 2.0), point(1.9, 2.0), point(1.9, 1.8), point(1.5, 2.0))
                                          .interiorRing(point(2.2, 2.1), point(2.4, 1.9), point(2.4, 1.7), point(2.1, 1.8),
                                                        point(2.2, 2.1))
                                          .build();
        Regions regions = new Regions(name, GeoJson.multiPolygon(GeoJson.polygon(point(1.1, 2.0),
                                                                                 point(2.3, 3.5),
                                                                                 point(3.7, 1.0),
                                                                                 point(1.1, 2.0)),
                                                                 polygonWithHoles));
        getDs().save(regions);

        // when
        Regions found = getDs().find(Regions.class).field("name").equal(name).get();

        // then
        assertThat(found, is(notNullValue()));
        assertThat(found, is(regions));
    }

    @Test
    public void shouldSaveAnEntityWithAGeoCollectionType() {
        // given
        String name = "What, everything?";
        Point point = point(3.0, 7.0);
        LineString lineString = lineString(point(1, 2), point(3, 5), point(19, 13));
        Polygon polygonWithHoles = GeoJson.polygonBuilder(point(1.1, 2.0), point(2.3, 3.5), point(3.7, 1.0), point(1.1, 2.0))
                                          .interiorRing(point(1.5, 2.0), point(1.9, 2.0), point(1.9, 1.8), point(1.5, 2.0))
                                          .interiorRing(point(2.2, 2.1), point(2.4, 1.9), point(2.4, 1.7), point(2.1, 1.8),
                                                        point(2.2, 2.1))
                                          .build();
        MultiPoint multiPoint = GeoJson.multiPoint(point(1, 2), point(3, 5), point(19, 13));
        MultiLineString multiLineString = GeoJson.multiLineString(lineString(point(1, 2), point(3, 5), point(19, 13)),
                                                                  lineString(point(1.5, 2.0),
                                                                             point(1.9, 2.0),
                                                                             point(1.9, 1.8),
                                                                             point(1.5, 2.0)));
        MultiPolygon multiPolygon = GeoJson.multiPolygon(GeoJson.polygon(point(1.1, 2.0),
                                                                         point(2.3, 3.5),
                                                                         point(3.7, 1.0),
                                                                         point(1.1, 2.0)),
                                                         GeoJson.polygonBuilder(point(1.2, 3.0),
                                                                                point(2.5, 4.5),
                                                                                point(6.7, 1.9),
                                                                                point(1.2, 3.0))
                                                                .interiorRing(point(3.5, 2.4),
                                                                              point(1.7, 2.8),
                                                                              point(3.5, 2.4))
                                                                .build());
        GeometryCollection geometryCollection = GeoJson.geometryCollectionBuilder()
                                                       .add(point)
                                                       .add(lineString)
                                                       .add(polygonWithHoles)
                                                       .add(multiPoint)
                                                       .add(multiLineString)
                                                       .add(multiPolygon)
                                                       .build();
        AllTheThings allTheThings = new AllTheThings(name, geometryCollection);

        // when
        getDs().save(allTheThings);

        // then use the underlying driver to ensure it was persisted correctly to the database
        DBObject storedArea = getDs().getCollection(AllTheThings.class).findOne(new BasicDBObject("name", name),
                                                                                new BasicDBObject("_id", 0)
                                                                                .append("className", 0));
        assertThat(storedArea, is(notNullValue()));
        assertThat(storedArea.toString(), JSONMatcher.jsonEqual("  {"
                                                                + " name: '" + name + "',"
                                                                + " everything: "
                                                                + " {"
                                                                + "  type: 'GeometryCollection', "
                                                                + "  geometries: "
                                                                + "  ["
                                                                + "    {"
                                                                + "     type: 'Point', "
                                                                + "     coordinates: [7.0, 3.0]"
                                                                + "    }, "
                                                                + "    {"
                                                                + "     type: 'LineString', "
                                                                + "     coordinates: [ [ 2.0,  1.0],"
                                                                + "                    [ 5.0,  3.0],"
                                                                + "                    [13.0, 19.0] ]"
                                                                + "    },"
                                                                + "    {"
                                                                + "     type: 'Polygon', "
                                                                + "     coordinates: "
                                                                + "       [ [ [ 2.0, 1.1],"
                                                                + "           [ 3.5, 2.3],"
                                                                + "           [ 1.0, 3.7],"
                                                                + "           [ 2.0, 1.1] "
                                                                + "         ],"
                                                                + "         [ [ 2.0, 1.5],"
                                                                + "           [ 2.0, 1.9],"
                                                                + "           [ 1.8, 1.9],"
                                                                + "           [ 2.0, 1.5] "
                                                                + "         ],"
                                                                + "         [ [ 2.1, 2.2],"
                                                                + "           [ 1.9, 2.4],"
                                                                + "           [ 1.7, 2.4],"
                                                                + "           [ 1.8, 2.1],"
                                                                + "           [ 2.1, 2.2] "
                                                                + "         ]"
                                                                + "       ]"
                                                                + "    },"
                                                                + "    {"
                                                                + "     type: 'MultiPoint', "
                                                                + "     coordinates: [ [ 2.0,  1.0],"
                                                                + "                    [ 5.0,  3.0],"
                                                                + "                    [13.0, 19.0] ]"
                                                                + "    },"
                                                                + "    {"
                                                                + "     type: 'MultiLineString', "
                                                                + "     coordinates: "
                                                                + "        [ [ [ 2.0,  1.0],"
                                                                + "            [ 5.0,  3.0],"
                                                                + "            [13.0, 19.0] "
                                                                + "          ], "
                                                                + "          [ [ 2.0, 1.5],"
                                                                + "            [ 2.0, 1.9],"
                                                                + "            [ 1.8, 1.9],"
                                                                + "            [ 2.0, 1.5] "
                                                                + "          ]"
                                                                + "        ]"
                                                                + "    },"
                                                                + "    {"
                                                                + "     type: 'MultiPolygon', "
                                                                + "     coordinates: [ [ [ [ 2.0, 1.1],"
                                                                + "                        [ 3.5, 2.3],"
                                                                + "                        [ 1.0, 3.7],"
                                                                + "                        [ 2.0, 1.1],"
                                                                + "                      ]"
                                                                + "                    ],"
                                                                + "                    [ [ [ 3.0, 1.2],"
                                                                + "                        [ 4.5, 2.5],"
                                                                + "                        [ 1.9, 6.7],"
                                                                + "                        [ 3.0, 1.2] "
                                                                + "                      ],"
                                                                + "                      [ [ 2.4, 3.5],"
                                                                + "                        [ 2.8, 1.7],"
                                                                + "                        [ 2.4, 3.5] "
                                                                + "                      ],"
                                                                + "                    ]"
                                                                + "                  ]"
                                                                + "    }"
                                                                + "  ]"
                                                                + " }"
                                                                + "}"));
    }

    @Test
    public void shouldRetrieveGeoCollectionType() {
        // given
        String name = "What, everything?";
        Point point = point(3.0, 7.0);
        LineString lineString = lineString(point(1, 2), point(3, 5), point(19, 13));
        Polygon polygonWithHoles = GeoJson.polygonBuilder(point(1.1, 2.0), point(2.3, 3.5), point(3.7, 1.0), point(1.1, 2.0))
                                          .interiorRing(point(1.5, 2.0), point(1.9, 2.0), point(1.9, 1.8), point(1.5, 2.0))
                                          .interiorRing(point(2.2, 2.1), point(2.4, 1.9), point(2.4, 1.7), point(2.1, 1.8),
                                                        point(2.2, 2.1))
                                          .build();
        MultiPoint multiPoint = GeoJson.multiPoint(point(1, 2), point(3, 5), point(19, 13));
        MultiLineString multiLineString = GeoJson.multiLineString(lineString(point(1, 2), point(3, 5), point(19, 13)),
                                                                  lineString(point(1.5, 2.0),
                                                                             point(1.9, 2.0),
                                                                             point(1.9, 1.8),
                                                                             point(1.5, 2.0)));
        MultiPolygon multiPolygon = GeoJson.multiPolygon(GeoJson.polygon(point(1.1, 2.0),
                                                                         point(2.3, 3.5),
                                                                         point(3.7, 1.0),
                                                                         point(1.1, 2.0)),
                                                         GeoJson.polygonBuilder(point(1.2, 3.0),
                                                                                point(2.5, 4.5),
                                                                                point(6.7, 1.9),
                                                                                point(1.2, 3.0))
                                                                .interiorRing(point(3.5, 2.4),
                                                                              point(1.7, 2.8),
                                                                              point(3.5, 2.4))
                                                                .build());
        GeometryCollection geometryCollection = GeoJson.geometryCollectionBuilder()
                                                       .add(point)
                                                       .add(lineString)
                                                       .add(polygonWithHoles)
                                                       .add(multiPoint)
                                                       .add(multiLineString)
                                                       .add(multiPolygon)
                                                       .build();
        AllTheThings allTheThings = new AllTheThings(name, geometryCollection);
        getDs().save(allTheThings);

        // when
        AllTheThings found = getDs().find(AllTheThings.class).field("name").equal(name).get();

        // then
        assertThat(found, is(notNullValue()));
        assertThat(found, is(allTheThings));
    }

    @SuppressWarnings("UnusedDeclaration")
    private static final class Route {
        private String name;
        private LineString route;

        private Route() {
        }

        private Route(final String name, final LineString route) {
            this.name = name;
            this.route = route;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Route route1 = (Route) o;

            if (name != null ? !name.equals(route1.name) : route1.name != null) {
                return false;
            }
            if (route != null ? !route.equals(route1.route) : route1.route != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (route != null ? route.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Route{"
                   + "name='" + name + '\''
                   + ", route=" + route
                   + '}';
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    private static final class Area {
        private String name;
        private Polygon area;

        private Area() {
        }

        private Area(final String name, final Polygon area) {
            this.name = name;
            this.area = area;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Area area1 = (Area) o;

            if (area != null ? !area.equals(area1.area) : area1.area != null) {
                return false;
            }
            if (name != null ? !name.equals(area1.name) : area1.name != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (area != null ? area.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Area{"
                   + "name='" + name + '\''
                   + ", area=" + area
                   + '}';
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    private static final class Stores {
        private String name;
        private MultiPoint locations;

        private Stores() {
        }

        private Stores(final String name, final MultiPoint locations) {
            this.name = name;
            this.locations = locations;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Stores stores = (Stores) o;

            if (!locations.equals(stores.locations)) {
                return false;
            }
            if (!name.equals(stores.name)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + locations.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "Stores{"
                   + "name='" + name + '\''
                   + ", locations=" + locations
                   + '}';
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    private static final class Paths {
        private String name;
        private MultiLineString paths;

        private Paths() {
        }

        private Paths(final String name, final MultiLineString paths) {
            this.name = name;
            this.paths = paths;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Paths paths1 = (Paths) o;

            if (!name.equals(paths1.name)) {
                return false;
            }
            if (!paths.equals(paths1.paths)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + paths.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "Paths{"
                   + "name='" + name + '\''
                   + ", paths=" + paths
                   + '}';
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    private static final class Regions {
        private String name;
        private MultiPolygon regions;

        private Regions() {
        }

        private Regions(final String name, final MultiPolygon regions) {
            this.name = name;
            this.regions = regions;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Regions regions1 = (Regions) o;

            if (!name.equals(regions1.name)) {
                return false;
            }
            if (!regions.equals(regions1.regions)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + regions.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "Regions{"
                   + "name='" + name + '\''
                   + ", regions=" + regions
                   + '}';
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    private static final class AllTheThings {
        private GeometryCollection everything;
        private String name;

        private AllTheThings() {
        }

        private AllTheThings(final String name, final GeometryCollection everything) {
            this.name = name;
            this.everything = everything;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            AllTheThings that = (AllTheThings) o;

            if (!everything.equals(that.everything)) {
                return false;
            }
            if (!name.equals(that.name)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = everything.hashCode();
            result = 31 * result + name.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "AllTheThings{"
                   + "everything=" + everything
                   + ", name='" + name + '\''
                   + '}';
        }
    }
}
