package com.data.profile.web.dao;

import com.data.profile.web.model.Group;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface GroupMapper {

    // 查询
    Group selectSimpleByGroupId(String groupId); // 根据ID查询

    Group selectByGroupId(String groupId); // 根据ID查询

    List<Group> selectSimpleByGroupName(String groupName); // 根据名字查询

    List<Group> selectByGroupName(String groupName); // 根据名字查询

    List<Group> selectSimpleByParams(Group group); // 根据参数查询

    List<Group> selectByParams(Group group); // 根据参数查询

    List<Group> selectSimpleByKeyword(String keyword); // 模糊查询

    List<Group> selectByKeyword(String keyword); // 模糊查询

    // 插入
    int insert(Group group); // 插入全部

    int insertSelective(Group group); // 选择性插入

    // 删除
    int deleteByGroupId(String groupId); // 根据ID删除

    // 更新
    int updateByGroupIdSelective(Group row);

    int updateByGroupId(Group group);
}
