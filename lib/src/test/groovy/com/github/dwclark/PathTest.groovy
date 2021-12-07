package com.github.dwclark

import spock.lang.*

class PathTest extends Specification {

    def 'test compare'() {
        expect:
        new Path("abc", 10) < new Path("sdf", 11)
        new Path("abc", 11) > new Path("sdf", 10)
        (new Path("abc", 10) <=> new Path("sdf", 10)) == 0
    }

    def 'test next hop'() {
        setup:
        def pp = new PossiblePath('b' as char, 'c' as char, 'AB', 'y', 6)

        expect:
        new Path('abc', 10) == new Path('ab', 4).nextHop(pp)
        new Path('abc', 10) == new Path('ab', 4) + pp
    }

    def 'test visited'(){
        expect:
        new Path('abc', 10).visited('a' as char)
        !new Path('abc', 10).visited('k' as char)
    }

    def 'test current hop'() {
        expect:
        new Path('abcdef', 10).currentHop == 'f'
    }

    def 'test has intervening unvisited'() {
        setup:
        def p = new Path('abc', 10)

        expect:
        p.hasInterveningUnvisited('f')
        p.hasInterveningUnvisited('abck')
        !p.hasInterveningUnvisited('ab')
    }

    def 'test get node'() {
        setup:
        def one = new Path('abc', 10)
        def two = new Path('bac', 10)
        def three = new Path('acb', 10)
        def four = new Path('abefghijc', 10)

        expect:
        one.node == two.node
        one.node != three.node
        one.node != four.node
    }
}
