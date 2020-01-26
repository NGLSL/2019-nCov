package xin.codedream.ncov.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class AreaStat {

    /**
     * provinceName : 湖北省
     * provinceShortName : 湖北
     * confirmedCount : 549
     * suspectedCount : 0
     * curedCount : 31
     * deadCount : 24
     * comment :
     * cities : [{"cityName":"武汉","confirmedCount":495,"suspectedCount":0,"curedCount":31,"deadCount":23},{"cityName":"孝感","confirmedCount":22,"suspectedCount":0,"curedCount":0,"deadCount":0},{"cityName":"黄冈","confirmedCount":12,"suspectedCount":0,"curedCount":0,"deadCount":0},{"cityName":"荆州","confirmedCount":8,"suspectedCount":0,"curedCount":0,"deadCount":0},{"cityName":"荆门","confirmedCount":8,"suspectedCount":0,"curedCount":0,"deadCount":0},{"cityName":"仙桃","confirmedCount":2,"suspectedCount":0,"curedCount":0,"deadCount":0},{"cityName":"宜昌","confirmedCount":1,"suspectedCount":0,"curedCount":0,"deadCount":1},{"cityName":"十堰","confirmedCount":1,"suspectedCount":0,"curedCount":0,"deadCount":0}]
     */

    private String provinceName;
    private String provinceShortName;
    private int confirmedCount;
    private int suspectedCount;
    private int curedCount;
    private int deadCount;
    private String comment;
    private List<CitiesBean> cities;

    @Data
    public static class CitiesBean {
        /**
         * cityName : 武汉
         * confirmedCount : 495
         * suspectedCount : 0
         * curedCount : 31
         * deadCount : 23
         */

        private String cityName;
        private int confirmedCount;
        private int suspectedCount;
        private int curedCount;
        private int deadCount;
    }
}
