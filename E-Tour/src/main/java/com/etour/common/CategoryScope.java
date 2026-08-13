package com.etour.common;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.etour.entities.Category;

public final class CategoryScope {

    private static final int MAX_DEPTH = 20;

    private static final Map<String, Set<String>> THEMES = Map.of(
            "SPT", Set.of("CKD", "ECD", "SLC"),
            "CKD", Set.of("ECD", "SLC"),
            "ECD", Set.of("CKD", "SLC"),
            "SLC", Set.of("CKD", "ECD")
    );

    private CategoryScope() {
    }

    public static Set<Integer> resolve(Integer categoryId, Collection<Category> allCategories) {

        Set<Integer> scope = new LinkedHashSet<>();
        if (categoryId == null) {
            return scope;
        }

        Map<Integer, List<Integer>> childrenOf = new HashMap<>();
        Map<Integer, String> codeOf = new HashMap<>();
        Map<String, List<Integer>> idsByCode = new HashMap<>();

        for (Category c : allCategories) {
            if (c.getCategoryId() == null) {
                continue;
            }
            codeOf.put(c.getCategoryId(), c.getCatCode());

            if (c.getCatCode() != null) {
                idsByCode.computeIfAbsent(c.getCatCode(), k -> new java.util.ArrayList<>())
                         .add(c.getCategoryId());
            }
            if (c.getParent() != null && c.getParent().getCategoryId() != null) {
                childrenOf.computeIfAbsent(c.getParent().getCategoryId(),
                                           k -> new java.util.ArrayList<>())
                          .add(c.getCategoryId());
            }
        }

        addWithDescendants(categoryId, childrenOf, scope);

        String code = codeOf.get(categoryId);
        if (code != null) {
            for (String member : THEMES.getOrDefault(code, Set.of())) {
                for (Integer id : idsByCode.getOrDefault(member, List.of())) {
                    addWithDescendants(id, childrenOf, scope);
                }
            }
        }

        return scope;
    }

    private static void addWithDescendants(Integer rootId,
                                           Map<Integer, List<Integer>> childrenOf,
                                           Set<Integer> into) {

        Deque<Integer> queue = new ArrayDeque<>();
        queue.add(rootId);

        int guard = 0;
        while (!queue.isEmpty() && guard++ < MAX_DEPTH * 100) {
            Integer current = queue.poll();
            if (current == null || !into.add(current)) {
                continue;
            }
            queue.addAll(childrenOf.getOrDefault(current, List.of()));
        }
    }
}
