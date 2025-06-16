package generationgap.co.kr.mapper.history;

import generationgap.co.kr.domain.mypage.HistoryDto;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface HistoryMapper {
    List<HistoryDto> findHistoryByUserIdx(Long userIdx);
}