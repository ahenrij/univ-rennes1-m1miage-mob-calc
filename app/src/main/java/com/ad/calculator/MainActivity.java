package com.ad.calculator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Locale;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private int currentOperatorId;
    private String currentOperator;
    private String currentDisplay;
    private EditText etDisplay;
    private ImageView imgBackspace;
    //


    private String OP_ADD, OP_SUB, OP_MUL, OP_DIV, OP_MOD;

    private String[] operators;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etDisplay = findViewById(R.id.et_display);
        imgBackspace = findViewById(R.id.img_backspace);

        initValues();
    }

    private void initValues() {

        OP_ADD = getResources().getString(R.string.op_add);
        OP_SUB = getResources().getString(R.string.op_sub);
        OP_MUL = getResources().getString(R.string.op_mul);
        OP_DIV = getResources().getString(R.string.op_div);
        OP_MOD = getResources().getString(R.string.op_mod);

        operators = new String[]{OP_ADD, OP_SUB, OP_MUL, OP_DIV, OP_MOD};

        setCurrentDisplay("");
        setCurrentOperator("");

        imgBackspace.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                clearAll(view);
                return true;
            }
        });
    }

    public void addOperator(View view) {

        if (!currentDisplay.isEmpty()) {

            String operator = ((Button) view).getText().toString();
            Integer operatorId = view.getId();

            //No operator in the display
            if (displayContainsOperator()) {

                //Operator at the display's end
                if (displayEndsWithOperator()) {
                    setCurrentDisplay(currentDisplay.substring(0, currentDisplay.length()-1) + "<font color=\"#" + Integer.toHexString(ContextCompat.getColor(this, R.color.colorAccent)) + "\">" + operator + "</font>");
                } else {

                    //There is an operator which is not at the end of expr, make the evaluation...
                    if (evaluateDisplay()) { //...and append the new operator if evaluation succeed
                        addOperatorToDisplay(operator, operatorId);
                    }
                }

            } else {
                addOperatorToDisplay(operator, operatorId);
            }
        }
    }

    /**
     * @param view  Button holding the single number to append as text
     * @post Append number or . to display if allowed
     */
    public void addNumber(View view) {
        String number = ((Button) view).getText().toString();

        if (number.equals(".")) {

            if (!getCurrentOperand().contains(".")) {
                if (displayEndsWithOperator() || currentDisplay.isEmpty()) {
                    number = "0" + number;
                }
                setCurrentDisplay(currentDisplay + number);
            }

        } else {
            setCurrentDisplay((currentDisplay.equals("0")) ? number : currentDisplay + number);
        }

    }

    public void doBackSpace(View view) {
        if (!currentDisplay.isEmpty()) {
            setCurrentDisplay(currentDisplay.substring(0, currentDisplay.length() - 1));
        }
    }

    public void clearAll(View view) {
        setCurrentDisplay("");
    }

    public void doCalculation(View view) {

        if (displayEndsWithOperator()) {
            Toast.makeText(this, "Un format non valide est utilisé.", Toast.LENGTH_SHORT).show();
        } else if (displayContainsOperator()) {
            evaluateDisplay();
        }
    }

    private void setCurrentDisplay(String currentDisplay) {
        this.currentDisplay = currentDisplay;
        etDisplay.setText(Html.fromHtml(currentDisplay));
    }

    private void setCurrentOperator(String currentOperator) {
        this.currentOperator = currentOperator;
    }

    private boolean displayContainsOperator() {

        boolean contains = false;
        for (String operator: operators) {
            if (currentDisplay.contains(operator)) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    private boolean displayEndsWithOperator() {

        boolean endsWith = false;
        for (String operator: operators) {
            if (currentDisplay.endsWith(operator)) {
                endsWith = true;
                break;
            }
        }
        return endsWith;
    }


    private void addOperatorToDisplay(String operator, Integer id) {
        if (!displayContainsOperator()) {
            setCurrentDisplay(currentDisplay + operator);
            setCurrentOperator(operator);
            this.currentOperatorId = id;
        }
    }

    /**
     * @pre display contains an operator not at the end : an operation !!
     * @post calculate expression display and set it to display
     * @return true if display has been properly evaluated, and false + toast error message if not
     */
    private boolean evaluateDisplay() {

        double result = 0;
        //Quote operator to avoid special characters regex exception
        String[] operands = currentDisplay.split(Pattern.quote(this.currentOperator));

        switch (this.currentOperatorId) {
            case R.id.op_add:
                result = Double.parseDouble(operands[0]) + Double.parseDouble(operands[1]);
                break;
            case R.id.op_sub:
                result = Double.parseDouble(operands[0]) - Double.parseDouble(operands[1]);
                break;
            case R.id.op_div:
                if (Double.parseDouble(operands[1]) == 0.0) {
                    Toast.makeText(this, "Division par zéro invalide", Toast.LENGTH_SHORT).show();
                    return false;
                }
                result = Double.parseDouble(operands[0]) / Double.parseDouble(operands[1]);
                break;
            case R.id.op_mul:
                result = Double.parseDouble(operands[0]) * Double.parseDouble(operands[1]);
                break;
            case R.id.op_mod:
                result = mod(Integer.parseInt(operands[0]), Integer.parseInt(operands[1]));
                break;
            default:
                Toast.makeText(this, "Operateur not reconnu.", Toast.LENGTH_SHORT).show();
                return false;
        }

        //if result if integer, show it as int
        if (doubleIsInteger(result)) {
            setCurrentDisplay(String.valueOf((int) result));
        } else {
            //Show double result with precision of result_precision integer after . if needed
            setCurrentDisplay((String.valueOf(result).split(Pattern.quote("."))[1].length() > getResources().getInteger(R.integer.result_precision)) ? String.format(Locale.FRANCE,"%." +  getResources().getInteger(R.integer.result_precision) + "f", result) : String.valueOf(result));
        }
        return true;
    }

    /**
     *
     * @return the current operand that user is typing and empty if no operands
     */
    private String getCurrentOperand() {
        if (!displayContainsOperator()) {
            return currentDisplay;
        } else {
            return displayEndsWithOperator() ? "" : currentDisplay.split(Pattern.quote(this.currentOperator))[1];
        }
    }

    /**
     * @return x modulo y
     */
    private int mod(int x, int y)
    {
        int result = x % y;
        return result < 0? result + y : result;
    }

    /**
     *
     * @param result (of operation)
     * @return true if result is an integer (.0)
     */
    private boolean doubleIsInteger(Double result) {
        return (result == Math.floor(result)) && !Double.isInfinite(result);
    }
}