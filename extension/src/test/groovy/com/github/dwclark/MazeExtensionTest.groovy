package com.github.dwclark

import spock.lang.Specification
import static MazeExtension.encode

class MazeExtensionTest extends Specification {
    
    def "all char and string extensions work"() {
        expect:
        '#'.wall
        ('#' as Character).wall
        !'#'.door
        ('A'..'Z').every { s ->
            Character c = s as Character
            (s.door && !s.wall && !s.blank) && (c.door && !c.wall && !c.blank)
        }
        
        ('a'..'z').every { s ->
            Character c = s as Character
            (s.key && !s.door && !s.wall && !s.blank) && (c.key && !c.door && !c.wall)
        }
        
        '.'.blank
        !'.'.door

        '@'.start
        ('@' as Character).start

        ('1'..'9').every { s ->
            Character c = s as Character
            s.start && c.start
        }

        'A'.keyFor == 'a'
        ('A' as Character).keyFor == 'a'
    }

    def 'all position extensions work'() {
        expect:
        encode(1,2).v == 1
        encode(1,2).h == 2
        encode(2,2).neighbors == [ encode(1,2), encode(3,2), encode(2,1), encode(2,3) ] as Long[]
    }

    def 'key extensions'() {
        expect:
        'a'.toKey() == 0b1
        'abcde'.toKey() == 0b11111
        'abe'.toKey() == 0b10011
        'abe'.toKey().canOpen('A' as Character)
        'abcde'.toKey().canOpen('ABCDE')
        !'abcde'.toKey().canOpen('ABCDEF')
        !0.canOpen('A')
        0.canOpen('')
        'abcde'.toKey().canOpen('')
    }
}
