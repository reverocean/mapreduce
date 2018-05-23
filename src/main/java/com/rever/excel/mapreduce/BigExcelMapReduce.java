package com.rever.excel.mapreduce;

import com.monitorjbl.xlsx.StreamingReader;
import com.rever.commons.Pair;
import com.rever.excel.mapreduce.beans.AggregateResult;
import com.rever.excel.mapreduce.beans.SaleRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.*;

public class BigExcelMapReduce {
    public static final int REDUCER_COUNT = 3;
    private String excelFile;
    private ExecutorService mapperExecutorService;
    private ExecutorService reducerExecutorService;
    private List<CountDownLatch> latches = new ArrayList<>();
    private SaleRecordShuffler shuffler = new SaleRecordShuffler();

    public BigExcelMapReduce(String excelFile) {
        this.excelFile = excelFile;
        mapperExecutorService = Executors.newFixedThreadPool(10);
        reducerExecutorService = Executors.newFixedThreadPool(REDUCER_COUNT);
    }

    public void start() {
        try (InputStream is = new FileInputStream(excelFile)) {
            Workbook workbook = StreamingReader.builder()
                    .rowCacheSize(100)    // number of rows to keep in memory (defaults to 10)
                    .bufferSize(4096)     // buffer size to use when reading InputStream to file (defaults to 1024)
                    .open(is);
            Sheet sheet = workbook.getSheetAt(0);

            doMap(sheet);
            mapperExecutorService.shutdown();

            doReduce();

            System.out.println("Success");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (!mapperExecutorService.isShutdown()) {

                mapperExecutorService.shutdownNow();
            }
        }
    }

    private void doReduce() {
        List<Map<String, ConcurrentLinkedQueue<AggregateResult>>> reducerDatas = shuffler.doSplit(REDUCER_COUNT);
        List<Pair<String, AggregateResult>> result = reducerDatas.stream()
                .map(reducerData -> {
                    Future<List<Pair<String, AggregateResult>>> submit = reducerExecutorService.submit(new SaleRecordReducer(reducerData));
                    return submit;
                })
                .map(future -> {
                    try {
                        return future.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    return new ArrayList<Pair<String, AggregateResult>>();
                }).reduce((list1, list2) -> {
                    List<Pair<String, AggregateResult>> pairs = new ArrayList<>(list1);
                    pairs.addAll(list2);
                    return pairs;
                }).get();
        result.forEach(System.out::println);
    }

    private void doMap(Sheet sheet) throws InterruptedException {
        int i = 0;
        List<SaleRecord> saleRecords = new ArrayList<>();
        boolean isFirstRow = true;
        for (Row r : sheet) {
            if (isFirstRow) {
                isFirstRow = false;
                continue;
            }
            Cell countryCell = r.getCell(1);
            Cell unitCostCell = r.getCell(10);
            i++;
            saleRecords.add(new SaleRecord(countryCell.getStringCellValue(), unitCostCell.getNumericCellValue()));

            if (i == 1000) {
                CountDownLatch countDownLatch = new CountDownLatch(1);
                i = 0;
                latches.add(countDownLatch);
                mapperExecutorService.submit(new SaleRecordMapper(shuffler, saleRecords, countDownLatch));
                saleRecords = new ArrayList<>();
            }
        }
        if (i != 0) {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            latches.add(countDownLatch);
            mapperExecutorService.submit(new SaleRecordMapper(shuffler, saleRecords, countDownLatch));
        }

        for (CountDownLatch latch : latches) {
            latch.await();
        }
    }
}
