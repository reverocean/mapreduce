package com.rever.decisiontree;

import org.dom4j.Element;
import org.dom4j.Node;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Double.MIN_VALUE;

public class ID3 extends AbstractDecisionTree {

    public ID3(List<String> attributes, List<List<String>> attributeValuesList, List<String[]> data, int decatt) {
        super(decatt, data, attributes, attributeValuesList);

        root.addElement("DecisionTree").addAttribute("value", "null");
    }

    private double getEntropy(int[] labelCounts) {

        int sum = Arrays.stream(labelCounts).sum();
        double entropy = Arrays.stream(labelCounts)
                .mapToDouble((int value) -> (double) value)
                .reduce(0.0, (ent, labelCount) -> ent - labelCount * Math.log(labelCount + MIN_VALUE) / Math.log(2));

        entropy += sum * Math.log(sum + MIN_VALUE) / Math.log(2);
        entropy /= sum;
        return entropy;
    }

    private boolean isInfoPure(List<Integer> dataIndexes) {
        return dataIndexes.stream()
                .map(this::getLabelValue)
                .collect(Collectors.toSet())
                .size() == 1;
//        String labelValue = getLabelValue(dataIndexes.get(0));
//        for (int i = 1; i < dataIndexes.size(); i++) {
//            String next = getLabelValue(dataIndexes.get(i));
//            if (!labelValue.equals(next)) {
//                return false;
//            }
//        }
//
//        return true;
    }

    private String getLabelValue(Integer dataIndex) {
        return data.get(dataIndex)[decatt];
    }

    private double calNodeEntropy(List<Integer> dataIndexes, int attributeIndex) {
        int dataSize = dataIndexes.size();
        double entropy = 0.0;

        int[][] info = initMatrixForAttributeValueToLabel(attributeIndex);

        int[] count = new int[getAttributeValuesSize(attributeIndex)];

        for (Integer dataIndexe : dataIndexes) {
            int dataIndex = dataIndexe;
            String nodeValue = data.get(dataIndex)[attributeIndex];
            int nodeind = attributeValuesList.get(attributeIndex).indexOf(nodeValue);
            count[nodeind]++;
            String decvalue = getLabelValue(dataIndex);
            int decind = getLabelValues().indexOf(decvalue);
            info[nodeind][decind]++;
        }

        for (int i = 0; i < info.length; i++) {
            entropy += getEntropy(info[i]) * count[i] / dataSize;
        }
        return entropy;
    }

    private int[][] initMatrixForAttributeValueToLabel(int attributeIndex) {
        int[][] info = new int[getAttributeValuesSize(attributeIndex)][];
        for (int i = 0; i < info.length; i++) {
            info[i] = new int[getLabelValuesSize()];
        }
        return info;
    }

    private int getLabelValuesSize() {
        return getLabelValues().size();
    }

    private List<String> getLabelValues() {
        return attributeValuesList.get(decatt);
    }

    private int getAttributeValuesSize(int attributeIndex) {
        return attributeValuesList.get(attributeIndex).size();
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
