/*
 * Ant Group
 * Copyright (c) 2004-2025 All Rights Reserved.
 */
package com.alipay.FinMathBench.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

/**
 * @author luojing.wp
 * @version MapCartesianProduct.java, v 0.1 November 10, 2025 7:38 PM luojing.wp
 */
public class MapCartesianProduct {

    // Use Pair to represent (key, value) pairs
    public static <A, B> List<List<Pair<A, B>>> generateProduct(Map<A, List<B>> inputMap) {
        List<List<Pair<A, B>>> result = new ArrayList<>();

        // Handle special cases: if input is null or any list is empty, the Cartesian product is empty
        if (inputMap == null || inputMap.isEmpty()) {
            return result;
        }
        for (List<B> list : inputMap.values()) {
            if (list == null || list.isEmpty()) {
                return result; // If any list is empty, the final Cartesian product is empty
            }
        }

        // To maintain order, we convert keys to a list. If input is LinkedHashMap, the order will be preserved.
        List<A> keys = new ArrayList<>(inputMap.keySet());

        // Start the recursive process
        generateRecursive(inputMap, keys, 0, new ArrayList<>(), result);

        return result;
    }

    private static <A, B> void generateRecursive(
            Map<A, List<B>> inputMap,
            List<A> keys,
            int keyIndex,
            List<Pair<A, B>> currentCombination,
            List<List<Pair<A, B>>> allCombinations) {

        // Base condition: when all keys are processed, a combination is complete
        if (keyIndex == keys.size()) {
            allCombinations.add(new ArrayList<>(currentCombination)); // Add a copy of the current combination
            return;
        }

        A currentKey = keys.get(keyIndex);
        List<B> valuesForCurrentKey = inputMap.get(currentKey);

        // Iterate through all possible values for the current key
        for (B value : valuesForCurrentKey) {
            // Create a key-value pair (using Java 9+ Pair)
            // For Java 8 or earlier, use: new AbstractMap.SimpleEntry<>(currentKey, value)
            Pair<A, B> pair = Pair.of(currentKey, value);

            currentCombination.add(pair); // Add the current selection to the combination

            // Recursively process the next key
            generateRecursive(inputMap, keys, keyIndex + 1, currentCombination, allCombinations);

            // Backtrack: remove the last added element to try the next value for the current key
            currentCombination.remove(currentCombination.size() - 1);
        }
    }

    public static void main(String[] args) {
        // Use LinkedHashMap to ensure key order matches insertion order, making the order of elements in output combinations predictable
        Map<String, List<Integer>> input = new LinkedHashMap<>();
        input.put("a", Arrays.asList(1, 2));
        input.put("b", Arrays.asList(3, 4, 5));
        input.put("c", Arrays.asList(6, 7));

        List<List<Pair<String, Integer>>> output = generateProduct(input);

        System.out.println("Total " + output.size() + " results:");
        for (List<Pair<String, Integer>> combination : output) {
            String comboStr = combination.stream()
                    .map(entry -> "(" + entry.getKey() + ", " + entry.getValue() + ")")
                    .collect(Collectors.joining(", ", "[", "]"));
            System.out.println(comboStr);
        }

        System.out.println("\n--- Test case with an empty list ---");
        Map<String, List<Integer>> inputWithEmptyList = new LinkedHashMap<>();
        inputWithEmptyList.put("x", Arrays.asList(10, 20));
        inputWithEmptyList.put("y", new ArrayList<>()); // Empty list
        inputWithEmptyList.put("z", Arrays.asList(30));

        List<List<Pair<String, Integer>>> outputEmpty = generateProduct(inputWithEmptyList);
        System.out.println("Total " + outputEmpty.size() + " results (expected to be 0)"); // Should output 0

        System.out.println("\n--- Test case with empty input Map ---");
        Map<String, List<Integer>> emptyInputMap = new LinkedHashMap<>();
        List<List<Pair<String, Integer>>> outputEmptyMap = generateProduct(emptyInputMap);
        System.out.println("Total " + outputEmptyMap.size() + " results (expected to be 0)"); // Should output 0
    }
}
