package xin.codedream.email.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@Data
public class Statistics {

    /**
     * id : 1
     * createTime : 1579537899000
     * modifyTime : 1579860497000
     * infectSource : 野生动物，可能中华菊头蝠
     * passWay : 未完全掌握，存在人传人、医务人员感染、一定范围社区传播
     * imgUrl : https://img1.dxycdn.com/2020/0123/733/3392575782185696736-73.jpg
     * dailyPic : https://img1.dxycdn.com/2020/0124/981/3392719124572016783-73.jpg
     * summary :
     * deleted : false
     * countRemark : 全国 确诊 887 例 疑似 1076 例 治愈 35 例 死亡 26 例
     * virus : 新型冠状病毒 2019-nCoV
     * remark1 : 病毒是否变异：存在可能
     * remark2 : 疫情是否扩散：是
     * remark3 :
     * remark4 :
     * remark5 :
     */

    private int id;
    private long createTime;
    private long modifyTime;
    private Date time;
    private String infectSource;
    private String passWay;
    private String imgUrl;
    private String dailyPic;
    private String summary;
    private boolean deleted;
    private String countRemark;
    private String virus;
    private String remark1;
    private String remark2;
    private String remark3;
    private String remark4;
    private String remark5;
    private String generalRemark;
}
