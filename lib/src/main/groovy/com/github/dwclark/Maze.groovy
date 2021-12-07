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

    private void nextPaths(Path current, IntOpenHashSet visited, PriorityQueue<Path> pending) {
        List<Path> ret = []
        boolean doLogging = false
        if(current.hops == 'ixzpnfm') {
            println "#################### Found ixzpnfm"
            doLogging = true
        }
        
        List<PossiblePath> possiblePaths = pathCache[current.currentHop]
        for(int i = 0; i < possiblePaths.size(); ++i) {
            PossiblePath pp = possiblePaths.get(i)
            if(!current.visited(pp.two) && //not alread visited
               current.keys.canOpen(pp.doors) && //we can open everything on the way
               !current.hasInterveningUnvisited(pp.keysOnWay)) { //can't pass through unvisited keys
                Path path = current.nextHop(pp)
                if(doLogging) {
                    println "#################### Next Path ${path}"
                }
                if(!pending.contains(path.node)) {
                    if(doLogging) {
                        println "#################### Adding ${path}"
                    }
                    
                    pending.add(current.nextHop(pp))
                }
            }
        }
    }

    public Path shortest() {
        PriorityQueue<Path> pending = new PriorityQueue<Path>(0xFFFF)
        pending.add(new Path('@', 0))
        Path current = null
        Integer neededKeys = keys.join('').toKey()
        IntOpenHashSet visited = new IntOpenHashSet()
        
        while((current = pending.poll()) != null) {
            println "testing ${current}"
            if(current.keys == neededKeys) {
                return current
            }
            
            if(visited.add(current.node)) { //pq does not de-duplicate
                nextPaths(current, visited, pending)
            }
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
