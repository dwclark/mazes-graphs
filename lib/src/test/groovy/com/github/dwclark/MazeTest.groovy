package com.github.dwclark

import spock.lang.*
import static MazeExtension.encode

class MazeTest extends Specification {

    String s1 = """
#########
#b.A.@.a#
#########
""".trim()
    
    def 'test of interest'() {
        setup:
        def maze = new Maze(s1, false)

        expect:
        maze['@'] == encode(1,5)
        maze['b'] == encode(1,1)
        maze['A'] == encode(1,3)
        maze[encode(1,3)] == 'A' as Character
        maze.at(1,3) == 'A' as Character
        maze.at(0,0).wall
        maze.doors as Set == ['A' as Character] as Set
        maze.keys as Set == ['a' as Character, 'b' as Character] as Set
        maze.starts as Set == [ '@' as Character ] as Set
        maze.toString() == s1
    }

    String s2 = """
##########
#A..a..@.#
#.######.#
#.######.#
#.######.#
##########
""".trim()

    String simplified2 = """
##########
####a..@##
##########
##########
##########
##########
""".trim()
    
    def 'test simplify'() {
        setup:
        def maze = new Maze(s2, true)

        expect:
        maze.toString() == simplified2
    }

    def 'test populate edges'() {
        setup:
        def maze = new Maze(s1, false)
        def edges = maze.allEdges('@' as Character)

        expect:
        edges.size() == 2
        edges.find { e -> e.destination == 'b' && e.distance == 4 && e.doors == 'A' }
        edges.find { e -> e.destination == 'a' && e.distance == 2 && e.doors == '' }
    }

    String beast = """
#################
#i.G..c...e..H.p#
########.########
#j.A..b...f..D.o#
########@########
#k.E..a...g..B.n#
########.########
#l.F..d...h..C.m#
#################
""".trim()

    def 'test beast edges'() {
        setup:
        def maze = new Maze(beast, false)
        def edges = maze.allEdges('@' as Character)
        def fromk = maze.allEdges('k' as Character)
        
        expect:
        'bfag'.every { c -> edges.find { e -> e.destination == c && e.distance == 3 && e.doors == '' } }
        'cedh'.every { c -> edges.find { e -> e.destination == c && e.distance == 5 && e.doors == '' } }
        edges.find { e -> e.destination == 'i' && e.distance == 10 && e.doors == 'G' && e.keysOnWay == 'c' }
        edges.size() == 'abcdefghijklmnop'.length()
        'abcdefghijklmnop'.every { maze.allEdges(it as Character).size() == 15 }
        fromk.find { e -> e.destination == 'p' && e.distance == 18 && e.doors == 'EH' && e.keysOnWay == 'ae' }
        fromk.find { e -> e.destination == 'm' && e.distance == 16 && e.doors == 'CE' && e.keysOnWay == 'ah' }
    }

    def 'test legal edges'() {
        setup:
        def maze = new Maze(beast, true)
        def fromjTok = maze.allEdges('j' as Character).find { e -> e.destination == 'k' }
        def fromaTof = maze.allEdges('a' as Character).find { e -> e.destination == 'f' }

        expect:
        fromjTok.legal(new Vertex('j', 'abe'))
        !fromjTok.legal(new Vertex('j', 'ae'))
        !fromjTok.legal(new Vertex('j', 'ab'))
        !fromjTok.legal(new Vertex('j', 'be'))
        !fromjTok.legal(new Vertex('j', 'abek'))

        fromaTof.legal(new Vertex('a', ''))
        !fromaTof.legal(new Vertex('a', 'f'))
        fromaTof.legal(new Vertex('a', 'ceb'))
    }
    
    def 'test solve simple'() {
        setup:
        def maze = new Maze(s1, true)
        def path = maze.shortest()
        
        expect:
        path.distance == 8
    }

    def 'test solve beast'() {
        setup:
        def maze = new Maze(beast, true)
        def path = maze.shortest()
        
        expect:
        path.distance == 136
    }

    String s3 = """
########################
#f.D.E.e.C.b.A.@.a.B.c.#
######################.#
#d.....................#
########################
""".trim()

    def 'test solve s3'() {
        setup:
        def maze = new Maze(s3, true)
        def path = maze.shortest()
        
        expect:
        path.distance == 86
    }

    String s4 = """
########################
#...............b.C.D.f#
#.######################
#.....@.a.B.c.d.A.e.F.g#
########################
""".trim()

    def 'test solve s4'() {
        setup:
        def maze = new Maze(s4, true)
        def path = maze.shortest()
        
        expect:
        path.distance == 132
    }

    String s5 = """
########################
#@..............ac.GI.b#
###d#e#f################
###A#B#C################
###g#h#i################
########################
""".trim()

    def 'test solve s5'() {
        setup:
        def maze = new Maze(s5, true)
        def path = maze.shortest()
        
        expect:
        path.distance == 81
    }

    def 'test solve part 1'() {
        setup:
        String s = MazeTest.classLoader.getResourceAsStream('part1.txt').text
        def maze = new Maze(s, true)
        def path = maze.shortest()

        expect:
        path.distance == 5068
    }
}
