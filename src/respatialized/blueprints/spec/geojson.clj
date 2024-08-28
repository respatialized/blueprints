(ns respatialized.blueprints.spec.geojson
  "spec implementation for a Clojure equivalent of the of RFC7946 specification for GeoJSON."
  (:require [clojure.spec.alpha :as spec]))


;; 1.  Introduction

;;    GeoJSON is a format for encoding a variety of geographic data
;;    structures using JavaScript Object Notation (JSON) [RFC7159].  A
;;    GeoJSON object may represent a region of space (a Geometry), a
;;    spatially bounded entity (a Feature), or a list of Features (a
;;    FeatureCollection).  GeoJSON supports the following geometry types:
;;    Point, LineString, Polygon, MultiPoint, MultiLineString,
;;    MultiPolygon, and GeometryCollection.  Features in GeoJSON contain a
;;    Geometry object and additional properties, and a FeatureCollection
;;    contains a list of Features.

;;    The format is concerned with geographic data in the broadest sense;
;;    anything with qualities that are bounded in geographical space might
;;    be a Feature whether or not it is a physical structure.  The concepts
;;    in GeoJSON are not new; they are derived from preexisting open
;;    geographic information system standards and have been streamlined to
;;    better suit web application development using JSON.

;;    GeoJSON comprises the seven concrete geometry types defined in the
;;    OpenGIS Simple Features Implementation Specification for SQL [SFSQL]:
;;    0-dimensional Point and MultiPoint; 1-dimensional curve LineString
;;    and MultiLineString; 2-dimensional surface Polygon and MultiPolygon;

;; and the heterogeneous GeometryCollection.  GeoJSON representations of
;; instances of these geometry types are analogous to the well-known
;; binary (WKB) and well-known text (WKT) representations described in
;; that same specification.

;; GeoJSON also comprises the types Feature and FeatureCollection.
;; Feature objects in GeoJSON contain a Geometry object with one of the
;; above geometry types and additional members.  A FeatureCollection
;; object contains an array of Feature objects.  This structure is
;; analogous to that of the Web Feature Service (WFS) response to
;; GetFeatures requests specified in [WFSv1] or to a Keyhole Markup
;; Language (KML) Folder of Placemarks [KMLv2.2].  Some implementations
;; of the WFS specification also provide GeoJSON-formatted responses to
;; GetFeature requests, but there is no particular service model or
;; Feature type ontology implied in the GeoJSON format specification.

;; Since its initial publication in 2008 [GJ2008], the GeoJSON format
;; specification has steadily grown in popularity.  It is widely used in
;; JavaScript web-mapping libraries, JSON-based document databases, and
;; web APIs.

;; 1.1.  Requirements Language

;; The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT",
;; "SHOULD", "SHOULD NOT", "RECOMMENDED", "NOT RECOMMENDED", "MAY", and
;; "OPTIONAL" in this document are to be interpreted as described in
;; [RFC2119].

;; 1.2.  Conventions Used in This Document

;; The ordering of the members of any JSON object defined in this
;; document MUST be considered irrelevant, as specified by [RFC7159].

;; Some examples use the combination of a JavaScript single-line comment
;; (//) followed by an ellipsis (...) as placeholder notation for
;; content deemed irrelevant by the authors.  These placeholders must of
;; course be deleted or otherwise replaced, before attempting to
;; validate the corresponding JSON code example.

;; Whitespace is used in the examples inside this document to help
;; illustrate the data structures, but it is not required.  Unquoted
;; whitespace is not significant in JSON.

;; 1.3.  Specification of GeoJSON

;; This document supersedes the original GeoJSON format specification
;; [GJ2008].

;; 1.4.  Definitions

;; o  JavaScript Object Notation (JSON), and the terms object, member,
;; name, value, array, number, true, false, and null, are to be
;; interpreted as defined in [RFC7159].

;; o  Inside this document, the term "geometry type" refers to seven
;; case-sensitive strings: "Point", "MultiPoint", "LineString",
;; "MultiLineString", "Polygon", "MultiPolygon", and
;; "GeometryCollection".

;; o  As another shorthand notation, the term "GeoJSON types" refers to
;; nine case-sensitive strings: "Feature", "FeatureCollection", and
;; the geometry types listed above.

;; o  The word "Collection" in "FeatureCollection" and
;; "GeometryCollection" does not have any significance for the
;; semantics of array members.  The "features" and "geometries"
;; members, respectively, of these objects are standard ordered JSON
;; arrays, not unordered sets.

;; 1.5.  Example

;; A GeoJSON FeatureCollection:

(def section-1.5-example
  {"type"     "FeatureCollection"
   "features" [{"type"       "Feature"
                "geometry"   {"type" "Point" "coordinates" [102.0 0.5]}
                "properties" {"prop0" "value0"}}
               {"type"       "Feature"
                "geometry"   {"type"        "LineString"
                              "coordinates" [[102.0 0.0] [103.0 1.0] [104.0 0.0]
                                             [105.0 1.0]]}
                "properties" {"prop0" "value0" "prop1" 0.0}}
               {"type"       "Feature"
                "geometry"   {"type"        "Polygon"
                              "coordinates" [[[100.0 0.0] [101.0 0.0]
                                              [101.0 1.0] [100.0 1.0]
                                              [100.0 0.0]]]}
                "properties" {"prop0" "value0" "prop1" {"this" "that"}}}]})

;; 2.  GeoJSON Text

;; A GeoJSON text is a JSON text and consists of a single GeoJSON
;; object.

;; 3.  GeoJSON Object

;; A GeoJSON object represents a Geometry, Feature, or collection of
;; Features.

;; o  A GeoJSON object is a JSON object.

;; o  A GeoJSON object has a member with the name "type".  The value of
;; the member MUST be one of the GeoJSON types.

;; o  A GeoJSON object MAY have a "bbox" member, the value of which MUST
;; be a bounding box array (see Section 5).

;; o  A GeoJSON object MAY have other members (see Section 6).

;; 3.1.  Geometry Object

;; A Geometry object represents points, curves, and surfaces in
;; coordinate space.  Every Geometry object is a GeoJSON object no
;; matter where it occurs in a GeoJSON text.

;; o  The value of a Geometry object's "type" member MUST be one of the
;; seven geometry types (see Section 1.4).

;; o  A GeoJSON Geometry object of any type other than
;; "GeometryCollection" has a member with the name "coordinates".
;; The value of the "coordinates" member is an array.  The structure
;; of the elements in this array is determined by the type of
;; geometry.  GeoJSON processors MAY interpret Geometry objects with
;; empty "coordinates" arrays as null objects.

(spec/def :longitude double?)
(spec/def :latitude double?)

;; NOTE: the reason for unbounded lat/lon definitions is to conform with §4:
;; "...where all involved parties have a prior arrangement, alternative
;; coordinate reference systems can be used without risk of data being
;; misinterpreted."

(spec/def :geojson/position
  ;; TODO: support arrays of additional elements that may be ignored?
  (spec/or (spec/tuple :longitude :latitude)
           ;; altitude
           (spec/tuple :longitude :latitude double?)))


;; spec ties keyword and predicate together too tightly for
;; the :coordinates key to have a contextual meaning

(spec/def :geojson/point (s/keys :req []))
