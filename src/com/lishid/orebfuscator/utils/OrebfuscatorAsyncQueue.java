package com.lishid.orebfuscator.utils;

import java.util.ArrayDeque;

public class OrebfuscatorAsyncQueue<E>
{   
    private final Object lockObject = new Object();
    private ArrayDeque<E> list = new ArrayDeque<E>();
    
    public void clear()
    {
        synchronized(lockObject)
        {
            list.clear();
        }
    }
    
    public void queue(E input)
    {
        synchronized(lockObject)
        {
            list.add(input);
            lockObject.notify();
        }
    }
    
    public E dequeue() throws InterruptedException
    {
        synchronized(lockObject)
        {
            while(list.size() <= 0)
            {
                lockObject.wait();
            }
            return list.pop();
        }
    }
}
