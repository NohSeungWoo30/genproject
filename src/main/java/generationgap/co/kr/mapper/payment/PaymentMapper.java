package generationgap.co.kr.mapper.payment;

import generationgap.co.kr.domain.mypage.PaymentDto;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface PaymentMapper {
    List<PaymentDto> findPaymentsByUserIdx(Long userIdx);
}