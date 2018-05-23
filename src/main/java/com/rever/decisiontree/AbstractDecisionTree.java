package com.rever.decisiontree;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Double.MIN_VALUE;

public abstract class AbstractDecisionTree {
    List<String> attributes;
    List<List<String>> attributeValuesList;
    List<String[]> data;
    int decatt;
    private Document xmldoc;
    Element root;

    public AbstractDecisionTree(List<String> attributes, List<List<String>> attributeValuesList, List<String[]> data, int decatt) {
        this.decatt = decatt;
        this.data = data;
        xmldoc = DocumentHelper.createDocument();
        root = xmldoc.addElement("root");

        this.attributes = attributes;
        this.attributeValuesList = attributeValuesList;

        root.addElement("DecisionTree").addAttribute("value", "null");

    }

    public void generateDecisionTree() {
        List<Integer> attributeIndexes = getIntegerStream(attributes.size())
                .filter(this::isNotDecatt)
                .collect(Collectors.toList());

        List<Integer> dataIndexes = getIntegerStream(data.size())
                .collect(Collectors.toList());

        buildDT("DecisionTree", "null", dataIndexes, attributeIndexes);
        writeXML("/Users/hayhe/Workspace/java/al/src/main/resources/" + this.getClass().getSimpleName() +".xml");
    }

    protected boolean isInfoPure(List<Integer> dataIndexes) {
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

    protected String getLabelValue(Integer dataIndex) {
        return data.get(dataIndex)[decatt];
    }

    protected int getLabelValuesSize() {
        return getLabelValues().size();
    }

    protected List<String> getLabelValues() {
        return attributeValuesList.get(decatt);
    }

    protected int getAttributeValuesSize(int attributeIndex) {
        return attributeValuesList.get(attributeIndex).size();
    }

    protected double calculateEntropy(int[] labelCounts) {

        int sum = Arrays.stream(labelCounts).sum();
        if (sum == 0) {
            return 0;
        }
        double entropy = Arrays.stream(labelCounts)
                .mapToDouble(value -> (double) value)
                .reduce(0.0, (ent, labelCount) -> ent - labelCount * Math.log(labelCount + MIN_VALUE) / Math.log(2));

        entropy += sum * Math.log(sum + MIN_VALUE) / Math.log(2);
        entropy /= sum;
        return entropy;
    }

    protected double calNodeEntropy(List<Integer> dataIndexes, int attributeIndex) {
        double entropy = 0.0;

        int[][] info = initMatrixForAttributeValueToLabel(attributeIndex);

        int[] count = new int[getAttributeValuesSize(attributeIndex)];

        for (Integer dataIndexe : dataIndexes) {
            String nodeValue = data.get(dataIndexe)[attributeIndex];
            int nodeind = attributeValuesList.get(attributeIndex).indexOf(nodeValue);
            count[nodeind]++;
            String decvalue = getLabelValue(dataIndexe);
            int decind = getLabelValues().indexOf(decvalue);
            info[nodeind][decind]++;
        }

        for (int i = 0; i < info.length; i++) {
            entropy += calculateEntropy(info[i]) * count[i] / dataIndexes.size();
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

    protected abstract void buildDT(String decisionTree, String attributeValue, List<Integer> dataIndexes, List<Integer> attributeIndexes);

    protected Stream<Integer> getIntegerStream(int size) {
        return IntStream
                .range(0, size)
                .boxed();
    }

    private boolean isNotDecatt(Integer i) {
        return i != decatt;
    }

    private void writeXML(String filename) {
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
