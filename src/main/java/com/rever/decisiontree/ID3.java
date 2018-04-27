package com.rever.decisiontree;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static java.lang.Double.MIN_VALUE;

public class ID3 {
    private List<String> attributes;
    private List<List<String>> attributeValuesList;
    private List<String[]> data;

    int decatt;
    public static final String attributeRegexString = "@attribute(.*)[{](.*?)[}]";

    Document xmldoc;
    Element root;

    public ID3(List<String> attributes, List<List<String>> attributeValuesList, List<String[]> data) {
        this.attributes = attributes;
        this.attributeValuesList = attributeValuesList;
        this.data = data;

        xmldoc = DocumentHelper.createDocument();
        root = xmldoc.addElement("root");
        root.addElement("DecisionTree").addAttribute("value", "null");
    }

    public void setDec(int n) {
        if (n < 0 || n > attributes.size()) {
            System.out.println("决策变量指定错误。");
            System.exit(2);
        }

        decatt = n;
    }

    public void setDec(String name) {
        int n = attributes.indexOf(name);
        setDec(n);
    }

    public double getEntropy(int[] labelCounts) {

        int sum = Arrays.stream(labelCounts).sum();
        double entropy = Arrays.stream(labelCounts)
                .mapToDouble((int value) -> {
                    double v = value;
                    return v;
                })
                .reduce(0.0, (ent, labelCount) -> ent - labelCount * Math.log(labelCount + MIN_VALUE) / Math.log(2));

        entropy += sum * Math.log(sum + MIN_VALUE) / Math.log(2);
        entropy /= sum;
        return entropy;
    }

    public boolean isInfoPure(List<Integer> dataIndexes) {
        return dataIndexes.stream()
                .map(index -> getLabelValue(index))
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

    public double calNodeEntropy(List<Integer> dataIndexes, int attributeIndex) {
        int dataSize = dataIndexes.size();
        double entropy = 0.0;

        int[][] info = initMatrixForAttributeValueToLabel(attributeIndex);

        int[] count = new int[getAttributeValuesSize(attributeIndex)];

        for (int i = 0; i < dataSize; i++) {
            int dataIndex = dataIndexes.get(i);
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

    public void buildDT(String name, String value, List<Integer> dataIndexes, List<Integer> attributeIndexes) {
        Element ele = null;

        List<Node> list = root.selectNodes("//" + name);
        Iterator<Node> iterator = list.iterator();
        while (iterator.hasNext()) {
            Node element = iterator.next();
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


    public void writeXML(String filename) {
        try {
            File file = new File(filename);
            if (!file.exists())
                file.createNewFile();
            FileWriter fw = new FileWriter(file);
            OutputFormat format = OutputFormat.createPrettyPrint(); // 美化格式
            XMLWriter output = new XMLWriter(fw, format);
            output.write(xmldoc);
            output.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
