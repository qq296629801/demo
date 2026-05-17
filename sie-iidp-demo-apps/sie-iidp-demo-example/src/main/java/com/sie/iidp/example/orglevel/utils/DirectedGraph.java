package com.sie.iidp.example.orglevel.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * 有向图
 */
public class DirectedGraph {

    private Map<String, List<String>> adjacencyList;
    private final String DEFAULT_EMPTY_NODE_VAL = "-9999";

    public DirectedGraph() {
        adjacencyList = new HashMap<>();
    }

    public void addEdge(String source, String destination) {
        if (StringUtils.isEmpty(source)) {
            source = DEFAULT_EMPTY_NODE_VAL;
        }
        if (StringUtils.isEmpty(destination)) {
            destination = DEFAULT_EMPTY_NODE_VAL;
        }
        adjacencyList.putIfAbsent(source, new ArrayList<>());
        adjacencyList.get(source).add(destination);
    }

    /**
     * 判断能否删除边
     *
     * @param source
     * @param destination
     * @return
     */
    public boolean canRemoveEdge(String source, String destination) {
        return !adjacencyList.containsKey(destination);
//        if (!adjacencyList.containsKey(source) && !adjacencyList.containsKey(destination)) {
//            return false; // 不存在的节点边不可删除
//        }
//        // 进行深度优先搜索，判断是否存在层级关系
//        Set<String> visited = new HashSet<>();
//        return !hasHierarchy(source, destination, visited);
    }

    private boolean hasHierarchy(String source, String destination, Set<String> visited) {
        if (source.equals(destination)) {
            return true; // 找到层级关系
        }

        visited.add(source);
        List<String> neighbors = adjacencyList.get(source);
        if (neighbors != null) {
            for (String neighbor : neighbors) {
                if (!visited.contains(neighbor) && hasHierarchy(neighbor, destination, visited)) {
                    return true;
                }
            }
        }

        return false; // 未找到层级关系
    }

    public boolean hasCycle() {
        Set<String> visited = new HashSet<>();
        for (String node : adjacencyList.keySet()) {
            if (hasCycleUtil(node, visited)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasCycleUtil(String node, Set<String> visited) {
        visited.add(node);
        List<String> neighbors = adjacencyList.get(node);
        if (neighbors != null) {
            for (String neighbor : neighbors) {
                if (!visited.contains(neighbor) && hasCycleUtil(neighbor, visited)) {
                    return true;
                } else if (visited.contains(neighbor)) {
                    return true;
                }
            }
        }
        visited.remove(node);
        return false;
    }
}