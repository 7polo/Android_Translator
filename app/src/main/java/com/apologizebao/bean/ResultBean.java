package com.apologizebao.bean;

import java.util.List;

/**
 * Created by apologizebao on 17-1-19.
 */
public class ResultBean {

    /**
     * web : [{"value":["井宿","好","货井"],"key":"Well"},{"value":["老井","老井 (电影)","八角井"],"key":"Old Well"},{"value":["做得好","干得好","熟透的"],"key":"Well Done"}]
     * query : well
     * translation : ["好吧"]
     * errorCode : 0
     * basic : {"phonetic":"wel","uk_phonetic":"wel","explains":["n. 井；源泉","adj. 良好的；健康的；适宜的","adv. 很好地；充分地；满意地；适当地","v. 涌出","","n. (Well)人名；(英、德、荷)韦尔"],"us_phonetic":"wɛl"}
     */
    private List<WebEntity> web;
    private String query;
    private List<String> translation;
    private int errorCode;
    private BasicEntity basic;

    public class WebEntity {
        /**
         * value : ["井宿","好","货井"]
         * key : Well
         */
        public List<String> value;
        public String key;

        public List<String> getValue() {
            return value;
        }

        public void setValue(List<String> value) {
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }

    public class BasicEntity {
        /**
         * phonetic : wel
         * uk_phonetic : wel
         * explains : ["n. 井；源泉","adj. 良好的；健康的；适宜的","adv. 很好地；充分地；满意地；适当地","v. 涌出","","n. (Well)人名；(英、德、荷)韦尔"]
         * us_phonetic : wɛl
         */
        private String phonetic;
        private String uk_phonetic;
        private List<String> explains;
        private String us_phonetic;

        public String getPhonetic() {
            return phonetic;
        }

        public void setPhonetic(String phonetic) {
            this.phonetic = phonetic;
        }

        public List<String> getExplains() {
            //解决空串出现的问题
            for (int i = 0; i < explains.size(); ) {
                if (explains.get(i).trim().equals("")) {
                    explains.remove(i);
                } else i++;
            }
            return explains;
        }

        public void setExplains(List<String> explains) {
            this.explains = explains;
        }

        public void setUs_phonetic(String us_phonetic) {
            this.us_phonetic = us_phonetic == null || us_phonetic.equals("null") ? null : us_phonetic;
        }

        public String getUs_phonetic() {
            return us_phonetic;
        }

        public void setUk_phonetic(String uk_phonetic) {
            this.uk_phonetic = uk_phonetic == null || uk_phonetic.equals("null") ? null : uk_phonetic;
        }

        public String getUk_phonetic() {
            return uk_phonetic;
        }


        @Override
        public String toString() {
            return "BasicEntity{" +
                    "phonetic='" + phonetic + '\'' +
                    ", uk_phonetic='" + uk_phonetic + '\'' +
                    ", explains=" + explains +
                    ", us_phonetic='" + us_phonetic + '\'' +
                    '}';
        }
    }

    public List<WebEntity> getWeb() {
        return web;
    }

    public void setWeb(List<WebEntity> web) {
        this.web = web;
    }

    public String getQuery() {
        return "" + query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<String> getTranslation() {
        return translation;
    }

    public void setTranslation(List<String> translation) {
        this.translation = translation;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public BasicEntity getBasic() {
        return basic;
    }

    public void setBasic(BasicEntity basic) {
        this.basic = basic;
    }

    @Override
    public String toString() {
        return "ResultBean{" +
                "web=" + web +
                ", query='" + query + '\'' +
                ", translation=" + translation +
                ", errorCode=" + errorCode +
                ", basic=" + basic +
                '}';
    }
}
