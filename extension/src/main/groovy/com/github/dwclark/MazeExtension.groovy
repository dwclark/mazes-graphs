package com.github.dwclark

import groovy.transform.CompileStatic

@CompileStatic
class MazeExtension {
    static final Character MAZE_NULL = Character.valueOf(' ' as char)
    static final int DIFF = 32
    static final char WALL = '#' as char
    static final char DOOR_LOWER = 'A' as char
    static final char DOOR_UPPER = 'Z' as char
    static final char KEY_LOWER = 'a' as char
    static final char KEY_UPPER = 'z' as char
    static final char BLANK = '.' as char
    static final char START = '@' as char
    static final char START_LOWER = '1' as char
    static final char START_UPPER = '9' as char
    
    static char extract(String s) {
        if(s != null && s.length() == 1)
            return s.charAt(0)
        else
            throw new IllegalArgumentException('invalid maze element')
    }

    static boolean isWall(final Character c) {
        return c.charValue() == WALL
    }

    static boolean isWall(final String s) {
        return extract(s) == WALL
    }

    static boolean isDoor(final Character c) {
        char val = c.charValue()
        return (DOOR_LOWER <= val) && (val <= DOOR_UPPER)
    }

    static boolean isDoor(final String s) {
        char val = extract(s)
        return (DOOR_LOWER <= val) && (val <= DOOR_UPPER)
    }

    static boolean isKey(final Character c) {
        char val = c.charValue()
        return (KEY_LOWER <= val) && (val <= KEY_UPPER)
    }

    static boolean isKey(final String s) {
        char val = extract(s);
        return (KEY_LOWER <= val) && (val <= KEY_UPPER)
    }

    static boolean isBlank(final Character c) {
        return c.charValue() == BLANK
    }

    static boolean isBlank(final String s) {
        return extract(s) == BLANK
    }

    static boolean isStart(final Character c) {
        char val = c.charValue()
        return val == START || ((START_LOWER <= val) && (val <= START_UPPER))
    }

    static boolean isStart(final String s) {
        char val = extract(s)
        return val == START || ((START_LOWER <= val) && (val <= START_UPPER))
    }

    static Character getKeyFor(final Character c) {
        assert isDoor(c)
        int val = c.charValue() as int
        return Character.valueOf((val + 32) as char)
    }

    static String getKeyFor(final String s) {
        assert isDoor(s)
        int val = extract(s) as int
        return (val + 32) as char
    }

    static int getV(final Long arg) {
        long val = arg.longValue();
        return 0xFFFF_FFFF & (val >>> 32)
    }

    static int getH(final Long arg) {
        long val = arg.longValue();
        return 0xFFFF_FFFFF & val
    }

    static Long encode(int v, int h) {
        long lngV = (long) v
        long lngH = (long) h
        return Long.valueOf((lngV << 32) | lngH)
    }

    static Long[] getNeighbors(final Long arg) {
        int v = getV(arg)
        int h = getH(arg)
        return [encode(v-1,h), encode(v+1,h),
                encode(v,h-1), encode(v,h+1)] as Long[]
    }

    static int _key(char c) {
        if(isStart(c)) {
            return 0
        }

        if(isKey(c)) {
            int shiftBy = (c.charValue() as int) - (KEY_LOWER as int)
            return (1 << shiftBy)
        }

        throw new IllegalArgumentException("$c is not a start or key")
    }
    
    static Integer toKey(Character c) {
        return _key(c)
    }
    
    static Integer toKey(String s) {
        int ret = 0
        for(int i = 0; i < s.length(); ++i) {
            ret |= _key(s.charAt(i))
        }
        
        return ret
    }

    static String keyToString(Integer k) {
        int theKey = k.intValue()
        StringBuilder sb = new StringBuilder()
        for(char c = KEY_LOWER; c <= KEY_UPPER; ++c) {
            if((_key(c) & theKey) != 0) {
                sb.append(c)
            }
        }

        return sb.toString()
    }

    static int getNumKeys(Integer k) {
        return Integer.bitCount(k.intValue())
    }

    static boolean hasKey(Integer i, Character c) {
        int otherKey = _key(c.charValue())
        return (i.intValue() & otherKey) != 0
    }

    static boolean hasKey(Integer i, String s) {
        assert s.length() == 1
        int otherKey = _key(s.charAt(0))
        return (i.intValue() & otherKey) != 0
    }

    static boolean hasAllKeys(Integer theKey, String s) {
        int me = theKey.intValue()
        int them = toKey(s).intValue();
        return (me & them) == them
    }

    static Integer addKey(Integer i, Character c) {
        int otherKey = _key(c.charValue())
        return Integer.valueOf(i.intValue() | otherKey)
    }

    static boolean canOpen(Integer key, Character c) {
        assert isDoor(c)
        int asKey = toKey(c.toLowerCase())
        int self = key.intValue()
        return (self & asKey) != 0
    }

    static boolean canOpen(Integer key, String s) {
        if(s.length() == 0) {
            return true
        }
        
        for(int i = 0; i < s.length(); ++i) {
            if(!canOpen(key, Character.valueOf(s.charAt(i)))) {
                return false
            }
        }

        return true
    }
}
