package com.github.dwclark

import groovy.transform.CompileStatic
import groovy.transform.TailRecursive
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import static MazeExtension.encode

@CompileStatic
class Maze {
    final static List<int[]> TO_ADD = List.<int[]>of([-1,0] as int[], [0,-1] as int[],
                                                     [1,0] as int[],  [0,1] as int[])
    final String[] contents
    final Map<Character,Long> ofInterest = [:]
    final Map<Character,List<Edge>> edgeCache = [:]
    
    Maze(List<String> list, boolean simplify) {
        this(list as String[], simplify)
    }
    
    Maze(String[] contents, boolean simplify) {
        this.contents = simplify ? _deadEndFill(contents) : contents
        this.ofInterest = populateOfInterest().asImmutable()
        this.edgeCache = allEdges().asImmutable()
    }

    Maze(String s, boolean simplify) {
        this(s.split('\n') as String[], simplify)
    }

    Long getAt(Character c) {
        return ofInterest[c]
    }

    Long getAt(String s) {
        return getAt(s.charAt(0))
    }

    Character getAt(Long pos) {
        return contents[pos.v][pos.h] as Character
    }

    Character at(int v, int h) {
        return contents[v][h] as Character
    }

    Collection<Character> getDoors() {
        return ofInterest.keySet().findAll { it.door }
    }

    Collection<Character> getKeys() {
        return ofInterest.keySet().findAll { it.key }
    }

    Collection<Character> getStarts() {
        return ofInterest.keySet().findAll { it.start }
    }

    @Override
    public String toString() {
        return contents.join('\n')
    }

    public Maze deadEndFill() {
        return new Maze(contents, true)
    }

    private void eachNeighbor(Long location, Closure c) {
        eachNeighbor(location.v, location.h, c)
    }
    
    private void eachNeighbor(int v, int h, Closure c) {
        TO_ADD.each { int[] toAdd ->
            int newV = v + toAdd[0];
            int newH = h + toAdd[1];

            if(0 <= newV && newV < contents.length &&
               0 <= newH && newH < contents[0].length() &&
               !at(newV, newH).wall) {
                c.call(encode(newV, newH))
            }
        }
    }

    public List<Edge> allEdges(Character from) {
        List<Edge> ret = []
        Deque<Edge> stack = new ArrayDeque<>()
        Edge starting = Edge.starting(from, getAt(from))
        Set<Long> visited = new HashSet<>()
        stack.push(starting)

        while(stack.size() != 0) {
            Edge edge = stack.pop()
            if(edge.complete) {
                ret.add(edge.complete())
                edge = edge.continuing()
            }
            
            Long location = edge.destinationLocation
            visited.add(location)
            eachNeighbor(location) { Long neighbor ->
                if(!visited.contains(neighbor)) {
                    stack.push(edge.nextEdge(getAt(neighbor), neighbor))
                }
            }
        }
        
        return ret.asImmutable()
    }
    
    public Map<Character,List<Edge>> allEdges() {
        Map<Character,List<Edge>> ret = [:]
        (starts + keys).each { Character c ->
            ret[c] = allEdges(c)
        }
        
        return ret
    }

    static class Path implements Comparable<Path> {
        final Vertex vertex
        final int distance

        Path(Vertex vertex, int distance) {
            this.vertex = vertex
            this.distance = distance
        }

        Path plus(Edge e) {
            return new Path(vertex + e, distance + e.distance)
        }
        
        @Override
        int compareTo(Path other) {
            return Integer.compare(distance, other.distance)
        }
        
        @Override
        String toString() {
            return "Path(vertex: ${vertex} distance: ${distance})"
        }

        @Override
        boolean equals(Object o) {
            if(!(o instanceof Path)) {
                return false
            }
            
            Path rhs = (Path) o
            return distance == rhs.distance && vertex == rhs.vertex
        }

        @Override
        int hashCode() {
            return 31 * distance + vertex.hashCode()
        }
    }
    
    private void addEdges(Path current, Set<Vertex> visited, NoDupPQ<Path> pending) {
        Vertex vertex = current.vertex
        List<Edge> possibleEdges = edgeCache[vertex.id]
        for(int i = 0; i < possibleEdges.size(); ++i) {
            Edge e = possibleEdges.get(i)
            
            if(e.legal(vertex)) {
                Path newPath = current + e
                if(!visited.contains(newPath.vertex)) {
                    pending.add(newPath)
                }
            }
        }
    }
    
    public Path shortest() {
        NoDupPQ<Path> pending = new NoDupPQ<>();
        pending.add(new Path(new Vertex('@' as char, 0), 0))
        Path current = null
        Integer neededKeys = keys.join('').toKey()
        Set<Vertex> visited = new HashSet<>()
        
        while((current = pending.poll()) != null) {
            Vertex v = current.vertex
            if(v.keys == neededKeys) {
                return current
            }
            
            visited.add(v)
            addEdges(current, visited, pending)
        }
        
        return null
    }

    private Map<Character,Long> populateOfInterest() {
        Map<Character,Long> ret = [:]
        for(int v = 0; v < contents.length; ++v) {
            for(int h = 0; h < contents[0].length(); ++h) {
                Character c = contents[v][h] as Character
                if(c.door || c.key || c.start) {
                    ret[c] = encode(v,h)
                }
            }
        }

        return ret
    }

    private String[] _deadEndFill(String[] ary) {
        List<List<Character>> tmp = ary.collect { it as Character[] as List<Character> }
        def wallCount = { int v, int h ->
            if(v < 0 || v >= tmp.size() || h < 0 || h >= tmp[0].size()) {
                return 0
            }
            else {
                return tmp[v][h].wall ? 1 : 0
            }
        }
        
        boolean keepGoing = true
        while(keepGoing) {
            keepGoing = false
            for(int v = 0; v < tmp.size(); ++v) {
                for(int h = 0; h < tmp[0].size(); ++h) {
                    Character c = tmp[v][h]
                    if(c.blank || c.door) {
                        if((wallCount(v+1,h) + wallCount(v-1,h) +
                            wallCount(v,h+1) + wallCount(v,h-1)) >= 3) {
                            tmp[v][h] = '#' as Character
                            keepGoing = true
                        }
                    }
                }
            }
        }

        return tmp.collect { new String(it as char[]) } as String[]
    }
}
