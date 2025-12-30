package com.data.profile.manager.service.engine;

import com.data.profile.manager.dao.engine.ExportDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 功能：分析引擎-投递
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/12/30 11:37
 */
@Service
public class ExportService {
    @Autowired
    private ExportDao exportDao;

    public void export(){
        exportDao.exportGroup("");
    }
}
