package com.rever.decisiontree;

import org.dom4j.Element;
import org.dom4j.Node;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ID3 extends AbstractDecisionTree {

    public ID3(List<String> attributes, List<List<String>> attributeValuesList, List<String[]> data, int decatt) {
        super(attributes, attributeValuesList, data, decatt);

    }

    protected void buildDT(String name, String value, List<Integer> dataIndexes, List<Integer> attributeIndexes) {
        Element ele = null;

        List<Node> list = root.selectNodes("//" + name);
        for (Node element : list) {
            if (element instanceof Element) {
                ele = (Element) element;
                if (ele.attributeValue("value").equals(value)) {
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

        List<String> minEntropyAttributeValues = attributeValuesList.get(minEntropyAttributeIndex);
        for (String attributeValue : minEntropyAttributeValues) {
            ele.addElement(minEntropyAttributeName).addAttribute("value", attributeValue);

            List<Integer> attributeValueDataIndexes = dataIndexes.stream()
                    .filter(dataIndex -> data.get(dataIndex)[minEntropyAttributeIndex].equals(attributeValue))
                    .collect(Collectors.toList());

            if (attributeValueDataIndexes.isEmpty()) {
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

            buildDT(minEntropyAttributeName, attributeValue, attributeValueDataIndexes, attributeIndexes);
        }
    }

    private int getMinEntropyAttributeIndex(List<Integer> dataIndexes, List<Integer> attributeIndexes) {
        int minIndex = -1;
        double minEntropy = Double.MAX_VALUE;
        for (int i = 0; i < attributeIndexes.size(); i++) {
            if (i == decatt) {
                continue;
            }

            double entropy = calNodeEntropy(dataIndexes, attributeIndexes.get(i));
            if (entropy < minEntropy) {
                minIndex = attributeIndexes.get(i);
                minEntropy = entropy;
            }
        }
        return minIndex;
    }


}
