package com.github.dwclark

import groovy.transform.CompileStatic
import static MazeExtension.MAZE_NULL

@CompileStatic
class Edge {
    final Character source
    final Character destination
    final Long destinationLocation
    final String doors
    final String keysOnWay
    final int distance

    Edge(Character source, Character destination, Long location, String doors, String keysOnWay, int distance) {
        this.source = source
        this.destination = destination
        this.destinationLocation = location
        this.doors = doors
        this.keysOnWay = keysOnWay
        this.distance = distance
    }
    
    static Edge starting(Character source, Long location) {
        return new Edge(source, MAZE_NULL, location, '', '', 0)
    }

    String nextDoors(Character c) {
        if(c.door) {
            List<Character> tmp = (doors.toCharArray() as List<Character>) + c
            tmp.sort()
            return new String(tmp as char[])
        }
        else {
            return doors
        }
    }

    String nextKeysOnWay(Character c) {
        if(c.key) {
            List<Character> tmp = (keysOnWay.toCharArray() as List<Character>) + c
            tmp.sort()
            return new String(tmp as char[])
        }
        else {
            return keysOnWay
        }
    }
    
    Edge nextEdge(Character c, Long location) {
        assert !c.wall
        return new Edge(source, c.key ? c : destination, location, nextDoors(c), nextKeysOnWay(c), distance + 1)
    }

    Edge nextEdge(String s, Long location) {
        assert s.length() == 1
        return nextEdge(s as Character, location)
    }

    boolean isComplete() {
        return destination != MAZE_NULL && !destination.start
    }

    Edge complete() {
        return new Edge(source, destination, destinationLocation, doors, keysOnWay - destination, distance)
    }

    Edge continuing() {
        return new Edge(source, MAZE_NULL, destinationLocation, doors, keysOnWay, distance)
    }

    public boolean legal(Vertex v) {
        return (v.keys.canOpen(doors) && //only if doors are open
                v.keys.hasAllKeys(keysOnWay) && //only if no uncollected keys on way
                !v.keys.hasKey(destination))
    }

    @Override
    String toString() {
        return "($source,$destination) distance: $distance doors: $doors keysOnWay: ${keysOnWay}"
    }
}
