package com.github.dwclark

import groovy.transform.CompileStatic

@CompileStatic
class NoDupPQ<T> {
    Set<T> theSet = new HashSet<T>()
    PriorityQueue<T> queue = new PriorityQueue<>()

    void add(T val) {
        if(theSet.add(val))
            queue.add(val)
    }

    T poll() {
        T ret = queue.poll()
        theSet.remove(ret)
        return ret
    }
}
