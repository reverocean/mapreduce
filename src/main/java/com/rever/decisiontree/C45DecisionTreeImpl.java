package com.rever.decisiontree;

import com.rever.commons.Pair;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class C45DecisionTreeImpl extends AbstractDecisionTree {
    public C45DecisionTreeImpl(List<String> attributes, List<List<String>> attributeValuesList, List<String[]> data, int decatt) {
        super(attributes, attributeValuesList, data, decatt);
    }

    @Override
    protected void buildDT(String attributeName, String attributeValue, List<Integer> dataIndexes, List<Integer> attributeIndexes) {
        Element ele = null;

        List<Node> list = root.selectNodes("//" + attributeName);
        for (Node element : list) {
            if (element instanceof Element) {
                ele = (Element) element;
                if (ele.attributeValue("value").equals(attributeValue)) {
                    break;
                }
            }
        }

        if (isInfoPure(dataIndexes)) {
            ele.setText(getLabelValue(dataIndexes.get(0)));
            return;
        }

        int minEntropyAttributeIndex = getMinEntropyAttributeIndex(dataIndexes, attributeIndexes);

        String minEntropyAttributeName = attributes.get(minEntropyAttributeIndex);
        attributeIndexes.remove(new Integer(minEntropyAttributeIndex));

        if (attributeIndexes.isEmpty()) {
            ele.setText(getLabelValue(dataIndexes.get(0)));
            return;
        }

        List<String> minEntropyAttributeValues = attributeValuesList.get(minEntropyAttributeIndex);
        for (String selectedAttributeValue : minEntropyAttributeValues) {
            ele.addElement(minEntropyAttributeName).addAttribute("value", selectedAttributeValue);

            List<Integer> attributeValueDataIndexes = dataIndexes.stream()
                    .filter(dataIndex -> data.get(dataIndex)[minEntropyAttributeIndex].equals(selectedAttributeValue))
                    .collect(Collectors.toList());

            if(attributeValueDataIndexes.isEmpty()){
                Map<String, Long> labelCountsMap = dataIndexes.stream()
                        .map(this::getLabelValue)
                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

                String label = labelCountsMap.entrySet().stream()
                        .max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1)
                        .get()
                        .getKey();
                ele.setText(label);
                continue;
            }
            buildDT(minEntropyAttributeName, selectedAttributeValue, attributeValueDataIndexes, attributeIndexes);
        }
    }

    private int getMinEntropyAttributeIndex(List<Integer> dataIndexes, List<Integer> attributeIndexes) {
//        int minIndex = -1;
//        double minEntropy = Double.MAX_VALUE;
//        for (int i = 0; i < attributeIndexes.size(); i++) {
//            if (i == decatt) {
//                continue;
//            }
//
//            double entropy = calNodeEntropy(dataIndexes, attributeIndexes.get(i));
//            if (entropy < minEntropy) {
//                minIndex = attributeIndexes.get(i);
//                minEntropy = entropy;
//            }
//        }
//        return minIndex;

        if(attributeIndexes.size() == 1){
            return attributeIndexes.get(0);
        }

        double parentEntropy = calculateParentEntropy(dataIndexes);
        List<Double> incrementalEntropys = attributeIndexes.stream()
                .filter(index -> !index.equals(decatt))
                .map(attributeIndex -> parentEntropy - calNodeEntropy(dataIndexes, attributeIndex))
                .collect(Collectors.toList());

        double averageIncrementalEntropy = incrementalEntropys.stream().mapToDouble(Double::doubleValue).average().getAsDouble();
        List<Pair<Integer, Double>> biggerThanAverageIncrementalEntropyAttributeIndexes = getIntegerStream(incrementalEntropys.size())
                .map(index -> new Pair<Integer, Double>(attributeIndexes.get(index), incrementalEntropys.get(index)))
                .filter(pair -> pair.getValue() > averageIncrementalEntropy)
                .collect(Collectors.toList());



        List<Pair<Integer, Double>> intrinsicValues = biggerThanAverageIncrementalEntropyAttributeIndexes.stream()
                .map((Pair<Integer, Double> pair) -> new Pair<Integer, Double>(pair.getKey(), calculateIntrinsicValue(dataIndexes, pair.getKey())))
                .collect(Collectors.toList());

        int size = biggerThanAverageIncrementalEntropyAttributeIndexes.size();
        List<Pair<Integer, Double>> incrementalEntropyRates = IntStream.range(0, size)
                .mapToObj(index -> {
                    double incrementalEntropy = incrementalEntropys.get(index);
                    Pair<Integer, Double> intrinsicValuePair = intrinsicValues.get(index);
                    double intrinsicValue = intrinsicValuePair.getValue();
                    return new Pair<Integer, Double>(intrinsicValuePair.getKey(), new Double(incrementalEntropy / intrinsicValue));
                }).collect(Collectors.toList());

        return incrementalEntropyRates.stream().max(Comparator.comparingDouble(pair -> pair.getValue())).get().getKey();
    }

    private double calculateIntrinsicValue(List<Integer> dataIndexes, int attributeIndex) {
        int sum = dataIndexes.size();

        return getCountsGroupByAttributeValues(dataIndexes, attributeIndex)
                .mapToDouble(count -> (double) count)
                .map(count -> -(count / sum) * Math.log(count / sum + Double.MIN_VALUE) / Math.log(2))
                .sum();
    }

    private IntStream getCountsGroupByAttributeValues(List<Integer> dataIndexes, int attributeIndex) {
        Map<String, Long> attributeValueCounts = dataIndexes
                .stream()
                .map(dataIndex -> data.get(dataIndex))
                .map(row -> row[attributeIndex])
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return attributeValueCounts
                .values()
                .stream()
                .mapToInt(Long::intValue);
    }

    private double calculateParentEntropy(List<Integer> dataIndexes) {
        int[] labelCounts = getCountsGroupByAttributeValues(dataIndexes, decatt).toArray();
        return calculateEntropy(labelCounts);
    }

    /**
     * 1. calculate each incremental entropy
     *  1.1 calculate each entropy
     *  1.2 incremental entropy = parent entropy - each entropy
     * 2. calculate average incremental entropy
     * 3. filter bigger than average incremental entropy attribute
     * 4. calculate each incremental entropy rate
     * 5. find max incremental entropy rate attribute
     */


}
