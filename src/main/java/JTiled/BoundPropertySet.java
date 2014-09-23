package JTiled;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Parent;
import javafx.scene.control.TextField;

import java.util.HashMap;

public class BoundPropertySet
{
    class Data {
        TextField textField;
        StringProperty property;
    }

    HashMap<String, Data> properties = new HashMap<>();

    BoundPropertySet(Parent root, String... selectors)
    {
        for (String s : selectors) {
            Data d = new Data();
            d.textField = (TextField)root.lookup("#" + s);
            d.property = new SimpleStringProperty();
            d.textField.setText("0");
            d.property.bindBidirectional(d.textField.textProperty());
            properties.put(s, d);
        }
    }

    String getValue(String s) {
        return properties.containsKey(s) ? properties.get(s).property.getValue() : null;
    }

    int getIntValue(String s, int defaultValue) {
        try {
            return Integer.parseInt(getValue(s));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    void setStringValue(String s, String value) {
        Data d = properties.get(s);
        if (d == null)
            return;

        d.textField.setText(value);
    }

    void setIntValue(String s, int value) {
        Data d = properties.get(s);
        if (d == null)
            return;

        d.textField.setText(Integer.toString(value));
    }

}
