package hello;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author: liangcan
 * @version: 1.0
 * @date: 2018/12/13 16:32
 * @describtion: Quote
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Quote {
    private String type;
    private Value value;

    public Quote() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Quote{" +
                "type='" + type + '\'' +
                ", value=" + value +
                '}';
    }
}
