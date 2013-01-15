package com.lishid.orebfuscator.utils;

import java.util.ArrayList;

public class OrebfuscatorAsyncQueue<E>
{   
    private final Object lockObject = new Object();
    private ArrayList<E> list = new ArrayList<E>();
    
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
            lockObject.notifyAll();
        }
    }
    
    public E dequeue()
    {
        synchronized(lockObject)
        {
            while(list.size() <= 0)
            {
                try
                {
                    lockObject.wait();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
            return list.remove(list.size() - 1);
        }
    }
}
