package com.data.profile.manager.utils;

import com.data.profile.common.domain.UploadLabel;
import com.google.common.collect.Lists;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * 功能：上传CSV标签到数据表
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/4/19 12:30
 */
public class UploadCsvFile {
    public static List<UploadLabel> upload(String filePath) {
        List<UploadLabel> labels = Lists.newArrayList();
        CSVReader reader = null;
        try {
            reader = new CSVReaderBuilder(new FileReader(filePath)).build();
            String[] record;
            while ((record = reader.readNext()) != null) {
                UploadLabel label = new UploadLabel();
                label.setEntityId(record[0]);
                label.setValue(record[1]);
                labels.add(label);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return labels;
    }
}
