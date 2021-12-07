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

    def 'test populate possible paths'() {
        setup:
        def maze = new Maze(s1, false)
        def paths = maze.allPossible('@' as Character)

        expect:
        paths.find { p -> p.two == 'b' && p.distance == 4 && p.doors == 'A' }
        paths.find { p -> p.two == 'a' && p.distance == 2 && p.doors == '' }
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

    def 'test beast possible paths'() {
        setup:
        def maze = new Maze(beast, false)
        def paths = maze.allPossible('@' as Character)

        expect:
        'bfag'.every { c -> paths.find { p -> p.two == c && p.distance == 3 && p.doors == '' } }
        'cedh'.every { c -> paths.find { p -> p.two == c && p.distance == 5 && p.doors == '' } }
        [ ['i','G'], ['p','H'], ['l','F'], ['m','C'] ].every { pair ->
            paths.find { p -> p.two == pair[0] && p.distance == 10 && p.doors == pair[1] }
        }
        [ ['j','A'], ['o','D'], ['k','E'], ['n','B'] ].every { pair ->
            paths.find { p -> p.two == pair[0] && p.distance == 8 && p.doors == pair[1] }
        }
    }

    def 'test beast possible paths from p'() {
        setup:
        def maze = new Maze(beast, false)
        def paths = maze.allPossible('p' as Character)

        expect:
        paths.find { p -> p.two == 'm' && p.distance == 20 && p.doors == 'CH' }
        paths.find { p -> p.two == 'o' && p.distance == 16 && p.doors == 'DH' }
    }

    def 'test beast all possible paths'() {
        setup:
        def maze = new Maze(beast, false)
        17 == maze.pathCache.size()
        maze.pathCache.every { c, list -> list.size() == 16 || list.size() == 15 }
    }

    @Ignore
    def 'test solve simple'() {
        setup:
        def maze = new Maze(s1, true)
        def path = maze.shortest()
        
        expect:
        path.distance == 8
        path.hops == '@ab'
    }

    @Ignore
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

    @Ignore
    def 'test solve s3'(){
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

    @Ignore
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

    @Ignore
    def 'test solve s5'() {
        setup:
        def maze = new Maze(s5, true)
        def path = maze.shortest()
        
        expect:
        path.distance == 81
    }

    //bfs solution arrived at ixzpnfmtaejvrcyqwoldksubgh
    //we stop at    ixzpnfm (1132)
    //why, we found ixpzfnm (1132) before, therefore we did not add, therefore no children were explored
    def 'test solve part 1'() {
        setup:
        String s = MazeTest.classLoader.getResourceAsStream('part1.txt').text
        def maze = new Maze(s, false)
        def path = maze.shortest()
        println path.distance
    }

    @Ignore
    def 'test path cache'() {
        setup:
        String s = MazeTest.classLoader.getResourceAsStream('part1.txt').text
        def maze = new Maze(s, true)
        println maze
        println()
        maze.pathCache.each { n, paths ->
            println "For $n:"
            paths.eachWithIndex { path, i -> println "    ${i+1}: ${path}" } }
    }
}
