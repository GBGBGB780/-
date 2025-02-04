package com.chinahitech.shop.service;

import com.chinahitech.shop.bean.Activity;
import com.chinahitech.shop.bean.Group;
import com.chinahitech.shop.bean.IndividualGroup;
import com.chinahitech.shop.bean.User;
import com.chinahitech.shop.bean.notAddedToDatabase.GroupNum;
import com.chinahitech.shop.exception.*;
import com.chinahitech.shop.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class IndividualGroupService {
    @Autowired
    private IndividualGroupMapper individualGroupMapper;
    @Autowired
    private StuMapper stuMapper;
    @Autowired
    private ManagerMapper managerMapper;
    @Autowired
    private GroupMapper groupMapper;
    @Autowired
    private TopManagerMapper topManagerMapper;
    @Autowired
    private IndividualActivityMapper individualActivityMapper;
    @Autowired
    private ActivityMapper activityMapper;

    public List<IndividualGroup> getGroupByStuId(String userId) {
        User user = validateStuName(userId);
        return individualGroupMapper.getGroupByStuId(userId);
    }

    public List<IndividualGroup> getStudentsByGroup(int groupId, String searchInfo) {
        Group group = validateGroup(groupId);
        if (searchInfo == null || searchInfo.trim().isEmpty()) {
            return individualGroupMapper.getAllStudentsByGroup(groupId);
        } else {
            return individualGroupMapper.getStudentsByGroupAndStuName(searchInfo, groupId);
        }
    }

    public List<IndividualGroup> getAllStudents(String searchInfo) {
        if (searchInfo == null || searchInfo.trim().isEmpty()) {
            return individualGroupMapper.getAllStudents();
        } else {
            return individualGroupMapper.getStudentsByStuName(searchInfo);
        }
    }

    public List<Group> getAllManagedGroups(String managerId) {
        //检测管理者名字是否一致
        User user = validateManagerName(managerId);

        List<IndividualGroup> individualGroupList = individualGroupMapper.getAllManagedGroups(managerId);
//        System.out.println(individualGroupList);
        List<Group> groupList = new ArrayList<>();
        for (IndividualGroup individualGroup : individualGroupList) {
//            System.out.println(individualGroup.getGroupId());
            groupList.add(groupMapper.getGroupById(individualGroup.getGroupId()));
        }
        return groupList;
    }

    public IndividualGroup getUserByUserIdAndGroupId(String userId, int groupId) {
        User user = validateStuName(userId);
        Group group = validateGroup(groupId);
        return individualGroupMapper.getUserByUserIdAndGroupId(userId, groupId);
    }

    public List<GroupNum> getGroupMembers() {
        List<GroupNum> groupList = individualGroupMapper.getGroupMembers();
//        System.out.println(groupList);
        for (GroupNum group : groupList) {
            group.setGroupName(groupMapper.getGroupById(group.getGroupId()).getName());
            group.setHot(groupMapper.getGroupById(group.getGroupId()).getHot());
        }
        groupList.sort(Comparator.comparingInt(GroupNum::getHot).reversed());
        return groupList;
    }

    public void addGroupStudent(int groupId, String userId, String position) {
        User user = validateStu(userId);
        Group group = validateGroup(groupId);
        IndividualGroup test = individualGroupMapper.getUserByUserIdAndGroupId(userId, groupId);
        if (test != null) {
            throw new InsertException("用户"+ userId +"在社团"+ groupId +"中已存在，无法重复加入");
        }
        IndividualGroup individualGroup  = new IndividualGroup();
        //初始化信息
        individualGroup.setGroupId(groupId);
        individualGroup.setUserId(userId);
        individualGroup.setUserName(user.getUserName());
        Date date = new Date();
        individualGroup.setStatus(0);
        individualGroup.setCreateTime(date);
        individualGroup.setModifyTime(date);

        if (position != null) {
            individualGroup.setPosition(position);
        } else {
            individualGroup.setPosition("普通成员");
        }

        int i = individualGroupMapper.insert(individualGroup);
//        int i = individualGroupMapper.addGroupStudent(individualGroup.getUserId(), individualGroup.getGroupId(),
//                individualGroup.getPosition(), individualGroup.getUserName(), 0, date, date);
        if(i != 1){
            throw new InsertException("社团"+ groupId +"添加学生"+ userId +"失败");
        }
    }

    public void modifyGroupStudent(int groupId, String userId, String position) {
        validateStatus(groupId, userId);
        //初始化学生信息
//        individualGroup.setGroupId(groupId);
//        individualGroup.setUserId(userId);
//        individualGroup.setUserName(stu.getUserName());
        if (position == null) {
            position = "普通成员";
        }
        Date date = new Date();

        int i = individualGroupMapper.modifyGroupStudent(position, date, groupId, userId);
        if(i != 1){
            throw new UpdateException("社团"+ groupId +"修改学生"+ userId +"的信息失败");
        }
    }

    public void deleteGroupStudent(int groupId, String userId) {
        validateStatus(groupId, userId);
        //初始化学生信息
//        individualGroup.setGroupId(groupId);
//        individualGroup.setUserId(userId);
//        individualGroup.setUserName(stu.getUserName());

        int i = individualGroupMapper.deleteGroupStudent(groupId, userId);
        if(i != 1){
            throw new DeleteException("社团"+ groupId +"删除学生"+ userId +"的信息失败");
        }
    }

    public void updatePermission(int groupId, String userId, int status) {

        User user = topManagerMapper.getByNumNoStatus(userId);
        if (user == null) {
            throw new EntityNotFoundException("用户"+ userId +"不存在");
        }
        if (user.getStatus() > 10) {
            throw new AccessDeniedException("用户"+ userId +"在社团"+ groupId +"中拥有超级管理员权限，你的权限不足");
        }
        IndividualGroup individualGroup = getUserByUserIdAndGroupId(userId, groupId);
        if (individualGroup == null) {
            throw new EntityNotFoundException("用户"+ userId +"在社团"+ groupId +"中不存在");
        }
        Group group = validateGroup(groupId);
        //初始化学生信息
//        individualGroup.setGroupId(groupId);
//        individualGroup.setUserId(userId);
//        individualGroup.setUserName(stu.getUserName());
        Date date = new Date();

        int i = individualGroupMapper.updatePermission(groupId, userId, status, date);
        if(i != 1){
            throw new UpdateException("社团"+ groupId +"修改学生"+ userId +"的权限失败");
        }

        //检验活动里权限是否需要修改
        //获取该社团组织的活动
        List<Activity> activityList = activityMapper.getActivityByGroupName(group.getName());
        if(activityList == null){
            throw new EntityNotFoundException("社团"+ group.getName() +"组织的活动不存在");
        }
        for(Activity activity : activityList){
            //检验该用户参加的并由该社团组织的活动权限是否需要修改
            int j = individualActivityMapper.updatePermission(activity.getId(), userId, status, date);
            if(j != 1){
                throw new UpdateException("活动"+ activity.getId() +"修改学生"+ userId +"的权限失败");
            }
        }

        //检验用户表里权限是否需要修改
        validateUserStatus(userId);
    }

    public void transferStatus(int groupId, String managerId, String userId) {
        //提升用户权限
        updatePermission(groupId, userId, 1);
        //降低自身权限
        updatePermission(groupId, managerId, 0);
    }

    //检测服务函数

    //查询该学生是否存在
    public User validateStu(String userId) {
        User stu = stuMapper.getByNum(userId);
        if (stu == null) {
            throw new EntityNotFoundException("学生"+ userId +"不存在");
        }
        return stu;
    }

    //查询该管理员是否存在
    public User validateManager(String userId) {
        User user = managerMapper.getByNum(userId);
        if (user == null) {
            throw new EntityNotFoundException("管理员"+ userId +"不存在");
        }
        return user;
    }

    //检测该学生在社团中保存的姓名与个人资料中的姓名是否一致
    public User validateStuName(String userId) {
        List<IndividualGroup> individualGroupList = individualGroupMapper.getGroupByStuId(userId);
        //查询该学生是否存在
        User stu = validateStu(userId);
        for (IndividualGroup individualGroup : individualGroupList) {
            if (!Objects.equals(individualGroup.getUserName(), stu.getUserName())) {
                throw new AccessDeniedException("学生"+ userId +"在社团"+ individualGroup.getGroupId() +"中的信息有误");
            }
        }
        return stu;
    }

    //检测该管理员在社团中保存的姓名与个人资料中的姓名是否一致
    public User validateManagerName(String userId) {
        List<IndividualGroup> individualGroupList = individualGroupMapper.getAllManagedGroups(userId);
        //查询该管理员是否存在
        User user = validateManager(userId);
        for (IndividualGroup individualGroup : individualGroupList) {
            if (!Objects.equals(individualGroup.getUserName(), user.getUserName())) {
                throw new AccessDeniedException("管理员"+ userId +"在社团"+ individualGroup.getGroupId() +"中的信息有误");
            }
        }
        return user;
    }

    //查询该社团是否存在
    public Group validateGroup(int groupId) {
        Group group = groupMapper.getGroupById(groupId);
        if (group == null) {
            throw new EntityNotFoundException("社团"+ groupId +"不存在");
        }
        return group;
    }

    //查询修改对象的权限
    public void validateStatus(int groupId, String userId) {
        User user = validateStuName(userId);
        Group group = validateGroup(groupId);
        //查询修改或删除的用户是否存在及其权限是否为管理员
        IndividualGroup individualGroup = individualGroupMapper.getUserByUserIdAndGroupId(userId, groupId);
        if (individualGroup == null) {
            throw new EntityNotFoundException("用户"+ userId +"在社团"+ groupId +"不存在");
        } else if (individualGroup.getStatus() >= 1) {
            throw new AccessDeniedException("用户"+ userId +"在社团"+ groupId +"中拥有管理员权限，你的权限不足");
        }
    }

    //查询用户权限是否正确
    public void validateUserStatus(String userId) {
        int topStatus = 0;
        List<IndividualGroup> individualGroupList = individualGroupMapper.getGroupByStuId(userId);
        for (IndividualGroup individualGroup : individualGroupList) {
            int tempStatus = individualGroup.getStatus();
            if (tempStatus >= topStatus) {
                topStatus = tempStatus;
            }
        }
        Date date = new Date();

        int j = topManagerMapper.updatePermission(userId, topStatus, date);
        if(j != 1){
            throw new UpdateException("学生"+ userId +"修改权限失败");
        }
    }
}
