package com.github.dwclark

import groovy.transform.CompileStatic
import static MazeExtension.KEY_LOWER

@CompileStatic
class Path implements Comparable<Path> {
    final String hops
    final int distance
    final Integer keys
    
    public Path(String hops, int distance) {
        this.hops = hops
        this.distance = distance
        this.keys = hops.toKey()
    }

    public int getNode() {
        int minVal = (int) (hops.charAt(hops.length() - 1) - KEY_LOWER)
        return keys | (minVal << 27)
    }

    Path nextHop(PossiblePath pp) {
        return new Path(hops + pp.two, distance + pp.distance)
    }

    Path plus(PossiblePath pp) {
        return nextHop(pp)
    }

    int compareTo(Path other) {
        return Integer.compare(distance, other.distance)
    }

    boolean visited(Character c) {
        return hops.indexOf((int) c.charValue()) != -1
    }

    boolean hasInterveningUnvisited(String keysOnWay) {
        for(int i = 0; i < keysOnWay.length(); ++i) {
            if(hops.indexOf((int) keysOnWay.charAt(i)) == -1) {
                return true
            }
        }

        return false;
    }

    Character getCurrentHop() {
        return Character.valueOf(hops.charAt(hops.length() - 1))
    }
    
    @Override
    String toString() {
        return "'$hops' $distance"
    }

    @Override
    boolean equals(Object o) {
        if(!(o instanceof Path))
            return false

        Path rhs = (Path) o
        return hops == rhs.hops && distance == rhs.distance
    }

    @Override
    int hashCode() { return 31 * hops.hashCode() + distance }
}
