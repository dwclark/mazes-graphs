package com.github.dwclark

import groovy.transform.CompileStatic
import static MazeExtension.MAZE_NULL

@CompileStatic
class PossiblePath {
    final Character one
    final Character two
    final String doors
    final String keysOnWay
    final int distance

    PossiblePath(Character one, Character two, String doors,
                 String keysOnWay, int distance) {
        this.one = one
        this.two = two
        this.doors = doors
        this.keysOnWay = keysOnWay
        this.distance = distance
    }
    
    static PossiblePath starting(Character c) {
        return new PossiblePath(c, MAZE_NULL, '', '', 0)
    }

    PossiblePath continuing() {
        return new PossiblePath(one, MAZE_NULL, doors, keysOnWay + two, distance)
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
    
    PossiblePath plus(Character c) {
        assert !c.wall
        Character newTwo = c.key ? c : two
        String newDoors = nextDoors(c)
        String newKeysOnWay = keysOnWay + c
        int newDistance = distance + 1
        return new PossiblePath(one, newTwo, newDoors, keysOnWay, newDistance)
    }

    PossiblePath plus(String s) {
        assert s.length() == 1
        return plus(s as Character)
    }

    boolean isComplete() {
        return two != MAZE_NULL && !two.start
    }

    @Override
    String toString() {
        return "($one,$two) distance: $distance doors: $doors"
    }
}
