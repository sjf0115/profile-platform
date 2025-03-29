package com.data.profile.web.controller;

import com.data.profile.common.domain.Response;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.model.Event;
import com.data.profile.service.EventService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 功能：事件
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/2 07:13
 */
@Slf4j
@RestController
@RequestMapping(value = "/event", produces = MediaType.APPLICATION_JSON_VALUE)
public class EventController {
    private static Logger LOG = LoggerFactory.getLogger(EventController.class);

    @Autowired
    private EventService eventService;

    @GetMapping(value = "/list")
    public Response getList(@RequestBody Event event) {
        List<Event> events = eventService.getList(event);
        return Response.success(events);
    }

    @GetMapping(value = "/detail")
    public Response getDetail(@RequestParam String eventId) {
        Optional<Event> optional = eventService.getDetail(eventId);
        if (optional.isPresent()) {
            return Response.success(optional.get());
        } else {
            return Response.error("请求的事件不存在", ResponseCode.ERROR);
        }
    }

    @PostMapping(value = "/save")
    public Response save(@RequestBody Event event) {
        int result = eventService.save(event);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("添加事件失败", ResponseCode.ERROR);
        }
    }
}