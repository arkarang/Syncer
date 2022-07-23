package com.minepalm.syncer.player.recovery;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class SearchResult {

    private final Map<Long, SimplePlayerLog> map;

    public SearchResult(Collection<SimplePlayerLog> collection){
        map = new ConcurrentHashMap<>();
    }

    public List<Long> sortDescendingTaskTime(){
        return null;
    }

    public List<Long> sortAscendingTaskTime(){
        return null;
    }

    public SimplePlayerLog get(long taskId){
        return null;
    }

    public int size(){
        return map.size();
    }

}
