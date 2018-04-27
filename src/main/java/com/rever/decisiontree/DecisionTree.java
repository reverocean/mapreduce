package com.rever.decisiontree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DecisionTree {
    private List<String> attributes = new ArrayList<>();
    private List<List<String>> attributeValuesList = new ArrayList<>();
    private List<String[]> data = new ArrayList<>();

    int decatt;
    public static final String attributeRegexString = "@attribute(.*)[{](.*?)[}]";


    public static void main(String[] args) {


        DecisionTree decisionTree = new DecisionTree();

        decisionTree.readARFF(new File("/Users/hayhe/Workspace/java/al/src/main/resources/weather.nominal.arff"));

        ID3 id3 = new ID3(decisionTree.attributes, decisionTree.attributeValuesList, decisionTree.data);
        id3.setDec("play");
        List<Integer> attributesWithoutLabel = new LinkedList<>();
        for (int i = 0; i < decisionTree.attributes.size(); i++) {
            if (i != id3.decatt)
                attributesWithoutLabel.add(i);
        }
        ArrayList<Integer> al = new ArrayList<>();
        for (int i = 0; i < decisionTree.data.size(); i++) {
            al.add(i);
        }

//        IntStream.range(0, decisionTree.data.size()).
        id3.buildDT("DecisionTree", "null", al, attributesWithoutLabel);
        id3.writeXML("/Users/hayhe/Workspace/java/al/src/main/resources/dt.xml");
        return;
    }

    public void readARFF(File file) {
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            Pattern attributePattern = Pattern.compile(ID3.attributeRegexString);
            while ((line = br.readLine()) != null) {
                Matcher matcher = attributePattern.matcher(line);

                if (isAttribute(matcher)) {
                    attributes.add(matcher.group(1).trim());
                    String[] values = matcher.group(2).split(",");
                    attributeValuesList.add(Arrays.stream(values).map(String::trim).collect(Collectors.toList()));

                } else if (line.startsWith("@data")) {
                    readData(br);
                } else {
                    continue;
                }
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readData(BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            if (line == "") {
                continue;
            }

            String[] row = line.split(",");
            data.add(row);
        }
    }

    private boolean isAttribute(Matcher matcher) {
        return matcher.find();
    }
}
