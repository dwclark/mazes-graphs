package com.github.dwclark

import groovy.transform.CompileStatic

@CompileStatic
abstract class EncodedPath {

    static long nextHop(long val, PossiblePath pp) {
        int distance = distance(val) + pp.distance
        char lastHop = pp.two
        int newKeys = ((int) (0xFFFF_FFFFL & val)) | Character.valueOf(lastHop).toKey()
        return encode(distance, lastHop, newKeys)
    }

    static long encode(int distance, char lastHop, int keys) {
        return (((long) distance) << 40) | (((long) lastHop) << 32) | (long) keys
    }

    static boolean visited(long val, Character c) {
        int myKey = (int) (0xFFFF_FFFFL & val)
        int theirKey = c.toKey()
        return (myKey & theirKey) != 0
    }

    static boolean canOpen(long val, String doors) {
        Integer myKey = (int) (0xFFFF_FFFFL & val)
        return myKey.canOpen(doors)
    }

    static boolean hasInterveningUnvisited(long val, String keysOnWay) {
        Integer myKey = (int) (0xFFFF_FFFFL & val)
        for(int i = 0; i < keysOnWay.length(); ++i) {
            Character c = Character.valueOf(keysOnWay.charAt(i))
            int theirKey = c.toKey()
            if((theirKey | myKey) != myKey) {
                return true
            }
        }

        return false;
    }

    static Character currentHop(long val) {
        return Character.valueOf((char) (0xFF & (val >>> 32)))
    }

    static int keys(long val) {
        return (int) (0xFFFF_FFFFL & val)
    }

    static int distance(long val) {
        return (int) (0xFF_FFFFL & (val >>> 40))
    }
}
