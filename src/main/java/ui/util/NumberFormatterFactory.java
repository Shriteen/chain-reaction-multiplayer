package ui.util;

import java.util.function.UnaryOperator;

import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;

/**
 * Factory class to provide TextFormatters
 */
public class NumberFormatterFactory{

    // Allows whole number inputs
    public static TextFormatter<Integer> wholeNumberFormatter(int defaultValue) {
        if(defaultValue<0)
            throw new IllegalArgumentException("defaultValue should be whole number");

        UnaryOperator<Change> integerFilter = change -> {
            String fieldText = change.getControlNewText();

            if(fieldText.equals("")){
                change.setText(String.valueOf(defaultValue));
                change.setCaretPosition(String.valueOf(defaultValue).length());
                return change;
            }

            try {
                if(Integer.parseInt(fieldText)<0)
                    return null;
                return change;
            }
            catch (NumberFormatException e) {
                return null;
            }
        };

        StringConverter<Integer> stringConverter = new IntegerStringConverter() {
                @Override
                public Integer fromString(String s) {
                    if (s.isEmpty())
                        return 0;
                    return super.fromString(s);
                }
            };

        return new TextFormatter<Integer>(stringConverter, defaultValue, integerFilter);
    }
	
}
