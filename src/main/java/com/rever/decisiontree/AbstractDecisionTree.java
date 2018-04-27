package com.rever.decisiontree;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class AbstractDecisionTree {
    List<String> attributes;
    List<List<String>> attributeValuesList;
    List<String[]> data;
    int decatt;
    private Document xmldoc;
    Element root;

    public AbstractDecisionTree(int decatt, List<String[]> data, List<String> attributes, List<List<String>> attributeValuesList) {
        this.decatt = decatt;
        this.data = data;
        xmldoc = DocumentHelper.createDocument();
        root = xmldoc.addElement("root");
        this.attributes = attributes;
        this.attributeValuesList = attributeValuesList;
    }

    public void generateDecisionTree() {
        List<Integer> attributeIndexes = getIntegerStream(attributes.size())
                .filter(this::isNotDecatt)
                .collect(Collectors.toList());

        List<Integer> dataIndexes = getIntegerStream(data.size())
                .collect(Collectors.toList());

        buildDT("DecisionTree", "null", dataIndexes, attributeIndexes);
        writeXML("/Users/hayhe/Workspace/java/al/src/main/resources/dt.xml");
    }

    protected abstract void buildDT(String decisionTree, String aNull, List<Integer> dataIndexes, List<Integer> attributeIndexes);

    private Stream<Integer> getIntegerStream(int size) {
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
