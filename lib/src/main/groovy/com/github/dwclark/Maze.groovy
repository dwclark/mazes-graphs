package com.github.dwclark

import groovy.transform.CompileStatic
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import static MazeExtension.encode

@CompileStatic
class Maze {
    final String[] contents
    final Map<Character,Long> ofInterest = [:]
    final Map<Character,List<PossiblePath>> pathCache = [:]
    
    Maze(List<String> list, boolean simplify) {
        this(list as String[], simplify)
    }
    
    Maze(String[] contents, boolean simplify) {
        this.contents = simplify ? _deadEndFill(contents) : contents
        this.ofInterest = populateOfInterest().asImmutable()
        this.pathCache = allPossible().asImmutable()
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

    private boolean canAdd(Long val) {
        return (0 <= val.v && val.v < contents.length &&
                0 <= val.h && val.h < contents[0].length() &&
                !getAt(val).wall)
    }

    public List<Long> neighbors(Long val) {
        List<Long> tmp = new ArrayList<>(4)
        int v = val.v
        int h = val.h

        Long up = encode(v-1, h)
        if(canAdd(up))
            tmp.add(up)

        Long down = encode(v+1, h)
        if(canAdd(down))
            tmp.add(down)

        Long left = encode(v, h-1)
        if(canAdd(left))
            tmp.add(left)

        Long right = encode(v, h+1)
        if(canAdd(right))
            tmp.add(right)

        return tmp
    }

    private void _allPossible(Set<Long> visited, List<PossiblePath> paths,
                              Long location, PossiblePath path) {
        PossiblePath current = path
        if(current.complete) {
            paths.add(current)
            current = current.continuing()
        }

        List<Long> neighbors = neighbors(location)
        for(int i = 0; i < neighbors.size(); ++i) {
            Long n = neighbors[i]
            if(visited.add(n)) {
                _allPossible(visited, paths, n, current + getAt(n))
            }
        }
    }

    public List<PossiblePath> allPossible(Character from) {
        List<PossiblePath> ret = []
        Long startAt = getAt(from)
        Set<Long> visited = [ startAt ] as Set<Long>;
        
        _allPossible(visited, ret, startAt, PossiblePath.starting(from))
        return ret.asImmutable()
    }
    
    public Map<Character,List<PossiblePath>> allPossible() {
        Map<Character,List<PossiblePath>> ret = [:]
        (starts + keys).each { Character c ->
            ret[c] = allPossible(c)
        }

        return ret
    }

    static class Path implements Comparable<Path> {
        final String visits
        final Vertex vertex
        final int distance

        Path(Vertex vertex, int distance) {
            this(vertex, '', distance)
        }

        Path(Vertex vertex, String visits, int distance) {
            this.vertex = vertex
            this.visits = visits
            this.distance = distance
        }

        Path plus(PossiblePath pp) {
            return new Path(vertex + pp, visits + pp.two, distance + pp.distance)
        }
        
        @Override
        int compareTo(Path other) {
            return Integer.compare(distance, other.distance)
        }
        
        @Override
        String toString() {
            return "Path(vertex: ${vertex} distance: ${distance}, visits: ${visits})"
        }
    }
    
    private void addPaths(Path current, Set<Vertex> visited, PriorityQueue<Path> pending) {
        Vertex vertex = current.vertex
        List<PossiblePath> possiblePaths = pathCache[vertex.id]
        for(int i = 0; i < possiblePaths.size(); ++i) {
            PossiblePath pp = possiblePaths.get(i)
            if(!vertex.keys.hasKey(pp.two) && //not already visited
               vertex.keys.canOpen(pp.doors) && //we can open everything on the way
               !vertex.hasInterveningUnvisited(pp.keysOnWay)) { //can't pass through unvisited keys
                Path newPath = current + pp
                if(!visited.contains(newPath.vertex)) {
                    pending.add(newPath)
                }
            }
        }
    }
    
    public Path shortest() {
        PriorityQueue<Path> pending = new PriorityQueue<Path>(0xFFFF)
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
            addPaths(current, visited, pending)
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
