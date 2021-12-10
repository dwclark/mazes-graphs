package com.github.dwclark

import groovy.transform.CompileStatic

@CompileStatic
class Vertex {
    final Character id
    final Integer keys
    
    public Vertex(Character id, Integer keys) {
        this.id = id
        this.keys = keys
    }

    Vertex plus(PossiblePath pp) {
        return new Vertex(pp.two, keys.addKey(pp.two))
    }
    
    boolean hasInterveningUnvisited(String keysOnWay) {
        return !keys.hasAllKeys(keysOnWay)
    }

    @Override
    String toString() {
        return "Vertex(id: ${id} keys: ${Integer.valueOf(keys).keyToString()})"
    }

    @Override
    boolean equals(Object o) {
        if(!(o instanceof Vertex))
            return false

        Vertex rhs = (Vertex) o
        return keys == rhs.keys && id == rhs.id
    }

    @Override
    int hashCode() { return 31 * keys + ((int) id.charValue()) }
}
