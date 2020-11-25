package SharingEnergy.OutsideData.service.impl;

import SharingEnergy.Object.OutsideDataObject;
import SharingEnergy.Object.ResponseObject;
import SharingEnergy.OutsideData.service.MainService;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * @ Class Name   : MainServiceImpl.java
 * @ Modification : SharingEnergy MAIN SERVICE IMPL
 * @
 * @ 최초 생성일  최초 생성자
 * @ ---------    ---------
 * @ 2020.11.24.    임태준
 * @
 * @  수정일        수정자
 * @ ---------    ---------
 * @
 **/
@Service
public class MainServiceImpl implements MainService {

    @Autowired
    private SqlSession sqlSession;

    public int outsideDataInsert(ArrayList<OutsideDataObject> outsideDataList) throws Exception {

        MainMapper mainMapper = sqlSession.getMapper(MainMapper.class);

        return mainMapper.outsideDataInsert(outsideDataList);
    }

    public int errorLogInsert(ResponseObject responseObject) throws Exception {

        MainMapper mainMapper = sqlSession.getMapper(MainMapper.class);

        return mainMapper.errorLogInsert(responseObject);
    }
}
