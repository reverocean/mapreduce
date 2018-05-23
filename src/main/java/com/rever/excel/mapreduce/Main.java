package com.rever.excel.mapreduce;

import com.rever.excel.mapreduce.BigExcelMapReduce;

import java.io.FileNotFoundException;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {

        BigExcelMapReduce mapReduce = new BigExcelMapReduce("/Users/hayhe/Workspace/java/al/src/main/resources/1500000 Sales Records 2.xlsx");
        mapReduce.start();
    }
}
